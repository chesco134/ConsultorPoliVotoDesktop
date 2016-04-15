package org.inspira.polivoto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.inspira.devox.security.MD5Hash;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.polivoto.networking.IOHandler;

public class AccionesConsultor {

    private final String HOST;
    private Socket socket;
    private IOHandler ioHandler; // Crea nueva instancia por cada conexión hecha.
    private JSONObject json;
    private Cipher cipher;
    private SecretKey secretKey;
    private final int LID;
    private byte[] chunk;
    private String tituloVotacion;
    private JSONArray preguntas;
    private JSONArray conteoOpcionesPregunta;
    private JSONObject startupData;
    private int totalDePreguntas;

    public int getLID() {
        return LID;
    }

    public AccionesConsultor(String host, String psswd) throws UnknownHostException,
            IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, JSONException {
        socket = new Socket(host, 23543);
        HOST = host;
        ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
        ioHandler.writeInt(-1);
        System.out.println("We're waiting for the public key...");
        // SecureKey tradeoff...
        // First you ask for the pub key.
        secretKey = null;
        cipher = null;
        chunk = ioHandler.handleIncommingMessage();
        System.out.println("We got " + chunk.length + " bytes.");
        X509EncodedKeySpec bobyPubKeySpec = new X509EncodedKeySpec(chunk);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey usrPubKey = keyFactory.generatePublic(bobyPubKeySpec);
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, usrPubKey);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // for example
        secretKey = keyGen.generateKey();

        byte[] cipB = cipher.doFinal(secretKey.getEncoded());
        System.out.println("Sending secret");
        ioHandler.sendMessage(cipB);
        System.out.println("Done sending secret");
        json = new JSONObject();
        json.put("uName", "Consultor");
        json.put("psswd", new MD5Hash().makeHash(psswd));
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        chunk = cipher.doFinal(json.toString().getBytes());
        ioHandler.sendMessage(chunk);
        LID = ioHandler.readInt();
        ioHandler.close();
        socket.close();
        System.out.println("LID: " + LID);
        System.out.println("Done ~~~");
        /*
         if(LID != -1){
         socket = new Socket(HOST, 23543);
         ioHandler = new IOHandler(new DataInputStream(socket.getInputStream())
         , new DataOutputStream(socket.getOutputStream()));
         ioHandler.writeInt(LID);
         json = new JSONObject();
         json.put("action", 15);
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);
         chunk = cipher.doFinal(json.toString().getBytes());
         ioHandler.sendMessage(chunk);
         chunk = ioHandler.handleIncommingMessage();
         cipher.init(Cipher.DECRYPT_MODE, secretKey);
         String scorchingFire = new String(cipher.doFinal(chunk));
         System.out.println("Startup Data: " + scorchingFire);
         startupData = new JSONObject(scorchingFire);
         ioHandler.close();
         socket.close();
         }
         */
        JSONObject jvotaciones = new JSONObject(consultaVotacionesDisponibles());
        consultaDetallesDeVotacion(jvotaciones.getJSONArray("titulos").getString(0));
    }
    
    public String consultaDetallesDeVotacion(String titulo){
        String resp = null;
        try {
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 18);// La acción pertinente para llevar a cabo la solicitud.
            json.put("titulo", titulo);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            resp = new String(cipher.doFinal(chunk));
            System.out.println("Recibimos: " + resp);
            ioHandler.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }

    public String consultaVotacionesDisponibles() {
        String resp = null;
        try {
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 17);// La acción pertinente para llevar a cabo la solicitud.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            resp = new String(cipher.doFinal(chunk));
            System.out.println("Recibimos: " + resp);
            ioHandler.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }

    public String consultaParametrosIniciales() {
        String resp = null;
        try {
            /**
             * ** Prueba consulta de boleta ***
             */
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 15);// La acción pertinente para llevar a cabo la solicitud.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            resp = new String(cipher.doFinal(chunk));
            ioHandler.close();
            socket.close();
        } catch (IOException | JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return resp;
    }

    public String consultaBoletaRemota(String boleta) {
        String result = null;
        try {
            /**
             * ** Prueba consulta de boleta ***
             */
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 14);
            json.put("boleta", boleta);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            int veredicto = Integer.parseInt(new String(cipher.doFinal(chunk)));
            System.out.println("Comprobando existencia de " + boleta + ": " + veredicto);
            socket.close();
            JSONObject json = new JSONObject();
            json.put("response", veredicto);
            result = json.toString();
            ioHandler.close();
            socket.close();
        } catch (IOException | JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized void consultaPreguntas() {
        if (preguntas == null) {
            try {
                /**
                 * ** Prueba consulta preguntas ***
                 */
                socket = new Socket(HOST, 23543);
                ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
                ioHandler.writeInt(LID);
                json = new JSONObject();
                json.put("action", 6);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                chunk = cipher.doFinal(json.toString().getBytes());
                ioHandler.sendMessage(chunk);
                chunk = ioHandler.handleIncommingMessage();
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                JSONArray resp = new JSONArray(new String(cipher.doFinal(chunk)));
                System.out.println("Preguntas de votacion actual:\n" + resp);
                socket.close();
                preguntas = new JSONArray();
                JSONObject row;
                for (int i = 0; i < resp.length(); i++) {
                    row = new JSONObject();
                    row.put("pregunta", resp.getString(i));
                    row.put("opciones", consultaOpcionesPregunta(resp.getString(i)));
                    preguntas.put(row);
                    System.out.println("FIERRO PARIENTE: " + row.toString());
                    totalDePreguntas = i;
                }
                ioHandler.close();
                socket.close();
            } catch (JSONException e) {
                Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Esto es una tontería: " + preguntas.length());
        }
    }

    private JSONArray consultaOpcionesPregunta(String pregunta) {
        JSONArray result = null;
        try {
            /**
             * ** Prueba consulta opciones de pregunta ***
             */
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 13);
            json.put("pregunta", pregunta);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            result = new JSONArray(new String(cipher.doFinal(chunk)));
            System.out.println("Opciones de \"" + pregunta + "\": " + result.toString());
            ioHandler.close();
            socket.close();
        } catch (JSONException e) {

        } catch (IOException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public synchronized void consultaConteoOpciones() {

        try {
            /**
             * ** Prueba consulta preguntas ***
             */
            System.out.println("We are not gonna take it!!");
            conteoOpcionesPregunta = new JSONArray();
            for (int i = 0; i < preguntas.length(); i++) {
                conteoOpcionesPregunta.put(consultaConteoOpcionesPregunta(preguntas.getJSONObject(i).getString("pregunta")));
            }
        } catch (JSONException e) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private JSONObject consultaConteoOpcionesPregunta(String pregunta) {
        JSONObject result = null;
        try {
            /**
             * ** Prueba consulta opciones de pregunta ***
             */
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 4);
            json.put("pregunta", pregunta);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            JSONArray jarr = new JSONArray(new String(cipher.doFinal(chunk)));
            int participantesQueRespondieronPregunta = 0;
            for (int i = 0; i < jarr.length(); i++) {
                participantesQueRespondieronPregunta += jarr.getJSONObject(i).getInt("cantidad");
            }
            result = new JSONObject();
            result.put("participantes", participantesQueRespondieronPregunta); // Es el número total de participantes por pregunta.
            result.put("conteo", jarr); // Arreglo de conteo de votos por opción
            System.out.println("# Participación sobre \"" + pregunta + "\": " + participantesQueRespondieronPregunta);
            ioHandler.close();
            socket.close();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public JSONObject getJson() {
        return json;
    }

    public String getTituloVotacion() {
        return tituloVotacion;
    }

    public JSONArray getPreguntas() {
        return preguntas;
    }

    public JSONArray getConteoOpcionesPregunta() {
        return conteoOpcionesPregunta;
    }

    public JSONObject getStartupData() {
        return startupData;
    }

    public int getTotalDePreguntas() {
        return totalDePreguntas;
    }

}
