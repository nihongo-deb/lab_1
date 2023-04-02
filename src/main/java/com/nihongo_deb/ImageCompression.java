package com.nihongo_deb;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
    /**
     * Буфер изображения
     */
    private BufferedImage image;
    /**
     * Текущие пиксели изображения.
     * Для избежания состояния-гонки, из этого массива данные копируются и помещаются в {@link ImageCompression#pixelsCopy}
     */
    private char [][][] pixels;
    /**
     * Слепок пикселей изображения, который будет изменяться и при окончании изменения будет помещаться в {@link ImageCompression#pixels}
     */
    private char [][][] pixelsCopy;
    /** Переменная, в которой будет храниться расширение изображения-родителя */
    private String extension;
    /** Матрица-ядро преобразования (увеличение контраста) */
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

    /**
     * Конструктор с загрузкой изображения
     * @param fileName имя файла, который требуется загрузить
     * @param isFromResources вид получения изображения <br/>
     * <ul>
     *      <li> true - изображение будет взято по имени из resouces.
     *      <li> false - изображение будет взято по абсолютному пути.
     * </ul>
     */
    public ImageCompression(String fileName, boolean isFromResources){
        if (isFromResources)
            readeImageFromResources(fileName);
        else readImageFromAbsoluteFilePath(fileName);
    }

    public ImageCompression() {

    }

    /**
     * Метод для инвертирования изображения целиком
     */
    public void applyNegative(){
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int pixelLength = pixels[0][0].length;

        for (int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                for (int channel = 0; channel < pixelLength; channel++){
                    pixels[col][row][channel] = (char) (255 - pixels[col][row][channel]);
                }
            }
        }

        refreshPixelsCopy();
    }

    /**
     * Метод для инвертирования части изображения
     */
    public void applyNegative(int fromCol, int fromRow, int toCol, int toRow){
        final int pixelLength = pixels[0][0].length;

        for (int col = fromCol; col < toCol; col++){
            for (int row = fromRow; row < toRow; row++){
                for (int channel = 0; channel < pixelLength; channel++){
                    pixels[col][row][channel] = (char) (255 - pixels[col][row][channel]);
                }
            }
        }
    }

    /**
     * Метод многопоточного инвертирования изображения.
     * Каждому потоку отдаётся своя часть изображения, которую он инвертирует и записывает в
     * {@link ImageCompression#pixelsCopy}
     * @param threadsNum количество потоков, используемые для инвертирования изображения
     */
    public void applyNegativeMultithreading(int threadsNum) throws InterruptedException {
        // пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        // длинна куска изображения, который поступит в поток на обработку
        int widthDelta = image.getWidth() / threadsNum;
        // остаток от деления длинны изображения на кол-во потоков
        // (пиксели которые не попали ни к одному потоку,
        // они пойдут на обработку последнему потоку)
        int unexpectedPixelRows = image.getWidth() % threadsNum;
        int height = image.getHeight();

        // передача обязанностей потокам от 1-го до threadsNum - 1
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
        // передача обязанности потоку с индексом threadsNum
        executorService.submit(
                new NegativeImageRunner(
                        (threadsNum - 1) * widthDelta,
                        0,
                        threadsNum * widthDelta + unexpectedPixelRows,
                        height
                )
        );
        // указываем, что потокам более задачи поступать не будут
        executorService.shutdown();
        // ожидание выполнения всех потоков (join)
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * Метода для свертки изображения с фильтром (повышение контраста) <br>
     * Ядро преобразования:<br>
     *  {@code 0 -1   0} <br>
     *  {@code -1  5 -1} <br>
     *  {@code 0 -1   0} <br>
     */
    public void applyConvolutionMatrix(){
        int pixelLength = pixels[0][0].length;
        int width = this.image.getWidth();
        int height = this.image.getHeight();

        char[][][] newPixels = new char[width][height][pixelLength];

        // пробегаемся по пикселям нового изображения учитывая обрезку в 1 пиксель по краям
        for (int y = 1; y < height - 2; y++){
            for (int x = 1; x < width - 2; x++){
                char[][][] partParentPixels = new char[3][3][pixelLength]; //[ширина][высота][RGB]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y
                for (int py = 0; py < 3; py++){
                    for (int px = 0; px < 3; px++){
                        partParentPixels[px][py] = pixels[x - 1 + px][y - 1 + py].clone();
                    }
                }
                //будущий сконвертированный пиксель
                char[] convertedPixel = new char[pixelLength];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                char multiplySum = 0;
                for (int channel = 0; channel < pixelLength; channel++){
                    for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++){
                        for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++){
                            multiplySum += convolutionMatrix[hMatrix][wMatrix] * partParentPixels[hMatrix][wMatrix][channel];
                        }
                    }
                    // присваиваем значение R, G или B в новый пиксель
                    //TODO 01.02.2023 переписать логику присвоение максимального значения
                    if (multiplySum <= (char) 255)
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
    /**
     * Метода для свертки ЧАСТИ изображения с фильтром (повышение контраста) <br>
     * Используется в многопоточной свёртке изображения {@link ImageCompression#applyConvolutionMatrixMultithreading}
     * <br>
     * Ядро преобразования:<br>
     *  {@code 0 -1   0} <br>
     *  {@code -1  5 -1} <br>
     *  {@code 0 -1   0} <br>
     * @param fromCol с какого столбца пикселей
     * @param fromRow с какой строки пикселей
     * @param toCol по какой столбец пикселей
     * @param toRow по какую строку пикселей
     */
    public void applyConvolutionMatrix(int fromCol, int fromRow, int toCol, int toRow){
        int pixelLength = pixels[0][0].length;

        char[][][] newPixels = new char[toCol - fromCol][toRow - fromRow][pixelLength];

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

                char[][][] partParentPixels = new char[3][3][pixelLength]; //[ширина][высота][RGB]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y

                for (int py = 0; py < 3; py++){
                    for (int px = 0; px < 3; px++){
                        partParentPixels[px][py] = pixels[x - 1 + px][y - 1 + py].clone();
                    }
                }
                //будущий сконвертированный пиксель
                char[] convertedPixel = new char[pixelLength];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                char multiplySum = 0;
                for (int channel = 0; channel < pixelLength; channel++){
                    for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++){
                        for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++){
                            multiplySum += convolutionMatrix[hMatrix][wMatrix] * partParentPixels[hMatrix][wMatrix][channel];
                        }
                    }
                    // присваиваем значение R, G или B в новый пиксель
                    //TODO 01.02.2023 переписать логику присвоение максимального значения
                    if (multiplySum <= (char) 255)
                        convertedPixel[channel] = multiplySum;
                    else
                        convertedPixel[channel] = (char) 255;
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
    /**
     * Метод многопоточной свёртки изображения.
     * Каждому потоку отдаётся своя часть изображения, которую он преобразует и записывает в
     * {@link ImageCompression#pixelsCopy}
     * @param threadsNum количество потоков, используемые для инвертирования изображения
     * @see ImageCompression#applyConvolutionMatrix(int fromCol, int fromRow, int toCol, int toRow)
     */
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

    /**
     * Метод конвертирования ЧАСТИ изображение в бинарное изображение,
     * то есть значение пикселя либо {@code (255, 255, 255)}, либо {@code (0, 0, 0)}.
     * Метод в основном использует в многопоточной конвертации изображения - <br>
     * {@link ImageCompression#applyBinaryMultithreading(int threadNum, short threshold)}
     *
     * @param fromCol с какого столбца пикселей
     * @param fromRow с какой строки пикселей
     * @param toCol по какой столбец пикселей
     * @param toRow по какую строку пикселей
     */
    public void applyBinary(int fromCol, int fromRow, int toCol, int toRow, short threshold){
        final int pixelLength = pixels[0][0].length;
        short middleIntensity = 0;
        char channelValue;

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
    /**
     * Метод многопоточного конвертирования изображение в бинарное изображение.
     * Каждому потоку отдаётся своя часть изображения, которую он преобразует и записывает в
     * {@link ImageCompression#pixelsCopy}
     * @param threadsNum количество потоков, используемые для инвертирования изображения
     * @see ImageCompression#applyBinary(int fromCol, int fromRow, int toCol, int toRow, short treshold)
     */
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

    /**
     * Метод наращивания ЧАСТИ изображения.
     * Используется после выполнения метода конвертации изображение в бинарное изображение <br>
     * {@link ImageCompression#applyBinary(int fromCol, int fromRow, int toCol, int toRow, short threshold)}.
     * Метод в основном используется в многопоточном наращивании: <br>
     * {@link ImageCompression#applyDilationMultithreading(int threadNum)}
     * @param fromCol с какого столбца пикселей
     * @param fromRow с какой строки пикселей
     * @param toCol по какой столбец пикселей
     * @param toRow по какую строку пикселей
     */
    public void applyDilation(int fromCol, int fromRow, int toCol, int toRow){
        final int pixelLength = pixels[0][0].length;

        for (int col = fromCol; col < toCol; col++){
            for (int row = fromRow; row < toRow; row++){
                if (pixels[col][row][0] > 0)
                    continue;
                for (int partCol = col - 1; partCol < col + 2; partCol++){
                    if (partCol < 0 || partCol >= toCol)
                        continue;
                    for (int partRow = row - 1; partRow < row + 2; partRow++){
                        if (partRow < 0 || partRow >= toRow)
                            continue;
                        if (partRow == row && partCol == col)
                            continue;

                        if (pixels[partCol][partRow][0] != 0) {
                            for (int channel = 0; channel < pixelLength; channel++){
                                pixelsCopy[col][row][channel] = 255;
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Метод многопоточного наращивания изображение, обычно выполняется после метода
     * {@link ImageCompression#applyBinary} или {@link ImageCompression#applyBinaryMultithreading}.
     * Каждому потоку отдаётся своя часть изображения, которую он преобразует и записывает в
     * {@link ImageCompression#pixelsCopy}
     * @param threadsNum количество потоков, используемые для инвертирования изображения
     * @see ImageCompression#applyBinary(int fromCol, int fromRow, int toCol, int toRow, short treshold)
     */
    public void applyDilationMultithreading(int threadsNum) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        int widthDelta = image.getWidth() / threadsNum;
        int unexpectedPixelRows = image.getWidth() % threadsNum;
        int height = image.getHeight();

        for (int i = 0; i < threadsNum - 1; i++) {
            executorService.submit(
                    new DilatationBinaryImageRunner(
                            i * widthDelta,
                            0,
                            (i + 1) * widthDelta,
                            height
                    )
            );
        }

        executorService.submit(
                new DilatationBinaryImageRunner(
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

    /**
     * Метод перезаписи пикселей в буфере изображения
     */
    public void writePixelsInImage(){
//        int[] rasterFormatPixels = new int[image.getWidth() * image.getHeight() * pixels[0][0].length];
//        WritableRaster raster = this.image.getRaster();

//        int pixelIndex = 0;
        int argb = 0;
        for (int col = 0; col < image.getHeight(); col++){
            for (int row = 0; row < image.getWidth(); row++){
//                argb = 255 << 24 + (int)pixels[row][col][0] << 16 + (int)pixels[row][col][1] << 8 + (int)pixels[row][col][2];

                image.setRGB(row, col, new Color((int)pixels[row][col][0], (int)pixels[row][col][1], (int)pixels[row][col][2]).getRGB());
            }
        }

//        raster.setPixels(0,0, image.getWidth(), image.getHeight(), rasterFormatPixels);
//        image.setData(raster);
    }

    /**
     * Метод запись значений пикселей в локальный массив, с которым будут проводиться операции по конвертированию изображения.
     * После выполнений всех необходимых конвертаций, значение пикселей будут записаны в буфер изображения
     */
    private void loadImagePixels() {
        final int width = image.getWidth();
        final int height = image.getHeight();
        byte[] unifiedPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int pixelLength;

        if (hasAlphaChannel)
            pixelLength = 4;
        else
            pixelLength = 3;
//        System.out.println(hasAlphaChannel);
        this.pixels = new char[width][height][pixelLength];

        for (int pixel = 0, row = 0, col = 0; pixel + 3 < unifiedPixels.length; pixel++){
            this.pixels[col][row][pixel % pixelLength] = (char)(unifiedPixels[pixel] & 0xff);
            if (pixel != 0 && pixel % 3 == 0)
                col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Метода вывода значений пикселей в консоль
     */
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

    /**
     * Метод для загрузки изображение из resources
     * @param name имя файла, который требуется загрузить
     */
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
    /**
     * Метод для загрузки изображение по абсолютному пути <br/>
     * @param absoluteFilePath путь до файла, который требуется загрузить
     */
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

    /**
     * Метод для обновления копии пикселей изображения, обычно вызывается сразу после загрузки изображения.
     */
    public void refreshPixelsCopy(){
        int pixelLength = this.pixels[0][0].length;

        this.pixelsCopy = null;
        this.pixelsCopy = new char[this.image.getWidth()][this.image.getHeight()][pixelLength];

        for (int col = 0; col < this.image.getWidth(); col++){
            for (int row = 0; row < this.image.getHeight(); row++){
                for (int RGBIndex = 0; RGBIndex < pixelLength; RGBIndex++)
                    this.pixelsCopy[col][row][RGBIndex] = this.pixels[col][row][RGBIndex];
                }
            }
        }


    /**
     * Метод для сохранения изображения. <br/>
     * Сохраняет последнее изменение<br/>
     *
     * @param newFileName путь до файла, который требуется загрузить
     */
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

    public char[][][] getPixels() {
        return pixels;
    }

    /**
     * Метод для обновления {@link ImageCompression#pixels}. Новые значения будут браться из {@link ImageCompression#pixelsCopy}.
     * Метод обычно вызывается сразу после конвертации изображения, чтобы в {@link ImageCompression#pixels} хранилась актуальная информация
     * об значении пикселей для будущих конвертаций или сохранения изображения.
     */
    public void setPixelsFromCopy(){
        int pixelLength = pixels[0][0].length;

        for (int col = 0; col < image.getWidth(); col++){
            for (int row = 0; row < image.getHeight(); row++){
                for (int channel = 0; channel < pixelLength; channel++){
                    this.pixels[col][row] = this.pixelsCopy[col][row].clone();
                }
            }
        }
    }

    /**
     * Класс-поток для перевода инвертирования части изображения.
     * @see ImageCompression#applyNegativeMultithreading(int threadNum)
     */
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

    /**
     * Класс-поток для свёртки части изображения.
     * @see ImageCompression#applyNegativeMultithreading(int threadNum)
     */
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

    /**
     * Класс-поток для конвертирования части изображения в бинарное изображения.
     * @see ImageCompression#applyBinaryMultithreading(int threadNum, short trashold)
     */
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

    /**
     * Класс-поток для наращивания части изображения.
     * @see ImageCompression#applyDilationMultithreading(int threadNum)
     */
    private class DilatationBinaryImageRunner implements Runnable{
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;

        public DilatationBinaryImageRunner(int fromCol, int fromRow, int toCol, int toRow) {
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toCol = toCol;
            this.toRow = toRow;
        }

        @Override
        public void run() {
            applyDilation(fromCol, fromRow, toCol, toRow);
        }
    }
}