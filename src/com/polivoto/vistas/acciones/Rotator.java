package com.polivoto.vistas.acciones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;

/**
 *
 * @author azaraf
 */
class Rotator extends Timer implements ActionListener {

    /** The plot. */
    private PiePlot3D plot3D;
    private PiePlot plot;
    /** The angle. */
    private int angle = 270;
    private int dimension = 0;
    /**
     * Constructor.
     *
     * @param plot  the plot.
     */
    Rotator(final PiePlot3D plot3D) {
        super(100, null);
        this.plot3D = plot3D;
        addActionListener(this);
        dimension = 1;
    }
    
    Rotator(final PiePlot plot) {
        super(100, null);
        this.plot = plot;
        addActionListener(this);
        dimension = 2;
    }

    /**
     * Modifies the starting angle.
     *
     * @param event  the action event.
     */
    public void actionPerformed(final ActionEvent event) {
        if (dimension == 1){
            this.plot3D.setStartAngle(this.angle);
        }else{
            this.plot.setStartAngle(this.angle);
        }
        this.angle = this.angle + 1;
        if (this.angle == 360) {
            this.angle = 0;
        }
    }

}
