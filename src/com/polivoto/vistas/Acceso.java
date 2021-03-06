/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas;

import com.polivoto.threading.AdminConexionAutomatica;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.ImageIcon;
import org.json.JSONException;
import com.polivoto.threading.IncommingRequestHandler;
import com.polivoto.threading.TareaDeConexion;
import com.polivoto.vistas.acciones.Cargando;
import java.net.Socket;
import javax.swing.JOptionPane;
import org.inspira.polivoto.AccionesConsultor;
import org.inspira.polivoto.proveedores.ProveedorDeArchivo;
import org.jdesktop.swingx.prompt.PromptSupport;

/**
 *
 * @author David Vargas
 */
public class Acceso extends javax.swing.JFrame {

    private AccionesConsultor accionesConsultor;
    private IncommingRequestHandler incommingRequestHandler;
    private String host;
    private String remoteHost;
    private String usrName;
    private String pwd;
    private boolean useExternalIP;

    public Acceso(String[] args) {
        if (args.length > 1) {
            host = args[0];
            remoteHost = args[1];
        } else if (args.length > 0) {
            host = args[0];
            remoteHost = null;
        } else {
            remoteHost = null;
            host = null;
        }
        useExternalIP = remoteHost == null;
        myStartup();
    }

    /**
     * Creates new form Results
     */
    public Acceso() {
        myStartup();
    }

    private void myStartup() {
        initComponents();
        PromptSupport.setPrompt("Consultor", usrTextField);
        PromptSupport.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT, usrTextField);
        PromptSupport.setPrompt("Contraseña", pwdTextField);
        PromptSupport.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT, pwdTextField);
        usrTextField.setEnabled(false);
        usrTextField.setText("Consultor");
        pwdTextField.requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jContacto = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        usrTextField = new javax.swing.JTextField();
        pwdTextField = new javax.swing.JPasswordField();
        boton = new javax.swing.JLabel();

        setLocationRelativeTo(null);
        jContacto.setTitle("PoliVoto Electrónico");
        jContacto.setBackground(new java.awt.Color(255, 255, 255));
        jContacto.setMinimumSize(new java.awt.Dimension(437, 260));
        jContacto.setResizable(false);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/logo.png"))); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("<html> <h2>PoliVoto Electrónico &copy</h2>  <p>Copia exclusiva para la Unidad Profesional <p>Interdisciplinaria en Ingeniería y Tecnologías <p>Avanzadas del Instituto Politécnico <p>Nacional. <p>Contacto y ayuda:</p>  <p>David A. Vargas: +52 (55) 2989 2764  <p>Juan Capiz: +52 (55) 1379 0281  <p>Alfonso De La Rosa: +52 (55) 4892 5376 <p>correo: contacto@votacionesipn.com</html>");

        javax.swing.GroupLayout jContactoLayout = new javax.swing.GroupLayout(jContacto.getContentPane());
        jContacto.getContentPane().setLayout(jContactoLayout);
        jContactoLayout.setHorizontalGroup(
            jContactoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jContactoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jContactoLayout.setVerticalGroup(
            jContactoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jContactoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jContactoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jContactoLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Ingresar");
        setIconImage(new ImageIcon(getClass().getResource("/com/polivoto/imagenes/icono.png")).getImage());
        setMinimumSize(new java.awt.Dimension(400, 410));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(400, 400));
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 400));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 400));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/lodo-mediano.png"))); // NOI18N

        usrTextField.setFont(new java.awt.Font("Roboto", 0, 17)); // NOI18N
        usrTextField.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(134, 36, 31)));
        usrTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                usrTextFieldKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                usrTextFieldKeyPressed(evt);
            }
        });

        pwdTextField.setFont(new java.awt.Font("Roboto", 0, 17)); // NOI18N
        pwdTextField.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(134, 36, 31)));
        pwdTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pwdTextFieldKeyPressed(evt);
            }
        });

        boton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        boton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/state1.png"))); // NOI18N
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                botonMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botonMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botonMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(usrTextField)
                    .addComponent(pwdTextField)
                    .addComponent(boton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(92, 92, 92))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel5)
                .addGap(80, 80, 80)
                .addComponent(usrTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pwdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(boton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void botonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMouseEntered
        boton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/state2.png")));
    }//GEN-LAST:event_botonMouseEntered

    private void botonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMouseExited
        boton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/state1.png")));
    }//GEN-LAST:event_botonMouseExited

    private void botonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMouseClicked
        (new Thread(new Loading())).start();
        botonClicked();
    }//GEN-LAST:event_botonMouseClicked

    private void botonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMousePressed
        boton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/state3.png")));
    }//GEN-LAST:event_botonMousePressed

    private void botonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMouseReleased
        boton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/state2.png")));
    }//GEN-LAST:event_botonMouseReleased

    private void usrTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usrTextFieldKeyPressed
        char c = evt.getKeyChar();
        if (c == '\n') {
            (new Thread(new Loading())).start();
            botonClicked();
        }
    }//GEN-LAST:event_usrTextFieldKeyPressed

    private void pwdTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pwdTextFieldKeyPressed
        char c = evt.getKeyChar();
        if (c == '\n') {
            (new Thread(new Loading())).start();
            botonClicked();
        }
    }//GEN-LAST:event_pwdTextFieldKeyPressed

    private void usrTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usrTextFieldKeyTyped
        char c = evt.getKeyChar();
        if (c == '\n') {
            pwdTextField.selectAll();
        }
    }//GEN-LAST:event_usrTextFieldKeyTyped

    /**
     * @param args the command line arguments
     */
    public void iniciar() {
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
            java.util.logging.Logger.getLogger(Acceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Acceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Acceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Acceso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel boton;
    private javax.swing.JDialog jContacto;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField pwdTextField;
    private javax.swing.JTextField usrTextField;
    // End of variables declaration//GEN-END:variables
    Cargando loading = new Cargando(this);

    private void connect(String host, String usrName, String pwd) {
        try {
            accionesConsultor = new AccionesConsultor(host, usrName, pwd);
            if(accionesConsultor.getLID() <= 0){
                usuarioInvalido();
            }else{
                incommingRequestHandler = new IncommingRequestHandler();
                incommingRequestHandler.setAccionesConsultor(this, accionesConsultor, remoteHost, useExternalIP);
                incommingRequestHandler.start(); // We need to keep track of this object.
                setVisible(false);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | JSONException ex) {
            loading.removeLoadingPanel();
            Logger.getLogger(Acceso.class.getName()).log(Level.SEVERE, null, ex);
            sinConexion();
        }
    }

    private void botonClicked() {
        usrName = usrTextField.getText();
        pwd = new String(pwdTextField.getPassword());
        if (host == null) {
            host = ProveedorDeArchivo.leerHost();
            if (host == null) {
                AdminConexionAutomatica admin;
                admin = new AdminConexionAutomatica();
                TareaDeConexion.EscuchaDeConexion escuchaDeConexion;
                escuchaDeConexion = new TareaDeConexion.EscuchaDeConexion() {
                    @Override
                    public void conexionExitosa(TareaDeConexion tarea) {
                        admin.cancelRunning(tarea);
                        ProveedorDeArchivo.escribirHost(tarea.getHost());
                        whatToDoWhenWeHaveTheHost(tarea.getHost());
                    }

                    @Override
                    public void conexionFallida() {
                        loading.removeLoadingPanel();
                    }
                };
                admin.setEscuchaConexion(escuchaDeConexion);
                admin.start();
                System.out.println("Servicio de búsqueda iniciado.");
            } else {
                new FirstServerConnection(host).start();
            }
        } else {
            new FirstServerConnection(host).start();
        }
    }

    class FirstServerConnection extends Thread {

        private String host;

        public FirstServerConnection(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(host, 23543);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            whatToDoWhenWeHaveTheHost(host);
        }
    }

    private void whatToDoWhenWeHaveTheHost(String host) {
        System.out.println("Now connecting...");
        connect(host, usrName, pwd);
    }

    private void usuarioInvalido() {
        JOptionPane.showMessageDialog(
                null,
                "Usuario inválido o Contraseña incorrecta",
                "Error",
                JOptionPane.OK_OPTION,
                new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/error.png"))
        );
    }

    private void sinConexion() {
        JOptionPane.showMessageDialog(
                null,
                "Es imposible conectarse al servidor.\nRevise la conexión.",
                "Error",
                JOptionPane.OK_OPTION,
                new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/alert.png"))
        );
    }

    class Loading implements Runnable {

        @Override
        public void run() {
            loading.setLoadingPanel();
        }

    }
}
