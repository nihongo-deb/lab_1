package com.nihongo_deb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        ImageNegativeCompression imageNegativeCompression = new ImageNegativeCompression();
        imageNegativeCompression.readeImageFromResources("not-square-gervant.jpg");
        imageNegativeCompression.setNegative();
        imageNegativeCompression.saveImage("neg-not-square-gervant");
    }
}
