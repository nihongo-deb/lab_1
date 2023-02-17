package com.nihongo_deb.ManualTests;
import com.nihongo_deb.ImageCompression;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 04.02.2023
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ImageCompression imageCompression = new ImageCompression("gervant_1024×768.jpg", true);
        imageCompression.applyConvolutionMatrix(200,200, imageCompression.getImage().getWidth(), imageCompression.getImage().getHeight());
        imageCompression.writePixelsInImage();
        imageCompression.saveImage("neg");
    }
}
