package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 02.04.2023
 */
public class ManualTest {
    public static void main(String[] args) throws IOException {
        ImageCompression imageCompression = new ImageCompression("gervant.jpg", true);


        BufferedImage image = imageCompression.getImage();

        Color pixel = new Color(image.getRGB(0,0));
        System.out.println("from image");
        System.out.println("R = " + pixel.getRed());
        System.out.println("G = " + pixel.getGreen());
        System.out.println("B = " + pixel.getBlue());
        System.out.println("-------");
        System.out.println("from char array");
        System.out.println("R = " + (int) imageCompression.getPixels()[0][0][0]);
        System.out.println("G = " + (int) imageCompression.getPixels()[0][0][1]);
        System.out.println("B = " + (int) imageCompression.getPixels()[0][0][2]);



        imageCompression.writePixelsInImage();
        imageCompression.saveImage("pizdec");
    }
}
