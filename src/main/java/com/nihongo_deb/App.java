package com.nihongo_deb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        ImageCompression imageCompression = new ImageCompression();
        imageCompression.readeImageFromResources("gervant.jpg");
        imageCompression.setNegative();
        imageCompression.saveImage("test");

        ImageIO.write(imageCompression.getImageConversionIterations().get(0), "jpg", new File("parent-test.jpg"));

    }
}
