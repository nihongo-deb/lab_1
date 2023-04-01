package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;

import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 01.04.2023
 */
public class TestImageCompression {
    public static void main(String[] args) throws InterruptedException, IOException {
        ImageCompression imageCompression = new ImageCompression("gervant.jpg", true);
        imageCompression.applyNegativeMultithreading(4);
        imageCompression.setPixelsFromCopy();
        imageCompression.writePixelsInImage();
        imageCompression.saveImage("char is god");
    }
}
