package com.nihongo_deb;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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
    private volatile List<BufferedImage> imageConversionIterations = new LinkedList<>();
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
     * Перед загрузкой {@link ImageCompressionMultithreading#imageConversionIterations} будет предварительно отчищен
     * @param name имя файла, который требуется загрузить
     */
    public void readeImageFromResources(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream io = classLoader.getResourceAsStream(name)){
            this.fileExtension = getExtensionOfFileByName(name);

            assert io != null;
            BufferedImage bufferedImage = ImageIO.read(io);
            imageConversionIterations.clear();
            imageConversionIterations.add(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Метод для загрузки изображение по абсолютному пути <br/>
     * Перед загрузкой {@link ImageCompressionMultithreading#imageConversionIterations} будет предварительно отчищен
     * @param absoluteImageFilePath путь до файла, который требуется загрузить
     */
    public void readImageFromAbsoluteFilePath(String absoluteImageFilePath){
        File file = new File(absoluteImageFilePath);
        try {
            this.fileExtension = getExtensionOfFileByName(absoluteImageFilePath);

            BufferedImage bufferedImage = ImageIO.read(file);
            imageConversionIterations.clear();
            imageConversionIterations.add(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для сохранения изображения. <br/>
     * Сохраняет последнее изменение (последний элемент из {@link ImageCompressionMultithreading#imageConversionIterations})<br/>
     *
     * @param newFileName путь до файла, который требуется загрузить
     */
    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(imageConversionIterations.get(imageConversionIterations.size() - 1), this.fileExtension, new File(newFileName + '.' + this.fileExtension));
    }

    /**
     * Метод для сохранения изображения. <br/>
     * Сохраняет последнее изменение (последний элемент из {@link ImageCompressionMultithreading#imageConversionIterations}). <br/>
     * Расширение файла родительское.
     * @param newFileName путь до файла, который требуется загрузить
     * @param extension расширение сохраняемого изображения
     */
    public void saveImage(String newFileName, String extension) throws IOException {
        BufferedImage lastConversionBufferedImage = imageConversionIterations.get(imageConversionIterations.size() - 1);
        ImageIO.write(lastConversionBufferedImage, extension, new File(newFileName + extension));
    }

    /**
     * Метод для инвертирования изображения <br/>
     * При этом родительское изображение или последний буфер изображения в
     * {@link ImageCompressionMultithreading#imageConversionIterations} не изменяется
     * <br/>
     * После инвертирования новый буфер добавляется в {@link ImageCompressionMultithreading#imageConversionIterations}
     */
    public void applyNegative() {
        BufferedImage newConvertedBufferedImage;
        if (imageConversionIterations.isEmpty()){
            throw new NullPointerException("load file correctly");
        } else {
            newConvertedBufferedImage = getNegativeImage(imageConversionIterations.get(imageConversionIterations.size() - 1));
            this.imageConversionIterations.add(newConvertedBufferedImage);
        }
    }
    /**
     * Метод для инвертирования изображения <br/>
     * <br/>
     * @param image изображение, которое будет инвертировано
     * @return инвертированное изображение
     */
    public BufferedImage getNegativeImage(BufferedImage image){
        // создаём копию изображение, которое будем инвертировать
        BufferedImage negativeImage = deepCopy(image);
        // получает WritableRaster с этого изображения, для доступа к цветовым каналам
        WritableRaster writableRaster = negativeImage.getRaster();

        // проходимся попиксельно по изображению
        for (int x = 0; x < writableRaster.getWidth(); x++){
            for (int y = 0; y < writableRaster.getHeight(); y++){
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
        negativeImage.setData(writableRaster);
        return negativeImage;
    }

    /**
     * Метод для многопоточного инвертирования изображения <br/>
     * <br/>
     * @param threadsNum количество потоков, которое будет задействовано для инвертирования
     * @return возвращает вертикальные куски изображения, кол-во кусков равно кол-ву задействованных потоков (threadsNum)
     */
    public BufferedImage[] applyNegativeMultithreading(int threadsNum) throws InterruptedException, ExecutionException {
        // пул потоков
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
        // массив из кусков родительского изображения (последнего изображения в imageConversionIterations)
        // данные куски будут инвертированы
        BufferedImage [] negPartsImages = getSubimages(imageConversionIterations.get(0), threadsNum);
        // массив объектов, для получения выходных параметров из потоков, порождённые master-потоком
        // здесь будут храниться инвертированные изображения(куски) из negPartsImages
        Future<BufferedImage> [] futures = new Future[threadsNum];

        // цикл для указания, куда следует записать потоку выходные значения после завершения
        for (int i = 0; i < threadsNum; i++)
            futures[i] = executorService.submit(new PartImageRunner(negPartsImages[i]));

        // запуск потоков (fork)
        executorService.shutdown();
        // ожидание выполнения всех потоков (join)
        executorService.awaitTermination(1, TimeUnit.HOURS);

        // перезаписываем массив изменёнными (инвертированными) изображениями
        for (int i = 0; i < threadsNum; i++)
            negPartsImages[i] = futures[i].get();

        return negPartsImages;
    }

    /**
     * Метод для разделения изображения на равные куски.
     * Если длинна изображения не делится на нужно кол-во кусков без остатка,
     * то остаток от деления будет прибавлен к последнему куску
     * <br/>
     * <br/>
     * @param parentImage изображения, которое нужно поделить
     * @param numSubimages требуемое кол-во кусков
     * @return возвращает вертикальные куски изображения
     */
    private BufferedImage [] getSubimages(BufferedImage parentImage, int numSubimages){
        BufferedImage [] parentPartsImages = new BufferedImage[numSubimages];

        final BufferedImage parent = parentImage;

        int widthDelta = parentImage.getWidth() / numSubimages;
        int unexpectedPixelRows = parentImage.getWidth() % numSubimages;

        for (int i = 0; i < numSubimages - 1; i++){
            parentPartsImages[i] = parent.getSubimage(i * widthDelta, 0, widthDelta, parent.getHeight());
        }
        parentPartsImages[parentPartsImages.length - 1] = parent.getSubimage((numSubimages - 1) * widthDelta, 0, widthDelta + unexpectedPixelRows, parent.getHeight());

        return parentPartsImages;
    }

    /**
     * Метод для склейки изображения из кусков
     * <br/>
     * <br/>
     * @param images изображения, которое нужно соединить
     */
    public void buildImageInPartsHorizontal(BufferedImage[] images){
        BufferedImage result = null;
        if (imageConversionIterations.get(imageConversionIterations.size() - 1) == null){
            throw new NullPointerException("load file correctly");
        } else {
            result = new BufferedImage(
                    imageConversionIterations.get(imageConversionIterations.size() - 1).getWidth(),
                    imageConversionIterations.get(imageConversionIterations.size() - 1).getHeight(),
                    imageConversionIterations.get(imageConversionIterations.size() - 1).getType()
            );
            Graphics2D graphics2D = result.createGraphics();
            int x = 0;
            for (BufferedImage image : images){
                graphics2D.drawImage(image, x, 0, null);
                x += image.getWidth();
            }
            graphics2D.dispose();
        }
        imageConversionIterations.add(result);
    }

    /**
     * Метод для свёртки изображения <br/>
     * При этом родительское изображение или последний буфер изображения в
     * {@link ImageCompressionMultithreading#imageConversionIterations} не изменяется
     * <br/>
     * После инвертирования новый буфер добавляется в {@link ImageCompressionMultithreading#imageConversionIterations}
     */
    public void applyConvolutionMatrix(){
        // копируем старое изображение, теперь это будущее новое изображение
        BufferedImage newBufferedImage = deepCopy(imageConversionIterations.get(imageConversionIterations.size() - 1));
        // берем от будущего нового изображение WritableRaster (объект, в котором можно манипулировать пикселями изображения)
        WritableRaster newWritableRaster = newBufferedImage.getRaster();
        // берем WritableRaster от изображения-родителя (последнее изображение в LinkedList imageConversionIterations)
        WritableRaster parentWritableRaster = imageConversionIterations.get(imageConversionIterations.size() - 1).getRaster();

        // записываем пиксели изображения
        int width = parentWritableRaster.getWidth();
        int height = parentWritableRaster.getHeight();
        // пробегаемся по пикселям нового изображения учитывая обрезку в 1 пиксель по краям
        for (int x = 1; x < width - 2; x++){
            for (int y = 1; y < height - 2; y++){
                // кусок пикселей из изображения-родителя,
                // от куда мы будем брать исходные значения пикселей для перемножения
                int[][][] partParentPixels = new int[3][3][4]; //[ширина][высота][RGBA]
                // копируем значения пикселей в массив partParentPixels
                // px/y - parent x/y
                for (int px = 0; px < 3; px++){
                    for (int py = 0; py < 3; py++){
                        partParentPixels[px][py] = parentWritableRaster.getPixel(x - 1 + px, y - 1 + py, new int[4]);
                    }
                }

                //будущий сконвертированный пиксель
                int[] convertedPixel = new int[4];
                // сумма перемножений матрицы(ядра) и куска родительского изображения
                int multiplySum = 0;
                for (int RGBIndex = 0; RGBIndex < 3; RGBIndex++) {
                    for (int wMatrix = 0; wMatrix < convolutionMatrix.length; wMatrix++) {
                        for (int hMatrix = 0; hMatrix < convolutionMatrix.length; hMatrix++) {
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
        imageConversionIterations.add(newBufferedImage);
    }

    /**
     * Геттер {@link ImageCompressionMultithreading#imageConversionIterations}
     */
    public List<BufferedImage> getImageConversionIterations() {
        return imageConversionIterations;
    }

    /**
     * Сеттер {@link ImageCompressionMultithreading#imageConversionIterations}
     */
    public void setImageConversionIterations(List<BufferedImage> imageConversionIterations) {
        this.imageConversionIterations = imageConversionIterations;
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

    private static BufferedImage[] deepCopy(BufferedImage [] bi){
        BufferedImage [] copedBufferedImages = new BufferedImage[bi.length];
        for (int i = 0; i < bi.length; i++){
            final ColorModel cm = bi[i].getColorModel();
            final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            final WritableRaster raster = bi[i].copyData(bi[i].getRaster().createCompatibleWritableRaster());
            copedBufferedImages[i] = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        }
        return copedBufferedImages;
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
    private class PartImageRunner implements Callable<BufferedImage> {
        private BufferedImage partBufferedImage;

        public PartImageRunner(BufferedImage partBufferedImage){
            this.partBufferedImage = partBufferedImage;
        }

        @Override
        public BufferedImage call() {
            return getNegativeImage(partBufferedImage);
        }
    }
}
