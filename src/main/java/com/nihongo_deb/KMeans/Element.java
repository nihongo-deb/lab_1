package com.nihongo_deb.KMeans;

import java.awt.*;
import java.util.Collections;

/**
 * @author KAWAIISHY
 * @project lab_1
 * @created 12.03.2023
 */

/**
 * Класс - элемент. Объекты класса служат в роли точки на координатной плоскости.
 * Каждый из элементов имеет свои координаты {@code X} = {@link Element#abscissa} и {@code Y} = {@link Element#ordinate}
 * <br>
 * Данный класс был написан для выполнения роли элемента кластера, для этого у него предусмотрены такие параметры как:
 * <ul>
 *     <li> {@link Element#colorIndex} - индекс кластера (индекс цвета)
 *     <li> {@link Element#color} - вет элемента, зависит от индекса кластера {@link Element#colorIndex}
 * </ul>
 * <br>
 * Для удобства приведения расчетов кластера НЕ к случайному значению, был имплементирован интерфейс {@link Comparable}.
 * Это было сделано для удобства тестирования и отслеживания эффективности работы программы в многопоточном режиме.
 */
public class Element implements Comparable{
    /**
     * Статическое поле - массив цветов, для удобства было в ручную определены цвета
     * для реализации программы в рамках данной задачи.
     */
    //TODO реализовать динамическое определение цвета, для применения неограниченного кол-ва кластеров
    private static Color[] colors = {
            Color.BLACK,
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA
    };
    /**
     * X - координата
     */
    public final double abscissa;
    /**
     * Y - координата
     */
    public final double ordinate;
    /**
     * Расстояние от начала координат {@code (0.0; 0.0)} до координат элемента.
     */
    public final double length;
    /**
     * Цвет элемента, зависит от того, в каком кластере находится элемент.
     */
    public Color color;
    /**
     * Индекс кластера, он равен индексу цвета в {@link Element#colors}
     */
    public byte colorIndex;

    /**
     * <h2>Конструктор</h2>
     * <br>
     * Используется при инициализации объектов. По умолчанию цвет черный.
     * <br>
     * {@link KMeansCSVDataLoader} method - {@code fillElements()}
     * @param abscissa координаты по оси абсцисс (Ox)
     * @param ordinate координаты по оси ординат (Oy)
     */
    public Element(double abscissa, double ordinate) {
        this.abscissa = abscissa;
        this.ordinate = ordinate;
        this.length = Math.sqrt(Math.pow(abscissa, 2) + Math.pow(ordinate, 2));

        this.color = Color.BLACK;
        this.colorIndex = (byte) 0;
    }
    /**
     * <h2>Конструктор</h2>
     * <br>
     * Используется при инициализации объектов.
     * <br>
     * {@link KMeansCluster} method - {@code initCenters()}
     * {@link KMeansCluster} method - {@code calculateCenters()}
     * @param abscissa координаты по оси абсцисс (Ox)
     * @param ordinate координаты по оси ординат (Oy)
     * @param colorIndex индекс кластера
     */
    public Element(double abscissa, double ordinate, int colorIndex) {
        this.abscissa = abscissa;
        this.ordinate = ordinate;
        this.length = Math.sqrt(Math.pow(abscissa, 2) + Math.pow(ordinate, 2));

        this.color = colors[colorIndex];
        this.colorIndex = (byte) colorIndex;
    }

    public void setColor(int colorIndex){
        this.color = colors[colorIndex];
    }

    @Override
    public String toString() {
        return abscissa + "\t" + ordinate + "\t" + color;
    }

    /**
     * Переопределённый метод из интерфейса {@link Comparable}. Нужен для сортировки элементов.
     * @see Collections#sort
     */
    @Override
    public int compareTo(Object o) {
        Element e = (Element) o;

        if (this.length == e.length)
            return 0;
        else
            if (this.length > e.length)
                return 1;
            else
                return -1;
    }
}
