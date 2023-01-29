package com.nihongo_deb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 28.01.2023
 */
public class ImageNegativeCompression {
    private BufferedImage bufferedImage;
    private WritableRaster writableRaster;

    public void readeImageFromResources(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream io = classLoader.getResourceAsStream(name)){
            bufferedImage = ImageIO.read(io);
            writableRaster = bufferedImage.getRaster();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readImageFromAbsoluteFilePath(String absoluteImageFilePath){
        File file = new File(absoluteImageFilePath);
        try {
            bufferedImage = ImageIO.read(file);
            writableRaster = bufferedImage.getRaster();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNegative() {
        if (writableRaster == null || bufferedImage == null){
            throw new NullPointerException("writableRaster or bufferedImage is null");
        } else {
            for (int x = 0; x < writableRaster.getWidth(); x++){
                for (int y = 0; y < writableRaster.getHeight(); y++){
                    int[] pixel = writableRaster.getPixel(x, y, new int[4]);
                    for (int RGBIndex = 0; RGBIndex < pixel.length - 1; RGBIndex++){
                        pixel[RGBIndex] = 255 - pixel[RGBIndex];
                    }
                    writableRaster.setPixel(x, y, pixel);
                }
            }
            bufferedImage.setData(writableRaster);
        }
    }

    public void saveImage(String newFileName) throws IOException {
        ImageIO.write(bufferedImage, "jpg", new File(newFileName + ".jpg"));
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public WritableRaster getWritableRaster() {
        return writableRaster;
    }

    public void setWritableRaster(WritableRaster writableRaster) {
        this.writableRaster = writableRaster;
    }
}
