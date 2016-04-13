/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas;

import com.polivoto.vistas.acciones.Datos;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.inspira.polivoto.AccionesConsultor;

/**
 *
 * @author azaraf
 */
public class Consultor extends javax.swing.JFrame {

    private static final Font FONT_PLANE = new Font("Roboto", 0, 24);
    private static final Font FONT_BOLD = new Font("Roboto", 1, 24);
    private static final Color COLOR_BLACK = new Color(0, 0, 0);
    private static final Color COLOR_WINE = new Color(134, 36, 31);
    private static CardLayout card = new CardLayout();
    private JPanel panelBarras;
    private JPanel panelPastel;
    private JPanel panelTabla;
    private AccionesConsultor accionesConsultor;
    private Datos data;
    private int pox;
    /**
     * Creates new form Consultor
     */
    public Consultor(){
        initComponents();
        Resultados.setLayout(card);
        panelBarras = new BarrasForm();
        panelPastel = new PastelForm();
        panelTabla = new TablaForm();
        Resultados.add(panelBarras, "barras");
        Resultados.add(panelPastel, "pastel");
        Resultados.add(panelTabla, "tabla");
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }
    
    public Consultor(int pox, AccionesConsultor accionesConsultor){
        this.accionesConsultor = accionesConsultor;
        this.data = new Datos(accionesConsultor, pox);
        
        this.pox = pox;
        initComponents();
        Resultados.setLayout(card);
        panelBarras = new BarrasForm();
        panelPastel = new PastelForm();
        panelTabla = new TablaForm();
        Resultados.add(panelBarras, "barras");
        Resultados.add(panelPastel, "pastel");
        Resultados.add(panelTabla, "tabla");
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        botonMenu = new javax.swing.JLabel();
        Resultados = new javax.swing.JPanel();
        panelLateral = new javax.swing.JPanel();
        iconoBarras = new javax.swing.JLabel();
        iconoTabla = new javax.swing.JLabel();
        iconoPastel = new javax.swing.JLabel();
        iconoPoliVoto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(977, 600));
        setSize(new java.awt.Dimension(977, 600));

        panelHeader.setBackground(new java.awt.Color(134, 36, 31));

        botonMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/lateral.png"))); // NOI18N
        botonMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonMenuMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botonMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Resultados.setBackground(new java.awt.Color(250, 250, 250));

        javax.swing.GroupLayout ResultadosLayout = new javax.swing.GroupLayout(Resultados);
        Resultados.setLayout(ResultadosLayout);
        ResultadosLayout.setHorizontalGroup(
            ResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 713, Short.MAX_VALUE)
        );
        ResultadosLayout.setVerticalGroup(
            ResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 620, Short.MAX_VALUE)
        );

        panelLateral.setVisible(banderaPanelLateral);
        panelLateral.setBackground(new java.awt.Color(240, 240, 240));

        iconoBarras.setFont(new java.awt.Font("Roboto", 0, 24)); // NOI18N
        iconoBarras.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        iconoBarras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/bars.png"))); // NOI18N
        iconoBarras.setText("<html>VER GRÁFICA<p>DE BARRAS</html>");
        iconoBarras.setIconTextGap(12);
        iconoBarras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                iconoBarrasMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                iconoBarrasMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                iconoBarrasMouseEntered(evt);
            }
        });

        iconoTabla.setFont(new java.awt.Font("Roboto", 0, 24)); // NOI18N
        iconoTabla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/chart.png"))); // NOI18N
        iconoTabla.setText("VER TABLA");
        iconoTabla.setEnabled(false);
        iconoTabla.setIconTextGap(12);
        iconoTabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                iconoTablaMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                iconoTablaMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                iconoTablaMouseEntered(evt);
            }
        });

        iconoPastel.setFont(new java.awt.Font("Roboto", 0, 24)); // NOI18N
        iconoPastel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/pie.png"))); // NOI18N
        iconoPastel.setText("<html>VER GRÁFICA<p>DE PASTEL</html>");
        iconoPastel.setIconTextGap(12);
        iconoPastel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                iconoPastelMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                iconoPastelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                iconoPastelMouseEntered(evt);
            }
        });

        iconoPoliVoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/header.png"))); // NOI18N

        javax.swing.GroupLayout panelLateralLayout = new javax.swing.GroupLayout(panelLateral);
        panelLateral.setLayout(panelLateralLayout);
        panelLateralLayout.setHorizontalGroup(
            panelLateralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLateralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLateralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconoTabla, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(iconoBarras)
                    .addComponent(iconoPastel)
                    .addGroup(panelLateralLayout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(iconoPoliVoto)
                        .addGap(63, 63, 63)))
                .addContainerGap())
        );
        panelLateralLayout.setVerticalGroup(
            panelLateralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLateralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(iconoPoliVoto)
                .addGap(51, 51, 51)
                .addComponent(iconoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(iconoPastel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(iconoTabla)
                .addContainerGap(265, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(panelLateral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Resultados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Resultados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelLateral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonMenuMouseClicked
        panelLateral.setVisible(!banderaPanelLateral);
        banderaPanelLateral = !banderaPanelLateral;
    }//GEN-LAST:event_botonMenuMouseClicked

    private void iconoBarrasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoBarrasMouseEntered
        mouseEntered((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoBarrasMouseEntered

    private void iconoBarrasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoBarrasMouseExited
        mouseExited((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoBarrasMouseExited

    private void iconoPastelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoPastelMouseEntered
        mouseEntered((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoPastelMouseEntered

    private void iconoPastelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoPastelMouseExited
        mouseExited((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoPastelMouseExited

    private void iconoTablaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoTablaMouseEntered
        mouseEntered((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoTablaMouseEntered

    private void iconoTablaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoTablaMouseExited
        mouseExited((JLabel)evt.getSource());
    }//GEN-LAST:event_iconoTablaMouseExited

    private void iconoBarrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoBarrasMouseClicked
        panelLateral.setVisible(false);
        data.setBarChartIn(panelBarras);
        card.show(Resultados, "barras");
    }//GEN-LAST:event_iconoBarrasMouseClicked

    private void iconoPastelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoPastelMouseClicked
        panelLateral.setVisible(false);
        data.setPieChartIn(panelPastel);
        card.show(Resultados, "pastel");
    }//GEN-LAST:event_iconoPastelMouseClicked

    private void iconoTablaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iconoTablaMouseClicked
        panelLateral.setVisible(false);
        card.show(Resultados, "tabla");
    }//GEN-LAST:event_iconoTablaMouseClicked

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
            java.util.logging.Logger.getLogger(Consultor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Consultor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Consultor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Consultor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Resultados;
    private javax.swing.JLabel botonMenu;
    private javax.swing.JLabel iconoBarras;
    private javax.swing.JLabel iconoPastel;
    private javax.swing.JLabel iconoPoliVoto;
    private javax.swing.JLabel iconoTabla;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelLateral;
    // End of variables declaration//GEN-END:variables
    private boolean banderaPanelLateral = false;

    private void mouseEntered(JLabel label) {
        label.setFont(FONT_BOLD);
        label.setForeground(COLOR_WINE);
    }

    private void mouseExited(JLabel label) {
        label.setFont(FONT_PLANE);
        label.setForeground(COLOR_BLACK);
    }

}
