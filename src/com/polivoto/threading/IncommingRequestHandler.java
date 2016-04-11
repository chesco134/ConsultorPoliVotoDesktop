/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.threading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPException;
import org.inspira.polivoto.AccionesConsultor;
import org.json.JSONException;
import org.json.JSONObject;
import com.polivoto.networking.SoapClient;

/**
 *
 * @author jcapiz
 */
public class IncommingRequestHandler extends Thread{
    
    private AccionesConsultor accionesConsultor;
    private ServerSocket server;

    public void setAccionesConsultor(AccionesConsultor accionesConsultor) {
        this.accionesConsultor = accionesConsultor;
    }
    
    @Override
    public void run(){
        try{
            server = new ServerSocket(5010);
            while(true){
                Socket socket = server.accept();
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                byte[] chunk = new byte[entrada.read()];
                entrada.read(chunk);
                JSONObject json = new JSONObject(new String(chunk));
                String resp = "";
                switch(json.getInt("action")){
                    case 1: // Local server needs to check out the status of one boleta...
                        json.put("action",8);
                        SoapClient sc = new SoapClient(json);
                        sc.setHost("192.168.1.72");
                        resp = sc.main();
                        break;
                    case 2: // Remote server needs to check out the status of one boleta locally...
                        resp = accionesConsultor.consultaBoletaRemota(json.getString("boleta"));
                        break;
                    default:
                }
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                salida.write(resp.getBytes().length);
                salida.write(resp.getBytes());
                socket.close();
            }
        }catch(JSONException | IOException e){
            e.printStackTrace();
        } catch (SOAPException ex) {
            Logger.getLogger(IncommingRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void terminarConexion(){
        try{
            server.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}