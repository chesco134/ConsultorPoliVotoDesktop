/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jcapiz
 */
public class ServicioDeIPExterna {
    
    private static final String GET_EXTERNAL_HOST = "http://votacionesipn.com/remoteIP.php";
    private static final String REMOTE_HOST = "http://votacionesipn.com/services/?tag=gimmeAddr";
    
    public static String obtenerIPExterna(){
        String ip = null;
        try{
            HttpURLConnection con = (HttpURLConnection) new URL(GET_EXTERNAL_HOST).openConnection();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            ip = baos.toString();
            baos.close();
            entrada.close();
            con.disconnect();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("IP exterior: " + ip);
        return ip;
    }
    
    public static String obtenerIPServidorRemoto(){
        String ip = null;
        try{
            HttpURLConnection con = (HttpURLConnection) new URL(REMOTE_HOST).openConnection();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            JSONObject json = new JSONObject(baos.toString());
            baos.close();
            entrada.close();
            con.disconnect();
            ip = json.getString("content");
        }catch(JSONException | IOException e){
            e.printStackTrace();
        }
        System.out.println("IP servidor remoto: " + ip);
        return ip;
    }
}
