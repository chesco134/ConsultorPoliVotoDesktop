package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Opcion extends ModeloDeDatos {

    //Opcion(idOpcion INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Reactivo TEXT not null)
    private String reactivo;

    public Opcion() {
    }

    public Opcion(String opcion){
        super();
        reactivo = opcion;
    }

    public Opcion(int id) {
        super(id);
    }

    public String getReactivo() {
        return reactivo;
    }

    public void setReactivo(String reactivo) {
        this.reactivo = reactivo;
    }
}
