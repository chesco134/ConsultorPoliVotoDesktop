/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.threading;

import com.polivoto.logica.Cronometro;
import com.polivoto.networking.IOHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

/**
 *
 * @author jcapiz
 */
public class ServicioDeSincronizacionDeReloj extends TimerTask {
    
    private String host;
    private Cronometro cronometro;

    public ServicioDeSincronizacionDeReloj(String host, Cronometro cronometro) {
        this.host = host;
        this.cronometro = cronometro;
    }
    
    @Override
    public void run(){
        try{
            Socket socket = new Socket(host, 33059);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            IOHandler ioHandler = new IOHandler(entrada, salida);
            long currentTime = ioHandler.readLong();
            //cronometro.setTiempoRestante(currentTime);
            if(currentTime <= 0) super.cancel();
            System.out.println("Reajuste de tiempo: " + currentTime);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
