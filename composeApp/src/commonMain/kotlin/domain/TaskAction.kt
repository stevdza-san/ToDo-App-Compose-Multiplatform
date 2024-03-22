package domain

sealed class TaskAction {
    data class Add(val task: ToDoTask) : TaskAction()
    data class Update(val task: ToDoTask) : TaskAction()
    data class Delete(val task: ToDoTask) : TaskAction()
    data class SetCompleted(val task: ToDoTask, val completed: Boolean) : TaskAction()
    data class SetFavorite(val task: ToDoTask, val isFavorite: Boolean) : TaskAction()
}