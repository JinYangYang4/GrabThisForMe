package com.example.grabthisforme.model.AppDataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao
import com.example.grabthisforme.model.conversation.data.dao.ConversationDao
import com.example.grabthisforme.model.conversation.data.dao.ConversationUserStateDao
import com.example.grabthisforme.model.goods.data.dao.GoodsDao
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsPriceEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsStateEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsUiEntity
import com.example.grabthisforme.model.friendAndGroup.data.dao.FriendAndGroupDao
import com.example.grabthisforme.model.friendAndGroup.data.entity.ChatGroupEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.message.data.dao.MessageDao
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.data.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.message.data.entity.MessageEntity
import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.data.dao.PostStatsDao
import com.example.grabthisforme.model.post.data.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.post.data.entity.PostReplyEntity
import com.example.grabthisforme.model.post.data.entity.PostStatsEntity
import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.order.data.entity.OrderEntity
import com.example.grabthisforme.model.relation.data.dao.ConversationRelationDao
import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.dao.StoreRelationDao
import com.example.grabthisforme.model.relation.data.entity.ConversationParticipantEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryItemEntity
import com.example.grabthisforme.model.relation.data.entity.StoreTagEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.relation.data.entity.UserPostEntity
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.store.data.dao.StoreDao
import com.example.grabthisforme.model.store.data.entity.StoreEntity
import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.entity.UserStatisticsEntity

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
