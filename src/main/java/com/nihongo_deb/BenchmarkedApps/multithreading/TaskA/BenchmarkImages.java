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

        ImageCompression image_10240_7680 = new ImageCompression(fileName10240_7680, true);
//        ImageCompression image_12800_9600 = new ImageCompression(fileName12800_9600, true);
//        ImageCompression image_20480_15360 = new ImageCompression(fileName20480_15360, true);

        int[] numThreads = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};
        int numIterations = 3;
        String[] fileNames = new String[]{fileName10240_7680, fileName12800_9600, fileName20480_15360};
        ImageCompression[] images = new ImageCompression[]{image_10240_7680};

        System.out.println("<< Task-A Benchmark >>\n");
        StringBuilder consoleMessage = new StringBuilder();
        for (int imageIndex = 0; imageIndex < images.length; imageIndex++) {
            for (int threads : numThreads) {
                for (int warmupIndex = 0; warmupIndex < 5; warmupIndex++) {
                    images[imageIndex].applyNegativeMultithreading(threads);
                    images[imageIndex].applyConvolutionMatrixMultithreading(threads);
                }

                long before = System.currentTimeMillis();
                for(int iteration = 0; iteration < numIterations; iteration++){
                    images[imageIndex].applyNegativeMultithreading(threads);
                    images[imageIndex].applyConvolutionMatrixMultithreading(threads);
                }
                long after = System.currentTimeMillis();

                consoleMessage.append("Elapse time: ").append((int)(after - before)/numIterations).append("ms\n");
                consoleMessage.append("Image:" ).append(fileNames[imageIndex]).append('\n');
                consoleMessage.append("Threads: ").append(threads).append('\n');
                System.out.println(consoleMessage);
            }

            images[imageIndex] = null;
        }

    }
}
