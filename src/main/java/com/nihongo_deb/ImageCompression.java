package com.nihongo_deb;

import com.nihongo_deb.Old.ImageCompressionMultithreading;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 14.02.2023
 */
public class ImageCompression {
    private BufferedImage image;
    private short [][][] pixels;
    private String extension;

    public ImageCompression(String fileName, boolean isFromResources){
        if (isFromResources)
            readeImageFromResources(fileName);
        else readImageFromAbsoluteFilePath(fileName);
        getImagePixels();
    }

    public ImageCompression() {

    }

    public void applyNegative(){
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int pixelLength = pixels[0][0].length;

        for (int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                for (int channel = 0; channel < pixelLength; channel++){
                    pixels[row][col][channel] = (short) (255 - pixels[row][col][channel]);
                }
            }
        }
    }

    public void applyNegative(int fromRow, int fromCol, int toRow, int toCol){
        final int pixelLength = pixels[0][0].length;

        for (int row = fromRow; row < toRow; row++){
            for (int col = fromCol; col < toCol; col++){
                for (int channel = 0; channel < pixelLength; channel++){
                    pixels[row][col][channel] = (short) (255 - pixels[row][col][channel]);
                }
            }
        }
    }

    public void applyNegativeMultithreading(int threadsNum) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        int widthDelta = image.getWidth() / threadsNum;
        int unexpectedPixelRows = image.getWidth() % threadsNum;
        int height = image.getHeight();

        for (int i = 0; i < threadsNum - 1; i++) {
            executorService.submit(
                    new NegativeImageRunner(
                            i * widthDelta,
                            0,
                            (i + 1) * widthDelta,
                            height
                    )
            );
        }

        executorService.submit(
                new NegativeImageRunner(
                        (threadsNum - 1) * widthDelta,
                        0,
                        threadsNum * widthDelta + unexpectedPixelRows,
                        height
                )
        );
        // запуск потоков (fork)
        executorService.shutdown();
        // ожидание выполнения всех потоков (join)
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    public void applyPixels(){
        int[] rasterFormatPixels = new int[image.getWidth() * image.getHeight() * pixels[0][0].length];
        WritableRaster raster = this.image.getRaster();

        int pixelIndex = 0;
        for (int row = 0; row < image.getHeight(); row++){
            for (int col = 0; col < image.getWidth(); col++){
                for (int channel = 0; channel < pixels[0][0].length; channel++){
                    rasterFormatPixels[pixelIndex] = pixels[row][col][channel];
                    pixelIndex++;
                }
            }
        }

        raster.setPixels(0,0, image.getWidth(), image.getHeight(), rasterFormatPixels);
        image.setData(raster);
    }

    private void getImagePixels() {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[] unifiedPixels;
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int pixelLength;

        if (hasAlphaChannel)
            pixelLength = 4;
        else
            pixelLength = 3;

        this.pixels = new short[height][width][pixelLength];
        unifiedPixels = image.getRaster().getPixels(0,0,width,height, new int[width * height * pixelLength]);
        for (int pixel = 0, row = 0, col = 0; pixel < unifiedPixels.length; pixel++){
            if (pixel > 1 && (pixel % pixelLength == 0))
                col++;

            if (col == width){
                col = 0;
                row++;
            }

            this.pixels[row][col][pixel % pixelLength] = (short) unifiedPixels[pixel];
        }
    }

    public void printPixels(){
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelLength = pixels[0][0].length;
        char[] channelsName = {'R', 'G', 'B', 'A'};


        for (int row = 0; row < height; row ++){
            for (int col = 0; col < width; col++){
                System.out.println("row: \t" + row + " col: \t" + col);
                for (int channel = 0; channel < pixelLength; channel++){
                    System.out.println(channelsName[channel] + ": \t" + pixels[row][col][channel]);
                }
            }
        }
    }

    public void readeImageFromResources(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream io = classLoader.getResourceAsStream(name)){
            this.extension = getExtensionOfFileByName(name);
            assert io != null;
            this.image = ImageIO.read(io);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (this.pixels == null)
            getImagePixels();
    }

    public void readImageFromAbsoluteFilePath(String absoluteFilePath){
        File file = new File(absoluteFilePath);
        try {
            this.extension = getExtensionOfFileByName(absoluteFilePath);
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (this.pixels == null)
            getImagePixels();
    }

    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(image, this.extension, new File(newFileName + '.' + this.extension));
    }

    private String getExtensionOfFileByName(String fileName){
        String extension = null;

        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0)
            extension = fileName.substring(lastIndex + 1);
        if (extension == null)
            throw new NullPointerException("File don't have extension");

        return extension;
    }

    public BufferedImage getImage() {
        return image;
    }

    public short[][][] getPixels() {
        return pixels;
    }

    private class NegativeImageRunner implements Runnable {
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;

        public NegativeImageRunner(int fromRow, int fromCol, int toRow, int toCol){
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
        }

        @Override
        public void run() {
            final int pixelLength = pixels[0][0].length;

            for (int row = fromRow; row < toRow; row++){
                for (int col = fromCol; col < toCol; col++){
                    for (int channel = 0; channel < pixelLength; channel++){
                        pixels[row][col][channel] = (short) (255 - pixels[row][col][channel]);
                    }
                }
            };
        }
    }
}
