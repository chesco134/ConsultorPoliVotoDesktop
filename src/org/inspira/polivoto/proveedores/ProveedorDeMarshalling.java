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
            json.put("idVotacion", votacion.getId());
            json.put("titulo", votacion.getTitulo());
            json.put("lugar", votacion.getLugar());
            json.put("fecha_inicial", votacion.getFechaInicio());
            json.put("fecha_final", votacion.getFechaFin());
            json.put("es_global", votacion.isGlobal() ? 1 : 0);
            json.put("id_place", votacion.getIdEscuela());
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
            json.put("preguntas", jpreguntas);
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
            votacion.setId(json.getInt("idVotacion"));
            votacion.setTitulo(json.getString("titulo"));
            votacion.setLugar(json.getString("lugar"));
            votacion.setFechaInicio(json.getLong("fecha_inicial"));
            votacion.setFechaFin(json.getLong("fecha_final"));
            votacion.setGlobal(json.getInt("es_global") != 0);
            JSONArray jpreguntas = json.getJSONArray("preguntas");
            votacion.setIdEscuela(json.getInt("id_place"));
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
