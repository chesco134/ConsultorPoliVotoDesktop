package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Escuela extends ModeloDeDatos {

    //idEscuela INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Nombre text not null, latitud real, longitud real
    private String nombre;
    private double latitud;
    private double longitud;

    public Escuela() {
    }

    public Escuela(int id) {
        super(id);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
