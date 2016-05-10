/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inspira.polivoto.proveedores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author jcapiz
 */
public class ProveedorDeArchivo {
    
    private static final String FILE_NAME = "hosti.txt";
    
    public static String leerHost(){
        String host = null;
        try{
            BufferedReader bf = new BufferedReader(new FileReader(new File(FILE_NAME)));
            host = bf.readLine();
            bf.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return host;
    }
    
    public static void escribirHost(String host){
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(new File(FILE_NAME)));
            pw.println(host);
            pw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
