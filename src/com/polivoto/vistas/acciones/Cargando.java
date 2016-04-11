/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas.acciones;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author azaraf
 */
public class Cargando {

    private JPanel glass;
    private JFrame frame;
    private int x;

    public Cargando(JFrame frame) {
        this.frame = frame;
        x = 0;
        crearGlassPanel();
    }

    public Cargando(JFrame frame, int x) {
        this.frame = frame;
        this.x = x;
        crearGlassPanel();
    }

    private void crearGlassPanel() {
        glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
                g.setColor(new Color(255,255,255, 230));
                g.fillRect(x, 0, frame.getWidth(), frame.getHeight());
            }
        };
        glass.setOpaque(false);
        glass.setLayout(new GridBagLayout());
        JLabel icon = new javax.swing.JLabel();

        icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/load.gif"))); // NOI18N

        icon.setText("Espere...");
        icon.setFont(new Font("Roboto", 1, 18));
        icon.setForeground(new Color(55,55,55));
        icon.setVerticalTextPosition(JLabel.BOTTOM);
        icon.setHorizontalTextPosition(JLabel.CENTER);
        glass.add(icon);
        glass.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume(); //To change body of generated methods, choose Tools | Templates.
                Toolkit.getDefaultToolkit().beep();
            }
            
        });
        frame.setGlassPane(glass);
    }

    public void setLoadingPanel() {
        glass.setVisible(true);
    }

    public void removeLoadingPanel() {
        glass.setVisible(false);
    }

    public JPanel getGlass() {
        return glass;
    }

}
