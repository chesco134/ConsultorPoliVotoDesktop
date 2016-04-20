package com.polivoto.shared;

import java.io.Serializable;
import java.util.LinkedList;

public class Pregunta implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String titulo;
	private LinkedList<Opcion> opciones;

	public Pregunta(String titulo) {
		this.titulo = titulo;
		opciones = new LinkedList<>();
	}

	public Opcion obtenerOpcion(int posicion){
		return opciones.get(posicion);
	}

    public void quitarOpcion(String opcion){
        int posicion = buscarOpcion(opcion);
        if( posicion > -1 )
            opciones.remove(posicion);
    }

	public void agregarOpcion(String opcion){
		int posicion = buscarOpcion(opcion);
		if( posicion == -1){
            opciones.add(new Opcion(opcion));
        }
	}

	public void agregarOpcion(Opcion opcion){
		int posicion = buscarOpcion(opcion.getNombre());
		if( posicion == -1){
			opciones.add(opcion);
		}
	}

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int obtenerCantidadDeOpciones(){
        return opciones.size();
    }

    private int buscarOpcion(String opcion){
		int posicion = -1;
		for(int i=0; i<opciones.size(); i++)
			if( opciones.get(i).getNombre().equals(opcion) ) {
				posicion = i;
				break;
			}
		return posicion;
	}

    public String getTitulo() {
        return titulo;
    }
}
