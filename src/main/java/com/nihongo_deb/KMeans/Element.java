package com.nihongo_deb.KMeans;

import java.awt.*;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
// класс для хранен данных элемента для кластеризации
public class Element implements Comparable{
    private static Color[] colors = {
            Color.BLACK,
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA
    };

    public final double abscissa;
    public final double ordinate;
    public final double length;
    public Color color;
    public byte colorIndex;

    public Element(double abscissa, double ordinate) {
        this.abscissa = abscissa;
        this.ordinate = ordinate;
        this.length = Math.sqrt(Math.pow(abscissa, 2) + Math.pow(ordinate, 2));

        this.color = Color.BLACK;
        this.colorIndex = (byte) 0;
    }

    public Element(double abscissa, double ordinate, int colorIndex) {
        this.abscissa = abscissa;
        this.ordinate = ordinate;
        this.length = Math.sqrt(Math.pow(abscissa, 2) + Math.pow(ordinate, 2));

        this.color = colors[colorIndex];
        this.colorIndex = (byte) colorIndex;
    }

    public void setColor(int colorIndex){
        this.color = colors[colorIndex];
    }

    @Override
    public String toString() {
        return abscissa + "\t" + ordinate + "\t" + color;
    }

    @Override
    public int compareTo(Object o) {
        Element e = (Element) o;

        if (this.length == e.length)
            return 0;
        else
            if (this.length > e.length)
                return 1;
            else
                return -1;
    }
}
