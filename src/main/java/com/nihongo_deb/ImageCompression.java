package com.nihongo_deb;

import org.openjdk.jmh.annotations.Benchmark;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 28.01.2023
 */

public class ImageCompression {
    private String fileExtension;
    private List<BufferedImage> imageConversionIterations = new LinkedList<>();
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

    public ImageCompression(){}

    public ImageCompression(String fileName, boolean isFromResources){
        if (isFromResources)
            readeImageFromResources(fileName);
        else readImageFromAbsoluteFilePath(fileName);
    }

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

    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(imageConversionIterations.get(imageConversionIterations.size() - 1), this.fileExtension, new File(newFileName + '.' + this.fileExtension));
    }

    public void saveImage(String newFileName, String extension) throws IOException {
        BufferedImage lastConversionBufferedImage = imageConversionIterations.get(imageConversionIterations.size() - 1);
        ImageIO.write(lastConversionBufferedImage, extension, new File(newFileName + extension));
    }

    public void applyNegative() {
        BufferedImage newConvertedBufferedImage;
        if (imageConversionIterations.isEmpty()){
            throw new NullPointerException("load file correctly");
        } else {
            newConvertedBufferedImage = deepCopy(imageConversionIterations.get(imageConversionIterations.size() - 1));
            WritableRaster writableRaster = newConvertedBufferedImage.getRaster();

            for (int x = 0; x < writableRaster.getWidth(); x++){
                for (int y = 0; y < writableRaster.getHeight(); y++){
                    int[] pixel = writableRaster.getPixel(x, y, new int[4]);
                    for (int RGBIndex = 0; RGBIndex < pixel.length - 1; RGBIndex++){
                        pixel[RGBIndex] = 255 - pixel[RGBIndex];
                    }
                    writableRaster.setPixel(x, y, pixel);
                }
            }

            newConvertedBufferedImage.setData(writableRaster);
            this.imageConversionIterations.add(newConvertedBufferedImage);
        }
    }

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

    public List<BufferedImage> getImageConversionIterations() {
        return imageConversionIterations;
    }

    public void setImageConversionIterations(List<BufferedImage> imageConversionIterations) {
        this.imageConversionIterations = imageConversionIterations;
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        final ColorModel cm = bi.getColorModel();
        final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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
}
