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

/**
 * Класс для кластеризации данных методом К-средних и многопоточного расчета CS-индекса.
 * Данные берутся из объекта класса {@link KMeansCSVDataLoader} и кластеризуются методом {@link KMeansCluster#doClustering()}.
 * <br>
 * <br>
 * Для проведения тестов центроиды изначально инициализируются не случайным образом, а занимают позицию определённого элемента.
 * Алгоритм первой инициализации центроид завязан на расстоянии элемента от начала координат (0.0; 0.0).
 * <br>
 * <br>
 * Каждый прокластеризованный элемент 'понимает', к какому кластеру он относится по присвоенному 'цвету',
 * который соответствует определённому кластеру {@link Element#colorIndex}
 */
public class KMeansCluster {
    /**
     * Список элементов, полученные из объекта класса {@link KMeansCSVDataLoader}
     */
    private List<Element> elements;
    /**
     * Количество кластеров
     */
    private int clustersNumber;
    /**
     * Список кластеров
     */
    private ArrayList<ArrayList<Element>> clusters;
    /**
     * Список центроид
     */
    private List<Element> centers;

    /**
     * <h2>Конструктор</h2>
     * Этапы конструктора:
     * <br>
     * <ul>
     *     <li> Получает ссылку на список отмасштабированных элементов из объекта класса {@link KMeansCSVDataLoader}
     *     <li> Сортирует элементы по возрастанию расстояния от начала координат
     *     <li> Инициализирует центроиды методом {@link KMeansCluster#initCenters()}
     * </ul>
     */
    public KMeansCluster(KMeansCSVDataLoader csvDataLoader, int clustersNumber){
        this.elements = csvDataLoader.getElements();
        Collections.sort(elements);

        this.clustersNumber = clustersNumber;
        this.clusters = new ArrayList<>();
        this.centers = new ArrayList<>();

        initCenters();
    }
    /**
     * <h2>Конструктор</h2>
     * Этапы конструктора:
     * <br>
     * <ul>
     *     <li> Получает ссылку на определённое количество ({@code elementsNumber}) элементов из объекта класса
     *     {@link KMeansCSVDataLoader}
     *     <li> Сортирует элементы по возрастанию расстояния от начала координат
     *     <li> Инициализирует центроиды методом {@link KMeansCluster#initCenters()}
     * </ul>
     */
    public KMeansCluster(KMeansCSVDataLoader csvDataLoader, int clustersNumber, int elementsNumber){
        this.elements = new ArrayList<>();
        this.clustersNumber = clustersNumber;
        this.clusters = new ArrayList<>();
        this.centers = new ArrayList<>();

        int csvDataLoaderSize = csvDataLoader.elementsSize();
        int index = 0;
        while (elements.size() < elementsNumber){
            elements.add(csvDataLoader.getElements().get(index));
            index += csvDataLoaderSize / elementsNumber;
        }

        Collections.sort(elements);

        initCenters();
    }

    /**
     *  Приватный метод для инициализации цетроид кластеров.
     *  Центороиды рассчитываются относительно удалённости элементов от центра.
     *  Поэтому перед этим в конструкторах предварительно были отсортированы элементы
     *  по удалённости их от начала координат.
     */
    private void initCenters(){
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
            centers.add(new Element(
                    elements.get((elements.size() - 1) / (i + 1)).abscissa,
                    elements.get((elements.size() - 1) / (i + 1)).ordinate,
                    i
            ));
        }
    }

    /**
     * Метод кластеризации методом К-средих
     */
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

    /**
     * Метод перерасчета центроид у сформированных кластеров.
     */
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

    /**
     * Метод определения к какой центроиде элемент находится ближе всего (присвоение его к определённому кластеру).
     */
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

    /**
     * Метод расчета расстояния между элементами.
     * <br>
     * Используется в методах:
     * @see KMeansCluster#farthestComrade
     * @see KMeansCluster#findCSIndex
     * @return расстояние между элементами
     */
    private static double distanceBetweenTwoElements(Element el1, Element el2){
        double distance =
                sqrt(
                        pow(abs(el1.abscissa - el2.abscissa), 2) +
                        pow(abs(el1.ordinate - el2.ordinate), 2)
                );

        return distance;
    }

    /**
     * Метод для расчета максимального расстояния для текущего элемента до элемента,
     * находящегося в том-же кластере. (одно из слагаемых для расчета числителя CS-индекса)
     * <br>
     * @see KMeansCluster#distanceBetweenTwoElements
     * @param element относительно какого элемента производится расчет
     * @return расстояние до максимальноудалённого элемента
     */
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

    /**
     * Метод для расчета CS-индекса. Многопоточный расчет используется только для расчета числителя индекса.
     * @param threadNum количество потоков, которые будут рассчитывать этот индекс
     * @return Pair.key - значение CS-индекса <br>
     *         Pair.value - время(в наносекундах затраченное на расчет)
     */
    public Pair<Double, Long> findCSIndex(int threadNum) throws InterruptedException, ExecutionException {
        double numerator = 0.0;
        double denominator = 0.0;

        // каждый элемент массива 'принадлежит' определённому потоку
        // после в конце выполнения каждого потока, элементы будут просумированы
        // сумма элементов будет равна числителю CS-индекса
        double [] partsOfNumerator = new double[threadNum];
        Arrays.fill(partsOfNumerator, 0.0);

        // пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CountDownLatch startLatch = new CountDownLatch(threadNum); // защелка, для одновременного начала расчета
        CountDownLatch endLatch = new CountDownLatch(threadNum); // защелка, возвращения в мастер-поток

        long before = 0;
        //цикл присвоения 'потоку' своей задачи
        for (int threadIndex = 0; threadIndex < threadNum; threadIndex++){
            executorService.submit(new CSIndexRunner(threadNum, threadIndex, partsOfNumerator, startLatch, endLatch));
            if (threadIndex == threadNum - 1){
                before = System.nanoTime();
            }
        }

        endLatch.await(); // ожидание исполнения всех потоков в мастер-потоке
        long after = System.nanoTime();

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        long elapseTime = after - before;

        // сумируем расчет каждого из потоков и получаем числитель
        for (Double d : partsOfNumerator) {
            numerator += d;
        }

        // подсчет знаменателя CS-индекса
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

    public int getClustersNumber() {
        return clustersNumber;
    }

    /**
     * Приватный класс имплементирующий интерфейс {@link Runnable}.
     * Данный класс используется для многопоточного расчета числителя
     * CS-индекса в методе {@link KMeansCluster#findCSIndex}
     *
     * Объектам данного класса в конструкторе поступает ссылка на все прокластерезованные элементы.
     * Каждый объект данного класса производит расчет определённого количество слагаемых числителя CS-индекса.
     * (пояснение - каждый элемент уже 'знает', в каком кластере он находится, и хранит в себе эту информацию)
     */
    private class CSIndexRunner implements Runnable {
        /**
         * Защелка для одномоментного старта потоков
         */
        private CountDownLatch startLatch;
        /**
         * Защелка, для перехода к join-у
         */
        private CountDownLatch endLatch;
        /**
         * Количество потоков, которые будут запущены в один момент времени
         */
        private int threadNum;
        /**
         * Индекс текущего потока
         */
        private int threadIndex;
        /**
         * Ссылка на массив, в котором будут храниться части подсчетов каждого из потоков.
         * Размер массива рамен {@link CSIndexRunner#threadNum}.
         * Индекс ячейки для записи для текущего потока равен {@link CSIndexRunner#threadIndex}
         */
        private double [] partsOfNumerator;
        /**
         * Подсчет текущего потока (1/{@link CSIndexRunner#threadNum} часть слагаемых числителя)
         */
        private double sum = 0.0;

        public CSIndexRunner(int threadNum, int threadIndex, double [] partsOfNumerator, CountDownLatch startLatch, CountDownLatch endLatch){
            this.threadNum = threadNum;
            this.threadIndex = threadIndex;
            this.partsOfNumerator = partsOfNumerator;
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        /**
         * Метод, который будет запускаться в отдельном потоке.
         * Также данный метод рассчитывает свое количество слагаемых для числителя CS-индекса.
         */
        @Override
        public void run() {
            startLatch.countDown(); // декрементирование стартовой защелки
            try {
                startLatch.await(); // ожидание 'отпирания' стартовой защелки
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // циклы подсчета числителей CS-индекса
            if (threadIndex == 0){ //если это первый поток, то он берёт на себя начальную часть списка элементов
                for (int elIndex = 0; elIndex < elements.size() / threadNum; elIndex++){
                    sum += farthestComrade(elements.get(elIndex));
                }
            } else {
                if (threadIndex == threadNum - 1){ //если это не крайний поток, то он берёт на себя конечную часть списка элементов
                    for (int elIndex = elements.size() * threadIndex / threadNum; elIndex < elements.size(); elIndex++){
                        sum += farthestComrade(elements.get(elIndex));
                    }
                } else { //тут всё ок
                    for (int elIndex = elements.size() * threadIndex / threadNum; elIndex < elements.size() * (threadIndex + 1) / threadNum; elIndex++){
                        sum += farthestComrade(elements.get(elIndex));
                    }
                }
            }

            partsOfNumerator[threadIndex] = sum;
            endLatch.countDown(); // декрементирование конечной защелки (её ждёт мастер-поток)
        }
    }
}
