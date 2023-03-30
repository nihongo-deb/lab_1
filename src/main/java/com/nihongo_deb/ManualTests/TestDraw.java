package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeans.CSVDataLoader;
import com.nihongo_deb.KMeans.KMeansCluster;
import com.nihongo_deb.KMeans.KMeansPrinter;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 30.03.2023
 */
public class TestDraw {
    public static void main(String[] args) throws IOException, CsvException {
        CSVDataLoader data = new CSVDataLoader("BD-Patients.csv", 37, 117);
        System.out.println(data.getAbscissaMax());
        System.out.println(data.getOrdinateMax());

        int clusterNumber = 4;
        KMeansCluster cluster = new KMeansCluster(data, clusterNumber);
        for (int i = 0; i < 21; i++)
            cluster.doClustering();

        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(100,100,600,450);

        frame.add(new KMeansPrinter(cluster, 1.5));
    }
}
