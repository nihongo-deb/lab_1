package com.nihongo_deb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;

public class App {
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Fork(value = 1)
    @Measurement(iterations = 3)
    public void main() throws IOException {
        ImageCompression imageCompression = new ImageCompression();
        imageCompression.readeImageFromResources("gervant.jpg");
        imageCompression.applyConvolutionMatrix();
        imageCompression.saveImage("gervant-convoluted");
    }
}
