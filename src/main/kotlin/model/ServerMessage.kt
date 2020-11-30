package model

import util.StreamUtil

abstract class ServerMessage {

    abstract fun writeTo(stream: java.io.OutputStream)
    companion object {

        fun readFrom(stream: java.io.InputStream): ServerMessage {
            when (StreamUtil.readInt(stream)) {
                GetAction.TAG -> return GetAction.readFrom(stream)
                Finish.TAG -> return Finish.readFrom(stream)
                DebugUpdate.TAG -> return DebugUpdate.readFrom(stream)
                else -> throw java.io.IOException("Unexpected tag value")
            }
        }
    }

    class GetAction : ServerMessage {
        lateinit var playerView: model.PlayerView
        var debugAvailable: Boolean = false
        constructor() {}
        constructor(playerView: model.PlayerView, debugAvailable: Boolean) {
            this.playerView = playerView
            this.debugAvailable = debugAvailable
        }
        companion object {
            val TAG = 0

            fun readFrom(stream: java.io.InputStream): GetAction {
                val result = GetAction()
                result.playerView = model.PlayerView.readFrom(stream)
                result.debugAvailable = StreamUtil.readBoolean(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            playerView.writeTo(stream)
            StreamUtil.writeBoolean(stream, debugAvailable)
        }
    }

    class Finish : ServerMessage {
        constructor() {}
        companion object {
            val TAG = 1

            fun readFrom(stream: java.io.InputStream): Finish {
                val result = Finish()
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
        }
    }

    class DebugUpdate : ServerMessage {
        lateinit var playerView: model.PlayerView
        constructor() {}
        constructor(playerView: model.PlayerView) {
            this.playerView = playerView
        }
        companion object {
            val TAG = 2

            fun readFrom(stream: java.io.InputStream): DebugUpdate {
                val result = DebugUpdate()
                result.playerView = model.PlayerView.readFrom(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            playerView.writeTo(stream)
        }
    }
}
