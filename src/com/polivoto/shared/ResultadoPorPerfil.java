/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.shared;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jcapiz
 */
public class ResultadoPorPerfil implements Serializable {
    
    private String perfil;
    private List<Opcion> opciones;

    public ResultadoPorPerfil(String perfil) {
        this.perfil = perfil;
    }

    public ResultadoPorPerfil() {
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public List<Opcion> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }
}
