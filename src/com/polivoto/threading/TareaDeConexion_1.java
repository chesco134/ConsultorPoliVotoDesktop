package com.polivoto.threading;

import com.polivoto.networking.IOHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.inspira.jcapiz.polivoto.pojo.ValoresEsperanzaDeTiempo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.inspira.polivoto.AccionesConsultor;
import org.inspira.polivoto.proveedores.LogProvider;

/**
 * Created by jcapiz on 11/05/16.
 */
public class TareaDeConexion_1 implements Runnable {

    private AccionesConsultor ac;
    private ValoresEsperanzaDeTiempo valoresEsperanzaDeTiempo;
    private int posicion;
    private long espera;
    private float tasaDeIncremento;

    public TareaDeConexion_1(AccionesConsultor ac, ValoresEsperanzaDeTiempo valoresEsperanzaDeTiempo, int posicion) {
        this.ac = ac;
        this.valoresEsperanzaDeTiempo = valoresEsperanzaDeTiempo;
        this.posicion = posicion;
        espera = 1000;
        tasaDeIncremento = 1.05f;
    }

    @Override
    public void run(){
        boolean success = false;
        while(!success)
        try{
            //System.out.println("Ejecutando tarea (pos "+posicion+")");
            Socket socket = new Socket(ac.getHost(), 23543);
            IOHandler ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(ac.getLID());
            JSONObject json = new JSONObject();
            json.put("action", 17);
            ioHandler.sendMessage(ac.cipherMessage(json.toString()));
            String resp = ac.decipherBytes(ioHandler.handleIncommingMessage());
            procesarMensajeLlegada(resp);
            success = true;
        }catch(JSONException | IOException e){
            e.printStackTrace();
            synchronized (this){
                try{
                    LogProvider.logMessage("TareaDeConexion", "Limite de espera: " + espera);
                    wait(espera *= tasaDeIncremento);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }   catch (InvalidKeyException ex) {
                Logger.getLogger(TareaDeConexion_1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(TareaDeConexion_1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(TareaDeConexion_1.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void procesarMensajeLlegada(String mensaje){
        long tLlegada = new java.util.Date().getTime();
        long tSalida;
        try{
            JSONObject json = new JSONObject(mensaje);
            tSalida = json.getLong("t_salida");
            valoresEsperanzaDeTiempo.agregarMillisSalida(posicion, tSalida);
            valoresEsperanzaDeTiempo.agregarMillisLlegada(posicion, tLlegada);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}