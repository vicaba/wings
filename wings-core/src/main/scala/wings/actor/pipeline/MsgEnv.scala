package wings.actor.pipeline

/**
  * Created by vicaba on 04/12/15.
  */
object MsgEnv {

  case class ToDevice[M](msg: M)

  case class ToArch[M](msg: M)

}
