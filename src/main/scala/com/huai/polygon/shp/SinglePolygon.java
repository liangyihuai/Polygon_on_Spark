package com.huai.polygon.shp;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个多边形。一个shp文件中可能会包含多个多边形
 * Created by liangyh on 7/25/2017.
 */
public class SinglePolygon implements Serializable{

    private static final long serialVersionUID = -6931071015041435548L;

    private String id;
    private String locationName;
    private List<Point> pointList = new ArrayList<Point>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    @Override
    public String toString() {
        return "SinglePolygon{" +
                "id='" + id + '\'' +
                ", locationName='" + locationName + '\'' +
                " locationSize = "+pointList.size()+"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SinglePolygon)) return false;

        SinglePolygon that = (SinglePolygon) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getLocationName() != null ? !getLocationName().equals(that.getLocationName()) : that.getLocationName() != null)
            return false;
        return getPointList() != null ? getPointList().equals(that.getPointList()) : that.getPointList() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getLocationName() != null ? getLocationName().hashCode() : 0);
        result = 31 * result + (getPointList() != null ? getPointList().hashCode() : 0);
        return result;
    }
}
