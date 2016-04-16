package com.polivoto.threading;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.inspira.polivoto.AccionesConsultor;
import org.json.JSONException;

/**
 * Created by jcapiz on 13/01/16.
 */
public class AdminConexionAutomatica extends Thread {

    private ConcurrentLinkedQueue<TareaDeConexion> filaDeTareasInferior;
    private ConcurrentLinkedQueue<TareaDeConexion> filaDeTareasSuperior;
    private TareaDeConexion.EscuchaDeConexion escuchaConexion;
    private int trabajadoresFinalizados;
    private int totalTrabajadores;
    private boolean running;

    public AdminConexionAutomatica() {
    }

    public void setEscuchaConexion(TareaDeConexion.EscuchaDeConexion escuchaConexion) {
        this.escuchaConexion = escuchaConexion;
    }
    
    public synchronized void cancelRunning(TareaDeConexion tarea){
        System.out.println("Hemos hecho la conexión: " + tarea.getHost());
        running = false;
    }

    public synchronized void trabajoTerminado() {
        trabajadoresFinalizados++;
        if (trabajadoresFinalizados >= totalTrabajadores) {
            if (running) {
                running = false;
                System.out.println("\"No pudimos encontrar al servidor, "
                        + "por favor ingrese la ip con la entrada manual.\"");
            }
        }
    }

    @Override
    public void run() {
        try {
            filaDeTareasInferior = new ConcurrentLinkedQueue<>();
            filaDeTareasSuperior = new ConcurrentLinkedQueue<>();
            descubrirServidor();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private void descubrirServidor() throws UnknownHostException {
        InetAddress thisHost = Inet4Address.getLocalHost();
        if(!thisHost.isLoopbackAddress()){
            String mAddr = thisHost.toString().split("/")[1];
            System.out.println("Inet4Address: " + mAddr);
            String[] slots = mAddr.split("\\.");
            String prefix = slots[0] + "." + slots[1] + "." + slots[2] + ".";
            int hostActual = Integer.parseInt(slots[3].trim());
            int tareasPendientesInferior = hostActual;
            for (int i = tareasPendientesInferior; i > 0; i--) {
                filaDeTareasInferior.add(new TareaDeConexion(escuchaConexion, prefix + i, 23543));
            }
            for (int i = hostActual + 1; i < 255; i++) {
                filaDeTareasSuperior.add(new TareaDeConexion(escuchaConexion, prefix + i, 23543));
            }
            running = true;
            int threadCount = 5;
            totalTrabajadores = 2 * threadCount;
            trabajadoresFinalizados = 0;
            TrabajadorDeConexionInferior[] trabajadoresParteInf = new TrabajadorDeConexionInferior[threadCount];
            for (int i = 0; i < threadCount; i++) {
                trabajadoresParteInf[i] = new TrabajadorDeConexionInferior();
                trabajadoresParteInf[i].setPriority(Thread.currentThread().getPriority() - 1);
                trabajadoresParteInf[i].start();
            }
            TrabajadorDeConexionSuperior[] trabajadoresParteSup = new TrabajadorDeConexionSuperior[threadCount];
            for (int i = 0; i < threadCount; i++) {
                trabajadoresParteSup[i] = new TrabajadorDeConexionSuperior();
                trabajadoresParteSup[i].setPriority(Thread.currentThread().getPriority() - 1);
                trabajadoresParteSup[i].start();
            }
        }else{
            System.out.println("Posiblemente no tenemos acceso a la red.");
        }
    }

    private class TrabajadorDeConexionInferior extends Thread {

        @Override
        public void run() {
            while (running) {
                TareaDeConexion task = filaDeTareasInferior.poll();
                if (task == null) {
                    break;
                }
                task.run();
            }
            trabajoTerminado();
        }
    }

    private class TrabajadorDeConexionSuperior extends Thread {

        @Override
        public void run() {
            while (running) {
                TareaDeConexion tarea = filaDeTareasSuperior.poll();
                if (tarea == null) {
                    break;
                }
                tarea.run();
            }
            trabajoTerminado();
        }
    }
}
