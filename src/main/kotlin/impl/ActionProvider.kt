package impl

import model.EntityAction

interface ActionProvider {
    fun provideActions(): Map<Int, EntityAction>
}