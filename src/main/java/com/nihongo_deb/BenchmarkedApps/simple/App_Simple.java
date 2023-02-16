package com.nihongo_deb.BenchmarkedApps.simple;

import com.nihongo_deb.ImageCompression;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 14.02.2023
 */
@State(Scope.Benchmark)
public class App_Simple {
    private static final ImageCompression imageCompression = new ImageCompression();
    {
        imageCompression.readeImageFromResources("huge-gervant-simple.jpg");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 2)
    @Fork(value = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 1)
    public void convertImage(){
        imageCompression.applyNegative();
    }

//    @TearDown
//    public void save() throws IOException {
//        imageCompression.saveImage("simple_les");
//    }
}
