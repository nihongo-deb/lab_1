package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;

import javax.imageio.ImageIO;
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
        ImageCompression imageCompression = new ImageCompression("gervant_20480x15360.jpg", true);
        imageCompression.writePixelsInImage();
        imageCompression.saveImage("pizdec");
    }
}
