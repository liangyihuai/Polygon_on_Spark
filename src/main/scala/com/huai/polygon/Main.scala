package com.huai.polygon

import java.util

import com.huai.polygon.shp.Point
import com.snatik.polygon.Polygon
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.io.Source

object Main {

  /**
    * 小方格的边长（正方形）
    */
  val gridLen: Double = 0.1d;

  /**
    * 所有的数据乘以的倍数，目的是把带有小数部分的数据转换成整数，便于后续计算和处理。
    */
  val multiple: Long = 1000000L;

  /**
    * 点数据文件
    */
  val POINT_FILE: String = "/home/liangyh/tmp/shp/part-00000.csv";

  private val logger = LoggerFactory.getLogger(Main.getClass.getName);


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("polygon_contain_point").setMaster("local");
    val sc = new SparkContext(conf);

    //过滤并获取所有点
    val rawRDD = sc.textFile(POINT_FILE);
    val positionPoints: RDD[(Double, Double)] = rawRDD
      .map(line => line.split(','))
      .filter(fields => fields.size > 3 && fields(2) != "" && fields(3) != "")
      .map(field => (field(2).toDouble, field(3).toDouble));

    val shapeFile = Source.fromFile("/home/liangyh/IdeaProjects/Polygon/src/test/data/shapePoints.txt");
    val shapePoints = shapeFile.getLines()
      .map(line => line.split(','))
      .map(field => new Point(field(0).toDouble, field(1).toDouble));

    val jShapePoints = new util.ArrayList[Point]();
    shapePoints.foreach(jShapePoints.add(_));

    val result = getCount(jShapePoints, positionPoints, sc);
    logger.warn(" Count = "+result.toString());
  }

  /**
    * 统计多边形中的点个数，该方法是算法的入口
    * @param shapePoints 多边形的描述点
    * @param positionPoints 地理位置点
    * @param sc
    * @return
    */
  def getCount(shapePoints:java.util.List[Point], positionPoints:RDD[(Double, Double)], sc:SparkContext): Int ={
    logger.debug("shapePoints size = "+shapePoints.size())

    val minAndMax = getMinAndMaxCoordinate(shapePoints);
    val box: Box = new Box(minAndMax.get(0), minAndMax.get(1), Main.gridLen, Main.multiple);

    val borderGridSet = getBorderGrids(box, shapePoints);
    val polygon_snatik = polygon(shapePoints, box);
    val insideGridSet = getInsideGrids(polygon_snatik, borderGridSet, box);

    val count = sc.accumulator(0, "count_accumulate");//统计在多边形中的点
    //广播变量，实行并行计算
    val borderGridSet_broadcast = sc.broadcast(borderGridSet);
    val insideGridSet_broadcast = sc.broadcast(insideGridSet);
    val polygon_broadcast = sc.broadcast(polygon_snatik);
    positionPoints.foreach(item => {
      val newGrid = box.getGrid(item._1, item._2);
      if(insideGridSet_broadcast.value.contains(newGrid)){//在多边形内部的方格中
        count += 1;
      }else if(borderGridSet_broadcast.value.contains(newGrid)){//在边界方格中
        //进一步判断是否在多边形内部
        if(polygon_broadcast.value.contains(new com.snatik.polygon.Point(item._1 * Main.multiple, item._2 * Main.multiple))){
          count += 1;
        }
      }
    });
    return count.value;
  }

  /**
    * 查找所有的边界网格
    * @param box
    * @param shapePoints
    * @return
    */
  def getBorderGrids(box:Box, shapePoints:java.util.List[Point]): mutable.HashSet[Grid] ={
    val result = new mutable.HashSet[Grid]();
    for(i <- 1 to(shapePoints.size())){
      var fromPoint:Point = null;
      var toPoint:Point = null;
      if(i == shapePoints.size()){
        fromPoint = shapePoints.get(i-1);
        toPoint = shapePoints.get(0);
      }else{
        fromPoint = shapePoints.get(i-1);
        toPoint = shapePoints.get(i);
      }

      val fromGrid = box.getGrid(fromPoint);
      val toGrid = box.getGrid(toPoint);

      if(fromGrid.x == toGrid.x ){
        myRange(fromGrid.y, toGrid.y).foreach(result += Grid(fromGrid.x, _, Main.gridLen, multiple));
        result += toGrid;
      }else if(fromGrid.y == toGrid.y){
        myRange(fromGrid.x, toGrid.x).foreach(result += Grid(_, fromGrid.y, Main.gridLen, multiple));
        result += toGrid;
      }else{
        var isGentle = true;//线段是否平缓，如果线段与x坐标的夹角的绝对值小于等于45度，就认为线段平缓。
        if(Math.abs(toPoint.getY - fromPoint.getY) > Math.abs(toPoint.getX - fromPoint.getX)) isGentle = false;

        val list:java.util.List[Integer] = new util.ArrayList[Integer]();
        if(isGentle){
          myRange(toGrid.x, fromGrid.x).foreach(list.add(_));//计算交点
          val iter = list.listIterator();
          while(iter.hasNext){
            val num:Int = iter.next();
            val xCoordinate = num * box.gridLen;

            val newYCoordinate1 = box.getYCoordinate_multiple(fromPoint, toPoint, xCoordinate-1);
            val newYCoordinate2 = box.getYCoordinate_multiple(fromPoint, toPoint, xCoordinate+1);

            result += box.getGrid_multiple(new Point(xCoordinate-1, newYCoordinate1));
            result += box.getGrid_multiple(new Point(xCoordinate+1, newYCoordinate2));
          }
          result += fromGrid;
          result += toGrid;
        }else{
          myRange(fromGrid.y, toGrid.y).foreach(list.add(_));
          val iter = list.listIterator();
          while(iter.hasNext){
            val num:Int = iter.next();
            val yCoordinate = num * box.gridLen;

            val newXCoordinate1 = box.getXCoordinate_multiple(fromPoint, toPoint, yCoordinate-1);
            val newXCoordinate2 = box.getXCoordinate_multiple(fromPoint, toPoint, yCoordinate+1);

            result += box.getGrid_multiple(new Point(newXCoordinate1, yCoordinate-1));
            result += box.getGrid_multiple(new Point(newXCoordinate2, yCoordinate+1));
          }
          result += fromGrid;
          result += toGrid;
        }
      }
    }
    return result;
  }

  /**
    * 返回polygon对象
    * 使用第三方包，用于检测点或者方格是否在多边形内部。
    * @param shapePoints
    * @param box
    * @return
    */
  def polygon(shapePoints:java.util.List[Point], box:Box):Polygon = {
    val builder = Polygon.Builder()
    val iterator = shapePoints.listIterator();
    while(iterator.hasNext){
      val point = iterator.next();
      val newPoint = box.multiple(point);
      builder.addVertex(new com.snatik.polygon.Point(newPoint.getX, newPoint.getY));
    }
    val polygon_snatik = builder.build();
    return polygon_snatik;
  }

  //使用传统方法，找到所有内部的网格。
  def getInsideGrids(polygon_snatik:Polygon,
                     borderGrids:mutable.HashSet[Grid],
                     box:Box): mutable.HashSet[Grid] ={
    val result = new mutable.HashSet[Grid]();
    for(xNo <- box.beginXGridNo to(box.endXGridNo)){
      for(yNo <- box.beginYGridNo to box.endYGridNo){
        val tmpGrid = Grid(xNo, yNo, Main.gridLen, multiple);
        if(!borderGrids.contains(tmpGrid)){
          val point = tmpGrid.getCenter();
          if(polygon_snatik.contains(new com.snatik.polygon.Point(point.getX, point.getY))){
            result += tmpGrid;
          }
        }
      }
    }
    return result;
  }


  /**
    * 获取一个多边形最外围的点。
    *
    * @param shapePoints
    * @return 返回两个点，这两个点构成一个长方形，这个长方形恰好能够围住该多边形。
    */
  def getMinAndMaxCoordinate(shapePoints:java.util.List[Point]): util.List[Point] = {
    val result = new util.ArrayList[Point](2)
    var left = Double.MaxValue
    var right = Double.MinValue
    var down = Double.MaxValue
    var up = Double.MinValue
    if (shapePoints.size < 3) {
      logger.error("多边形的只有两个点！！！！")
      throw new IllegalArgumentException
    }
    val iter = shapePoints.iterator();
    while(iter.hasNext){
      val point = iter.next();
      if (point.getX > right) right = point.getX
      if (point.getX < left) left = point.getX
      if (point.getY > up) up = point.getY
      if (point.getY < down) down = point.getY
    }
    result.add(new Point(left, down))
    result.add(new Point(right, up))
    result
  }

  /**
    * 自定义Scala中的range函数
    *
    * @param index1
    * @param index2
    * @return
    */
  def myRange(index1:Int, index2:Int):Range = {
    if(index1 < index2)
      Range(index1, index2);
    else
      Range(index1, index2, -1);
  }
}


