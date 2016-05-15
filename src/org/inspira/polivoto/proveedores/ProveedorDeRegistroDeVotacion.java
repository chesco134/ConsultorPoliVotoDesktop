/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inspira.polivoto.proveedores;

import com.polivoto.networking.SoapClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.soap.SOAPException;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jcapiz
 */
public class ProveedorDeRegistroDeVotacion {
    
    public static String LOCAL_ADDR;
    
    public static int solicitudDeRegistro(Votacion votacion) throws SOAPException, IOException{
        int idVotacion = -1;
        try{// Solicitud de registro ante el servidor global.
            JSONObject json = new JSONObject();
            json.put("Title", votacion.getTitulo());
            json.put("StartDate", votacion.getFechaInicio());
            json.put("FinishDate", votacion.getFechaFin());
            json.put("action", 1);
            SoapClient handler = new SoapClient(json);
            handler.setHost(LOCAL_ADDR);
            String resp = handler.main();
            System.out.println(json.getInt("action") + " resp: " + resp);
            idVotacion = Integer.parseInt(resp);
        }catch(NumberFormatException | JSONException e){
            e.printStackTrace();
        }
        return idVotacion;
    }
    
    public static void solicitudDeIncorporacionDeHostConsultor(Votacion votacion, String consultorHost) throws SOAPException, IOException{
        try{ // Da de alta el nombre del lugar y el host consultor.
            JSONObject json = new JSONObject();
            json.put("id_votacion", votacion.getId());
            json.put("title", votacion.getTitulo());
            json.put("id_place", votacion.getIdEscuela());
            json.put("host", consultorHost);
            json.put("action", 3);
            SoapClient handler = new SoapClient(json);
            handler.setHost(LOCAL_ADDR);
            System.out.println(json.getInt("action") + " resp: " + handler.main());
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    
    public static void solicitudDeRegistroDelCuestionario(Votacion votacion) throws SOAPException, IOException{
        try{// Registro del cuestionario.
            JSONObject json = new JSONObject();
            JSONArray content = new JSONArray();
            List<String> titulosPreguntas = new ArrayList<>();
            for(Pregunta pregunta : votacion.getPreguntas())
                titulosPreguntas.add(pregunta.getEnunciado());
            String[] preguntas = titulosPreguntas.toArray(new String[0]);
            JSONArray opsPregunta;
            JSONObject row;
            for (String str : preguntas) {
                row = new JSONObject();
                opsPregunta = new JSONArray();
                for(Opcion opcion : votacion.getPreguntas().get(votacion.buscarPregunta(str)).getOpciones())
                    opsPregunta.put(opcion.getReactivo());
                row.put("pregunta", str);
                row.put("opciones", opsPregunta);
                content.put(row);
            }
            json.put("id_votacion", votacion.getId());
            json.put("title", votacion.getTitulo());
            json.put("quiz", content);
            json.put("action", 4);
            SoapClient handler = new SoapClient(json);
            handler.setHost(LOCAL_ADDR);
            System.out.println(json.getInt("action") + " resp: " + handler.main());
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
