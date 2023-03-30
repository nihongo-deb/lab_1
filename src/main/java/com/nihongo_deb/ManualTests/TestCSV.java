package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeans.CSVDataLoader;
import com.nihongo_deb.KMeans.KMeansCluster;
import org.apache.commons.math3.util.Pair;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class TestCSV {
    public static void main(String[] args) throws Exception {
        CSVDataLoader data = new CSVDataLoader("BD-Patients.csv", 37, 117);
//        data.writeElementsInTXT("foundData.txt");

        int clusterNumber = 4;
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

        for (int threadNum = 1; threadNum < 9; threadNum++){
            System.out.println("Thread num: " + threadNum);
            for (int warmUpIter = 0; warmUpIter < 1000; warmUpIter++)
                cluster.findCSIndex(threadNum);

            Pair<Double, Long> pair = cluster.findCSIndex(threadNum);

//            System.out.println("Result: " + pair.getKey());
            System.out.println("Elapse time: " + pair.getValue());
            System.out.println("CS-index: " + pair.getKey());
            System.out.println("- - - -");
        }

    }
}