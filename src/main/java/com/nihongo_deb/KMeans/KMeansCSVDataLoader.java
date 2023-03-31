package com.nihongo_deb.KMeans;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс для получения данных из файлов типа {@code .csv} и предназначен для передачи его объекта
 * как аргумент конструктора класса {@link KMeansCluster}. Полученные данные будут автоматически отмасштабированы
 * в интервале {@code [0...1]}.
 * <br>
 * <br>
 * Все данные которые находились в файле, будут храниться в переменной {@link #allCSV}.
 * Данная переменная автоматически отчищается после вычленения двух необходимых колонок.
 * <br>
 * <br>
 * Для получения колонок, их индексы нужно написать в конструкторе класса вторым и третьим аргументом.
 * <blockquote><pre>
 * new KMeansCSVDataLoader(String fileName, int columnIndex1, int columnIndex2)
 * </pre></blockquote>
 * Далее данные строк колонок помещаются в объекты класса {@link Element} (одна строка = {@link Element} один объект).
 * <br>
 * Вычлененные и отмасштабированные данные (колонки) для удобства просмотра можно записать {@code .txt} с CSV разметкой
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class KMeansCSVDataLoader {
    /**
     * Переменная в которой хранятся все элементы их {@code .csv} файла
     * <br>
     * Автоматически отчищается в конце конструктора {@link KMeansCSVDataLoader#KMeansCSVDataLoader}, после вычленения необходимых колонок.
     */
    private List<String[]> allCSV;
    /**
     * Вычлененная информация из {@code .csv} файла
     * @see Element
     */
    private List<Element> elements = new ArrayList<>();
    /**
     * Индекс первой колонки
     *
     * <blockquote><pre>
     * new KMeansCSVDataLoader(String fileName, int {@link KMeansCSVDataLoader#columnIndex1}, int columnIndex2)</pre></blockquote>
     */
    private final int columnIndex1;
    /**
     * Индекс второй колонки
     * <blockquote><pre>
     * new KMeansCSVDataLoader(String fileName, int columnIndex1, int {@link KMeansCSVDataLoader#columnIndex2})</pre></blockquote>
     */
    private final int columnIndex2;

    /**
     * Данные для масштабирования графика, они будут инициализированы в методе {@link KMeansCSVDataLoader#initMaxMin()}
     * и переопределены в методе {@link KMeansCSVDataLoader#fillElements()}
     * <br>
     * {@link KMeansCSVDataLoader#ordinateMax} - максимальное значение найденное в первой колонке
     * <br>
     * {@link KMeansCSVDataLoader#ordinateMin} - минимальное значение найденное в первой колонке
     * <br>
     * {@link KMeansCSVDataLoader#abscissaMax} - максимальное значение найденное в первой колонке
     * <br>
     * {@link KMeansCSVDataLoader#abscissaMin} - минимальное значение найденное в первой колонке
     */
    private double ordinateMax = 0;
    private double ordinateMin = 0;

    private double abscissaMax = 0;
    private double abscissaMin = 0;

    /**
     * <h2>Конструктор</h2>
     * Этапы конструктора:
     * <br>
     * <ul>
     *     <li> Загружает содержимое файла в переменную {@link KMeansCSVDataLoader#allCSV}
     *     при помощи метода {@link KMeansCSVDataLoader#loadCSV}
     *     <li> Инициализирует переменные, которые отвечают за хранение
     *     максимального и минимального значения двух колонок
     *     <ul>
     *         <li> {@link KMeansCSVDataLoader#ordinateMax}
     *         <li> {@link KMeansCSVDataLoader#ordinateMin}
     *         <li> {@link KMeansCSVDataLoader#abscissaMax}
     *         <li> {@link KMeansCSVDataLoader#abscissaMin}
     *     </ul>
     *     при помощи метода {@link KMeansCSVDataLoader#initMaxMin()} для дальнейшего масштабирования.
     *     <li> Вычленяет элементы из колонок и записывает значения каждой строки
     *     в отдельный объект класса {@link Element} и помещает его в {@link KMeansCSVDataLoader#elements}
     *     при помощи метода {@link KMeansCSVDataLoader#fillElements()}. После чего происходит масштабирование данных.
     *     <li> В конце конструктора отчищается переменная {@link KMeansCSVDataLoader#allCSV}
     * </ul>
     * @param fileName имя файла типа .csv, из которого будут вычленены две колонки.
     *                 Файл должен лежать в папке resources.
     * @param columnIndex1 индекс первой колонки, которую требуется найти и вычленить из файла.
     * @param columnIndex2 индекс первой колонки, которую требуется найти и вычленить из файла.
     */
    public KMeansCSVDataLoader(String fileName, int columnIndex1, int columnIndex2) throws IOException, CsvException {
        this.columnIndex1 = columnIndex1;
        this.columnIndex2 = columnIndex2;

        // загружаем строки из CSV файла
        loadCSV(fileName);
        // инициализируем начальное максимальное и минимальное значение ординат и абсциссы
        initMaxMin();
        // заполнение данных для кластеризации
        fillElements();

        //отчищаем память
        allCSV = new LinkedList<>();
    }

    /**
     * Приватный метод для прочтения файла и записи его данных в переменную {@link KMeansCSVDataLoader#allCSV}
     * @param fileNameInResourcesPath имя .csv файла в папке resources
     * @see CSVReader
     */
    private void loadCSV(String fileNameInResourcesPath) throws IOException, CsvException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileNameInResourcesPath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReader(inputStreamReader)) {
            allCSV = reader.readAll();
        }
    }
    /**
     * Приватный метод для инициализации переменных
     * <ul>
     *     <li> {@link KMeansCSVDataLoader#ordinateMax}
     *     <li> {@link KMeansCSVDataLoader#ordinateMin}
     *     <li> {@link KMeansCSVDataLoader#abscissaMax}
     *     <li> {@link KMeansCSVDataLoader#abscissaMin}
     * </ul>
     * для масштабирования данных. Получают значения полноценной
     * (имеет данные как в первой, так и во второй колонке) строки двух колонок.
     */
    private void initMaxMin(){
        boolean noMaxMinInited = true;
        int csvRow = 1;
        while (noMaxMinInited){
            // нулевая строка содержит имена колонок
            boolean isDataExist =
                    (this.allCSV.get(csvRow)[this.columnIndex1] != null) && !allCSV.get(csvRow)[this.columnIndex1].isEmpty() &&
                    (this.allCSV.get(csvRow)[this.columnIndex2] != null) && !allCSV.get(csvRow)[this.columnIndex2].isEmpty();

            if (isDataExist){
                this.ordinateMax = Double.parseDouble(this.allCSV.get(csvRow)[this.columnIndex1]);
                this.ordinateMin = Double.parseDouble(this.allCSV.get(csvRow)[this.columnIndex1]);

                this.abscissaMax = Double.parseDouble(this.allCSV.get(csvRow)[this.columnIndex2]);
                this.abscissaMin = Double.parseDouble(this.allCSV.get(csvRow)[this.columnIndex2]);
                noMaxMinInited = false;
            }
            csvRow++;
        }
    }

    /**
     * Приватный метод для вычленения и построчной записи
     * данных из двух колонок в список {@link KMeansCSVDataLoader#elements}.
     *
     * @see Element
     */
    private void fillElements(){
        Element currentElement;
        // цикл записи элементов в список
        for (int csv = 1, i = 0; i < this.allCSV.size() - 1; i++, csv++){
            // комбинированное условие, которое проверяет строку
            // на одновременное наличие данных как из первого так и из второго столбца
            boolean isDataExist =
                    this.allCSV.get(csv)[this.columnIndex1] != null && !this.allCSV.get(csv)[this.columnIndex1].isEmpty() &&
                    this.allCSV.get(csv)[this.columnIndex2] != null && !this.allCSV.get(csv)[this.columnIndex2].isEmpty();
            if (isDataExist) {
                currentElement = new Element(
                        Double.parseDouble(this.allCSV.get(csv)[columnIndex1]),
                        Double.parseDouble(this.allCSV.get(csv)[columnIndex2]));

                // переопределение данных для масштабирования
                this.ordinateMax = Math.max(currentElement.ordinate, this.ordinateMax);
                this.ordinateMin = Math.min(currentElement.ordinate, this.ordinateMin);

                this.abscissaMax = Math.max(currentElement.abscissa, this.abscissaMax);
                this.abscissaMin = Math.min(currentElement.abscissa, this.abscissaMin);

                this.elements.add(currentElement);
            }
        }
        // масштабирование сформированного списка элементов
        elements.replaceAll(doubleElement -> new Element(
                (doubleElement.abscissa - abscissaMin) / (abscissaMax - abscissaMin),
                (doubleElement.ordinate - ordinateMin) / (ordinateMax - ordinateMin)
        ));
    }

    /**
     * Публичный метод для вывода вычлененных данных в следующем формате():
     * <br>
     * строка - S, первый столбец - 1C, второй столбец - 2C
     * <blockquote><pre>
     *          ...
     *  '(S)    (1C)' '(S)    (2C)'
     *  '(S + 1)(1C)' '(S + 1)(2C)'
     *  '(S + 2)(1C)' '(S + 2)(2C)'
     *          ...
     *  </pre></blockquote>
     */
    public void printElements(){
        StringBuilder stringBuilder = new StringBuilder();

        for (Element e : elements){
            stringBuilder.append(e.ordinate).append(' ').append(e.abscissa).append("\n");
        }

        System.out.println(stringBuilder.toString());
    }

    /**
     * Публичный метод для записи вычлененных элементов в TXT файл с CSV разметкой
     */
    public void writeElementsInTXT(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        StringBuilder stringBuilder = new StringBuilder();
        for (Element e : elements){
            stringBuilder.append(e.abscissa).append(";").append(e.ordinate).append("\n");
        }
        writer.append(stringBuilder.toString());
        writer.close();
    }

    public int elementsSize(){
        return elements.size();
    }

    public double getOrdinateMax() {
        return ordinateMax;
    }

    public double getOrdinateMin() {
        return ordinateMin;
    }

    public double getAbscissaMax() {
        return abscissaMax;
    }

    public double getAbscissaMin() {
        return abscissaMin;
    }

    public List<Element> getElements() {
        return elements;
    }
}
