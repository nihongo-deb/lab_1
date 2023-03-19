package com.nihongo_deb.KMeens.Graph;

import com.nihongo_deb.KMeens.CSVDataKMeansClustering;
import com.nihongo_deb.KMeens.Element;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class Dots extends JPanel {
    public static int index = 0;
    private CSVDataKMeansClustering data;

    public Dots(CSVDataKMeansClustering data){
        this.data = data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Element<Double> e : data.getElements()){
            g.fillOval((int) Math.round(e.abscissa) * 5, (int) Math.round(e.ordinate) * 5, 10,10);
        }
    }
}
