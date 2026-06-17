package com.example.grabthisforme.model.AppDataBase

import android.content.Context
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationDao
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationUserStateDao
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.friendAndGroup.data.local.dao.FriendAndGroupDao
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.ChatGroupEntity
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.goods.data.local.dao.GoodsDao
import com.example.grabthisforme.model.goods.data.local.entity.GoodsBaseEntity
import com.example.grabthisforme.model.goods.data.local.entity.GoodsPriceEntity
import com.example.grabthisforme.model.goods.data.local.entity.GoodsStateEntity
import com.example.grabthisforme.model.goods.data.local.entity.GoodsUiEntity
import com.example.grabthisforme.model.message.data.local.dao.MessageDao
import com.example.grabthisforme.model.message.data.local.entity.MessageEntity
import com.example.grabthisforme.model.order.data.local.dao.OrderDao
import com.example.grabthisforme.model.order.data.local.entity.OrderEntity
import com.example.grabthisforme.model.post.data.local.dao.PostDao
import com.example.grabthisforme.model.post.data.local.dao.PostStatsDao
import com.example.grabthisforme.model.post.data.local.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.local.entity.PostEntity
import com.example.grabthisforme.model.post.data.local.entity.PostReplyEntity
import com.example.grabthisforme.model.post.data.local.entity.PostStatsEntity
import com.example.grabthisforme.model.relation.data.dao.ConversationRelationDao
import com.example.grabthisforme.model.relation.data.dao.StoreRelationDao
import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.entity.ConversationParticipantEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryItemEntity
import com.example.grabthisforme.model.relation.data.entity.StoreTagEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.relation.data.entity.UserPostEntity
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.store.data.local.dao.StoreDao
import com.example.grabthisforme.model.store.data.local.entity.StoreEntity
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.data.local.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.local.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.local.entity.UserStatisticsEntity

@Database(
    entities = [
        SearchContent::class,
        UserAccountEntity::class,
        UserProfileEntity::class,
        UserStatisticsEntity::class,
        GoodsBaseEntity::class,
        GoodsPriceEntity::class,
        GoodsUiEntity::class,
        GoodsStateEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        ConversationUserStateEntity::class,
        SecondhandTradeEntity::class,
        OrderEntity::class,
        PostEntity::class,
        PostStatsEntity::class,
        UserPostEntity::class,
        UserLikedPostEntity::class,
        UserLikedStoreEntity::class,
        UserLikedGoodsEntity::class,
        PostCommentEntity::class,
        PostReplyEntity::class,
        StoreEntity::class,
        ConversationParticipantEntity::class,
        ChatGroupEntity::class,
        UserFriendRelationEntity::class,
        UserGroupRelationEntity::class,
        StoreGoodsCategoryEntity::class,
        StoreGoodsCategoryItemEntity::class,
        StoreTagEntity::class
    ],
    version = 34,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchDao(): SearchDao
    abstract fun userDao(): UserDao
    abstract fun goodsDao(): GoodsDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun conversationUserStateDao(): ConversationUserStateDao
    abstract fun conversationRelationDao(): ConversationRelationDao
    abstract fun orderDao(): OrderDao
    abstract fun postDao(): PostDao
    abstract fun postStatsDao(): PostStatsDao
    abstract fun userRelationDao(): UserRelationDao
    abstract fun storeRelationDao(): StoreRelationDao
    abstract fun storeDao(): StoreDao
    abstract fun friendAndGroupDao(): FriendAndGroupDao
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
