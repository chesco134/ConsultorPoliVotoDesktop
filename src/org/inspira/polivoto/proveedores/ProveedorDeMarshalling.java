package org.inspira.polivoto.proveedores;

import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ProveedorDeMarshalling {

    public static String marshallMyVotingObject(Votacion votacion){
        String serializedString = null;
        try{
            JSONObject json = new JSONObject();
            json.put("titulo", votacion.getTitulo());
            json.put("lugar", votacion.getLugar());
            json.put("fecha_inicial", votacion.getFechaInicio());
            json.put("fecha_final", votacion.getFechaFin());
            JSONObject jpregunta;
            JSONArray jpreguntas = new JSONArray();
            JSONArray jopciones;
            JSONObject jopcion;
            for(Pregunta pregunta : votacion.getPreguntas()){
                jpregunta = new JSONObject();
                jpregunta.put("enunciado", pregunta.getEnunciado());
                jopciones = new JSONArray();
                for(Opcion opcion : pregunta.getOpciones()){
                    jopcion = new JSONObject();
                    jopcion.put("enunciado", opcion.getReactivo());
                    jopciones.put(jopcion);
                }
                jpregunta.put("opciones", jopciones);
                jpreguntas.put(jpregunta);
            }
            serializedString = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return serializedString;
    }

    public static Votacion unmarshallMyVotingObject(String serializedObject){
        Votacion votacion = null;
        try{
            JSONObject json = new JSONObject(serializedObject);
            votacion = new Votacion();
            votacion.setTitulo(json.getString("titulo"));
            votacion.setLugar(json.getString("lugar"));
            votacion.setFechaInicio(json.getLong("fecha_inicial"));
            votacion.setFechaFin(json.getLong("fecha_final"));
            JSONArray jpreguntas = json.getJSONArray("preguntas");
            JSONObject jpregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            for(int i=0; i < jpreguntas.length(); i++){
                jpregunta = jpreguntas.getJSONObject(i);
                votacion.agregarPregunta(jpregunta.getString("enunciado"));
                jopciones = jpregunta.getJSONArray("opciones");
                for(int j=0; j < jopciones.length(); j++){
                    jopcion = jopciones.getJSONObject(j);
                    votacion.agregarOpcion(i, jopcion.getString("enunciado"));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return votacion;
    }
}
