/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.shared;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author jcapiz
 */
public class ResultadoPorPerfil implements Serializable {
    
    private String perfil;
    private Map<String, Integer> resultados;

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Map<String, Integer> getResultados() {
        return resultados;
    }

    public void setResultados(Map<String, Integer> resultados) {
        this.resultados = resultados;
    }
}
