package com.example.keyreceiveddemo.di

import android.content.Context
import com.example.keyreceiveddemo.data.EncryptedPreferencesImpl
import com.example.keyreceiveddemo.domain.EncryptedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptedPreferencesModule {
    @Provides
    @Singleton
    fun provideEncryptedPreferences(
        @ApplicationContext context: Context
    ): EncryptedPreferences {
        return EncryptedPreferencesImpl(context = context)
    }
}