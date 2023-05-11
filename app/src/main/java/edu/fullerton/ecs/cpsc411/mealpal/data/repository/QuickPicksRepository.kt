package edu.fullerton.ecs.cpsc411.mealpal.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.fullerton.ecs.cpsc411.mealpal.modules.ApplicationScope
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.QuickPicks as QuickPicksEnum
import edu.fullerton.ecs.cpsc411.mealpal.modules.QuickPicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickPicksRepository @Inject constructor(
    @QuickPicks private val quickPicksNetworkDataSource: CollectionReference,
    @ApplicationScope private val appScope: CoroutineScope
) {
    private val quickPickRanks = mutableMapOf<QuickPicksEnum, Int>()
    private val getRanksJob: Job = appScope.launch {
        QuickPicksEnum.values().forEach {
            quickPickRanks[it] = try {
                val docRef = quickPicksNetworkDataSource.document(it.name)
                val quickPickDocument = docRef.get().await()
                if (quickPickDocument.data != null) {
                    Timber.d("DocumentSnapshot data: ${quickPickDocument.data}")
                    quickPickDocument.getLong("rank")?.toInt() ?: 0
                } else {
                    Timber.d("No such document")
                    docRef.set(mapOf("created" to System.currentTimeMillis()), SetOptions.merge())
                    0
                }
            } catch (e : Exception) {
                Timber.e(e, "Get Quick Pick rank failed")
                0
            }
        }
    }

    suspend fun getRankedQuickPicks() : List<QuickPicksEnum> {
        getRanksJob.join()
        return quickPickRanks.entries.sortedByDescending { it.value }.map { it.key }
    }

    fun increaseQuickPickRank(pickName: String) {
        quickPicksNetworkDataSource.document(pickName).update("rank", FieldValue.increment(1))
            .addOnFailureListener { e -> Timber.e(e, "Failed to update quick pick rank") }
    }
}