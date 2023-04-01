package com.nihongo_deb.BenchmarkedApps.multithreading.TaskA;

import com.nihongo_deb.ImageCompression;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 14.02.2023
 */
@State(Scope.Benchmark)
public class App_A_6_Threads_12800x9600 {
    private static ImageCompression imageCompression = new ImageCompression();
    private static int numThreads;
    private static String fileName = "gervant_12800x9600.jpg";
    {
        imageCompression.readeImageFromResources(fileName);
        numThreads = 6;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 2)
    @Fork(value = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 1)
    public void convertImage() throws ExecutionException, InterruptedException {
        imageCompression.applyNegativeMultithreading(numThreads);
        imageCompression.applyConvolutionMatrixMultithreading(numThreads);
    }

    @TearDown
    public void save() throws IOException {
        imageCompression.writePixelsInImage();
        imageCompression.saveImage( + numThreads + "_threads_neg_comp_" + "gervant_12800x9600");
        imageCompression = null;
    }
}
