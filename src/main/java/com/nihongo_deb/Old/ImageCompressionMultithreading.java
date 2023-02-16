package com.nihongo_deb.Old;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.02.2023
 */

public class ImageCompressionMultithreading {
    /** переменная, в которой будет храниться расширение изображения-родителя */
    private String fileExtension;
    /** двусвязанный список, в котором храниться буфер каждого изменения изображения*/
    private BufferedImage image;
    /** метрица-ядро преобразования (увеличение контраста) */
    private static int[][] convolutionMatrix = new int [3][3];
    static {
        /*
         0 -1  0
        -1  5 -1
         0 -1  0
        */
        convolutionMatrix[0][0] = 0;
        convolutionMatrix[0][1] = -1;
        convolutionMatrix[0][2] = 0;

        convolutionMatrix[1][0] = -1;
        convolutionMatrix[1][1] = 5;
        convolutionMatrix[1][2] = -1;

        convolutionMatrix[2][0] = 0;
        convolutionMatrix[2][1] = -1;
        convolutionMatrix[2][2] = 0;
    }

    public ImageCompressionMultithreading(){}

    /**
     * Конструктор с загрузкой изображения
     * @param fileName имя файла, который требуется загрузить
     * @param isFromResources вид получения изображения <br/>
     * <ul>
     *      <li> true - изображение будет взято по имени из resouces.
     *      <li> false - изображение будет взято по абсолютному пути.
     * </ul>
     */
    public ImageCompressionMultithreading(String fileName, boolean isFromResources){
        if (isFromResources)
            readeImageFromResources(fileName);
        else readImageFromAbsoluteFilePath(fileName);
    }

    /**
     * Метод для загрузки изображение из resources
     * @param name имя файла, который требуется загрузить
     */
    public void readeImageFromResources(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream io = classLoader.getResourceAsStream(name)){
            this.fileExtension = getExtensionOfFileByName(name);

            assert io != null;
            this.image = ImageIO.read(io);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для загрузки изображение по абсолютному пути <br/>
     * @param absoluteImageFilePath путь до файла, который требуется загрузить
     */
    public void readImageFromAbsoluteFilePath(String absoluteImageFilePath){
        File file = new File(absoluteImageFilePath);
        try {
            this.fileExtension = getExtensionOfFileByName(absoluteImageFilePath);
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для сохранения изображения в отдельный файл. <br/>
     *
     * @param newFileName путь до файла, который требуется загрузить
     */
    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(image, this.fileExtension, new File(newFileName + '.' + this.fileExtension));
    }

    /**
     * Метод для инвертирования изображения <br/>
     * <br/>
     * @param image изображение, которое будет инвертировано
     */
    private void negative(BufferedImage image, int fromX, int fromY, int toX, int toY){
        // получает WritableRaster с этого изображения, для доступа к цветовым каналам
        WritableRaster writableRaster = image.getRaster();

        // проходимся попиксельно по изображению
        for (int x = fromX; x < toX; x++){
            for (int y = fromY; y < toY; y++){
                //System.out.println("ThreadID: \t" + Thread.currentThread().getId());
                // получаем пиксель с координатами 'x' и 'y' и вид пикселя
                // new int[4] => вид будет RGBA
                int[] pixel = writableRaster.getPixel(x, y, new int[4]);
                // цикл инвертирования пикселя (color_value = 255 - color_value)
                for (int RGBIndex = 0; RGBIndex < pixel.length - 1; RGBIndex++){
                    pixel[RGBIndex] = 255 - pixel[RGBIndex];
                }
                // перезаписываем изменённый пиксель
                writableRaster.setPixel(x, y, pixel);
            }
        }
        // перезаписываем старый writableRaster на новый (с инвертированными пикселями)
        image.setData(writableRaster);
    }

    /**
     * Метод для многопоточного инвертирования изображения <br/>
     * <br/>
     * @param threadsNum количество потоков, которое будет задействовано для инвертирования
     * @return возвращает вертикальные куски изображения, кол-во кусков равно кол-ву задействованных потоков (threadsNum)
     */
    public void applyNegative(int threadsNum) throws InterruptedException, ExecutionException {
        // пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);

        BufferedImage negBufferedImage = deepCopy(this.image);

        int widthDelta = negBufferedImage.getWidth() / threadsNum;
        int unexpectedPixelRows = negBufferedImage.getWidth() % threadsNum;
        int height = negBufferedImage.getHeight();

        for (int i = 0; i < threadsNum - 1; i++) {
            executorService.submit(
                    new NegativeImageRunner(
                            negBufferedImage,
                            i * widthDelta,
                            0,
                            (i + 1) * widthDelta,
                            height
                    )
            );
        }
        executorService.submit(
                new NegativeImageRunner(
                        negBufferedImage,
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
        // перезаписываем массив изменёнными (инвертированными) изображениями
        this.image = negBufferedImage;
    }

    /**
     * Метод для свёртки изображения <br/>
     */
    public void applyConvolutionMatrix(){
        // копируем старое изображение, теперь это будущее новое изображение
        BufferedImage newBufferedImage = deepCopy(this.image);
        // берем от будущего нового изображение WritableRaster (объект, в котором можно манипулировать пикселями изображения)
        WritableRaster newWritableRaster = newBufferedImage.getRaster();
        // берем WritableRaster от изображения-родителя (последнее изображение в LinkedList imageConversionIterations)
        WritableRaster parentWritableRaster = this.image.getRaster();

        // записываем пиксели изображения
        int width = parentWritableRaster.getWidth();
        int height = parentWritableRaster.getHeight();
        // пробегаемся по пикселям нового изображения учитывая обрезку в 1 пиксель по краям
        for (int y = 1; y < height - 2; y++){
            for (int x = 1; x < width - 2; x++){
                // кусок пикселей из изображения-родителя,
                // от куда мы будем брать исходные значения пикселей для перемножения
                int[][][] partParentPixels = new int[3][3][4]; //[ширина][высота][RGBA]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y
                for (int py = 0; py < 3; py++){
                    for (int px = 0; px < 3; px++){
                        partParentPixels[px][py] = parentWritableRaster.getPixel(x - 1 + px, y - 1 + py, new int[4]);
                    }
                }

                //будущий сконвертированный пиксель
                int[] convertedPixel = new int[4];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                int multiplySum = 0;
                for (int RGBIndex = 0; RGBIndex < 3; RGBIndex++) {
                    for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++) {
                        for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++) {
                            multiplySum += convolutionMatrix[wMatrix][hMatrix] * partParentPixels[wMatrix][hMatrix][RGBIndex];
                        }
                    }
                    // присваиваем значение R, G или B в новый пиксель
                    //TODO 01.02.2023 переписать логику присвоение максимального значения
                    if (multiplySum <= 255)
                        convertedPixel[RGBIndex] = multiplySum;
                    else
                        convertedPixel[RGBIndex] = 255;
                    multiplySum = 0;
                }
                // сохраняем пиксель в WritableRaster нового изображения
                newWritableRaster.setPixel(x, y, convertedPixel);
            }
        }
        // присваиваем все сконвертированные пиксели изображению
        newBufferedImage.setData(newWritableRaster);
        // добавляем изображение в LinkedList
        this.image = newBufferedImage;
    }

    /**
     * Геттер {@link ImageCompressionMultithreading#image}
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Сеттер {@link ImageCompressionMultithreading#image}
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Приватный метод для клонирования объекта класса {@link BufferedImage}
     *
     * @param bi объект, который нужно склонировать
     * @return копию входного объекта
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        final ColorModel cm = bi.getColorModel();
        final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Приватный метод для получения расширения изображения-родителя
     *
     * @param fileName название файла, включающее родителя
     * @return String - расширение файла родителя
     */
    private String getExtensionOfFileByName(String fileName){
        String extension = null;

        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0)
            extension = fileName.substring(lastIndex + 1);
        if (extension == null)
            throw new NullPointerException("File don't have extension");

        return extension;
    }

    /**
     * Класс, объекты которого будут переводить изображения в отдельном потоке и возвращать инвертированное изображение
     */
    private class NegativeImageRunner implements Runnable {
        private BufferedImage image;
        private int fromX;
        private int fromY;
        private int toX;
        private int toY;

        public NegativeImageRunner(BufferedImage image, int fromX, int fromY, int toX, int toY){
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.image = image;
        }

        @Override
        public void run() {
            negative(image, fromX, fromY, toX, toY);
        }
    }
}