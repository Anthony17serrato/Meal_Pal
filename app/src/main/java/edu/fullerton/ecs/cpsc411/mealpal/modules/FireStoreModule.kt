package edu.fullerton.ecs.cpsc411.mealpal.modules

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class QuickPicks

@InstallIn(SingletonComponent::class)
@Module
object FireStoreModule {
    @QuickPicks
    @Provides
    fun provideQuickPicksCollection(firestore: FirebaseFirestore) : CollectionReference {
        return firestore.collection("quick_picks")
    }

    @Provides
    @Singleton
    fun provideFireStore() : FirebaseFirestore {
        return Firebase.firestore
    }
}