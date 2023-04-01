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
public class App_Aa_16_Threads_10240x7680 {
    private static ImageCompression imageCompression = new ImageCompression();
    private static int numThreads;
    private static String fileName = "gervant_10240x7680.jpg";
    {
        imageCompression.readeImageFromResources(fileName);
        numThreads = 16;
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
        imageCompression.saveImage( + numThreads + "_threads_neg_comp_" + "gervant_10240x7680");
        imageCompression = null;
    }
}
