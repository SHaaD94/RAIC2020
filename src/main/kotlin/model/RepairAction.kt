package model

import util.StreamUtil

class RepairAction {
    var target: Int = 0
    constructor() {}
    constructor(target: Int) {
        this.target = target
    }
    companion object {

        fun readFrom(stream: java.io.InputStream): RepairAction {
            val result = RepairAction()
            result.target = StreamUtil.readInt(stream)
            return result
        }
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeInt(stream, target)
    }
}
