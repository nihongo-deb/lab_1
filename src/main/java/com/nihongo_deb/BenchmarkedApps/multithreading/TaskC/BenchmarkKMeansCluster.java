package com.nihongo_deb.BenchmarkedApps.multithreading.TaskC;

import com.nihongo_deb.KMeans.Element;
import com.nihongo_deb.KMeans.KMeansCSVDataLoader;
import com.nihongo_deb.KMeans.KMeansCluster;
import com.nihongo_deb.KMeans.KMeansPrinter;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 02.04.2023
 */
public class BenchmarkKMeansCluster {
    public static void main(String[] args) throws IOException, CsvException, ExecutionException, InterruptedException {
        KMeansCSVDataLoader data3Cl = new KMeansCSVDataLoader("BD-Patients.csv", 37, 117);
        KMeansCSVDataLoader data4Cl = new KMeansCSVDataLoader("BD-Patients.csv", 37, 117);
        KMeansCSVDataLoader data5Cl = new KMeansCSVDataLoader("BD-Patients.csv", 37, 117);
        // массив данных (объектов с данными для кластеризации)
        KMeansCSVDataLoader[] data = new KMeansCSVDataLoader[]{data3Cl, data4Cl, data5Cl};

        //массив кол-ва потоков
        int[] numThreads = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};
        //массив кол-ва кластеров
        int[] numClusters = new int[]{3, 4, 5};
        //количество итераций кластеризации
        int clusteringIterations = 25;
        //количество прогревочных итераций подсчета CS-индекса
        int numMeasurementIterations = 3;

        //объект, который будет в роли подопытного
        //герой 8)
        KMeansCluster cluster;

        for (int clusterIndex = 0; clusterIndex < data.length; clusterIndex++){
            cluster = new KMeansCluster(data[clusterIndex], numClusters[clusterIndex]);

            for (int iter = 0; iter < clusteringIterations; iter++){ // кластеризация
                cluster.doClustering();
            }

            int centerIndex = 0;
            for (Element e : cluster.getCenters()){ // вывод центров кластеров в консоль
                System.out.println("<<< CLUSTER CENTER №" + centerIndex + " " +  e.abscissa + "\t" + e.ordinate + " >>>");
                centerIndex++;
            }
            System.out.println("#######################");
            System.out.println("Start calculating CS-index");
            System.out.println("Clusters: " + numClusters[clusterIndex]);
            for (int th : numThreads){
                for (int measIter = 0; measIter < numMeasurementIterations; measIter++){ // прогрев тачки
                    cluster.findCSIndex(th);
                }

                Pair<Double, Long> result = cluster.findCSIndex(th);
                System.out.println("Threads: " + th);
                System.out.println("CS-index: " + result.getKey());
                System.out.println("Numeration calc-time: " + result.getValue() + "ns");
                System.out.println("- - -");
            }
            System.out.println("");
            new KMeansPrinter(cluster, 1.5); // вывод графика кластеров
        }
    }
}
