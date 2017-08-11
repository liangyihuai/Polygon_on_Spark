package com.huai.polygon.shp;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;

/**
 * 表示一个二维点
 * Created by liangyh on 7/25/2017.
 */
public class Point implements Serializable {

    private static final long serialVersionUID = 7052968731039090426L;

    private double x;
    private double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Point(Coordinate coordinate){
        this.x = coordinate.x;
        this.y = coordinate.y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (Double.compare(point.getX(), getX()) != 0) return false;
        return Double.compare(point.getY(), getY()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
