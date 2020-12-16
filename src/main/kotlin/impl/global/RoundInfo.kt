package impl.global

object RoundInfo {
    fun currentRound() =
        if (!State.playerView.fogOfWar)
            Round1
        else
            if (State.playerView.players.size == 4) Round2 else Final
}

abstract class Round
object Round1 : Round()
object Round2 : Round()
object Final : Round()
