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
import com.polivoto.shared.Opcion;
import com.polivoto.shared.ResultadoPorPerfil;
import com.polivoto.shared.Votacion;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.inspira.polivoto.proveedores.LogProvider;

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
    private JSONArray votacionesDisponibles;
    private String detallesDeVotacion;
    private List<ResultadoPorPerfil> resultadosPorPerfil;
    private Votacion votacion;
    private String localHost;
    private String remoteHost;

    public int getLID() {
        return LID;
    }

    public String getHost() {
        return HOST;
    }

    public AccionesConsultor(String host, String usrName, String psswd) throws UnknownHostException,
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
        json.put("uName", usrName);
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
    }

    public synchronized byte[] cipherMessage(String mensaje) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(mensaje.getBytes());
    }

    public synchronized String decipherBytes(byte[] chunk) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String resp;
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        resp = new String(cipher.doFinal(chunk), Charset.forName("UTF-8"));
        return resp;
    }

    public String enviarPeticionDeSincronia() throws IOException {
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
            LogProvider.logMessage("enviarPeticionDeSincronia", "Esperando mensaje...");
            chunk = ioHandler.handleIncommingMessage();
            System.out.println("Recibimos: " + resp);
            ioHandler.close();
            socket.close();
        } catch (JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }

    public void consultaDetallesDeVotacion(int idVotacion) {
        try {
            socket = new Socket(HOST, 23543);
            ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 18);// La acción pertinente para llevar a cabo la solicitud.
            json.put("idVotacion", idVotacion);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            detallesDeVotacion = new String(cipher.doFinal(chunk), Charset.forName("UTF-8"));
            System.out.println("Recibimos: " + detallesDeVotacion);
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
    }

    public void consultaVotacionesDisponibles() throws IOException {
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
            votacionesDisponibles = new JSONArray(new String(cipher.doFinal(chunk), Charset.forName("UTF-8")));
            System.out.println("Recibimos: " + votacionesDisponibles);
            ioHandler.close();
            socket.close();
        } catch (JSONException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String consultaParametrosIniciales() {
        String resp = null;
        try {
            /**
             * ** Prueba consulta de boleta ***
             */
            System.out.println("HOST: " + HOST);
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
            resp = new String(cipher.doFinal(chunk), Charset.forName("UTF-8"));
            ioHandler.close();
            socket.close();
            json = new JSONObject(resp);
            votacion = new Votacion(json.getString("titulo"));
            votacion.setFechaInicio(json.getLong("tiempo_inicial"));
            votacion.setFechaFin(json.getLong("tiempo_final_final"));
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
            result = new String(cipher.doFinal(chunk), Charset.forName("UTF-8"));
            System.out.println("Comprobando existencia de " + boleta + ": " + result);
            socket.close();
            ioHandler.close();
            socket.close();
        } catch (IOException | JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public long consultaEstampaDeTiempoServidor() throws IOException {
        /**
         * ** Prueba consulta de boleta ***
         */
        socket = new Socket(HOST, 33059);
        ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
        long result = ioHandler.readLong();
        socket.close();
        ioHandler.close();
        socket.close();
        return result;
    }

    public synchronized void dummyAskForQuiz() {
        try {
            socket = new Socket(HOST, 33059);
            ioHandler
                    = new IOHandler(new DataInputStream(socket.getInputStream()),
                            new DataOutputStream(socket.getOutputStream()));
            ioHandler.writeInt(LID);
            json = new JSONObject();
            json.put("action", 6);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            chunk = cipher.doFinal(json.toString().getBytes());
            ioHandler.sendMessage(chunk);
            chunk = ioHandler.handleIncommingMessage();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            JSONArray resp = new JSONArray(new String(cipher.doFinal(chunk), Charset.forName("UTF-8")));
            System.out.println("Preguntas de votacion actual:\n" + resp);
            socket.close();
            ioHandler.close();
            socket.close();
        } catch (IOException | JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public synchronized void consultaPreguntas() throws IOException {
        if (preguntas == null) {
            try {
                /**
                 * ** Prueba consulta preguntas ***
                 */
                socket = new Socket(HOST, 23543);
                ioHandler
                        = new IOHandler(new DataInputStream(socket.getInputStream()),
                                new DataOutputStream(socket.getOutputStream()));
                ioHandler.writeInt(LID);
                json = new JSONObject();
                json.put("action", 6);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                chunk = cipher.doFinal(json.toString().getBytes());
                ioHandler.sendMessage(chunk);
                chunk = ioHandler.handleIncommingMessage();
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                String drogo = new String(cipher.doFinal(chunk), Charset.forName("UTF-8"));
                System.out.println("Drogo says: " + drogo);
                JSONArray resp = new JSONArray(drogo);
                System.out.println("Preguntas de votacion actual:\n" + resp.toString());
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
                    votacion.agregaPregunta(resp.getString(i));
                }
                ioHandler.close();
                socket.close();
            } catch (JSONException e) {
                Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, e);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Esto es una insensatez: " + preguntas.length());
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
            result = new JSONArray(new String(cipher.doFinal(chunk), Charset.forName("UTF-8")));
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

    public void armarConteoOpciones(JSONObject jsonConPreguntas) {
        try {
            /**
             * ** Prueba consulta preguntas ***
             */
            votacion.setLugar(jsonConPreguntas.getString("lugar"));
            conteoOpcionesPregunta = new JSONArray();
            resultadosPorPerfil = new ArrayList<>();
            for (int i = 0; i < jsonConPreguntas.getInt("total_preguntas"); i++) {
                conteoOpcionesPregunta.put(armarConteoOpcionesPregunta(jsonConPreguntas.getString("pregunta_" + (i + 1)), jsonConPreguntas.getJSONObject("resultados_" + (i + 1))));
            }
        } catch (JSONException e) {
            Logger.getLogger(AccionesConsultor.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public JSONObject armarConteoOpcionesPregunta(String pregunta, JSONObject json) {
        JSONObject result = null;
        System.out.println("La pregunta en cuestión es: " + pregunta);
        try {
            JSONArray jarr = json.getJSONArray("resultados_normal");
            Opcion op;
            int participantesQueRespondieronPregunta = 0;
            for (int i = 0; i < jarr.length(); i++) {
                participantesQueRespondieronPregunta += jarr.getJSONObject(i).getInt("cantidad");
                op = new Opcion(jarr.getJSONObject(i).getString("reactivo"));
                op.setCantidad(jarr.getJSONObject(i).getInt("cantidad"));
                votacion.agregarOpcion(pregunta, op);
            }
            JSONArray jPerfiles = json.getJSONArray("resultados_por_perfil");
            JSONObject jPerfil;
            JSONArray jResultadosPerfil;
            ResultadoPorPerfil rpp;
            List<Opcion> opcionesPorPerfil;
            for (int i = 0; i < jPerfiles.length(); i++) {
                jPerfil = jPerfiles.getJSONObject(i);
                rpp = new ResultadoPorPerfil();
                rpp.setPerfil(jPerfil.getString("perfil"));
                jResultadosPerfil = jPerfil.getJSONArray("resultados");
                opcionesPorPerfil = new ArrayList<>();
                for (int j = 0; j < jResultadosPerfil.length(); j++) {
                    opcionesPorPerfil.add(new Opcion(jResultadosPerfil.getJSONObject(j).getString("reactivo"), jResultadosPerfil.getJSONObject(j).getInt("cantidad")));
                }
                rpp.setOpciones(opcionesPorPerfil);
                resultadosPorPerfil.add(rpp);
                votacion.agregarResultadoPorPerfil(pregunta, rpp);
            }
            result = new JSONObject();
            result.put("participantes", participantesQueRespondieronPregunta); // Es el número total de participantes por pregunta.
            result.put("conteo", jarr); // Arreglo de conteo de votos por opción
            System.out.println("# Participación sobre \"" + pregunta + "\": " + participantesQueRespondieronPregunta);
        } catch (JSONException e) {
            e.printStackTrace();
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
            json = new JSONObject(new String(cipher.doFinal(chunk), Charset.forName("UTF-8")));
            JSONArray jarr = json.getJSONArray("resultados_normal");
            Opcion op;
            int participantesQueRespondieronPregunta = 0;
            for (int i = 0; i < jarr.length(); i++) {
                participantesQueRespondieronPregunta += jarr.getJSONObject(i).getInt("cantidad");
                op = new Opcion(jarr.getJSONObject(i).getString("reactivo"));
                op.setCantidad(jarr.getJSONObject(i).getInt("cantidad"));
                votacion.getPreguntas().get(votacion.getPreguntas().indexOf(pregunta)).agregarOpcion(op);
            }
            JSONArray jPerfiles = json.getJSONArray("resultados_por_perfil");
            JSONObject jPerfil;
            JSONArray jResultadosPerfil;
            ResultadoPorPerfil rpp;
            List<Opcion> opcionesPorPerfil;
            for (int i = 0; i < jPerfiles.length(); i++) {
                jPerfil = jPerfiles.getJSONObject(i);
                rpp = new ResultadoPorPerfil();
                rpp.setPerfil(jPerfil.getString("perfil"));
                jResultadosPerfil = jPerfil.getJSONArray("resultados");
                opcionesPorPerfil = new ArrayList<>();
                for (int j = 0; j < jResultadosPerfil.length(); j++) {
                    opcionesPorPerfil.add(new Opcion(jResultadosPerfil.getJSONObject(j).getString("reactivo"), jResultadosPerfil.getJSONObject(j).getInt("cantidad")));
                }
                rpp.setOpciones(opcionesPorPerfil);
                resultadosPorPerfil.add(rpp);
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

    public void setPreguntas(JSONArray preguntas) {
        this.preguntas = preguntas;
    }

    public JSONArray getConteoOpcionesPregunta() {
        return conteoOpcionesPregunta;
    }

    public void setConteoOpcionesPregunta(JSONArray conteoOpcionesPregunta) {
        this.conteoOpcionesPregunta = conteoOpcionesPregunta;
    }

    public JSONObject getStartupData() {
        return startupData;
    }

    public int getTotalDePreguntas() {
        return totalDePreguntas;
    }

    public JSONArray getVotacionesDisponibles() {
        return votacionesDisponibles;
    }

    public String getDetallesDeVotacion() {
        return detallesDeVotacion;
    }
    
    public Votacion getVotacion(){
        return votacion;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

}
