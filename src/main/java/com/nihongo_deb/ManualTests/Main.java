package com.nihongo_deb.ManualTests;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 04.02.2023
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, CsvException {
        // 37 - HCO3_pvariance, 117 - Temp_pvariance

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("BD-Patients.csv");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        List<String[]> r;
        try (CSVReader reader = new CSVReader(inputStreamReader)) {
            r = reader.readAll();
        }

        // future X and Y
        List<Double> HCO3_pvariance = new ArrayList<>();
        List<Double> Temp_pvariance = new ArrayList<>();

        double HCO3_pvariance_max = 0;
        double Temp_pvariance_max = 0;

        double HCO3_pvariance_min = 0;
        double Temp_pvariance_min = 0;

        boolean initMax = true;
        for (int csv = 1, i = 0; i < r.size() - 1; i++, csv++){
            if (initMax){
                HCO3_pvariance_max = Double.parseDouble(r.get(csv)[37]);
                Temp_pvariance_max = Double.parseDouble(r.get(csv)[117]);

                HCO3_pvariance_min = Double.parseDouble(r.get(csv)[37]);
                Temp_pvariance_min = Double.parseDouble(r.get(csv)[117]);
                initMax = false;
            }

            if (r.get(csv)[37] != null && !r.get(csv)[37].isEmpty() && r.get(csv)[117] != null && !r.get(csv)[117].isEmpty()) {
                if (Double.parseDouble(r.get(csv)[37]) > HCO3_pvariance_max)
                    HCO3_pvariance_max = Double.parseDouble(r.get(csv)[37]);

                if(Double.parseDouble(r.get(csv)[117]) > Temp_pvariance_max)
                    Temp_pvariance_max = Double.parseDouble(r.get(csv)[117]);

                if (Double.parseDouble(r.get(csv)[37]) < HCO3_pvariance_max)
                    HCO3_pvariance_min = Double.parseDouble(r.get(csv)[37]);

                if(Double.parseDouble(r.get(csv)[117]) < Temp_pvariance_max)
                    Temp_pvariance_min = Double.parseDouble(r.get(csv)[117]);

                HCO3_pvariance.add(Double.parseDouble(r.get(csv)[37]));
                Temp_pvariance.add(Double.parseDouble(r.get(csv)[117]));
            }
        }

        System.out.println("HCO3_pvariance_max: " + HCO3_pvariance_max);
        System.out.println("Temp_pvariance_max: " + Temp_pvariance_max);
        System.out.println();
        System.out.println("HCO3_pvariance_min: " + HCO3_pvariance_min);
        System.out.println("Temp_pvariance_min: " + Temp_pvariance_min);
    }
}
