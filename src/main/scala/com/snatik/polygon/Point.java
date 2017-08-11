package com.snatik.polygon;

import java.io.Serializable;

/**
 * Point on 2D landscape
 *
 * @author Roman Kushnarenko (sromku@gmail.com)</br>
 */
public class Point implements Serializable{

    private static final long serialVersionUID = 8269551623841331337L;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x;
    public double y;

    @Override
    public String toString() {
        return String.format("(%f,%f)", x, y);
    }
}