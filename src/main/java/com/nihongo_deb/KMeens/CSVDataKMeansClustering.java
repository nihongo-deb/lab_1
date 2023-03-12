package com.nihongo_deb.KMeens;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class CSVDataKMeansClustering {
    private List<String[]> allCSV;
    private List<Element<Double>> elements = new LinkedList<>();
    private final int ordinateIndex;
    private final int abscissaIndex;

    // данные для отрисовки графика (границы графика)
    private double ordinateMax = 0;
    private double ordinateMin = 0;

    private double abscissaMax = 0;
    private double abscissaMin = 0;

    // TODO добавить чтение файла по абсолютному пути
    public CSVDataKMeansClustering(String resourcesPath, int ordinateIndex, int abscissaIndex) throws IOException, CsvException {
        this.ordinateIndex = ordinateIndex;
        this.abscissaIndex = abscissaIndex;

        loadCSV(resourcesPath);
        initMaxMin();
        // заполнение данных для кластеризации
        fillClusteringElements();
    }

    private void loadCSV(String resourcesPath) throws IOException, CsvException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesPath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReader(inputStreamReader)) {
            allCSV = reader.readAll();
        }
    }

    private void initMaxMin(){
        boolean noMaxMinInited = true;
        int csvRow = 1;
        while (noMaxMinInited){
            // нулевая строка содержит имена колонок
            boolean isDataExist =
                    this.allCSV.get(csvRow)[this.ordinateIndex] != null && !allCSV.get(csvRow)[this.ordinateIndex].isEmpty() &&
                            this.allCSV.get(csvRow)[this.abscissaIndex] != null && !allCSV.get(csvRow)[this.abscissaIndex].isEmpty();

            if (isDataExist){
                this.ordinateMax = Double.parseDouble(this.allCSV.get(csvRow)[this.ordinateIndex]);
                this.ordinateMin = Double.parseDouble(this.allCSV.get(csvRow)[this.ordinateIndex]);

                this.abscissaMax = Double.parseDouble(this.allCSV.get(csvRow)[this.abscissaIndex]);
                this.abscissaMin = Double.parseDouble(this.allCSV.get(csvRow)[this.abscissaIndex]);
                noMaxMinInited = false;
            }
            csvRow++;
        }
    }

    private void fillClusteringElements(){
        Element<Double> currentElement;
        for (int csv = 1, i = 0; i < this.allCSV.size() - 1; i++, csv++){
            boolean isDataExist =
                    this.allCSV.get(csv)[this.ordinateIndex] != null && !this.allCSV.get(csv)[this.ordinateIndex].isEmpty() &&
                    this.allCSV.get(csv)[this.abscissaIndex] != null && !this.allCSV.get(csv)[this.abscissaIndex].isEmpty();
            if (isDataExist) {
                currentElement = new Element<>(
                        Double.parseDouble(this.allCSV.get(csv)[ordinateIndex]),
                        Double.parseDouble(this.allCSV.get(csv)[abscissaIndex]));

                this.ordinateMax = currentElement.ordinate > this.ordinateMax ? currentElement.ordinate : this.ordinateMax;
                this.ordinateMin = currentElement.ordinate < this.ordinateMin ? currentElement.ordinate : this.ordinateMin;

                this.abscissaMax = currentElement.abscissa > this.abscissaMax ? currentElement.abscissa : this.abscissaMax;
                this.abscissaMin = currentElement.abscissa > this.abscissaMin ? currentElement.abscissa : this.abscissaMin;

                this.elements.add(currentElement);
            }
        }
    }

    public void printElements(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(allCSV.get(0)[ordinateIndex]).append(' ').append(allCSV.get(0)[abscissaIndex]).append("\n");

        for (Element e : elements){
            stringBuilder.append(e.ordinate).append(' ').append(e.abscissa).append("\n");
        }

        System.out.println(stringBuilder.toString());
    }
}
