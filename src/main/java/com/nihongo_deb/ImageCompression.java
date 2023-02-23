package com.nihongo_deb;

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
    private volatile short [][][] pixels;
    private short [][][] pixelsCopy;
    private String extension;

    private static short[][] convolutionMatrix = new short[3][3];
    static {
        /*
         0 -1  0
        -1  5 -1
         0 -1  0
        */
        convolutionMatrix[0][0] = (short) 0;
        convolutionMatrix[0][1] = (short) -1;
        convolutionMatrix[0][2] = (short) 0;

        convolutionMatrix[1][0] = (short) -1;
        convolutionMatrix[1][1] = (short) 5;
        convolutionMatrix[1][2] = (short) -1;

        convolutionMatrix[2][0] = (short) 0;
        convolutionMatrix[2][1] = (short) -1;
        convolutionMatrix[2][2] = (short) 0;
    }

    public ImageCompression(String fileName, boolean isFromResources){
        if (isFromResources)
            readeImageFromResources(fileName);
        else readImageFromAbsoluteFilePath(fileName);
        loadImagePixels();
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
                    pixels[col][row][channel] = (short) (255 - pixels[col][row][channel]);
                }
            }
        }

        refreshPixelsCopy();
    }

    public void applyNegative(int fromCol, int fromRow, int toCol, int toRow){
        final int pixelLength = pixels[0][0].length;

        for (int col = fromCol; col < toCol; col++){
            for (int row = fromRow; row < toRow; row++){
                for (int channel = 0; channel < pixelLength; channel++){
                    pixels[col][row][channel] = (short) (255 - pixels[col][row][channel]);
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

    public void applyConvolutionMatrix(){
        int pixelLength = pixels[0][0].length;
        // записываем пиксели изображения
        int width = this.image.getWidth();
        int height = this.image.getHeight();

        short[][][] newPixels = new short[width][height][pixelLength];

        // пробегаемся по пикселям нового изображения учитывая обрезку в 1 пиксель по краям
        for (int y = 1; y < height - 2; y++){
            for (int x = 1; x < width - 2; x++){
                short[][][] partParentPixels = new short[3][3][pixelLength]; //[ширина][высота][RGB]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y
                for (int py = 0; py < 3; py++){
                    for (int px = 0; px < 3; px++){
                        partParentPixels[px][py] = pixels[x - 1 + px][y - 1 + py].clone();
                    }
                }
                //будущий сконвертированный пиксель
                short[] convertedPixel = new short[pixelLength];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                short multiplySum = 0;
                for (int channel = 0; channel < pixelLength; channel++){
                    for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++){
                        for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++){
                            multiplySum += convolutionMatrix[hMatrix][wMatrix] * partParentPixels[hMatrix][wMatrix][channel];
                        }
                    }
                    // присваиваем значение R, G или B в новый пиксель
                    //TODO 01.02.2023 переписать логику присвоение максимального значения
                    if (multiplySum <= (short) 255)
                        convertedPixel[channel] = multiplySum;
                    else
                        convertedPixel[channel] = (short) 255;
                    multiplySum = (short) 0;
                }
                // сохраняем пиксель в WritableRaster нового изображения
                newPixels[x][y] = convertedPixel.clone();
            }
        }

        this.pixels = newPixels;
    }

    public void applyConvolutionMatrix(int fromCol, int fromRow, int toCol, int toRow){
        int pixelLength = pixels[0][0].length;

        short[][][] newPixels = new short[toCol - fromCol][toRow - fromRow][pixelLength];

        int noOffsetCol = 0;
        int noOffsetRow = 0;

        // пробегаемся по пикселям нового изображения учитывая обрезку в 1 пиксель по краям
        for (int x = fromCol; x < toCol; x++){
            if (x == 0)
                continue;
            if (x == image.getWidth() - 1)
                break;

            for (int y = fromRow; y < toRow; y++){
                if (y == 0)
                    continue;
                if (y == image.getHeight() - 1)
                    break;

                short[][][] partParentPixels = new short[3][3][pixelLength]; //[ширина][высота][RGB]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y

                for (int py = 0; py < 3; py++){
                    for (int px = 0; px < 3; px++){
                        partParentPixels[px][py] = pixels[x - 1 + px][y - 1 + py].clone();
                    }
                }
                //будущий сконвертированный пиксель
                short[] convertedPixel = new short[pixelLength];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                short multiplySum = 0;
                for (int channel = 0; channel < pixelLength; channel++){
                    for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++){
                        for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++){
                            multiplySum += convolutionMatrix[hMatrix][wMatrix] * partParentPixels[hMatrix][wMatrix][channel];
                        }
                    }
                    // присваиваем значение R, G или B в новый пиксель
                    //TODO 01.02.2023 переписать логику присвоение максимального значения
                    if (multiplySum <= (short) 255)
                        convertedPixel[channel] = multiplySum;
                    else
                        convertedPixel[channel] = (short) 255;
                    multiplySum = (short) 0;
                }
                // сохраняем пиксель в WritableRaster нового изображения
                newPixels[noOffsetCol][noOffsetRow] = convertedPixel.clone();
                noOffsetRow++;
            }
            noOffsetCol++;
            noOffsetRow = 0;
        }

        noOffsetCol = 0;
        noOffsetRow = 0;
        for (int x = fromCol; x < toCol; x++){
            if (x == 0)
                continue;
            if (x == image.getWidth() - 1)
                break;

            for (int y = fromRow; y < toRow; y++){
                if (y == 0)
                    continue;
                if (y == image.getHeight() - 1)
                    break;

                for (int channel = 0; channel < pixelLength; channel++){
                    this.pixelsCopy[x][y][channel] = newPixels[noOffsetCol][noOffsetRow][channel];
                }
                noOffsetRow++;
            }
            noOffsetCol++;
            noOffsetRow = 0;
        }
    }

    public void applyConvolutionMatrixMultithreading(int threadsNum) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        int widthDelta = image.getWidth() / threadsNum;
        int unexpectedPixelRows = image.getWidth() % threadsNum;
        int height = image.getHeight();

        for (int i = 0; i < threadsNum - 1; i++) {
            executorService.submit(
                    new ConvolutionMatrixImageRunner(
                            i * widthDelta,
                            0,
                            (i + 1) * widthDelta,
                            height
                    )
            );
        }

        executorService.submit(
                new ConvolutionMatrixImageRunner(
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
        setPixelsFromCopy();
    }

    public void applyBinary(int fromCol, int fromRow, int toCol, int toRow, short threshold){
        final int pixelLength = pixels[0][0].length;
        short middleIntensity = 0;
        short channelValue;

        for (int col = fromCol; col < toCol; col++){
            for (int row = fromRow; row < toRow; row++){
                for (int channel = 0; channel < pixelLength; channel++){
                    middleIntensity += pixels[col][row][channel] / pixelLength;
                }

                if (middleIntensity < threshold)
                    channelValue = 0;
                else
                    channelValue = 255;

                for (int channel = 0; channel < pixelLength; channel++){
                    pixelsCopy[col][row][channel] = channelValue;
                }

                middleIntensity = 0;
            }
        }
    }

    public void applyBinaryMultithreading(int threadsNum, short threshold) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        int widthDelta = image.getWidth() / threadsNum;
        int unexpectedPixelRows = image.getWidth() % threadsNum;
        int height = image.getHeight();

        for (int i = 0; i < threadsNum - 1; i++) {
            executorService.submit(
                    new BinaryImageRunner(
                            i * widthDelta,
                            0,
                            (i + 1) * widthDelta,
                            height,
                            threshold
                    )
            );
        }

        executorService.submit(
                new BinaryImageRunner(
                        (threadsNum - 1) * widthDelta,
                        0,
                        threadsNum * widthDelta + unexpectedPixelRows,
                        height,
                        threshold
                )
        );
        // запуск потоков (fork)
        executorService.shutdown();
        // ожидание выполнения всех потоков (join)
        executorService.awaitTermination(1, TimeUnit.HOURS);
        setPixelsFromCopy();
    }

    public void writePixelsInImage(){
        int[] rasterFormatPixels = new int[image.getWidth() * image.getHeight() * pixels[0][0].length];
        WritableRaster raster = this.image.getRaster();

        int pixelIndex = 0;
        for (int row = 0; row < image.getHeight(); row++){
            for (int col = 0; col < image.getWidth(); col++){
                for (int channel = 0; channel < pixels[0][0].length; channel++){
                    rasterFormatPixels[pixelIndex] = pixels[col][row][channel];
                    pixelIndex++;
                }
            }
        }

        raster.setPixels(0,0, image.getWidth(), image.getHeight(), rasterFormatPixels);
        image.setData(raster);
    }

    private void loadImagePixels() {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int[] unifiedPixels;
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int pixelLength;

        if (hasAlphaChannel)
            pixelLength = 4;
        else
            pixelLength = 3;

        this.pixels = new short[width][height][pixelLength];
        unifiedPixels = image.getRaster().getPixels(0,0,width,height, new int[width * height * pixelLength]);
        for (int pixel = 0, row = 0, col = 0; pixel < unifiedPixels.length; pixel++){
            if (pixel > 1 && (pixel % pixelLength == 0))
                col++;

            if (col == width){
                col = 0;
                row++;
            }

            this.pixels[col][row][pixel % pixelLength] = (short) unifiedPixels[pixel];
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
                    System.out.println(channelsName[channel] + ": \t" + pixels[col][row][channel]);
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
            loadImagePixels();
        refreshPixelsCopy();
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
            loadImagePixels();
        refreshPixelsCopy();
    }

    public void refreshPixelsCopy(){
        int pixelLength = this.pixels[0][0].length;

        this.pixelsCopy = new short[this.image.getWidth()][this.image.getHeight()][pixelLength];

        for (int col = 0; col < this.image.getWidth(); col++){
            for (int row = 0; row < this.image.getHeight(); row++){
                this.pixelsCopy[col][row] = this.pixels[col][row].clone();
            }
        }
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

    public void setPixelsFromCopy(){
        this.pixels = this.pixelsCopy;
    }

    private class NegativeImageRunner implements Runnable {
        private int fromCol;
        private int fromRow;
        private int toRow;
        private int toCol;

        public NegativeImageRunner(int fromCol, int fromRow, int toCol, int toRow){
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toRow = toRow;
            this.toCol = toCol;
        }

        @Override
        public void run() {
            applyNegative(fromCol, fromRow, toCol, toRow);
        }
    }

    private class ConvolutionMatrixImageRunner implements Runnable {
        private int fromCol;
        private int fromRow;
        private int toRow;
        private int toCol;

        public ConvolutionMatrixImageRunner(int fromCol, int fromRow, int toCol, int toRow){
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toRow = toRow;
            this.toCol = toCol;
        }

        @Override
        public void run() {
            applyConvolutionMatrix(fromCol, fromRow, toCol, toRow);
        }
    }

    private class BinaryImageRunner implements Runnable{
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;
        private short threshold;

        public BinaryImageRunner(int fromCol, int fromRow, int toCol, int toRow, short threshold) {
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toCol = toCol;
            this.toRow = toRow;
            this.threshold = threshold;
        }

        @Override
        public void run() {
            applyBinary(fromCol, fromRow, toCol, toRow, threshold);
        }
    }
}
