package com.huai.polygon

import com.huai.polygon.shp.Point

/**
  * Created by liangyh on 7/25/2017.
*/
class Grid (_x:Int, _y:Int, _gridLen:Double, multiple:Long) extends Serializable{

  val x = _x;
  val y = _y;
  val gridLen = (_gridLen*multiple).toLong;

  def getCenter():Point = {
    return new Point(x * gridLen + gridLen/2, y * gridLen + gridLen/2);
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Grid]

  override def equals(other: Any): Boolean = other match {
    case that: Grid =>
      (that canEqual this) &&
        x == that.x &&
        y == that.y
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Grid($x, $y)"

}

object Grid{

  def apply(_x: Int, _y: Int, _gridLen: Double, multiple:Long): Grid = new Grid(_x, _y, _gridLen, multiple);

  /**
    * 判断两个网格是否互为邻居。如果两个网格只有一个公共点，不互为邻居。
    * @param gridA
    * @param gridB
    * @return
    */
  def isNeighbor(gridA: Grid, gridB: Grid): Boolean= {
    if(gridA.equals(gridB))return false;
    if(gridA.x == gridB.x && (gridA.y+1 == gridB.y || gridA.y-1 == gridB.y ))return true;
    if((gridA.x+1 == gridB.x || gridA.x-1 == gridB.x) && gridA.y == gridB.y) return true;
    return false;
  }

}
