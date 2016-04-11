/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.logica;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Alfonso 7
 */
public class RecibirVotos {

    private boolean recibiendo = true;
    private boolean nuevo = false;
    private int votos = 0;
    private double poblacion = 0;
    private double porcentaje;
    private DecimalFormat decimales;
    private DefaultPieDataset data;
    private JFreeChart chart;
    private PiePlot plot;
    private ChartPanel chartPanel;
    
    private JLabel lblvotos_totales;
    private JLabel lblporcentaje;
    private JPanel pnlgrafica;
    JPanel pnl_consulta;
    JPanel pnl_espera;

    public boolean isRecibiendo() {
        return recibiendo;
    }

    public void setRecibiendo(boolean recibiendo) {
        this.recibiendo = recibiendo;
    }
    
    public void iniciarEscucha( int votos, double poblacion, JLabel lblvotos_totales, JLabel lblporcentaje, JPanel pnlgrafica, JPanel consulta, JPanel espera ) {
        recibiendo = true;
        this.votos = votos;
        this.lblvotos_totales = lblvotos_totales;
        this.lblporcentaje = lblporcentaje;
        this.pnlgrafica = pnlgrafica;  
        this.poblacion = poblacion;
        this.pnl_consulta = consulta;
        this.pnl_espera = espera;
        actualizarConteo(false);
    }
    
    public void actualizarConteo(boolean conIncremento){
        /*
        * Porcentaje
        */
        if( conIncremento )
            if( votos == poblacion){
                votos++;
                poblacion++;
            }else{
                votos++;
            }
        lblvotos_totales.setText(""+votos);
        porcentaje = (votos*100)/(poblacion == 0 ? 1 : poblacion);
        if(porcentaje >= 100.0 || (porcentaje%1) == 0)
            decimales = new DecimalFormat("0");
        else
            decimales = new DecimalFormat("0.00");
        lblporcentaje.setText(""+decimales.format(porcentaje)+"%");
        
        // Fuente de Datos
        data = new DefaultPieDataset();
        data.setValue("SI", votos);
        data.setValue("NO", poblacion-votos);
        // Creando el Grafico
        chart = ChartFactory.createPieChart(
         "", 
         data,false,false,false);
        chart.setBackgroundPaint(Color.white);
        
        plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setLabelGenerator(null);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("SI", new Color(0,204,0));
        plot.setSectionPaint("NO", new Color(218,24,24));
        
        // Crear el Panel del Grafico con ChartPanel
        chartPanel = new ChartPanel(chart,161,131,161,131,161,131,false,false,false,false,false,false);
        chartPanel.setEnabled(false);
        pnlgrafica.setLayout(new java.awt.BorderLayout());
        pnlgrafica.removeAll();
        pnlgrafica.add(chartPanel,BorderLayout.CENTER);
        pnlgrafica.validate();
    }
}