/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.logica;

import com.polivoto.threading.IncommingRequestHandler;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.inspira.polivoto.AccionesConsultor;
import org.json.JSONException;

/**
 *
 * @author azaraf
 */
public class ConstruirDatos {
    public static final String SUCCESSFUL = "success";
    public static final String BAD_PWD = "badp";
    public static final String BAD_USR = "badu";
    public static final String BAD_CONNECTION = "badc";
    public static final String FAIL = "fail";
    
    private static AccionesConsultor accionesConsultor;
    
    private ConstruirDatos() {
        
    }
    
    public static String loggear(String host, String pwd){
        String res = FAIL;
        try {
            accionesConsultor = new AccionesConsultor(host, pwd);
            accionesConsultor.consultaPreguntas();
            new IncommingRequestHandler().start();
            res = SUCCESSFUL;
        } catch (IOException ex) {
            res = BAD_CONNECTION;
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            res = "NoSuchAlgorithmException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            res = "InvalidKeySpecException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            res = "NoSuchPaddingException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            res = "InvalidKeyException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            res = "IllegalBlockSizeException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            res = "BadPaddingException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            res = "JSONException";
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static AccionesConsultor getAccionesConsultor() {
        return accionesConsultor;
    }
    
    public static String getNombreStartUp(){
        String startup = FAIL;
        try {
            startup = accionesConsultor.getStartupData().getString("lugar");
        } catch (JSONException ex) {
            Logger.getLogger(ConstruirDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return startup;
    }
}
