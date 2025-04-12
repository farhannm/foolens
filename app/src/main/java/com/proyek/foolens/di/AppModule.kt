package com.proyek.foolens.di

import com.proyek.foolens.AppInitializer
import com.proyek.foolens.DefaultAppInitializer
import com.proyek.foolens.data.remote.api.ApiService
import com.proyek.foolens.data.repository.AllergenRepositoryImpl
import com.proyek.foolens.data.repository.AuthRepositoryImpl
import com.proyek.foolens.data.repository.ProfileRepositoryImpl
import com.proyek.foolens.data.repository.UserAllergenRepositoryImpl
import com.proyek.foolens.domain.repository.AllergenRepository
import com.proyek.foolens.domain.repository.AuthRepository
import com.proyek.foolens.domain.repository.ProfileRepository
import com.proyek.foolens.domain.repository.UserAllergenRepository
import com.proyek.foolens.domain.usecases.AllergenUseCase
import com.proyek.foolens.domain.usecases.AuthUseCase
import com.proyek.foolens.domain.usecases.ProfileUseCase
import com.proyek.foolens.domain.usecases.UserAllergenUseCase
import com.proyek.foolens.util.TokenManager
import dagger.Binds
import dagger.Module
import dagger.Provides
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
    abstract fun bindAllergenRepository(
        allergenRepositoryImpl: AllergenRepositoryImpl
    ): AllergenRepository

    @Binds
    @Singleton
    abstract fun bindUserAllergenRepository(
        userAllergenRepositoryImpl: UserAllergenRepositoryImpl
    ): UserAllergenRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindAppInitializer(
        defaultAppInitializer: DefaultAppInitializer
    ): AppInitializer

    companion object {
        @Provides
        @Singleton
        fun provideAuthUseCase(
            authRepository: AuthRepository,
            tokenManager: TokenManager
        ): AuthUseCase {
            return AuthUseCase(authRepository, tokenManager)
        }

        @Provides
        @Singleton
        fun provideAllergenUseCase(
            allergenRepository: AllergenRepository
        ): AllergenUseCase {
            return AllergenUseCase(allergenRepository)
        }

        @Provides
        @Singleton
        fun provideUserAllergenUseCase(
            userAllergenRepository: UserAllergenRepository
        ): UserAllergenUseCase {
            return UserAllergenUseCase(userAllergenRepository)
        }

        @Provides
        @Singleton
        fun provideProfileUseCase(
            profileRepository: ProfileRepository
        ): ProfileUseCase {
            return ProfileUseCase(profileRepository)
        }
    }
}