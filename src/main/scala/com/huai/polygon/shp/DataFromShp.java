package com.huai.polygon.shp;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;
import java.io.File;
import java.util.Iterator;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从一个shp文件中所读取的所有有用的数据:包括图形的所有点、图形的id、图形的名字（比如地名）
 * Created by liangyh on 7/25/2017.
 */
public class DataFromShp {

    private enum GeomType {POLYGON, LINE, POINT};

    private List<SinglePolygon> polygonList = null;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    public List<SinglePolygon> readShape(String shpPath){
        if(shpPath == null)throw new NullPointerException();

        final List<SinglePolygon> result = new ArrayList<SinglePolygon>();

        File file = new File (shpPath);
        SimpleFeatureIterator featureIter = null;
        try{
            ShapefileDataStore shpDataStore = new ShapefileDataStore(file.toURI().toURL());
            shpDataStore.setCharset(Charset.forName("GBK"));//设置字符编码
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource (typeName);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            featureIter = featureCollection.features();

            while (featureIter.hasNext()) {//表示一个多边形
                SimpleFeature feature = featureIter.next();
                Iterator<Property> propertyIter = feature.getProperties().iterator();
                SinglePolygon tmpSinglePolygon = new SinglePolygon();
                while(propertyIter.hasNext()) {//表示一个多边形的一个属性
                    Property pro = propertyIter.next();
                    String field = pro.getName().toString();
                    Object value = pro.getValue();

                    if("the_geom".equals(field)){
                        if(GeomType.POLYGON == getGeometry(pro)){
                            MultiPolygon multiPolygon = (MultiPolygon)value;
                            Geometry geometry = multiPolygon.getBoundary();

                            for(Coordinate co: geometry.getCoordinates()){
                                tmpSinglePolygon.getPointList().add(new Point(co));
                            }
                        }
                    }else if("ID".equals(field)){
                        tmpSinglePolygon.setId(value.toString());
                    }/*else if("MC_GNQ".equals(field)){
                        ;
                    }*/else{
                        tmpSinglePolygon.setLocationName(value.toString());
                    }
                }
                result.add(tmpSinglePolygon);
            }

//            featureIter.close();
        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println(">>>>>>>>>>>>>> 共" + result.size() + "条数据");

        this.polygonList = result;
        return result;
    }

    /**
     * Retrieve information about the feature geometry
     */
    private GeomType getGeometry(Property pro) {
        Class<?> clazz= pro.getDescriptor().getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)) {
            return GeomType.POLYGON;

        } else if (LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)) {
            return GeomType.LINE;

        } else {
            return GeomType.POINT;
        }
    }

    /**
     * 获取多个多边形最外围的点。
     * @return 每一个多边形 返回两个点，这两个点构成一个长方形，这个长方形恰好能够围住该多边形。
     */
    public List<List<Point>> getMinAndMaxCoordinate(){
        if(this.polygonList == null)throw new IllegalArgumentException("Do this after read shp file!");
        List<List<Point>> result = new ArrayList<>(2);

        for(SinglePolygon polygon: this.polygonList){
            result.add(getMinAndMaxCoordinate(polygon));
        }
        return result;
    }

    /**
     * 获取一个多边形最外围的点。
     * @param polygon
     * @return 返回两个点，这两个点构成一个长方形，这个长方形恰好能够围住该多边形。
     */
    public List<Point> getMinAndMaxCoordinate(SinglePolygon polygon){
        List<Point> result = new ArrayList<>(2);

        double left = Double.MAX_VALUE;
        double right = Double.MIN_VALUE;
        double down = Double.MAX_VALUE;
        double up = Double.MIN_VALUE;
        if(polygon.getPointList().size() < 3) {
            logger.error("多边形的只有两个点！！！！");
            throw new IllegalArgumentException();
        }
        for(Point point: polygon.getPointList()){
            if(point.getX() > right) right = point.getX();
            if(point.getX() < left) left = point.getX();
            if(point.getY() > up) up = point.getY();
            if(point.getY() < down) down = point.getY();
        }
        result.add(new Point(left, down));
        result.add(new Point(right, up));

        //debug
//        System.out.println("left = "+left +", down = "+down +", right = "+right +", up = "+up);
        return result;
    }

    public static void main(String args[]){
//        String shpPath = "/home/liangyh/tmp/shp/Export_Output.shp";
        String shpPath = "/home/liangyh/tmp/jtfq7/jtfq7.shp";
        DataFromShp dataFromShp = new DataFromShp();
        List<SinglePolygon> list = dataFromShp.readShape(shpPath);
        for(SinglePolygon singlePolygon: list){
            System.out.println(singlePolygon);
        }
    }
}

