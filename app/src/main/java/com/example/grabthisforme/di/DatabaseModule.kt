package com.example.grabthisforme.di

import android.content.Context
import androidx.room.Room
import com.example.grabthisforme.model.AppDataBase.AppDatabase
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import com.example.grabthisforme.model.user.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) //ActivityComponent、FragmentComponent    安装到【全局单例】生命周期
object DatabaseModule {
    // 提供单例数据库
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "grab_this_for_me_core_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // 提供 Dao
    @Provides
    fun provideSearchDao(database: AppDatabase): SearchDao {
        return database.searchDao()
    }
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao{
        return database.userDao()
    }
}