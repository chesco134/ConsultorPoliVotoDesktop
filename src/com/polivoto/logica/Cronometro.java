/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.logica;

/**
 *
 * @author Alfonso 7
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.Timer;

public class Cronometro {

    private final JLabel this_seg, this_min, this_hrs;
    private long tiempoRestante;
    private String hrs = "", min = "", seg = "";
    private Timer timer;
    private boolean cronometroActivo;

    public Cronometro(JLabel hrs, JLabel min, JLabel seg, long tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
        this_min = min;
        this_hrs = hrs;
        this_seg = seg;
    }

    private String calcularEtiqueta(long tiempoActualMilis) {
        int horasRestantes = (int) (tiempoActualMilis / 3600000);
        long minutosRestantesMilis = tiempoActualMilis - (horasRestantes * 3600000);
        int minutosRestantes = (int) (minutosRestantesMilis / 60000);
        long segundosRestantesMilis = minutosRestantesMilis - (minutosRestantes * 60000);
        int segundosRestantes = (int) (segundosRestantesMilis / 1000);
        return (horasRestantes < 10 ? "0" + horasRestantes : horasRestantes)
                + ":"
                + (minutosRestantes < 10 ? "0" + minutosRestantes : minutosRestantes)
                + ":"
                + (segundosRestantes < 10 ? "0" + segundosRestantes : segundosRestantes);
    }

    private void actualizaEtiqueta(String datosDeEtiqueta) {
        //Colocamos en la etiqueta la informacion
        String[] datos = datosDeEtiqueta.split(":");
        hrs = datos[0];
        min = datos[1];
        seg = datos[2];
        this_seg.setText(seg);
        this_min.setText(min);
        this_hrs.setText(hrs);
    }

    //Iniciar el cronometro poniendo cronometroActivo 
    //en verdadero para que entre en el while
    public void iniciarCronometro() {
        cronometroActivo = true;
        timer = new Timer(1000, new CuentaRegresiva());
        timer.setRepeats(true);
        timer.start();
    }

    //Esto es para parar el cronometro
    public void pararCronometro() {
        cronometroActivo = false;
        timer.stop();
    }

    public boolean estatusCronometro() {
        return cronometroActivo;
    }

    private void iniciarCuentaRegresiva() {
        //Mientras cronometroActivo sea verdadero entonces seguira
        //calculando el tiempo restante.
        if (cronometroActivo && tiempoRestante > 0) {
            actualizaEtiqueta(calcularEtiqueta(tiempoRestante));
            tiempoRestante -= 1000;
        }else{
            pararCronometro();
        }
    }

    private class CuentaRegresiva implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            iniciarCuentaRegresiva();
        }
    }
}