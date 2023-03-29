package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeans.CSVDataLoader;
import com.nihongo_deb.KMeans.KMeansCluster;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class TestCSV {
    public static void main(String[] args) throws Exception {
        CSVDataLoader data = new CSVDataLoader("BD-Patients.csv", 37, 117);
//        data.writeElementsInTXT("foundData.txt");

        int clusterNumber = 5;
        KMeansCluster cluster = new KMeansCluster(data, clusterNumber);

        for (int cl = 0; cl < 30; cl ++) {
            for (int i = 0; i < clusterNumber; i++) {
//                System.out.println("-------------------");
                System.out.println("<<< CLUSTER №" + i + " " + cluster.getClusters().get(i).size() + " " + cluster.getCenters().get(i).abscissa + "\t" + cluster.getCenters().get(i).ordinate + " >>>");
//                for (Element e : cluster.getClusters().get(i)){
//                    System.out.println(e.abscissa + "\t\t\t" + e.ordinate);
//                }
//                System.out.println("-------------------");
            }
            cluster.doClustering();
            System.out.println("\n##########################################\n");
        }
        System.out.println("\n##########################################");
        System.out.println("##########################################");
        System.out.println("########################################## \n");
        System.out.println("start calculating CS-Index");
        System.out.println("\n##########################################");
        System.out.println("##########################################");
        System.out.println("########################################## \n");


        int threadNum = 1;
        long before;
        long after;
        System.out.println();

        for (int i = 12; i <= 12; i ++){
            ExecutorService es = Executors.newFixedThreadPool(i);

            // Warm-up
            for (int j = 0; j < 10; j++) {
                cluster.findCSIndex(i, es);
            }

            long time = 0;
            for (int j = 0; j < 100000; j++) {
                var res = cluster.findCSIndex(i, es);
                time += res.getValue();
            }

            System.out.println("Elapsed time for " + i + " threads:\n" + (time / 100000) + "\n");

            es.shutdown();
            es.awaitTermination(1, TimeUnit.DAYS);
        }



    }
}