package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeens.CSVDataLoader;
import com.nihongo_deb.KMeens.Element;
import com.nihongo_deb.KMeens.KMeansCluster;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.Random;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class TestCSV {
    public static void main(String[] args) throws IOException, CsvException {
        CSVDataLoader data = new CSVDataLoader("BD-Patients.csv", 37, 117);
        data.writeElementsInTXT("foundData.txt");

        int clusterNumber = 5;
        KMeansCluster cluster = new KMeansCluster(data, clusterNumber, 1000);

        for (int cl = 0; cl < 5; cl ++) {
            for (int i = 0; i < clusterNumber; i++) {
//                System.out.println("-------------------");
                System.out.println("<<< CLUSTER №" + i + " " + cluster.getClusters().get(i).size() + " " + cluster.getCenters().get(i).abscissa + "\t" + cluster.getCenters().get(i).ordinate + " >>>");
//                for (Element e : cluster.getClusters().get(i)){
//                    System.out.println(e.abscissa + "\t\t\t" + e.ordinate);
//                }
//                System.out.println("-------------------");
            }
            cluster.doClustering();
            System.out.println("\n ########################################## \n");
        }
    }
}
