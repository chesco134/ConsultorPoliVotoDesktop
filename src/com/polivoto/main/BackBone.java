/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.main;

import com.polivoto.vistas.Acceso;

/**
 *
 * @author azaraf
 */
public class BackBone {

    private Acceso acceso;
    
    public BackBone() {
        acceso = new Acceso();
    }
    
    public void iniciarSesion(){
        acceso.setVisible(true);
    }
}
