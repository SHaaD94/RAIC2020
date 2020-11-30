package model

import util.StreamUtil

abstract class ClientMessage {

    abstract fun writeTo(stream: java.io.OutputStream)
    companion object {

        fun readFrom(stream: java.io.InputStream): ClientMessage {
            when (StreamUtil.readInt(stream)) {
                DebugMessage.TAG -> return DebugMessage.readFrom(stream)
                ActionMessage.TAG -> return ActionMessage.readFrom(stream)
                DebugUpdateDone.TAG -> return DebugUpdateDone.readFrom(stream)
                RequestDebugState.TAG -> return RequestDebugState.readFrom(stream)
                else -> throw java.io.IOException("Unexpected tag value")
            }
        }
    }

    class DebugMessage : ClientMessage {
        lateinit var command: model.DebugCommand
        constructor() {}
        constructor(command: model.DebugCommand) {
            this.command = command
        }
        companion object {
            val TAG = 0

            fun readFrom(stream: java.io.InputStream): DebugMessage {
                val result = DebugMessage()
                result.command = model.DebugCommand.readFrom(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            command.writeTo(stream)
        }
    }

    class ActionMessage : ClientMessage {
        lateinit var action: model.Action
        constructor() {}
        constructor(action: model.Action) {
            this.action = action
        }
        companion object {
            val TAG = 1

            fun readFrom(stream: java.io.InputStream): ActionMessage {
                val result = ActionMessage()
                result.action = model.Action.readFrom(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            action.writeTo(stream)
        }
    }

    class DebugUpdateDone : ClientMessage {
        constructor() {}
        companion object {
            val TAG = 2

            fun readFrom(stream: java.io.InputStream): DebugUpdateDone {
                val result = DebugUpdateDone()
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
        }
    }

    class RequestDebugState : ClientMessage {
        constructor() {}
        companion object {
            val TAG = 3

            fun readFrom(stream: java.io.InputStream): RequestDebugState {
                val result = RequestDebugState()
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
        }
    }
}
