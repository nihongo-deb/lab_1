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
public class App_Aa_14_Threads_20480x15360 {
    private static ImageCompression imageCompression = new ImageCompression();
    private static int numThreads;
    private static String fileName = "gervant_20480x15360.jpg";
    {
        imageCompression.readeImageFromResources(fileName);
        numThreads = 14;
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
        imageCompression.setPixelsFromCopy();
        imageCompression.saveImage( + numThreads + "_threads_neg_comp_" + "gervant_20480x15360");
        imageCompression = null;
    }
}
