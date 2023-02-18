package com.nihongo_deb.BenchmarkedApps.multithreading;

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
public class App_Multithreading {
    private static final ImageCompression imageCompression = new ImageCompression();
    private static int numThreads;
    {
        imageCompression.readeImageFromResources("gervant_2048×1536.jpg");
        numThreads = 8;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 2)
    @Fork(value = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 1)
    public void convertImage() throws ExecutionException, InterruptedException {
        imageCompression.applyConvolutionMatrixMultithreading(numThreads);
    }

    @TearDown
    public void save() throws IOException {
        imageCompression.saveImage("multithreading");
    }
}
