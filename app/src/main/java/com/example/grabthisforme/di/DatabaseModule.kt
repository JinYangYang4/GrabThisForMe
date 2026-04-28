package com.example.grabthisforme.di

import android.content.Context
import com.example.grabthisforme.model.AppDataBase.AppDatabase
import com.example.grabthisforme.model.conversation.data.dao.ConversationDao
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import com.example.grabthisforme.model.goods.data.dao.GoodsDao
import com.example.grabthisforme.model.messageContent.data.dao.MessageDao
import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.user.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) //ActivityComponent、FragmentComponent    安装到【全局单例】生命周期
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    @Provides
    fun provideSearchDao(database: AppDatabase): SearchDao {
        return database.searchDao()
    }
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao{
        return database.userDao()
    }

    @Provides
    fun provideGoodsDao(database: AppDatabase): GoodsDao {
        return database.goodsDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }
    @Provides
    fun provideOrderDao(database : AppDatabase) : OrderDao{
        return database.orderDao()
    }
}
