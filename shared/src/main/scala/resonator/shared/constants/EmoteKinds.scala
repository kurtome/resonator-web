package resonator.shared.constants

object EmoteKinds extends Enumeration {
  type EmoteKind = EmoteKinds.Value
  val Heart = Value("heart")
  val Laugh = Value("laugh")
  val Cry = Value("cry")
  val Scowl = Value("scowl")
}
