/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas;

import com.polivoto.logica.Cronometro;
import com.polivoto.logica.RecibirVotos;
import com.polivoto.networking.IOHandler;
import com.polivoto.networking.SoapClient;
import com.polivoto.shared.Pregunta;
import com.polivoto.shared.ResultadoPorPerfil;
import com.polivoto.shared.Votacion;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.xml.soap.SOAPException;
import org.inspira.polivoto.AccionesConsultor;
import org.inspira.polivoto.proveedores.MarshallMySharedVotingObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author azaraf
 */
public class AnalistaLocal extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final AccionesConsultor accionesConsultor;
    private Cronometro cronometro;
    private RecibirVotos escuchar;
    private int votos = 0;
    private JSONObject json;
    private Integer poblacion;
    private CardLayout cardsPreguntas;
    private Consultor consultor;

    /**
     * Creates new form AnalistaD
     *
     * @param accionesConsultor
     */
    public AnalistaLocal(AccionesConsultor accionesConsultor) {
        this.accionesConsultor = accionesConsultor;
        initComponents();
        cardsPreguntas = new CardLayout();
        panelPreguntas.setLayout(cardsPreguntas);
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        panelVotando.setVisible(true);
        Panel3.setVisible(false);
        try {
            String startupDataString = this.accionesConsultor.consultaParametrosIniciales();
            json = new JSONObject(startupDataString);
            this.accionesConsultor.consultaPreguntas();
            long tFinal = json.getLong("tiempo_final");
            cronometro = new Cronometro(lblhrs, lblmin, lblseg, tFinal);
            cronometro.iniciarCronometro();
            System.out.println("Startup data: " + startupDataString);
            escuchar = new RecibirVotos();
            poblacion = json.getInt("poblacion");
            votos = json.getInt("votos");
            System.out.println("" + json.toString());
        } catch (IOException | JSONException ignore) {
            ignore.printStackTrace();
        }
        // Obtener el nombre de la zona
        panelMain.setLayout(cardLayout);
        panelMain.add(Panel1, "1");
        panelMain.add(Panel2, "2");
        panelMain.add(Panel3, "3");
        cardLayout.show(panelMain, "1");
        System.out.println("Startup dada: " + json.toString());
        escuchar.iniciarEscucha(votos, poblacion, lblvotos_totales, lblporcentaje, pnlgrafica);
        Service service = new Service();
        service.start();
        setPreguntasText();
        timerPaneles = new Timer(6000, new PanelesPreguntas());
        timerPaneles.start();
    }

    private void setPreguntasText() {
        JSONArray js = accionesConsultor.getPreguntas();
        for (int i = 0; i < js.length(); i++) {
            try {
                JPanel panel = new JPanel(new GridLayout(0, 1));
                panel.setBackground(new Color(255, 255, 255));
                panelPreguntas.add(panel, "Pregunta " + (i + 1));
                JLabel lab1 = new JLabel("Pregunta " + (i + 1) + ": " + ((JSONObject) js.get(i)).getString("pregunta"), JLabel.CENTER);
                lab1.setFont(new Font("Roboto", 1, 18));
                lab1.setForeground(new Color(134, 36, 31));
                panel.add(lab1);
                JSONArray jarr = ((JSONObject) js.get(i)).getJSONArray("opciones");
                for (int j = 0; j < jarr.length(); j++) {
                    JLabel lab2 = new JLabel("Opción " + (j + 1) + ": " + jarr.getString(j), JLabel.CENTER);
                    lab2.setFont(new Font("Roboto", 1, 15));
                    lab2.setForeground(new Color(0, 0, 0));
                    panel.add(lab2);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AnalistaLocal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cardsPreguntas.show(panelPreguntas, "Pregunta " + 1);
    }

    private class Service extends Thread {

        private IOHandler ioHandler;

        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(5004);
                /*
                 * Inicia cronómetro
                 */
                while (escuchar.isRecibiendo()) {
                    Socket socket = server.accept(); // We should perform some kind of validation...
                    ioHandler = new IOHandler(
                            new DataInputStream(socket.getInputStream()),
                            new DataOutputStream(socket.getOutputStream())
                    );
                    json = new JSONObject(new String(ioHandler.handleIncommingMessage(), Charset.forName("UTF-8")));
                    ioHandler.close();
                    socket.close();
                    switch (json.getInt("action")) {
                        case 1:
                            // aquí debemos obtener la cantidad de participantes
                            // al momento.
                            System.out.println("Voto nuevo");
                            escuchar.actualizarConteo(json.getInt("participantes")); // Necesita ajuste para incorporar valor leido.
                            break;
                        case 2:
                            System.out.println("Proceso Finalizado\n" + json.toString());
                            accionesConsultor.armarConteoOpciones(json);
                            Votacion votacion = accionesConsultor.getVotacion();
                            if (json.getBoolean("es_global")) {
                                json.put("host", accionesConsultor.getLocalHost());
                                json.put("action", 13);
                                new Thread() {
                                    @Override
                                    public void run() {
                                        SoapClient cli = new SoapClient(json);
                                        cli.setHost(accionesConsultor.getRemoteHost());
                                        try {
                                            cli.start();
                                        } catch (SOAPException | IOException ex) {
                                            Logger.getLogger(AnalistaLocal.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }.start();
                                System.out.println("Esperando a que se conecte");
                                socket = server.accept();
                                System.out.println("Se conectó!");
                                ioHandler = new IOHandler(
                                        new DataInputStream(socket.getInputStream()),
                                        new DataOutputStream(socket.getOutputStream())
                                );
                                String whatCameFromBeyond = new String(ioHandler.handleIncommingMessage());
                                ioHandler.sendMessage("Gracias".getBytes());
                                ioHandler.close();
                                socket.close();
                                System.out.println("what came from beyond: " + whatCameFromBeyond);
                                Votacion v = MarshallMySharedVotingObject.unmarshall(whatCameFromBeyond);
                                ResultadoPorPerfil rpp;
                                for (Pregunta pregunta : v.getPreguntas()) {
                                    rpp = new ResultadoPorPerfil("Total global");
                                    rpp.setOpciones(pregunta.getOpciones());
                                    votacion.agregarResultadoPorPerfil(pregunta.getTitulo(), rpp);
                                    for (ResultadoPorPerfil resultadoPorPerfil : pregunta.getResultadosPorPerfil()) {
                                        resultadoPorPerfil.setPerfil("(Global)".concat(resultadoPorPerfil.getPerfil()));
                                        votacion.agregarResultadoPorPerfil(pregunta.getTitulo(), resultadoPorPerfil);
                                    }
                                }
                            }
                            //incommingRequestHandler.terminarConexion();
                            cardLayout.show(panelMain, "3");
                            //escuchar.setRecibiendo(false);
                            consultor = new Consultor(votacion);
                            break;
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        Panel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        Panel1 = new javax.swing.JPanel();
        lblmsj_esperando = new javax.swing.JLabel();
        lbl_cargando = new javax.swing.JLabel();
        Panel2 = new javax.swing.JPanel();
        lblmsj_esperando1 = new javax.swing.JLabel();
        lbl_cargando1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        panelPrincipal = new javax.swing.JPanel();
        panelVotosTotales = new javax.swing.JPanel();
        lbl_votos_totales = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        lblvotos_totales = new javax.swing.JLabel();
        panelVotando = new javax.swing.JPanel();
        panelMain = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        panelEstado = new javax.swing.JPanel();
        encabezado = new javax.swing.JLabel();
        panelPreguntas = new javax.swing.JPanel();
        panelPorcentaje = new javax.swing.JPanel();
        lbl_porcentaje = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        lblporcentaje = new javax.swing.JLabel();
        pnlgrafica = new javax.swing.JPanel();
        panelTimer = new javax.swing.JPanel();
        lbl_tiemporest = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        lblhrs = new javax.swing.JLabel();
        lblmin = new javax.swing.JLabel();
        lblseg = new javax.swing.JLabel();
        lblpuntos = new javax.swing.JLabel();
        lblpuntos1 = new javax.swing.JLabel();

        Panel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("AHORA PUEDES CONSULTAR LOS RESULTADOS FINALES.");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/boton-consultar1.png"))); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jLabel1MouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel1MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel1MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout Panel3Layout = new javax.swing.GroupLayout(Panel3);
        Panel3.setLayout(Panel3Layout);
        Panel3Layout.setHorizontalGroup(
            Panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Panel3Layout.setVerticalGroup(
            Panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel3Layout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jLabel5)
                .addGap(43, 43, 43)
                .addComponent(jLabel1)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        Panel1.setBackground(new java.awt.Color(255, 255, 255));

        lblmsj_esperando.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        lblmsj_esperando.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblmsj_esperando.setText("<html><div align=\"center\">CÓMO VOTAR</html>");

        lbl_cargando.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/participando.gif"))); // NOI18N

        javax.swing.GroupLayout Panel1Layout = new javax.swing.GroupLayout(Panel1);
        Panel1.setLayout(Panel1Layout);
        Panel1Layout.setHorizontalGroup(
            Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblmsj_esperando)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_cargando)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Panel1Layout.setVerticalGroup(
            Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblmsj_esperando, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_cargando)
                .addGap(30, 30, 30))
        );

        Panel2.setBackground(new java.awt.Color(255, 255, 255));

        lblmsj_esperando1.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        lblmsj_esperando1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblmsj_esperando1.setText("<html><div align=\"center\">ESPERANDO A LOS ÚLTIMOS USUARIOS QUE YA HABÍAN INICIADO SU PROCESO DE VOTACIÓN FINALICEN.</html>");

        lbl_cargando1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/espera.gif"))); // NOI18N

        javax.swing.GroupLayout Panel2Layout = new javax.swing.GroupLayout(Panel2);
        Panel2.setLayout(Panel2Layout);
        Panel2Layout.setHorizontalGroup(
            Panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblmsj_esperando1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_cargando1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Panel2Layout.setVerticalGroup(
            Panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblmsj_esperando1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_cargando1)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VOTACIONES EN CURSO");

        jPanel1.setBackground(new java.awt.Color(134, 36, 31));
        jPanel1.setPreferredSize(new java.awt.Dimension(846, 60));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        panelPrincipal.setLayout(new java.awt.GridBagLayout());

        panelVotosTotales.setBackground(new java.awt.Color(255, 255, 255));
        panelVotosTotales.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 0, 0), 3, true));

        lbl_votos_totales.setFont(new java.awt.Font("Roboto", 1, 16)); // NOI18N
        lbl_votos_totales.setText("VOTOS TOTALES:");

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        lblvotos_totales.setFont(new java.awt.Font("Roboto", 1, 100)); // NOI18N
        lblvotos_totales.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblvotos_totales.setText("0");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblvotos_totales, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(206, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblvotos_totales, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelVotosTotalesLayout = new javax.swing.GroupLayout(panelVotosTotales);
        panelVotosTotales.setLayout(panelVotosTotalesLayout);
        panelVotosTotalesLayout.setHorizontalGroup(
            panelVotosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVotosTotalesLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(panelVotosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelVotosTotalesLayout.createSequentialGroup()
                        .addComponent(lbl_votos_totales)
                        .addGap(0, 389, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelVotosTotalesLayout.setVerticalGroup(
            panelVotosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVotosTotalesLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(lbl_votos_totales)
                .addGap(8, 8, 8)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelPrincipal.add(panelVotosTotales, gridBagConstraints);

        panelVotando.setBackground(new java.awt.Color(255, 255, 255));
        panelVotando.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 0, 0), 3, true));

        panelMain.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 794, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 359, Short.MAX_VALUE)
        );

        jLabel8.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        jLabel8.setText("Preguntas");

        panelEstado.setBackground(new java.awt.Color(255, 255, 255));

        encabezado.setBackground(new java.awt.Color(255, 255, 255));
        encabezado.setFont(new java.awt.Font("Roboto", 1, 30)); // NOI18N
        encabezado.setForeground(new java.awt.Color(134, 36, 31));
        encabezado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        encabezado.setText("VOTACIONES EN PROGRESO          ");

        javax.swing.GroupLayout panelEstadoLayout = new javax.swing.GroupLayout(panelEstado);
        panelEstado.setLayout(panelEstadoLayout);
        panelEstadoLayout.setHorizontalGroup(
            panelEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(encabezado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelEstadoLayout.setVerticalGroup(
            panelEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(encabezado, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelPreguntas.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelPreguntasLayout = new javax.swing.GroupLayout(panelPreguntas);
        panelPreguntas.setLayout(panelPreguntasLayout);
        panelPreguntasLayout.setHorizontalGroup(
            panelPreguntasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelPreguntasLayout.setVerticalGroup(
            panelPreguntasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 97, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelVotandoLayout = new javax.swing.GroupLayout(panelVotando);
        panelVotando.setLayout(panelVotandoLayout);
        panelVotandoLayout.setHorizontalGroup(
            panelVotandoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVotandoLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(panelEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
            .addGroup(panelVotandoLayout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panelVotandoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelVotandoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelPreguntas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelVotandoLayout.setVerticalGroup(
            panelVotandoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVotandoLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(panelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPreguntas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelPrincipal.add(panelVotando, gridBagConstraints);

        panelPorcentaje.setBackground(new java.awt.Color(255, 255, 255));
        panelPorcentaje.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 0, 0), 3, true));
        panelPorcentaje.setMaximumSize(new java.awt.Dimension(589, 205));

        lbl_porcentaje.setFont(new java.awt.Font("Roboto", 1, 20)); // NOI18N
        lbl_porcentaje.setText("PORCENTAJE DE PARTICIPACIÓN:");

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setMaximumSize(new java.awt.Dimension(564, 155));

        lblporcentaje.setBackground(new java.awt.Color(255, 255, 255));
        lblporcentaje.setFont(new java.awt.Font("Roboto", 1, 80)); // NOI18N
        lblporcentaje.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblporcentaje.setText("0%");

        pnlgrafica.setEnabled(false);
        pnlgrafica.setMaximumSize(new java.awt.Dimension(161, 131));

        javax.swing.GroupLayout pnlgraficaLayout = new javax.swing.GroupLayout(pnlgrafica);
        pnlgrafica.setLayout(pnlgraficaLayout);
        pnlgraficaLayout.setHorizontalGroup(
            pnlgraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );
        pnlgraficaLayout.setVerticalGroup(
            pnlgraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 131, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblporcentaje, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addGap(34, 34, 34)
                .addComponent(pnlgrafica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlgrafica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblporcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelPorcentajeLayout = new javax.swing.GroupLayout(panelPorcentaje);
        panelPorcentaje.setLayout(panelPorcentajeLayout);
        panelPorcentajeLayout.setHorizontalGroup(
            panelPorcentajeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPorcentajeLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(panelPorcentajeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelPorcentajeLayout.createSequentialGroup()
                        .addComponent(lbl_porcentaje)
                        .addGap(0, 204, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelPorcentajeLayout.setVerticalGroup(
            panelPorcentajeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPorcentajeLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(lbl_porcentaje)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelPorcentajeLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelPrincipal.add(panelPorcentaje, gridBagConstraints);

        panelTimer.setBackground(new java.awt.Color(255, 255, 255));
        panelTimer.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 0, 0), 3, true));
        panelTimer.setMaximumSize(new java.awt.Dimension(535, 185));
        panelTimer.setMinimumSize(new java.awt.Dimension(535, 185));

        lbl_tiemporest.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        lbl_tiemporest.setText("TIEMPO RESTANTE:");

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblhrs.setBackground(new java.awt.Color(255, 255, 255));
        lblhrs.setFont(new java.awt.Font("Roboto", 1, 90)); // NOI18N
        lblhrs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblhrs.setText("00");
        lblhrs.setOpaque(true);
        jPanel9.add(lblhrs, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 160, 80));

        lblmin.setBackground(new java.awt.Color(255, 255, 255));
        lblmin.setFont(new java.awt.Font("Roboto", 1, 90)); // NOI18N
        lblmin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblmin.setText("00");
        lblmin.setOpaque(true);
        jPanel9.add(lblmin, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 140, 80));

        lblseg.setBackground(new java.awt.Color(255, 255, 255));
        lblseg.setFont(new java.awt.Font("Roboto", 1, 90)); // NOI18N
        lblseg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblseg.setText("00");
        lblseg.setOpaque(true);
        jPanel9.add(lblseg, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 60, 150, 80));

        lblpuntos.setBackground(new java.awt.Color(255, 255, 255));
        lblpuntos.setFont(new java.awt.Font("Roboto", 1, 90)); // NOI18N
        lblpuntos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblpuntos.setText(":");
        lblpuntos.setOpaque(true);
        jPanel9.add(lblpuntos, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 40, 70));

        lblpuntos1.setBackground(new java.awt.Color(255, 255, 255));
        lblpuntos1.setFont(new java.awt.Font("Roboto", 1, 90)); // NOI18N
        lblpuntos1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblpuntos1.setText(":");
        lblpuntos1.setOpaque(true);
        jPanel9.add(lblpuntos1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 60, 40, 70));

        javax.swing.GroupLayout panelTimerLayout = new javax.swing.GroupLayout(panelTimer);
        panelTimer.setLayout(panelTimerLayout);
        panelTimerLayout.setHorizontalGroup(
            panelTimerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTimerLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(panelTimerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelTimerLayout.createSequentialGroup()
                        .addComponent(lbl_tiemporest)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelTimerLayout.setVerticalGroup(
            panelTimerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTimerLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(lbl_tiemporest)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelTimerLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelPrincipal.add(panelTimer, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1397, Short.MAX_VALUE)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        //Abrir siguiente ventana
        consultor.iniciar();
        setVisible(false);
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jLabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseEntered
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/boton-consultar2.png"))); // NOI18N

    }//GEN-LAST:event_jLabel1MouseEntered

    private void jLabel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseExited
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/boton-consultar1.png"))); // NOI18N

    }//GEN-LAST:event_jLabel1MouseExited

    private void jLabel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MousePressed
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/boton-consultar3.png"))); // NOI18N

    }//GEN-LAST:event_jLabel1MousePressed

    private void jLabel1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseReleased
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/boton-consultar1.png"))); // NOI18N
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel1MouseReleased

    /**
     * @param args the command line arguments
     */
    public void init() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AnalistaLocal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AnalistaLocal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AnalistaLocal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AnalistaLocal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AnalistaLocal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Panel1;
    private javax.swing.JPanel Panel2;
    private javax.swing.JPanel Panel3;
    private javax.swing.JLabel encabezado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lbl_cargando;
    private javax.swing.JLabel lbl_cargando1;
    private javax.swing.JLabel lbl_porcentaje;
    private javax.swing.JLabel lbl_tiemporest;
    private javax.swing.JLabel lbl_votos_totales;
    private javax.swing.JLabel lblhrs;
    private javax.swing.JLabel lblmin;
    private javax.swing.JLabel lblmsj_esperando;
    private javax.swing.JLabel lblmsj_esperando1;
    private javax.swing.JLabel lblporcentaje;
    private javax.swing.JLabel lblpuntos;
    private javax.swing.JLabel lblpuntos1;
    private javax.swing.JLabel lblseg;
    private javax.swing.JLabel lblvotos_totales;
    private javax.swing.JPanel panelEstado;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelPorcentaje;
    private javax.swing.JPanel panelPreguntas;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelTimer;
    private javax.swing.JPanel panelVotando;
    private javax.swing.JPanel panelVotosTotales;
    private javax.swing.JPanel pnlgrafica;
    // End of variables declaration//GEN-END:variables
    private Timer timerPaneles;

    class Marquesina implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String texto = encabezado.getText().substring(1) + encabezado.getText().substring(0, 1);
            encabezado.setText(texto);
        }

    }

    class PanelesPreguntas implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cardsPreguntas.next(panelPreguntas);
        }

    }

}
