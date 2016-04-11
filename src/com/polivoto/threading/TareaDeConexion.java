package com.polivoto.threading;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by jcapiz on 13/01/16.
 */
public class TareaDeConexion {

    private final int port;
    private final String host;
    private EscuchaDeConexion escucha;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public interface EscuchaDeConexion{
        void conexionExitosa(TareaDeConexion tarea);
    }

    public TareaDeConexion(EscuchaDeConexion escucha, String host, int port){
        this.escucha = escucha;
        this.port = port;
        this.host = host;
    }

    public void run(){
        InetSocketAddress inAddr = null;
        try{
            Socket socket = new Socket();
            inAddr = new InetSocketAddress(host,port);
            socket.connect(inAddr, 3000);
            escucha.conexionExitosa(this);
            socket.close();
        }catch(IOException e){
        }
    }
}