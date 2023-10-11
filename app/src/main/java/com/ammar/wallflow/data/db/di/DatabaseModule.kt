package com.ammar.wallflow.data.db.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ammar.wallflow.IoDispatcher
import com.ammar.wallflow.data.db.AppDatabase
import com.ammar.wallflow.data.db.manualmigrations.MIGRATION_1_2
import com.ammar.wallflow.data.db.manualmigrations.MIGRATION_3_4
import com.ammar.wallflow.data.db.manualmigrations.MIGRATION_6_7
import com.ammar.wallflow.extensions.TAG
import com.ammar.wallflow.model.ObjectDetectionModel
import com.ammar.wallflow.model.toEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideLastUpdatedDao(appDatabase: AppDatabase) = appDatabase.lastUpdatedDao()

    @Provides
    fun providesWallhavenPopularTagsDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenPopularTagsDao()

    @Provides
    fun providesWallhavenSearchQueryDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenSearchQueryDao()

    @Provides
    fun providesWallhavenSearchQueryRemoteKeysDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenSearchQueryRemoteKeysDao()

    @Provides
    fun providesWallhavenSearchQueryWallpapersDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenSearchQueryWallpapersDao()

    @Provides
    fun providesWallhavenWallpapersDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenWallpapersDao()

    @Provides
    fun providesWallhavenTagsDao(appDatabase: AppDatabase) = appDatabase.wallhavenTagsDao()

    @Provides
    fun providesWallhavenSearchHistoryDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenSearchHistoryDao()

    @Provides
    fun providesObjectDetectionModelDao(appDatabase: AppDatabase) =
        appDatabase.objectDetectionModelDao()

    @Provides
    fun providesWallhavenSavedSearchDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenSavedSearchDao()

    @Provides
    fun providesAutoWallpaperHistoryDao(appDatabase: AppDatabase) =
        appDatabase.autoWallpaperHistoryDao()

    @Provides
    fun providesFavoritesDao(appDatabase: AppDatabase) = appDatabase.favoriteDao()

    @Provides
    fun providesWallhavenUploadersDao(appDatabase: AppDatabase) =
        appDatabase.wallhavenUploadersDao()

    @Provides
    fun providesRateLimitDao(appDatabase: AppDatabase) = appDatabase.rateLimitDao()

    lateinit var appDatabase: AppDatabase

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AppDatabase {
        appDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app",
        ).apply {
            addMigrations(
                MIGRATION_1_2,
                MIGRATION_3_4,
                MIGRATION_6_7,
            )
            addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        val handler = CoroutineExceptionHandler { _, e ->
                            Log.e(TAG, "onCreate: ", e)
                        }
                        CoroutineScope(ioDispatcher).launch(handler) {
                            // insert default models
                            Log.i(TAG, "onCreate: Inserting default model")
                            appDatabase.objectDetectionModelDao().upsert(
                                ObjectDetectionModel.DEFAULT.toEntity(),
                            )
                        }
                    }
                },
            )
        }.build()
        return appDatabase
    }
}
