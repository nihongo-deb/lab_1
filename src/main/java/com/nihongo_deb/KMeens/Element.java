package com.nihongo_deb.KMeens;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */
// класс для хранен данных элемента для кластеризации
public class Element <K>{
    final K ordinate;
    final K abscissa;

    public Element(K ordinate, K abscissa) {
        this.ordinate = ordinate;
        this.abscissa = abscissa;
    }
}
