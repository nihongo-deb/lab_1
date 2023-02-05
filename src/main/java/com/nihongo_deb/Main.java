package com.nihongo_deb;

import com.nihongo_deb.BenchmarkedApps.App1024X768;

import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 04.02.2023
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // объект класса ImageCompression, в котором будет
        // храниться изначальное изображение и его последующие изменения
        // в связном списке (LinkedList)
        ImageCompression imOnlyNegative = new ImageCompression();
        // буферезируем изображение из папки resources
        imOnlyNegative.readeImageFromResources("gervant.jpg");
        // выполняем инвертирование, при этом буфер старого изображение не изменяется
        // инвертированное изображение будет помещено в связный список
        imOnlyNegative.applyNegative();
        // сохраняем буфер последнего изображения с расширением родителя (.jpg)
        imOnlyNegative.saveImage("neg-gervant");

        // проделываем те же операции, только изображение передаём через конструктор перовым аргументом
        // второй аргумент показывает, что файл будет взят из resources
        ImageCompression imOnlyCompression = new ImageCompression("gervant.jpg", true);
        // применяем свёртку с фильтром
        imOnlyCompression.applyConvolutionMatrix();
        imOnlyCompression.saveImage("comp-gervant");

        // проделываем те же операции
        ImageCompression imNegativeCompression = new ImageCompression("gervant.jpg", true);
        imNegativeCompression.applyNegative();
        imNegativeCompression.applyConvolutionMatrix();
        imNegativeCompression.saveImage("neg-cmp-gervant");
    }
}
