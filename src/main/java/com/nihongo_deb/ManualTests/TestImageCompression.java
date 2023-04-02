package com.nihongo_deb.ManualTests;

import com.nihongo_deb.ImageCompression;

import java.io.IOException;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 01.04.2023
 */
public class TestImageCompression {
    public static void main(String[] args) throws InterruptedException, IOException {
        // загружаем изображение в 3 объекта ImageCompression
        // imageNeg - продемонстрирует инвертирование изображения
        // imageComp - продемонстрирует свёртку с фильтром
        // imageNegComp - продемонстрирует инвертирование и свёртку
        ImageCompression imageNeg = new ImageCompression("gervant.jpg", true);
        ImageCompression imageComp = new ImageCompression("gervant.jpg", true);
        ImageCompression imageNegComp = new ImageCompression("gervant.jpg", true);

        // загружаем изображение в 2 объекта ImageCompression
        // imageBin - продемонстрирует перевод изображение в бинарное
        // imageBinDel - продемонстрирует операцию наращивания
        ImageCompression imageBin = new ImageCompression("gervant.jpg", true);
        ImageCompression imageBinDel = new ImageCompression("gervant.jpg", true);

        //демонстрировать каждый объект будет в 4 потоках

        //инвертирование
        imageNeg.applyNegativeMultithreading(4);
        imageNeg.writePixelsInImage();
        imageNeg.saveImage("returned_images/neg_gervant");
        //свёртка с фильтром
        imageComp.applyConvolutionMatrixMultithreading(4);
        imageComp.writePixelsInImage();
        imageComp.saveImage("returned_images/comp_gervant");
        //инвертирование + свёртка
        imageNegComp.applyNegativeMultithreading(4);
        imageNegComp.applyConvolutionMatrixMultithreading(4);
        imageNegComp.writePixelsInImage();
        imageNegComp.saveImage("returned_images/neg_comp_gervant");
        //перевод в бинарное изображение
        imageBin.applyBinaryMultithreading(4, (short) 200);
        imageBin.writePixelsInImage();
        imageBin.saveImage("returned_images/bin_gervant");
        //перевод в бинарное + наращивание
        imageBinDel.applyBinaryMultithreading(4, (short) 200);
        imageBinDel.applyDilationMultithreading(4);
        imageBinDel.writePixelsInImage();
        imageBinDel.saveImage("returned_images/bin_del_gervant");

    }
}
