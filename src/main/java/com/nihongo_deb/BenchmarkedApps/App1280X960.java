package com.nihongo_deb.BenchmarkedApps;

import com.nihongo_deb.ImageCompression;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
@State(Scope.Benchmark)
public class App1280X960 {
    private static final ImageCompression imageCompression = new ImageCompression();
    {
        imageCompression.readeImageFromResources("gervant_1280x960.jpg");
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
        imageCompression.saveImage("conv-gervant_1280x960");
    }
}
