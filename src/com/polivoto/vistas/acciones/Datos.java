/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas.acciones;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.List;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.inspira.polivoto.AccionesConsultor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.TextAnchor;
import org.jfree.util.Rotation;
import org.json.JSONException;

/**
 *
 * @author azaraf
 */
public class Datos {

    private AccionesConsultor ac;
    private DefaultPieDataset pieDataset;
    private int pox;
    private boolean rotating;
    private LinkedList<Color> colores = new LinkedList<>();
    

    public Datos(AccionesConsultor ac) {
        this.ac = ac;
        ac.consultaConteoOpciones();
        setcolors();
    }

    public Datos(AccionesConsultor ac, int pox) {
        this.ac = ac;
        this.pox = pox;
        ac.consultaConteoOpciones();
        this.rotating = false;
        setcolors();
    }

    public Datos(AccionesConsultor ac, int pox, boolean rotating) {
        this.ac = ac;
        this.pox = pox;
        ac.consultaConteoOpciones();
        this.rotating = rotating;
        setcolors();
    }
    

    private PieDataset crearDatasetPie() {
        DefaultPieDataset datos = new DefaultPieDataset();
        try {
            if (ac.getConteoOpcionesPregunta().getJSONObject(pox).getInt("participantes") == 0) {
                datos.setValue("No hay ningún voto", 0);
            }else{
                for (int i = 0; i < ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").length(); i++){
                    int p = ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getInt("cantidad");
                    if (ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo").equals("Anular mi voto")){
                        datos.setValue("Nulo", p);
                    }else{
                        datos.setValue(ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo"), p);
                    }
                }
            }
        }catch(JSONException ex){
            ex.printStackTrace();
        }
        return datos;
    }

    private JFreeChart crearChartPie(PieDataset dataset, String titulo) {
        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, false, true, false);
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.ANTICLOCKWISE);
        
        plot.setNoDataMessage("No hay votos");
        try{
            if(ac.getConteoOpcionesPregunta().getJSONObject(pox).getInt("participantes") != 0){
                int j = 0;
                for (int i = 0; i < ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").length(); i++){
                    try{
                        if (ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo").equals("Anular mi voto")){
                            plot.setSectionPaint("Nulo", Color.lightGray);
                        }else{
                            plot.setSectionPaint(ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo"), colores.get(j));
                            j++;
                        }
                    }catch(JSONException ex){
                        ex.printStackTrace();
                    }
                }
            }
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        plot.setSimpleLabels(true);
        plot.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.white));
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator("{0}: {2} de votos", new DecimalFormat("0"), new DecimalFormat("0.000%"));
        plot.setLabelGenerator(gen);
        if (rotating){
            final Rotator rotate = new Rotator(plot);
            rotate.start();
        }
        return chart;
    }
    
    public void setPieChartIn(JPanel panel){
        PieDataset dataset = crearDatasetPie();
        JFreeChart chart = null;
        try{
            chart = crearChartPie(dataset, ac.getPreguntas().getJSONObject(pox).getString("pregunta"));
        }catch(JSONException ex){
            ex.printStackTrace();
        }
        ChartPanel pie = new ChartPanel(chart);
        pie.setBounds(panel.getVisibleRect());
        panel.removeAll();
        panel.add(pie);
        panel.repaint();
    }
    
    public void setBarChartIn(JPanel panel){
        CategoryDataset dataset = crearDatasetBar();
        JFreeChart chart = createChartBar(dataset);
        ChartPanel barChart = new ChartPanel(chart);
        barChart.setBounds(panel.getVisibleRect());
        panel.removeAll();
        panel.add(barChart);
        panel.repaint();
    }

    private CategoryDataset crearDatasetBar() {
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        try {
            String sector = ac.getPreguntas().getJSONObject(pox).getString("pregunta");
        
            if (ac.getConteoOpcionesPregunta().getJSONObject(pox).getInt("participantes") == 0) {
                datos.addValue(0, sector ,"No hay ningún voto");
            }else{
                for (int i = 0; i < ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").length(); i++){
                    int p = ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getInt("cantidad");
                    if (ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo").equals("Anular mi voto")){
                        datos.addValue(p, sector, "Nulo");
                    }else{
                        datos.addValue(p, sector, ac.getConteoOpcionesPregunta().getJSONObject(pox).getJSONArray("conteo").getJSONObject(i).getString("reactivo"));
                    }
                }
            }
        }catch(JSONException ex){
            ex.printStackTrace();
        }
        return datos;
    }

    private JFreeChart createChartBar(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                    "Votacion",
                    "Opciones",
                    "Total de votos",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    true
            );
        try {
            
            chart.addSubtitle(new TextTitle(ac.getPreguntas().getJSONObject(pox).getString("pregunta")));
            chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.white));
            CategoryPlot plot = chart.getCategoryPlot();
            org.jfree.chart.axis.CategoryAxis axis = plot.getDomainAxis();
            axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.39269908169872414D));
//            final CategoryItemRenderer itemRenderer = new CustomRenderer(
//                    new Paint[] {Color.green, Color.red, Color.blue, Color.yellow, Color.magenta, Color.cyan, Color.pink, Color.lightGray}
//            );
////        itemRenderer.setBaseItemLabelsVisible(true);
////        BarRenderer barRenderer = (BarRenderer)itemRenderer;
////        barRenderer.setItemMargin(0.2D);
////        plot.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, new java.awt.Color(147, 60, 45)));
////        //        StandardCategoryToolTipGenerator toolTipGenerator = new StandardCategoryToolTipGenerator("{0}: {2} %", new DecimalFormat("0"));
//            //
//            plot.setNoDataMessage("SIN DATOS!");
//            
//            
////        renderer.setLabelGenerator(new StandardCategoryLabelGenerator());
//            itemRenderer.setItemLabelsVisible(true);
//            final ItemLabelPosition p = new ItemLabelPosition(
//                    ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 45.0
//            );
//            itemRenderer.setPositiveItemLabelPosition(p);
//            plot.setRenderer(itemRenderer);
            
            // change the margin at the top of the range axis...
            final ValueAxis rangeAxis = plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setLowerMargin(0.15);
            rangeAxis.setUpperMargin(0.15);
            
        } catch (JSONException ex) {
            Logger.getLogger(Datos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return chart;
    }

    private void setcolors() {
        colores.add(Color.red);
        colores.add(Color.green);
        colores.add(Color.blue);
        colores.add(Color.yellow);
        colores.add(Color.magenta);
        colores.add(Color.cyan);
        colores.add(Color.pink);
        colores.add(Color.orange);
        colores.add(Color.darkGray);
        colores.add(Color.white);
    }
}
