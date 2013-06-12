package scala.slick.yy

import scala.language.implicitConversions
import scala.slick.lifted.{ Case, AbstractTable }
import scala.slick.ast.BaseTypedType
import scala.slick.jdbc.JdbcBackend
import scala.slick.SlickException
import scala.slick.profile.BasicDriver
import scala.slick.driver.{ H2Driver, JdbcDriver, JdbcProfile }

trait YYSlickCake extends YYType with YYSlickCakeTuples with YYSlickLowPriorityImplicits {
  val Ordering = YYOrdering
  val String = YYOrdering.String
  val Int = YYOrdering.Int
  val Long = YYOrdering.Long
  val Double = YYOrdering.Double
  implicit def fixClosureContraVariance[T, U <: YYRep[T], S](x: U => S) = x.asInstanceOf[YYRep[T] => S]
  implicit def yyRepIntToColumnOps(x: CakeRep[scala.Int]): Int = x.asInstanceOf[Int]
  implicit def yyRepLongToColumnOps(x: CakeRep[scala.Long]): Long = x.asInstanceOf[Long]
  implicit def yyRepStringToColumnOps(x: CakeRep[Predef.String]): String = x.asInstanceOf[String]
  implicit def yyRepDoubleToColumnOps(x: CakeRep[scala.Double]): Double = x.asInstanceOf[Double]
  implicit def yyRepSQueryToQuery[T](x: CakeRep[scala.slick.yy.Shallow.Query[T]]): Query[T] = x.asInstanceOf[Query[T]]
  implicit def yyColumnOptionToYYOption[T](x: YYColumn[Option[T]]): YYOption[T] = YYOption.fromYYColumn(x)
  object Queryable {
    def apply[T](implicit t: YYTable[T]): Query[T] = YYQuery.apply(t)
  }
  object Query {
    def apply[T](v: YYRep[T]): YYQuery[T] = YYQuery.apply(v)
    def ofTable[T](t: YYTable[T]): YYQuery[T] = YYQuery.apply(t)
  }
  object Shallow {
    object ColumnOps {
      def apply[T](value: YYColumn[T]): YYColumn[T] = value
    }
    def stringWrapper(value: String): String = value
    class OptMaker[T](val value: YYColumn[T]) {
      def ? : YYOption[T] = YYOption.fromPlainColumn(value.underlying)
    }
    object OptMaker {
      def apply[T](value: YYColumn[T]): OptMaker[T] = new OptMaker(value)
    }
    object SingleColumnQuery {
      def apply[T](value: YYQuery[T]): YYSingleColumnQuery[T] = new YYSingleColumnQuery(value)
    }
    def nonesFirst[T]: YYOrdering[Option[T]] = YYOrdering.nonesFirst
    def nonesLast[T]: YYOrdering[Option[T]] = YYOrdering.nonesLast
    def nullsFirst[T]: YYOrdering[T] = YYOrdering.nullsFirst
    def nullsLast[T]: YYOrdering[T] = YYOrdering.nullsLast
  }
  object scalaYY extends scalaYYTuples {
    type Query[T] = scala.slick.yy.Shallow.Query[T]
    type Option[T] = scala.Option[T]
  }
  def intWrapper(value: Int): Int = value
  def __ifThenElse[T: BaseTypedType](c: => Boolean, t: Column[T], e: Column[T]): Column[T] =
    YYColumn(Case.If(c.underlying) Then (t.underlying) Else (e.underlying))
  def __equals[T](t: Column[T], e: Column[T]) = t === e
  object Table {
    def getTable[S](implicit mapping: Table[S]): Table[S] = mapping
  }
}

trait YYSlickLowPriorityImplicits {
  // These two implicits are needed for the cake to be type checked!

  // Type of this one is JdbcProfile and not JdbcDriver in order to make it lower priority in comparison with the implicit driver which will
  // provided by the user. If type of this one is JdbcDriver, we would get 'ambiguous implicit' error. 
  implicit def dummyDriver: JdbcProfile = throw new SlickException("You forgot to provide appropriate implicit jdbc driver for YY block!")
  // The reason of generality of this type is the same as the above one.
  implicit def dummySession: JdbcBackend#Session = throw new SlickException("You forgot to provide implicit session for YY block!")
}