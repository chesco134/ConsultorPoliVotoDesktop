/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.threading;

import com.polivoto.networking.IOHandler;
import com.polivoto.networking.ServicioDeIPExterna;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPException;
import org.inspira.polivoto.AccionesConsultor;
import org.json.JSONException;
import org.json.JSONObject;
import com.polivoto.networking.SoapClient;
import com.polivoto.shared.Opcion;
import com.polivoto.shared.ResultadoPorPerfil;
import com.polivoto.vistas.AnalistaLocal;
import com.polivoto.vistas.Consultor;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.inspira.polivoto.proveedores.LogProvider;
import org.inspira.polivoto.proveedores.ProveedorDeRecursos;
import org.json.JSONArray;


/**
 *
 * @author jcapiz
 */
public class IncommingRequestHandler extends Thread {

    private AccionesConsultor accionesConsultor;
    private ServerSocket server;
    private String remoteHost;
    private boolean useExternalHost;
    private boolean isShowing;
    private String localHost;
    private JFrame mainFrame;
    private int idVotacion;

    public void setAccionesConsultor(JFrame mainFrame, AccionesConsultor accionesConsultor, String remoteHost, boolean useExternalHost) {
        this.mainFrame = mainFrame;
        this.accionesConsultor = accionesConsultor;
        this.remoteHost = remoteHost;
        this.useExternalHost = useExternalHost;
        isShowing = false;
        try {
            localHost = useExternalHost ? ServicioDeIPExterna.obtenerIPExterna() : Inet4Address.getLocalHost().getHostAddress() + ":8080";
        } catch (UnknownHostException ex) {
            Logger.getLogger(IncommingRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            Map<String, Charset> m = Charset.availableCharsets();
            for(String key : m.keySet())
                System.out.println("The charset is: " + key + " -> " + m.get(key).name() + ", which for human is: " + m.get(key).displayName());
            server = new ServerSocket(5010);
            while (true) {
                Socket socket = server.accept();
                new AccionRequerida(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminarConexion() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AccionRequerida extends Thread {

        private Socket socket;

        public AccionRequerida(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            DataInputStream entrada = null;
            DataOutputStream salida = null;
            IOHandler ioHandler = null;
            try {
                entrada = new DataInputStream(socket.getInputStream());
                salida = new DataOutputStream(socket.getOutputStream());
                ioHandler = new IOHandler(entrada, salida);
                byte[] chunk = ioHandler.handleIncommingMessage();
                String smthg = ioHandler.getAllocatedString();
                System.out.println("Llegó: " + smthg + ". From " + socket.getRemoteSocketAddress());
                JSONObject json = new JSONObject(smthg);
                String resp;
                SoapClient sc;
                resp = "¡Listo!";
                if (useExternalHost) {
                    remoteHost = ServicioDeIPExterna.obtenerIPServidorRemoto();
                }
                switch (json.getInt("action")) {
                    case 1: // Local server needs to check out the status of one boleta...
                        json.put("action", 8);
                        sc = new SoapClient(json);
                        sc.setHost(remoteHost);
                        resp = sc.start();
                        break;
                    case 2: // Remote server needs to check out the status of one boleta locally...
                        resp = accionesConsultor.consultaBoletaRemota(json.getString("boleta"));
                        break;
                    case 3: // Local server wants to go global.
                        System.out.println("Inet4Addr " + Inet4Address.getLocalHost().getHostAddress());
                        if (localHost != null) {
                            try {
                                json.put("action", 11);
                                json.put("host", localHost);
                                SoapClient cli = new SoapClient(json);
                                cli.setHost(remoteHost);
                                resp = cli.start();
                            } catch (JSONException | SOAPException e) {
                                e.printStackTrace();
                            }
                        } else {
                            resp = "Servicio por el momento no disponible";
                        }
                        break;
                    case 5: // Solicitud de registro sólo de incorporación de Host consultor
                        try {
                            idVotacion = json.getInt("id_votacion");
                            json.put("action", 3);
                            json.put("host", useExternalHost ? ServicioDeIPExterna.obtenerIPExterna() : Inet4Address.getLocalHost().getHostAddress() + ":8080");
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 3" : resp.concat(", 3");
                        }
                        break;
                    case 6: // Solicitud de cuestionario
                        try {
                            json.put("action", 7);
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                            System.out.println("El quiz dice: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 7" : resp.concat(", 7");
                        }
                        break;
                    case 7: // Solicitud de procesos disponibles
                        try {
                            json.put("action", 5);
                            System.out.println("Es el caso 5! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 5" : resp.concat(", 5");
                        }
                        break;
                    case 8: // Solicitud de finalizacion
                        try {
                            json.put("action", 6);
                            System.out.println("Es el caso 6! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.start();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 6" : resp.concat(", 6");
                        }
                        break;
                    case 9:
                        ioHandler.sendMessage("¡Listo!".getBytes());
                        socket.close();
                        try {
                            accionesConsultor.consultaVotacionesDisponibles();
                            JSONArray array = accionesConsultor.getVotacionesDisponibles();
                            Map<String, Integer> elementos = new TreeMap<>();
                            Map<String, Integer> repetidos = new TreeMap<>();
                            Integer cantidad;
                            String textoAlterno;
                            boolean containsKey;
                            for (int i = 0; i < array.length(); i++) {
                                json = array.getJSONObject(i);
                                containsKey = elementos.containsKey(json.getString("titulo"));
                                if (containsKey) {
                                    cantidad = repetidos.get(json.getString("titulo"));
                                    cantidad = cantidad == null ? 1 : cantidad + 1;
                                    repetidos.put(json.getString("titulo"), cantidad);
                                    textoAlterno = json.getString("titulo") + "(" + (cantidad < 10 ? "0" + cantidad : cantidad) + ")";
                                    elementos.put(textoAlterno, json.getInt("idVotacion"));
                                } else {
                                    elementos.put(json.getString("titulo"), json.getInt("idVotacion"));
                                }
                            }
                            String tituloSeleccionado
                                    = (String) JOptionPane.showInputDialog(null, "Por favor seleccione un título de votación:",
                                            "Votaciones hechas", JOptionPane.QUESTION_MESSAGE,
                                            null, elementos.keySet().toArray(new String[]{}), elementos.keySet().iterator().next());
                            com.polivoto.shared.Votacion mVotacion = new com.polivoto.shared.Votacion(tituloSeleccionado);
                            accionesConsultor.consultaDetallesDeVotacion(elementos.get(tituloSeleccionado));
                            String detallesVotacion = accionesConsultor.getDetallesDeVotacion();
                            json = new JSONObject(detallesVotacion);
                            mVotacion.setFechaFin(json.getLong("Fecha_Fin"));
                            mVotacion.setFechaInicio(json.getLong("Fecha_Inicio"));
                            mVotacion.setLugar(json.getString("lugar"));
                            JSONArray jpreguntas = json.getJSONArray("preguntas");
                            accionesConsultor.setPreguntas(jpreguntas);
                            JSONArray extra = new JSONArray();
                            JSONObject result;
                            Opcion opcion;
                            ResultadoPorPerfil rpp;
                            List<Opcion> opciones;
                            for (int j = 0; j < jpreguntas.length(); j++) {
                                int participantesQueRespondieronPregunta = 0;
                                for (int i = 0; i < jpreguntas.getJSONObject(j).getJSONArray("opciones").length(); i++) {
                                    participantesQueRespondieronPregunta += jpreguntas.getJSONObject(j).getJSONArray("opciones").getJSONObject(i).getInt("cantidad");
                                }
                                result = new JSONObject();
                                result.put("participantes", participantesQueRespondieronPregunta); // Es el número total de participantes por pregunta.
                                result.put("conteo", jpreguntas.getJSONObject(j).getJSONArray("opciones")); // Arreglo de conteo de votos por opción
                                extra.put(result);
                                mVotacion.agregaPregunta(jpreguntas.getJSONObject(j).getString("pregunta"));
                                for (int k = 0; k < jpreguntas.getJSONObject(j).getJSONArray("opciones").length(); k++) {
                                    mVotacion.agregarOpcion(jpreguntas.getJSONObject(j).getString("pregunta"), new Opcion(jpreguntas.getJSONObject(j).getJSONArray("opciones").getJSONObject(k).getString("reactivo"), jpreguntas.getJSONObject(j).getJSONArray("opciones").getJSONObject(k).getInt("cantidad")));
                                }
                                for (int k = 0; k < jpreguntas.getJSONObject(j).getJSONArray("resultados_perfiles").length(); k++) {
                                    rpp = new ResultadoPorPerfil(jpreguntas.getJSONObject(j).getJSONArray("resultados_perfiles").getJSONObject(k).getString("perfil"));
                                    opciones = new ArrayList<>();
                                    for (int l = 0; l < jpreguntas.getJSONObject(j).getJSONArray("resultados_perfiles").getJSONObject(k).getJSONArray("opciones").length(); l++) {
                                        opciones.add(new Opcion(jpreguntas.getJSONObject(j).getJSONArray("resultados_perfiles").getJSONObject(k).getJSONArray("opciones").getJSONObject(l).getString("reactivo"), jpreguntas.getJSONObject(j).getJSONArray("resultados_perfiles").getJSONObject(k).getJSONArray("opciones").getJSONObject(l).getInt("cantidad")));
                                    }
                                    rpp.setOpciones(opciones);
                                    mVotacion.agregarResultadoPorPerfil(jpreguntas.getJSONObject(j).getString("pregunta"), rpp);
                                }
                            }
                            accionesConsultor.setConteoOpcionesPregunta(extra);
                            Consultor consultor = new Consultor(mVotacion);
                            consultor.iniciar();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 10:
                        if (!isShowing) {
                            isShowing = true;
                            ioHandler.sendMessage("¡Listo!".getBytes());
                            socket.close();
                            continuar();
                        } else {
                            resp = "Ya habíamos empezado";
                        }
                        break;
                    case 11: // Carga de secretos
                        try {
                            json.put("action", 9);
                            System.out.println("Es el caso 9! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.start();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 9" : resp.concat(", 9");
                        }
                        break;
                    case 12: // Atención a sincronía de tiempos
                        json.put("t_salida", new java.util.Date().getTime());
                        resp = json.toString();
                        break;
                    case 13: // Petición de perfiles
                        try {
                            json.put("action", 10);
                            System.out.println("Es el caso 10! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 10" : resp.concat(", 10");
                        }
                        break;
                    case 14: // Enviar perfiles de votación
                        try {
                            json.put("action", 2);
                            System.out.println("Es el caso 2! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                            resp = resp.equals("success") ? "¡Listo!" : "Servicio por el momento no disponible";
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 2" : resp.concat(", 2");
                        }
                        break;
                    case 15: // Solicitar perfiles de votación
                        try {
                            json.put("action", 10);
                            System.out.println("Es el caso 10! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 10" : resp.concat(", 10");
                        }
                        break;
                    case 16: // Obtener estampa de tiempo de servidor principal
                        long estampaDeTiempo = accionesConsultor.consultaEstampaDeTiempoServidor();
                        json = new JSONObject();
                        json.put("estampa_de_tiempo", estampaDeTiempo);
                        resp = json.toString();
                        break;
                    case 17: // Server whants to know main timestamp
                        json.put("action", 12);
                        try {
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.start();
                        } catch (IOException | SOAPException e) {
                            resp = json.toString();
                        }
                        break;
                    case 18:
                        json.put("action", 14);
                        json.put("host", localHost);
                        sc = new SoapClient(json);
                        sc.setHost(remoteHost);
                        resp = sc.start();
                        break;
                    default:
                }
                ioHandler.sendMessage(resp.getBytes(Charset.forName("UTF-8")));
                socket.close();
            } catch (SOAPException | IOException | JSONException e) {
                try {
                    ioHandler.sendMessage("Ocurrió un fallo de conexión, no podemos atenderle por el momento".getBytes());
                    socket.close();
                } catch (NullPointerException | IOException putoSiCaesAqui) {
                    putoSiCaesAqui.printStackTrace();
                }
                Logger.getLogger(IncommingRequestHandler.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void yaVasAEmpezar() {
        final long startingMillis = new java.util.Date().getTime();
        EsperanzaDeTiempo esperanzaDeTiempo = new EsperanzaDeTiempo(1, 1, accionesConsultor, (long) 3.6e5, new EsperanzaDeTiempo.TimeToCookAction() {

            @Override
            public void coocked(Long esperanzaCalculada, String status) {
                long finishingMillis = new java.util.Date().getTime();
                if ("Ok".equals(status)) {
                    LogProvider.logMessage(getClass().getName(), "Terminamos a las: " + ProveedorDeRecursos.obtenerFecha());
                    LogProvider.logMessage(getClass().getName(), "Tiempo total: " + ProveedorDeRecursos.obtenerFormatoEnHoras(finishingMillis - startingMillis));
                    LogProvider.logMessage(getClass().getName(), "Esperanza calculada: " + esperanzaCalculada);
                    //continuar(esperanzaCalculada);
                } else {
                    final JFrame f = new JFrame();
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            f.dispose();
                        }
                    }, 2700);
                    JOptionPane.showMessageDialog(f, status);
                }
            }
        });
        LogProvider.logMessage(getClass().getName(), "Iniciamos a las: " + ProveedorDeRecursos.obtenerFecha());
        esperanzaDeTiempo.bake();
    }

    private void continuar() {
        accionesConsultor.setLocalHost(localHost);
        accionesConsultor.setRemoteHost(remoteHost);
        AnalistaLocal analistaLocal = new AnalistaLocal(accionesConsultor);
        analistaLocal.init();
    }
}
