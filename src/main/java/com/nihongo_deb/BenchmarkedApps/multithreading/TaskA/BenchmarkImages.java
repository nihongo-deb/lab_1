package com.nihongo_deb.BenchmarkedApps.multithreading.TaskA;

import com.nihongo_deb.ImageCompression;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 01.04.2023
 */
public class BenchmarkImages {
    public static void main(String[] args) throws InterruptedException {
        String fileName10240_7680 = "gervant_10240x7680.jpg";
        String fileName12800_9600 = "gervant_12800x9600.jpg";
        String fileName20480_15360 = "gervant_20480x15360.jpg";

        int[] numThreads = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};
        int numIterations = 3;
        String[] imagesName = new String[]{fileName10240_7680, fileName12800_9600, fileName20480_15360};

        ImageCompression image;
        long before;
        long after;
        System.out.println("<< Task-A Benchmark >>\n");
        StringBuilder consoleMessage = new StringBuilder();
        for (int imageIndex = 0; imageIndex < imagesName.length; imageIndex++) {
            image = new ImageCompression(imagesName[imageIndex], true);
            System.out.println("Image " + imagesName[imageIndex] + " loaded");
            System.out.println("#########################");

            for (int threads : numThreads) {
                System.out.println("Start warmup");
                before = System.currentTimeMillis();
                for (int warmupIndex = 0; warmupIndex < 5; warmupIndex++) { // разогрев тачки
                    image.applyNegativeMultithreading(threads);
                    image.applyConvolutionMatrixMultithreading(threads);
                }
                after = System.currentTimeMillis();
                System.out.println("End warmup\n" +
                        "Average Elapse-time: " + (long)((after - before) / 5));

                System.out.println("--");

                System.out.println("Measurement starts");
                before = System.currentTimeMillis();
                for(int iteration = 0; iteration < numIterations; iteration++){ // замер
                    image.applyNegativeMultithreading(threads);
                    image.applyConvolutionMatrixMultithreading(threads);
                }
                System.out.println("Measurement ends");
                after = System.currentTimeMillis();
                // принт в консоль
                consoleMessage.append("Average Elapse-time: ").append((int)(after - before)/numIterations).append("ms\n");
                consoleMessage.append("Image:" ).append(imagesName[imageIndex]).append('\n');
                consoleMessage.append("Threads: ").append(threads).append('\n').append('\n');
                System.out.println(consoleMessage);

                System.out.println("#########################");
            }
        }

    }
}
