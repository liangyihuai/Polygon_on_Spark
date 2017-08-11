package com.huai.polygon.test;

import com.huai.polygon.shp.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyh on 7/28/2017.
 */
public class Test {

    public static List<Point> getPointList(){
        List<Point> result = new ArrayList<>();
        result.add(new Point(1.2, 5.4));
        result.add(new Point(3.7, 6.6));
        result.add(new Point(5.8, 5.8));
        result.add(new Point(3.6, 3.5));
        result.add(new Point(4.7, 2.5));
        result.add(new Point(6.5, 2.5));
        result.add(new Point(7.4, 1.5));
        result.add(new Point(2,1));
        return result;
    }

    public static List<Point> getMinAndMaxCoordinate(){
        List<Point> result = new ArrayList<>();
        result.add(new Point(1.2, 1));
        result.add(new Point(7.4, 6.6));
        return result;
    }
}
