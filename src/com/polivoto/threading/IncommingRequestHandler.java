/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.threading;

import com.polivoto.networking.IOHandler;
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
import com.polivoto.vistas.AnalistaLocal;
import com.polivoto.vistas.Consultor;
import java.net.Inet4Address;
import javax.swing.JOptionPane;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.polivoto.proveedores.ProveedorDeMarshalling;
import org.inspira.polivoto.proveedores.ProveedorDeRegistroDeVotacion;
import org.json.JSONArray;

/**
 *
 * @author jcapiz
 */
public class IncommingRequestHandler extends Thread {

    private AccionesConsultor accionesConsultor;
    private ServerSocket server;
    private String remoteHost;

    public void setAccionesConsultor(AccionesConsultor accionesConsultor, String remoteHost) {
        this.accionesConsultor = accionesConsultor;
        this.remoteHost = remoteHost;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(5010);
            while (true) {
                Socket socket = server.accept();
                new AccionRequerida(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminarConexion() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AccionRequerida extends Thread {

        private Socket socket;

        public AccionRequerida(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                IOHandler ioHandler = new IOHandler(entrada, salida);
                byte[] chunk = ioHandler.handleIncommingMessage();
                String smthg = new String(chunk);
                System.out.println("Llegó: " + smthg + ". From " + socket.getRemoteSocketAddress());
                JSONObject json = new JSONObject(smthg);
                String resp;
                resp = "¡Listo!";
                switch (json.getInt("action")) {
                    case 1: // Local server needs to check out the status of one boleta...
                        json.put("action", 8);
                        SoapClient sc = new SoapClient(json);
                        sc.setHost("192.168.1.78");
                        resp = sc.main();
                        break;
                    case 2: // Remote server needs to check out the status of one boleta locally...
                        resp = accionesConsultor.consultaBoletaRemota(json.getString("boleta"));
                        break;
                    case 3: // Local server wants to go global.
                        JSONObject jvotacion = json.getJSONObject("objeto_serializado");
                        Votacion votacion = ProveedorDeMarshalling.unmarshallMyVotingObject(jvotacion.toString());
                        System.out.println("Inet4Addr " + Inet4Address.getLocalHost().getHostAddress());
                        ProveedorDeRegistroDeVotacion.LOCAL_ADDR = remoteHost;
                        try {
                            ProveedorDeRegistroDeVotacion.solicitudDeRegistro(votacion);
                        } catch (SOAPException | IOException e) {
                            resp = "Error en 1";
                        }
                        try {
                            ProveedorDeRegistroDeVotacion.solicitudDeIncorporacionAlProceso(votacion);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 2" : resp.concat(", 2");
                        }
                        try {
                            ProveedorDeRegistroDeVotacion.solicitudDeIncorporacionDeHostConsultor(votacion, Inet4Address.getLocalHost().getHostAddress());
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 3" : resp.concat(", 3");
                        }
                        try {
                            ProveedorDeRegistroDeVotacion.solicitudDeRegistroDelCuestionario(votacion);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 4" : resp.concat(", 4");
                        }
                        break;
                    case 4: // Solicitud de registro de zona
                        try {
                            json.put("action", 2);
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.main();
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 2" : resp.concat(", 2");
                        }
                        break;
                    case 5: // Solicitud de registro sólo de incorporación de Host consultor
                        try {
                            json.put("action", 3);
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.main();
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 3" : resp.concat(", 3");
                        }
                        break;
                    case 6: // Solicitud de cuestionario
                        try {
                            json.put("action", 7);
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 7" : resp.concat(", 7");
                        }
                        break;
                    case 7: // Solicitud de cuestionario
                        try {
                            json.put("action", 5);
                            System.out.println("Es el caso 7! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 6" : resp.concat(", 6");
                        }
                        break;
                    case 8: // Solicitud de finalizacion
                        try {
                            json.put("action", 6);
                            System.out.println("Es el caso 6! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 6" : resp.concat(", 6");
                        }
                        break;
                    case 9:
                        ioHandler.sendMessage("¡Listo!".getBytes());
                        socket.close();
                        try {
                            accionesConsultor.consultaVotacionesDisponibles();
                            json = new JSONObject(accionesConsultor.getVotacionesDisponibles());
                            JSONArray array = json.getJSONArray("titulos");
                            String[] titulos = new String[array.length()];
                            for (int i = 0; i < titulos.length; i++) {
                                titulos[i] = array.getString(i);
                            }
                            String tituloSeleccionado
                                    = (String) JOptionPane.showInputDialog(null, "Por favor seleccione un título de votación:",
                                            "Votaciones hechas", JOptionPane.QUESTION_MESSAGE,
                                            null, titulos, titulos[0]);
                            accionesConsultor.consultaDetallesDeVotacion(tituloSeleccionado);
                            String detallesVotacion = accionesConsultor.getDetallesDeVotacion();
                            json = new JSONObject(detallesVotacion);
                            JSONArray jpreguntas = json.getJSONArray("preguntas");
                            accionesConsultor.setPreguntas(jpreguntas);
                            JSONArray extra = new JSONArray();
                            JSONObject result;
                            for (int j = 0; j < jpreguntas.length(); j++) {
                                int participantesQueRespondieronPregunta = 0;
                                for (int i = 0; i < jpreguntas.getJSONObject(j).getJSONArray("opciones").length(); i++) {
                                    participantesQueRespondieronPregunta += jpreguntas.getJSONObject(j).getJSONArray("opciones").getJSONObject(i).getInt("cantidad");
                                }
                                result = new JSONObject();
                                result.put("participantes", participantesQueRespondieronPregunta); // Es el número total de participantes por pregunta.
                                result.put("conteo", jpreguntas.getJSONObject(j).getJSONArray("opciones")); // Arreglo de conteo de votos por opción
                                extra.put(result);
                            }
                            accionesConsultor.setConteoOpcionesPregunta(extra);
                            for (int i = 0; i < jpreguntas.length(); i++) {
                                Consultor consultor = new Consultor(i, accionesConsultor, "Sco");
                                consultor.iniciar();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        ioHandler.sendMessage("¡Listo!".getBytes());
                        socket.close();
                        continuar();
                        break;
                    case 11: // Carga de secretos
                        try {
                            json.put("action", 9);
                            System.out.println("Es el caso 9! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.main();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 9" : resp.concat(", 9");
                        }
                        break;
                    default:
                }
                ioHandler.sendMessage(resp.getBytes());
                socket.close();
            } catch (SOAPException | IOException | JSONException e) {
                Logger.getLogger(IncommingRequestHandler.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void continuar() {
        AnalistaLocal analistaLocal = new AnalistaLocal(accionesConsultor);
        analistaLocal.setIncommingRequestHandler(this);
        analistaLocal.init();
    }
}
