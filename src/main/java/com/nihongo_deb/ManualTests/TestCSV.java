package com.nihongo_deb.ManualTests;

import com.nihongo_deb.KMeens.CSVDataKMeansClustering;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
public class TestCSV {
    public static void main(String[] args) throws IOException, CsvException {
        CSVDataKMeansClustering data = new CSVDataKMeansClustering("BD-Patients.csv", 37, 117);
        data.printElements();
    }
}
