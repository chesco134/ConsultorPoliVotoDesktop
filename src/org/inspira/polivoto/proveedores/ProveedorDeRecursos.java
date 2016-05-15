package org.inspira.polivoto.proveedores;

import java.util.Calendar;

/**
 * Created by jcapiz on 7/04/16.
 */
public class ProveedorDeRecursos {

    public static String obtenerFecha(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                year
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }

    public static String obtenerFecha(java.util.Date fecha){
        if( fecha.getTime() < 0 )
            return "---";
        Calendar c = Calendar.getInstance();
        c.setTime(fecha);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                year
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }

    public static String obtenerFormatoEnHoras(long millis){
        long hours = (millis/(long)36e5);
        long minute = ((hours == 0l ? millis : (millis = (millis - hours*(long)36e5)))/(long)6e4);
        float second = ( minute == 0l ? millis : (millis - minute*(float)6e4))/1000;
        return (hours < 10 ? "0" + hours : hours)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }
}
