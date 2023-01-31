package com.nihongo_deb;

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
            BufferedImage bufferedImage = ImageIO.read(file);
            imageConversionIterations.add(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNegative() {
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

    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(imageConversionIterations.get(imageConversionIterations.size() - 1), this.fileExtension, new File(newFileName + '.' + this.fileExtension));
    }

    public void saveImage(String newFileName, String extension) throws IOException {
        BufferedImage lastConversionBufferedImage = imageConversionIterations.get(imageConversionIterations.size() - 1);
        ImageIO.write(lastConversionBufferedImage, this.fileExtension, new File(newFileName + extension));
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
