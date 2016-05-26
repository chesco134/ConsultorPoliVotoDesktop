package org.inspira.polivoto.proveedores;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.polivoto.shared.Opcion;
import com.polivoto.shared.ResultadoPorPerfil;
import com.polivoto.shared.Votacion;

public class MarshallMySharedVotingObject {

    public static Votacion unmarshall(String jsonstr) {
        Votacion votacion = null;
        try {
            JSONObject json = new JSONObject(jsonstr);
            votacion = new Votacion(json.getString("titulo"));
            votacion.setFechaFin(json.getLong("fecha_fin"));
            votacion.setFechaInicio(json.getLong("fecha_inicio"));
            votacion.setLugar(json.getString("lugar"));
            JSONArray jpreguntas = json.getJSONArray("preguntas");
            JSONObject jpregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            JSONArray jresultadosPorPerfil;
            JSONObject jresultadoPorPerfil;
            ResultadoPorPerfil rpp;
            List<Opcion> opciones;
            for (int i = 0; i < jpreguntas.length(); i++) {
                jpregunta = jpreguntas.getJSONObject(i);
                votacion.agregaPregunta(jpregunta.getString("enunciado"));
                jopciones = jpregunta.getJSONArray("opciones");
                for (int j = 0; j < jopciones.length(); j++) {
                    jopcion = jopciones.getJSONObject(j);
                    votacion.agregarOpcion(jpregunta.getString("enunciado"), new Opcion(jopcion.getString("enunciado"), jopcion.getInt("cantidad")));
                }
                jresultadosPorPerfil = jpregunta.getJSONArray("resultados_por_perfil");
                for (int j = 0; j < jresultadosPorPerfil.length(); j++) {
                    jresultadoPorPerfil = jresultadosPorPerfil.getJSONObject(j);
                    rpp = new ResultadoPorPerfil(jresultadoPorPerfil.getString("perfil"));
                    jopciones = jresultadoPorPerfil.getJSONArray("opciones");
                    opciones = new ArrayList<>();
                    for (int k = 0; k < jopciones.length(); k++) {
                        jopcion = jopciones.getJSONObject(k);
                        opciones.add(new Opcion(jopcion.getString("enunciado"), jopcion.getInt("cantidad")));
                    }
                    rpp.setOpciones(opciones);
                    votacion.agregarResultadoPorPerfil(jpregunta.getString("enunciado"), rpp);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return votacion;
    }
}
