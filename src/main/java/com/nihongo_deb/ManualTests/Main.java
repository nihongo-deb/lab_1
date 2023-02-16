package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 04.02.2023
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ImageCompression ic = new ImageCompression();
        ic.readeImageFromResources("gervant5000.jpg");
        ic.applyNegative();
    }
}
