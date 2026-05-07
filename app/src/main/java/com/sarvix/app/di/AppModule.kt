package com.sarvix.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.ChatRepository
import com.sarvix.app.data.repository.MatchRepository
import com.sarvix.app.data.repository.PostRepository
import com.sarvix.app.data.repository.ProfileRepository
import com.sarvix.app.data.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository = ProfileRepository(firestore, storage)

    @Provides
    @Singleton
    fun provideTranslationRepository(): TranslationRepository = TranslationRepository()

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        translationRepository: TranslationRepository
    ): ChatRepository = ChatRepository(firestore, authRepository, translationRepository)

    @Provides
    @Singleton
    fun provideMatchRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): MatchRepository = MatchRepository(firestore, authRepository)

    @Provides
    @Singleton
    fun providePostRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        authRepository: AuthRepository,
        translationRepository: TranslationRepository
    ): PostRepository = PostRepository(firestore, storage, authRepository, translationRepository)
}