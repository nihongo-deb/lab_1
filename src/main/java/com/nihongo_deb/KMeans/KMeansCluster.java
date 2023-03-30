package com.nihongo_deb.KMeans;

import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.*;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 24.03.2023
 */
public class KMeansCluster {
    // элементы из CSV - файла
    private List<Element> elements;
    // кол-во кластеров (= кол-ву центров)
    private int clustersNumber;
    // кластеры
    private ArrayList<ArrayList<Element>> clusters;
    // центры
    private List<Element> centers;

    public KMeansCluster(CSVDataLoader csvDataLoader, int clustersNumber){
        this.elements = csvDataLoader.getElements();
        Collections.sort(elements);

        this.clustersNumber = clustersNumber;
        this.clusters = new ArrayList<>();
        this.centers = new ArrayList<>();

        initCenters();
    }

    public KMeansCluster(CSVDataLoader csvDataLoader, int clustersNumber, int elementsNumber){
        this.elements = new ArrayList<>();
        this.clustersNumber = clustersNumber;
        this.clusters = new ArrayList<>();
        this.centers = new ArrayList<>();


        int csvDataLoaderSize = csvDataLoader.elementsSize();
        Random random = new Random();
        int index = 0;
        while (elements.size() < elementsNumber){
            elements.add(csvDataLoader.getElements().get(index));
//            index += random.nextInt(csvDataLoaderSize / elementsNumber - 1) + 1;
            index += csvDataLoaderSize / elementsNumber;
        }

        Collections.sort(elements);

        initCenters();
    }

    private void initCenters(){
        Random random = new Random();
        double ordinateMax = elements.get(0).ordinate;
        double ordinateMin = elements.get(0).ordinate;
        double abscissaMax = elements.get(0).abscissa;
        double abscissaMin = elements.get(0).abscissa;

        for (Element e : elements){
            ordinateMax = Math.max(ordinateMax, e.ordinate);
            ordinateMin = Math.min(ordinateMin, e.ordinate);

            abscissaMax = Math.max(abscissaMax, e.abscissa);
            abscissaMin = Math.min(abscissaMin, e.abscissa);
        }

        for (int i = 0; i < clustersNumber; i ++) {
            clusters.add(new ArrayList<Element>());
//            centers.add(new Element(
//                    abscissaMin + (abscissaMax - abscissaMin) * random.nextDouble(),
//                    ordinateMin + (ordinateMax - ordinateMin) * random.nextDouble(),
//                    i
//            ));

//            centers.add(new Element(
//                    abscissaMax * i / clustersNumber,
//                    ordinateMax * i / clustersNumber,
//                    i
//            ));
            centers.add(new Element(
                    elements.get((elements.size() - 1) / (i + 1)).abscissa,
                    elements.get((elements.size() - 1) / (i + 1)).ordinate,
                    i
            ));
        }
    }

    public void doClustering(){
        for (int i = 0; i < clustersNumber; i++){
            clusters.get(i).clear();
        }

        for (Element e : elements){
            int clusterIndex = findNearestCenters(e);
            e.setColor(clusterIndex);
            clusters.get(clusterIndex).add(e);
        }

        calculateCenters();
    }

    private void calculateCenters(){
        double averageAbscissa;
        double averageOrdinate;
        for (int i = 0; i < clustersNumber; i++){
            if (clusters.get(i).isEmpty())
                break;

            averageAbscissa = 0;
            averageOrdinate = 0;

            for (var e : clusters.get(i)){
                averageAbscissa += e.abscissa;
                averageOrdinate += e.ordinate;
            }
            averageAbscissa = averageAbscissa / clusters.get(i).size();
            averageOrdinate = averageOrdinate / clusters.get(i).size();
            centers.set(i, new Element(averageAbscissa, averageOrdinate, i));
        }
    }

    private int findNearestCenters(Element element){
        double minLength = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < clustersNumber; i++){
            double currentLength =
                    sqrt(
                            pow(abs(centers.get(i).abscissa - element.abscissa), 2) +
                            pow(abs(centers.get(i).ordinate - element.ordinate), 2)
                    );

            if (currentLength < minLength){
                minLength = currentLength;
                index = i;
            }
        }
        return index;
    }

    private static double distanceBetweenTwoElements(Element el1, Element el2){
        double distance =
                sqrt(
                        pow(abs(el1.abscissa - el2.abscissa), 2) +
                        pow(abs(el1.ordinate - el2.ordinate), 2)
                );

        return distance;
    }

    private double farthestComrade(Element element){
        double maxDistance = 0.0;

        for (Element e : clusters.get(element.colorIndex)){
            double currentDist = distanceBetweenTwoElements(element, e);
            if(maxDistance < currentDist){
                maxDistance = currentDist;
            }
        }

        return maxDistance;
    }

    public Pair<Double, Long> findCSIndex(int threadNum) throws InterruptedException, ExecutionException {
        double numerator = 0.0;
        double denominator = 0.0;

        double [] partsOfNumerator = new double[threadNum];
        Arrays.fill(partsOfNumerator, 0.0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CountDownLatch startLatch = new CountDownLatch(threadNum);
        CountDownLatch endLatch = new CountDownLatch(threadNum);

        long before = 0;
        for (int threadIndex = 0; threadIndex < threadNum; threadIndex++){
            executorService.submit(new CSIndexRunner(threadNum, threadIndex, partsOfNumerator, startLatch, endLatch));
            if (threadIndex == threadNum - 1){
                before = System.currentTimeMillis();
            }
        }

        endLatch.await();
        long after = System.currentTimeMillis();

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        long elapseTime = after - before;

        for (Double d : partsOfNumerator) {
            numerator += d;
        }

        for (int clusterIndex = 0; clusterIndex < clustersNumber; clusterIndex++){
            double minDist = Double.MAX_VALUE;
            for (Element c1 : centers){
                for (Element c2 : centers){
                    double currentDist = distanceBetweenTwoElements(c1, c2);
                    if (minDist > currentDist && c1 != c2){
                        minDist = currentDist;
                    }
                }
            }
            denominator += minDist;
        }

        double result = numerator / denominator;
        return new Pair<Double, Long>(result, elapseTime);
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public ArrayList<ArrayList<Element>> getClusters() {
        return clusters;
    }

    public void setClusters(ArrayList<ArrayList<Element>> clusters) {
        this.clusters = clusters;
    }

    public List<Element> getCenters() {
        return centers;
    }

    public void setCenters(List<Element> centers) {
        this.centers = centers;
    }

    private class CSIndexRunner implements Runnable {
        private CountDownLatch startLatch;
        private CountDownLatch endLatch;
        private int threadNum;
        private int threadIndex;
        private double [] partsOfNumerator;
        private double sum = 0.0;

        public CSIndexRunner(int threadNum, int threadIndex, double [] partsOfNumerator, CountDownLatch startLatch, CountDownLatch endLatch){
            this.threadNum = threadNum;
            this.threadIndex = threadIndex;
            this.partsOfNumerator = partsOfNumerator;
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {
            startLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (threadIndex == 0){
                for (int elIndex = 0; elIndex < elements.size() / threadNum; elIndex++){
                    sum += farthestComrade(elements.get(elIndex));
                }
            } else {
                if (threadIndex == threadNum - 1){
                    for (int elIndex = elements.size() * threadIndex / threadNum; elIndex < elements.size(); elIndex++){
                        sum += farthestComrade(elements.get(elIndex));
                    }
                } else {
                    for (int elIndex = elements.size() * threadIndex / threadNum; elIndex < elements.size() * (threadIndex + 1) / threadNum; elIndex++){
                        sum += farthestComrade(elements.get(elIndex));
                    }
                }
            }

            partsOfNumerator[threadIndex] = sum;
            endLatch.countDown();
        }
    }
}
