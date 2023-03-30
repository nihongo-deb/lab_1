package com.nihongo_deb.KMeans;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 24.03.2023
 */
public class KMeansPrinter extends JComponent {
    private KMeansCluster kMeansCluster;
    private Graphics2D graph;
    private int marg = 10;
    private double increaseValue;

    public KMeansPrinter (KMeansCluster kMeansCluster, double increaseValue){
        this.kMeansCluster = kMeansCluster;
        this.increaseValue = increaseValue;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        graph = (Graphics2D) g;
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // get width and height
        int width = getWidth();
        int height = getHeight();

        // draw graph
        graph.draw(new Line2D.Double(marg, marg, marg, height - marg)); // Y
        graph.draw(new Line2D.Double(marg, height - marg, width - marg, height - marg)); // X

        int x;
        int y;
        for(Element e : kMeansCluster.getElements()){
            graph.setColor(e.color);
            x = (int) (e.ordinate * width * increaseValue) + marg;
            y = height - (int) (e.abscissa * height * increaseValue) - marg;
            graph.drawOval(x, y, 2,2);
//            System.out.println(e.abscissa + " " + e.ordinate + " -> " + x + " " + y);
        }
    }

}
