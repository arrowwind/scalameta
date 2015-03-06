import org.scalatest._
import scala.meta._
import scala.meta.dialects.Scala211

class QuasiquoteSuite extends FunSuite {
  test("q\"foo($term, ..$terms, $term)\"") {
    val term = q"x"
    val terms = List(q"y", q"z")
    assert(q"foo($term, ..$terms, $term)".show[Code] === "foo(x, y, z, x)")
  }

  test("q\"foo[..$types]\"") {
    val types = List(t"T", t"U")
    assert(q"foo[..$types]".show[Code] === "foo[T, U]")
  }

  test("rank-0 liftables") {
    assert(q"foo[${42}]".show[Code] === "foo[42]")
    assert(q"${42}".show[Code] === "42")
  }

  test("rank-1 liftables") {
    implicit def custom[U >: List[Term]]: Liftable[List[Int], U] = Liftable(_.map(x => q"$x"))
    assert(q"foo(..${List(1, 2, 3)})".show[Code] === "foo(1, 2, 3)")
  }

  test("q\"$foo(${x: Int})\"") {
    q"foo(42)" match {
      case q"$foo(${x: Int})" =>
        assert(foo.show[Code] === "foo")
        assert(x == 42)
    }
  }

  test("q\"$foo(${x: Int}, ..$ys, $z)\"") {
    q"foo(1, 2, 3)" match {
      case q"$_(${x: Int}, ..$y, $z)" =>
        assert(x === 1)
        assert(y.map(_.show[Code]) === List("2"))
        assert(z.show[Code] === "3")
    }
  }

  test("q\"foo($x, ..$ys, $z, ..$ts)\"") {
    val x = q"1"
    val ys = List(q"2")
    val z = q"3"
    val ts = Nil
    assert(q"foo($x, ..$ys, $z, ..$ts)".show[Code] === "foo(1, 2, 3)")
  }
}