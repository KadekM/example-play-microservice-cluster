package common

object BarEvents {
  final val KEY = "bar-events"

  final case class EntityInserted(allEntities: Seq[Int])
}
