package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;
import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 02.04.2023
 */
public class ManualTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        ImageCompression imageCompression = new ImageCompression("gervant.jpg", true);
        imageCompression.applyBinaryMultithreading(4, (short) 200);
        imageCompression.applyDilationMultithreading(4);
        imageCompression.writePixelsInImage();
        imageCompression.saveImage("pizdec");
    }
}
