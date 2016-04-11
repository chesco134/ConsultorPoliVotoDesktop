package Test;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JPanel;

class BlockInput extends JFrame {

    JButton b;

    public BlockInput() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        setTitle("Block input");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create JButton
        b = new JButton("Block input");
        add(b);

        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Create a JPanel with semi-transparent
                // black background
                // This will be glass pane
                JPanel p = new JPanel() {
                    public void paintComponent(Graphics g) {
                        g.setColor(new Color(0,0,0,60));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };

                // Set it non-opaque
                p.setOpaque(false);

                // Set layout to JPanel
                p.setLayout(new GridBagLayout());

                // Add the jlabel with the image icon
                JLabel lbl_cargando = new javax.swing.JLabel();

                lbl_cargando.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/polivoto/imagenes/load.gif"))); // NOI18N

                lbl_cargando.setText("Espere...");
                lbl_cargando.setFont(new Font("Roboto", 0, 18));
                lbl_cargando.setForeground(new Color(240,240,240));
                lbl_cargando.setVerticalTextPosition(JLabel.BOTTOM);
                lbl_cargando.setHorizontalTextPosition(JLabel.CENTER);
                p.add(lbl_cargando);
                // Take glass pane
                setGlassPane(p);

                // Add MouseListener
                p.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent me) {
                        // Consume the event, now the input is blocked
                        me.consume();

                        // Create beep sound, when mouse is pressed
                        Toolkit.getDefaultToolkit().beep();
                    }
                });

                // Make it visible, it isn't by default because
                // it is set as glass pane
                p.setVisible(true);
            }
        });

        setSize(400, 400);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        BlockInput blockInput = new BlockInput();
    }
}
