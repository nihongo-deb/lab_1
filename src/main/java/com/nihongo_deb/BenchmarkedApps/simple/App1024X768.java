package com.nihongo_deb.BenchmarkedApps.simple;

import com.nihongo_deb.ImageCompression;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 04.02.2023
 */
@State(Scope.Benchmark)
public class App1024X768 {
    private static final ImageCompression imageCompression = new ImageCompression();
    {
        imageCompression.readeImageFromResources("gervant_1024×768.jpg");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Fork(value = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3)
    public void convertImage(){
        imageCompression.applyNegative();
        imageCompression.applyConvolutionMatrix();
    }

    @TearDown
    public void save() throws IOException {
        imageCompression.saveImage("conv-gervant_1024×768");
    }
}
