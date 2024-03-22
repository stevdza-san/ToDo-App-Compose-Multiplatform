package data

import domain.RequestState
import domain.ToDoTask
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MongoDB {
    private var realm: Realm? = null

    init {
        configureTheRealm()
    }

    private fun configureTheRealm() {
        if (realm == null || realm!!.isClosed()) {
            val config = RealmConfiguration.Builder(
                schema = setOf(ToDoTask::class)
            )
                .compactOnLaunch()
                .build()
            realm = Realm.open(config)
        }
    }

    fun readActiveTasks(): Flow<RequestState<List<ToDoTask>>> {
        return realm?.query<ToDoTask>(query = "completed == $0", false)
            ?.asFlow()
            ?.map { result ->
                RequestState.Success(
                    data = result.list.sortedByDescending { task -> task.favorite }
                )
            } ?: flow { RequestState.Error(message = "Realm is not available.") }
    }

    fun readCompletedTasks(): Flow<RequestState<List<ToDoTask>>> {
        return realm?.query<ToDoTask>(query = "completed == $0", true)
            ?.asFlow()
            ?.map { result -> RequestState.Success(data = result.list) }
            ?: flow { RequestState.Error(message = "Realm is not available.") }
    }

    suspend fun addTask(task: ToDoTask) {
        realm?.write { copyToRealm(task) }
    }

    suspend fun updateTask(task: ToDoTask) {
        realm?.write {
            try {
                val queriedTask = query<ToDoTask>("_id == $0", task._id)
                    .first()
                    .find()
                queriedTask?.let {
                    findLatest(it)?.let { currentTask ->
                        currentTask.title = task.title
                        currentTask.description = task.description
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    suspend fun setCompleted(task: ToDoTask, taskCompleted: Boolean) {
        realm?.write {
            try {
                val queriedTask = query<ToDoTask>(query = "_id == $0", task._id)
                    .find()
                    .first()
                queriedTask.apply { completed = taskCompleted }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    suspend fun setFavorite(task: ToDoTask, isFavorite: Boolean) {
        realm?.write {
            try {
                val queriedTask = query<ToDoTask>(query = "_id == $0", task._id)
                    .find()
                    .first()
                queriedTask.apply { favorite = isFavorite }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    suspend fun deleteTask(task: ToDoTask) {
        realm?.write {
            try {
                val queriedTask = query<ToDoTask>(query = "_id == $0", task._id)
                    .first()
                    .find()
                queriedTask?.let {
                    findLatest(it)?.let { currentTask ->
                        delete(currentTask)
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}