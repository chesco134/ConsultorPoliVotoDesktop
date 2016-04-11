/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polivoto.vistas.acciones;

import org.jfree.chart.renderer.category.BarRenderer;
import java.awt.Paint;
/**
 *
 * @author azaraf
 */
public class CustomRenderer 
    extends BarRenderer
  {
    private Paint[] colors;
    
    public CustomRenderer(Paint[] paramArrayOfPaint)
    {
      this.colors = paramArrayOfPaint;
    }
    
    public Paint getItemPaint(int paramInt1, int paramInt2)
    {
      return this.colors[(paramInt2 % this.colors.length)];
    }
  }

