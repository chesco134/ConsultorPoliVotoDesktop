/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.threading;

import com.polivoto.networking.IOHandler;
import com.polivoto.networking.ServicioDeIPExterna;
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
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.polivoto.proveedores.LogProvider;
import org.inspira.polivoto.proveedores.ProveedorDeMarshalling;
import org.inspira.polivoto.proveedores.ProveedorDeRecursos;
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
    private boolean useExternalHost;
    private JFrame mainFrame;

    public void setAccionesConsultor(JFrame mainFrame, AccionesConsultor accionesConsultor, String remoteHost, boolean useExternalHost) {
        this.mainFrame = mainFrame;
        this.accionesConsultor = accionesConsultor;
        this.remoteHost = remoteHost;
        this.useExternalHost = useExternalHost;
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
                if(useExternalHost)
                    remoteHost = ServicioDeIPExterna.obtenerIPServidorRemoto();
                switch (json.getInt("action")) {
                    case 1: // Local server needs to check out the status of one boleta...
                        json.put("action", 8);
                        SoapClient sc = new SoapClient(json);
                        sc.setHost(remoteHost);
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
                        String mHost = useExternalHost ? ServicioDeIPExterna.obtenerIPExterna() : Inet4Address.getLocalHost().getHostAddress();
                        if (mHost != null) {
                            try {
                                votacion.setId(ProveedorDeRegistroDeVotacion.solicitudDeRegistro(votacion));
                            } catch (SOAPException | IOException e) {
                                resp = "Error en 1";
                            }
                            try {
                                ProveedorDeRegistroDeVotacion.solicitudDeIncorporacionDeHostConsultor(votacion, mHost);
                            } catch (SOAPException | IOException e) {
                                resp = "¡Listo!".equals(resp) ? "Error en 3" : resp.concat(", 3");
                            }
                            try {
                                ProveedorDeRegistroDeVotacion.solicitudDeRegistroDelCuestionario(votacion);
                            } catch (SOAPException | IOException e) {
                                resp = "¡Listo!".equals(resp) ? "Error en 4" : resp.concat(", 4");
                            }
                            try {
                                JSONObject json1 = new JSONObject();
                                json1.put("action", 11);
                                json1.put("host", useExternalHost ? mHost : mHost.concat(":8080"));
                                json1.put("idVotacion", votacion.getId());
                                SoapClient cli = new SoapClient(json1);
                                cli.setHost(remoteHost);
                                resp = cli.main();
                            } catch (JSONException | SOAPException e) {
                                e.printStackTrace();
                            }
                            json.put("objeto_serializado", ProveedorDeMarshalling.marshallMyVotingObject(votacion));
                        } else {
                            resp = "Servicio por el momento no disponible";
                        }
                        json.put("resp", resp);
                        resp = json.toString();
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
                            System.out.println("El quiz dice: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 7" : resp.concat(", 7");
                        }
                        break;
                    case 7: // Solicitud de procesos disponibles
                        try {
                            json.put("action", 5);
                            System.out.println("Es el caso 5! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 5" : resp.concat(", 5");
                        }
                        break;
                    case 8: // Solicitud de finalizacion
                        try {
                            json.put("action", 6);
                            System.out.println("Es el caso 6! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            handler.main();
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
                            JSONArray array = accionesConsultor.getVotacionesDisponibles();
                            Map<String, Integer> elementos = new TreeMap<>();
                            Map<String, Integer> repetidos = new TreeMap<>();
                            Integer cantidad;
                            String textoAlterno;
                            boolean containsKey;
                            for (int i = 0; i < array.length(); i++) {
                                json = array.getJSONObject(i);
                                containsKey = elementos.containsKey(json.getString("titulo"));
                                if (containsKey) {
                                    cantidad = repetidos.get(json.getString("titulo"));
                                    cantidad = cantidad == null ? 1 : cantidad + 1;
                                    repetidos.put(json.getString("titulo"), cantidad);
                                    textoAlterno = json.getString("titulo") + "(" + (cantidad < 10 ? "0" + cantidad : cantidad) + ")";
                                    elementos.put(textoAlterno, json.getInt("idVotacion"));
                                } else {
                                    elementos.put(json.getString("titulo"), json.getInt("idVotacion"));
                                }
                            }
                            String tituloSeleccionado
                                    = (String) JOptionPane.showInputDialog(null, "Por favor seleccione un título de votación:",
                                            "Votaciones hechas", JOptionPane.QUESTION_MESSAGE,
                                            null, elementos.keySet().toArray(new String[]{}), elementos.keySet().iterator().next());
                            accionesConsultor.consultaDetallesDeVotacion(elementos.get(tituloSeleccionado));
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
                    case 12: // Atención a sincronía de tiempos
                        json.put("t_salida", new java.util.Date().getTime());
                        resp = json.toString();
                        break;
                    case 13: // Petición de tiempo actual
                        try {
                            json.put("action", 10);
                            System.out.println("Es el caso 10! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 10" : resp.concat(", 10");
                        }
                        break;
                    case 14: // Enviar perfiles de votación
                        try {
                            json.put("action", 2);
                            System.out.println("Es el caso 2! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            resp = resp.equals("success") ? "¡Listo!" : "Servicio por el momento no disponible";
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 2" : resp.concat(", 2");
                        }
                        break;
                    case 15: // Solicitar perfiles de votación
                        try {
                            json.put("action", 10);
                            System.out.println("Es el caso 10! json: " + json.toString());
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                            resp = resp.equals("success") ? "¡Listo!" : "Servicio por el momento no disponible";
                            System.out.println("We got: " + resp);
                        } catch (SOAPException | IOException e) {
                            resp = "¡Listo!".equals(resp) ? "Error en 10" : resp.concat(", 10");
                        }
                        break;
                    case 16: // Obtener estampa de tiempo de servidor principal
                        long estampaDeTiempo = accionesConsultor.consultaEstampaDeTiempoServidor();
                        json = new JSONObject();
                        json.put("estampa_de_tiempo", estampaDeTiempo);
                        resp = json.toString();
                        break;
                    case 17: // Server whants to know main timestamp
                        json.put("action", 12);
                        try {
                            SoapClient handler = new SoapClient(json);
                            handler.setHost(remoteHost);
                            resp = handler.main();
                        } catch (IOException | SOAPException e) {
                            resp = json.toString();
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

    private void yaVasAEmpezar() {
        final long startingMillis = new java.util.Date().getTime();
        EsperanzaDeTiempo esperanzaDeTiempo = new EsperanzaDeTiempo(1, 1, accionesConsultor, (long) 3.6e5, new EsperanzaDeTiempo.TimeToCookAction() {

            @Override
            public void coocked(Long esperanzaCalculada, String status) {
                long finishingMillis = new java.util.Date().getTime();
                if ("Ok".equals(status)) {
                    LogProvider.logMessage(getClass().getName(), "Terminamos a las: " + ProveedorDeRecursos.obtenerFecha());
                    LogProvider.logMessage(getClass().getName(), "Tiempo total: " + ProveedorDeRecursos.obtenerFormatoEnHoras(finishingMillis - startingMillis));
                    LogProvider.logMessage(getClass().getName(), "Esperanza calculada: " + esperanzaCalculada);
                    //continuar(esperanzaCalculada);
                } else {
                    final JFrame f = new JFrame();
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            f.dispose();
                        }
                    }, 2700);
                    JOptionPane.showMessageDialog(f, status);
                }
            }
        });
        LogProvider.logMessage(getClass().getName(), "Iniciamos a las: " + ProveedorDeRecursos.obtenerFecha());
        esperanzaDeTiempo.bake();
    }

    private void continuar() {
        AnalistaLocal analistaLocal = new AnalistaLocal(accionesConsultor);
        analistaLocal.setIncommingRequestHandler(this);
        analistaLocal.init();
    }
}
