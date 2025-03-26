package com.proyek.foolens.di

import com.proyek.foolens.AppInitializer
import com.proyek.foolens.DefaultAppInitializer
import com.proyek.foolens.data.repository.AuthRepositoryImpl
import com.proyek.foolens.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAppInitializer(
        defaultAppInitializer: DefaultAppInitializer
    ): AppInitializer
}