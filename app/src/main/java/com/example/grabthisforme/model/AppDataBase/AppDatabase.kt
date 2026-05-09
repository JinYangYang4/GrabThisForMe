package com.example.grabthisforme.model.AppDataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import com.example.grabthisforme.model.conversation.data.dao.ConversationDao
import com.example.grabthisforme.model.goods.data.dao.GoodsDao
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsPriceEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsStateEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsUiEntity
import com.example.grabthisforme.model.messageContent.data.dao.MessageDao
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.messageContent.data.entity.MessageEntity
import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.order.data.entity.OrderEntity
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity

@Database(
    entities = [
        SearchContent::class,
        UserAccountEntity::class,
        UserProfileEntity::class,
        GoodsBaseEntity::class,
        GoodsPriceEntity::class,
        GoodsUiEntity::class,
        GoodsStateEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        SecondhandTradeEntity::class,
        OrderEntity::class,
        PostEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchDao(): SearchDao
    abstract fun userDao(): UserDao
    abstract fun goodsDao(): GoodsDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun orderDao(): OrderDao
    abstract fun postDao(): PostDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grab_this_for_me_core_db"
                )

                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
