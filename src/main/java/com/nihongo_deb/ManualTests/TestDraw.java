package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeans.KMeansCSVDataLoader;
import com.nihongo_deb.KMeans.KMeansCluster;
import com.nihongo_deb.KMeans.KMeansPrinter;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 30.03.2023
 */
public class TestDraw {
    public static void main(String[] args) throws IOException, CsvException {
        int clusterNumber = 5;
        for (int i = 0; i < 21; i++) {
            KMeansCSVDataLoader data = new KMeansCSVDataLoader("BD-Patients.csv", 37, 117);
            KMeansCluster cluster = new KMeansCluster(data, clusterNumber);
            for (int k = 0; k < i; k ++) {
                cluster.doClustering();
            }
            new KMeansPrinter(cluster, 1.5);
        }
    }
}
