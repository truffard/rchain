package coop.rchain.rosette.prim

import coop.rchain.rosette.prim.rblfloat._
import coop.rchain.rosette.{Ctxt, Fixnum, Ob, PC, RblBool, RblFloat => RFloat, Tuple}
import org.scalatest._

class RblFloatSpec extends FlatSpec with Matchers {
  val ctxt = Ctxt(
    tag = null,
    nargs = 1,
    outstanding = 0,
    pc = PC.PLACEHOLDER,
    rslt = null,
    trgt = null,
    argvec = Tuple(1, Fixnum(1)),
    env = null,
    code = null,
    ctxt = null,
    self2 = null,
    selfEnv = null,
    rcvr = null,
    monitor = null,
  )

  "flPlus" should "correctly add float numbers" in {
    val newCtxt = ctxt.copy(nargs = 5, argvec = Tuple(5, RFloat(.1)))
    flPlus.fnSimple(newCtxt) should be(Right(RFloat(.5)))

    val newCtxt2 = ctxt.copy(nargs = 0, argvec = Tuple.NIL)
    flPlus.fnSimple(newCtxt2) should be(Right(RFloat(0)))

    val newCtxt3 = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(.1)))
    flPlus.fnSimple(newCtxt3) should be(Right(RFloat(0.1)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 5, argvec = Tuple(5, Ob.NIV))
    flPlus.fnSimple(newCtxt) should be('left)
  }

  "flMinus" should "correctly subtract RblFloat" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, RFloat(1.1)))
    flMinus.fnSimple(newCtxt) should be(Right(RFloat(0.0)))
  }

  it should "correctly invert the RblFloat" in {
    val newCtxt2 = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(1.1)))
    flMinus.fnSimple(newCtxt2) should be(Right(RFloat(-1.1)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, Ob.NIV))
    flMinus.fnSimple(newCtxt) should be('left)
  }

  "flTimes" should "correctly multiply float number" in {
    val newCtxt = ctxt.copy(nargs = 3, argvec = Tuple(3, RFloat(0.5)))
    flTimes.fnSimple(newCtxt) should be(Right(RFloat(0.125)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 3, argvec = Tuple(3, Ob.NIV))
    flTimes.fnSimple(newCtxt) should be('left)
  }

  "flDiv" should "correctly divide float numbers" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(7.5)), Tuple(RFloat(2.5))))
    flDiv.fnSimple(newCtxt) should be(Right(RFloat(3)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, Ob.NIV))
    flDiv.fnSimple(newCtxt) should be('left)
  }

  "flLt" should "correctly return whether former smaller than latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.1)), Tuple(RFloat(2.2))))
    flLt.fnSimple(newCtxt) should be(Right(RblBool(true)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.1)), Tuple(Ob.NIV)))
    flLt.fnSimple(newCtxt) should be('left)
  }

  "flLe" should "correctly return whether former smaller than or equal to latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, RFloat(2.1)))
    flLe.fnSimple(newCtxt) should be(Right(RblBool(true)))
  }

  it should "fail for non-fixnum arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2)), Tuple(Ob.NIV)))
    flLe.fnSimple(newCtxt) should be('left)
  }

  "flGt" should "correctly return whether former greater than latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.3)), Tuple(RFloat(2.2))))
    flGt.fnSimple(newCtxt) should be(Right(RblBool(true)))
  }

  it should "fail for non-fixnum arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.1)), Tuple(Ob.NIV)))
    flGt.fnSimple(newCtxt) should be('left)
  }

  "flGe" should "correctly return whether former greater than or equal to latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.2)), Tuple(RFloat(2.2))))
    flGe.fnSimple(newCtxt) should be(Right(RblBool(true)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.1)), Tuple(Ob.NIV)))
    flGe.fnSimple(newCtxt) should be('left)
  }

  "flEq" should "correctly return whether former equal to the latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2.2)), Tuple(RFloat(2.2))))
    flEq.fnSimple(newCtxt) should be(Right(RblBool(true)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(Fixnum(2)), Tuple(Ob.NIV)))
    flEq.fnSimple(newCtxt) should be('left)
  }

  "flNe" should "correctly return whether former is not equal to latter" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(5)), Tuple(RFloat(5))))
    flNe.fnSimple(newCtxt) should be(Right(RblBool(false)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2)), Tuple(Ob.NIV)))
    flNe.fnSimple(newCtxt) should be('left)
  }

  "flMin" should "correctly return the smallest input value" in {
    val newCtxt = ctxt.copy(nargs = 4, argvec = Tuple(Tuple(3, RFloat(2.1)), Tuple(RFloat(2.2))))
    flMin.fnSimple(newCtxt) should be(Right(RFloat(2.1)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, Ob.NIV))
    flMin.fnSimple(newCtxt) should be('left)
  }

  "flMax" should "correctly return the greatest input value" in {
    val newCtxt = ctxt.copy(nargs = 4, argvec = Tuple(Tuple(3, RFloat(2.1)), Tuple(RFloat(2.2))))
    flMax.fnSimple(newCtxt) should be(Right(RFloat(2.2)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, Ob.NIV))
    flMax.fnSimple(newCtxt) should be('left)
  }

  "flAbs" should "correctly return absolute value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(-2.1)))
    flAbs.fnSimple(newCtxt) should be(Right(RFloat(2.1)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(1, Ob.NIV))
    flAbs.fnSimple(newCtxt) should be('left)
  }

  "flExp" should "correctly return e to the power of the input value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(2.5)))
    flExp.fnSimple(newCtxt) should be(Right(RFloat(math.exp(2.5))))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flExp.fnSimple(newCtxt) should be('left)
  }

  "flExpt" should "correctly return first argument to the power of second argument" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(Tuple(RFloat(2)), Tuple(RFloat(.5))))
    flExpt.fnSimple(newCtxt) should be(Right(RFloat(math.pow(2, .5))))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 2, argvec = Tuple(2, Ob.NIV))
    flExpt.fnSimple(newCtxt) should be('left)
  }

  "flLog" should "correctly return result of natural logarithm for input value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(Math.E)))
    flLog.fnSimple(newCtxt) should be(Right(RFloat(1)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flLog.fnSimple(newCtxt) should be('left)
  }

  "flLog10" should "correctly return result of common logarithm for input value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(100.0)))
    flLog10.fnSimple(newCtxt) should be(Right(RFloat(2.0)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flLog10.fnSimple(newCtxt) should be('left)
  }

  "flCeil" should "correctly return ceiling of input value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(2.1)))
    flCeil.fnSimple(newCtxt) should be(Right(RFloat(3.0)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flCeil.fnSimple(newCtxt) should be('left)
  }

  "flFloor" should "correctly return floor of input value" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(2.1)))
    flFloor.fnSimple(newCtxt) should be(Right(RFloat(2.0)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flFloor.fnSimple(newCtxt) should be('left)
  }

  "flAtan" should "correctly return the arc tangent of a value " in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(1.0)))
    // the returned angle is in the range -pi/2 through pi/2.
    flAtan.fnSimple(newCtxt) should be(Right(RFloat(Math.PI / 4)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flAtan.fnSimple(newCtxt) should be('left)
  }

  "flSin" should "correctly return the trigonometric sine of an angle." in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(Math.PI)))
    flSin.fnSimple(newCtxt) should be(Right(RFloat(Math.sin(Math.PI))))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flSin.fnSimple(newCtxt) should be('left)
  }

  "flCos" should "correctly return the trigonometric cosine of an angle." in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(Math.PI)))
    flCos.fnSimple(newCtxt) should be(Right(RFloat(Math.cos(Math.PI))))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flFloor.fnSimple(newCtxt) should be('left)
  }

  "flToFx" should "correctly convert the RblFloat value to Fixnum" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(RFloat(2.1)))
    flToFx.fnSimple(newCtxt) should be(Right(Fixnum(2)))
  }

  it should "fail for non-RblFloat arguments" in {
    val newCtxt = ctxt.copy(nargs = 1, argvec = Tuple(Ob.NIV))
    flToFx.fnSimple(newCtxt) should be('left)
  }
}
