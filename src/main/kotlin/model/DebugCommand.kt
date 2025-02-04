package model

import util.StreamUtil

abstract class DebugCommand {

    abstract fun writeTo(stream: java.io.OutputStream)
    companion object {

        fun readFrom(stream: java.io.InputStream): DebugCommand {
            when (StreamUtil.readInt(stream)) {
                Add.TAG -> return Add.readFrom(stream)
                Clear.TAG -> return Clear.readFrom(stream)
                else -> throw java.io.IOException("Unexpected tag value")
            }
        }
    }

    class Add : DebugCommand {
        lateinit var data: model.DebugData
        constructor() {}
        constructor(data: model.DebugData) {
            this.data = data
        }
        companion object {
            val TAG = 0

            fun readFrom(stream: java.io.InputStream): Add {
                val result = Add()
                result.data = model.DebugData.readFrom(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            data.writeTo(stream)
        }
    }

    class Clear : DebugCommand {
        constructor() {}
        companion object {
            val TAG = 1

            fun readFrom(stream: java.io.InputStream): Clear {
                val result = Clear()
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
        }
    }
}
