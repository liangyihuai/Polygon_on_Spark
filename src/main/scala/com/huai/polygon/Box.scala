package com.huai.polygon

import com.huai.polygon.shp.Point
import org.slf4j.LoggerFactory

/**
  * 该类表示能围住多边形的框。
  * 构造函数所传进去的参数分别是：该长方形左下角的点、右上角的点和正方形网格的边长以及需要增加的倍数。
  * Created by liangyh on 7/25/2017.
  */
class Box(_leftBottom: Point, _rightUp: Point, _gridLen: Double, multiple:Long) extends Serializable{
  require(_leftBottom != null && _rightUp != null);
  require(checkPoint(_leftBottom) && checkPoint(_rightUp));
  require(multiple > 0);


  val gridLen:Long = (_gridLen * multiple).toLong;

  private val logger = LoggerFactory.getLogger(Box.getClass.getName);

  {
    _leftBottom.setX((_leftBottom.getX*multiple).toLong);
    _leftBottom.setY((_leftBottom.getY*multiple).toLong);
    _rightUp.setX((_rightUp.getX*multiple).toLong);
    _rightUp.setY((_rightUp.getY*multiple).toLong);
  }

  val beginXGridNo:Int = {
    (_leftBottom.getX/gridLen).toInt
  }

  val endXGridNo:Int = {
    (_rightUp.getX/gridLen).toInt;
  }

  val beginYGridNo:Int = {
    (_leftBottom.getY/gridLen).toInt;
  }

  val endYGridNo:Int = {
    (_rightUp.getY/gridLen).toInt;
  }


  private def checkPoint(point: Point):Boolean = {
    if(point.getX <= 0 || point.getY <= 0) false else true;
  }

  def getGrid(point:Point):Grid = {
    val tmpPoint = multiple(point);
    val x = (tmpPoint.getX/gridLen).toInt;
    val y = (tmpPoint.getY/gridLen).toInt;

    return new Grid(x, y, _gridLen, multiple);
  }

  /**
    * 获取网格，
    * @param xCoor 需要加倍
    * @param yCoor 需要加倍
    * @return
    */
  def getGrid(xCoor:Double, yCoor:Double):Grid = {
    val point = multiple(xCoor, yCoor);
    val x = (point.getX/gridLen).toInt;
    val y = (point.getY/gridLen).toInt;

    return new Grid(x, y, _gridLen, multiple);
  }

  /**
    * 获取网格
    * @param point 不需要再加倍
    * @return
    */
  def getGrid_multiple(point: Point):Grid = {
    val x = ((point.getX)/gridLen).toInt;
    val y = ((point.getY)/gridLen).toInt;

    return new Grid(x, y, _gridLen, multiple);
  }

  /**
    * 某一点的y轴为yCoordinate，计算该点所在线段上的x坐标。其中fromPoint和toPoint两点构成线段。
    *
    * @param fromPoint
    * @param toPoint
    * @param yCoordinate
    * @return
    */
  def getXCoordinate_multiple(fromPoint:Point, toPoint:Point, yCoordinate:Long):Long ={
    val from = multiple(fromPoint);
    val to = multiple(toPoint);

    val xCoordinate = (from.getX + (yCoordinate - from.getY)* (to.getX-from.getX)/(to.getY-from.getY)).toLong;
    return xCoordinate;
  }

  /**
    * 某一点的x轴为xCoordinate，计算该点所在线段上的y坐标。其中fromPoint和toPoint两点构成线段。
    *
    * @param fromPoint
    * @param toPoint
    * @param xCoordinate
    * @return
    */
  def getYCoordinate_multiple(fromPoint:Point, toPoint:Point, xCoordinate: Long):Long ={
    val from = multiple(fromPoint);
    val to = multiple(toPoint);

    val yCoordinate = (from.getY + (xCoordinate - from.getX)* (to.getY - from.getY) /(to.getX - from.getX)).toLong;

    return yCoordinate;
  }

  def multiple(point: Point): Point ={
    new Point(point.getX * multiple, point.getY * multiple);
  }

  def multiple(x:Double, y:Double):Point = {
    new Point(x * multiple, y * multiple);
  }

}


object Box{
  def apply(_leftBottom: Point, _rightUp: Point, _gridLen: Double, multiple: Long): Box
        = new Box(_leftBottom, _rightUp, _gridLen, multiple);
}