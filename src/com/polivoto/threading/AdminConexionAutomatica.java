package com.polivoto.threading;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jcapiz on 13/01/16.
 */
public class AdminConexionAutomatica extends Thread implements TareaDeConexion.EscuchaDeConexion{

    private ConcurrentLinkedQueue<TareaDeConexion> filaDeTareasInferior;
    private ConcurrentLinkedQueue<TareaDeConexion> filaDeTareasSuperior;
    private int trabajadoresFinalizados;
    private int totalTrabajadores;
    private boolean running;

    public AdminConexionAutomatica(){
    }

    @Override
    public synchronized void conexionExitosa(TareaDeConexion tarea) {
        running = false;
    }

    public synchronized void trabajoTerminado(){
        trabajadoresFinalizados++;
        if(trabajadoresFinalizados >= totalTrabajadores){
        }
    }

    @Override
    public void run(){
        try {
            filaDeTareasInferior = new ConcurrentLinkedQueue<>();
            filaDeTareasSuperior = new ConcurrentLinkedQueue<>();
            NetworkInterface nif = NetworkInterface.getByName("wlan0");
            String str = "";
            Enumeration<InetAddress> addrs = nif.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                str = str.concat(addr.toString() + "\n");
            }
            String[] arrs = str.split("/");
            String mAddr = arrs[arrs.length - 1];
            String[] slots = mAddr.split("\\.");
            String prefix = slots[0] + "." + slots[1] + "." + slots[2] + ".";
            int hostActual = Integer.parseInt(slots[3].trim());
            int tareasPendientesInferior = hostActual;
            for( int i=tareasPendientesInferior ; i>0 ; i-- ){
                filaDeTareasInferior.add(new TareaDeConexion(this,prefix+i,23543));
            }
            for( int i=hostActual+1 ; i < 255 ; i++ ){
                filaDeTareasSuperior.add(new TareaDeConexion(this,prefix+i,23543));
            }
            running = true;
            int threadCount = 5;
            totalTrabajadores = 2*threadCount;
            trabajadoresFinalizados = 0;
            TrabajadorDeConexionInferior[] trabajadoresParteInf = new TrabajadorDeConexionInferior[threadCount];
            for(int i=0; i<threadCount; i++){
                trabajadoresParteInf[i] = new TrabajadorDeConexionInferior();
                trabajadoresParteInf[i].setPriority(Thread.currentThread().getPriority() - 1 );
                trabajadoresParteInf[i].start();
            }
            TrabajadorDeConexionSuperior[] trabajadoresParteSup = new TrabajadorDeConexionSuperior[threadCount];
            for(int i=0; i<threadCount; i++){
                trabajadoresParteSup[i] = new TrabajadorDeConexionSuperior();
                trabajadoresParteSup[i].setPriority(Thread.currentThread().getPriority() - 1 );
                trabajadoresParteSup[i].start();
            }
        }catch(NullPointerException | IOException e){
            e.printStackTrace();
        }
    }

    private class TrabajadorDeConexionInferior extends Thread{

        @Override public void run(){
            while(running){
                TareaDeConexion task = filaDeTareasInferior.poll();
                if(task == null)
                    break;
                task.run();
            }
            trabajoTerminado();
        }
    }

    private class TrabajadorDeConexionSuperior extends Thread{

        @Override public void run(){
            while(running){
                TareaDeConexion tarea = filaDeTareasSuperior.poll();
                if(tarea == null)
                    break;
                tarea.run();
            }
            trabajoTerminado();
        }
    }
}
