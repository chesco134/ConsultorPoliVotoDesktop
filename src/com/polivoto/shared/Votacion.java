package com.polivoto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 1/01/16.
 */
public class Votacion implements Serializable {

    private String titulo;
    private String lugar;
    private long fechaInicio;
    private long fechaFin;
    private List<Pregunta> preguntas;

    public Votacion(String titulo){
        this.titulo = titulo;
        preguntas = new ArrayList<>();
    }

    public void agregaPregunta(Pregunta pregunta){
        int posicion = buscaPregunta(pregunta.getTitulo());
        if(posicion == -1){
            preguntas.add(pregunta);
        }
    }

    public void agregaPregunta(String pregunta){
        int posicion = buscaPregunta(pregunta);
        if(posicion == -1){
            preguntas.add(new Pregunta(pregunta));
        }
    }
    
    public void agregarOpcion(String pregunta, Opcion opcion){
        int posicion = buscaPregunta(pregunta);
        if(posicion > -1){
            preguntas.get(posicion).agregarOpcion(opcion);
        }
    }
    
    public void agregarResultadoPorPerfil(String pregunta, ResultadoPorPerfil resultadoPorPerfil){
        int posicion = buscaPregunta(pregunta);
        if(posicion > -1)
            preguntas.get(posicion).agregarResultadoPorPerfil(resultadoPorPerfil);
    }

    public String getTitulo(){
        return titulo;
    }

    public List<Pregunta> getPreguntas(){
        return preguntas;
    }

    public int buscaPregunta(String pregunta){
        int posicion = -1;
        for(int i=0; i<preguntas.size(); i++)
            if(preguntas.get(i).getTitulo().equals(pregunta)){
                posicion = i;
                break;
            }
        return posicion;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}
