package com.example.grabthisforme.model.goods.data.mock

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.UUID

internal object GoodsSampleData {
    fun get20RepeatGoods(): List<Goods> {
        val templates = listOf(
            Goods(
                id = 1L,
                storeId = 101L,
                name = "宿舍早餐三明治",
                message = "校内便利店现做，适合早八顺手带走",
                price = 12.8,
                sale_number = 368,
                pic = "food_pic",
                category = Goods.GoodsCategory.FOOD,
                discountPrice = 9.9,
                discountTag = "早八特惠",
                tag = "便利店 现做",
                stock = 35,
                isHot = true,
                soldCount = 124
            ),
            Goods(
                id = 2L,
                storeId = 102L,
                name = "图书馆自习咖啡",
                message = "拿铁加浓，支持备注少冰和燕麦奶",
                price = 18.0,
                sale_number = 225,
                pic = "food_pic_2",
                category = Goods.GoodsCategory.FOOD,
                discountPrice = 14.8,
                discountTag = "第二杯半价",
                tag = "咖啡店 热销",
                stock = 28,
                isHot = true,
                soldCount = 96
            ),
            Goods(
                id = 3L,
                storeId = 103L,
                name = "打印店论文装订套餐",
                message = "黑白双面打印，支持封面塑封和骑马钉",
                price = 22.0,
                sale_number = 119,
                pic = "food_pic_3",
                category = Goods.GoodsCategory.BOOK,
                discountPrice = 18.0,
                discountTag = "毕业季",
                tag = "打印店 刚需",
                stock = 60,
                soldCount = 43
            ),
            Goods(
                id = 4L,
                storeId = 104L,
                name = "社团活动应援短袖",
                message = "基础款纯棉短袖，可加印社团名",
                price = 39.9,
                sale_number = 88,
                pic = "food_pic_4",
                category = Goods.GoodsCategory.CLOTHING,
                discountPrice = 32.0,
                discountTag = "团购价",
                tag = "工作室 定制",
                stock = 42,
                soldCount = 17
            ),
            Goods(
                id = 5L,
                storeId = 105L,
                name = "宿舍收纳三件套",
                message = "床底盒、桌面盒和挂篮组合，适合小空间",
                price = 29.9,
                sale_number = 176,
                pic = "food_pic_5",
                category = Goods.GoodsCategory.HOME,
                discountPrice = 24.9,
                discountTag = "满2件95折",
                tag = "生活馆 热卖",
                stock = 55,
                soldCount = 71
            ),
            Goods(
                id = 6L,
                storeId = 106L,
                name = "考试周荧光笔套装",
                message = "五色一套，做重点和错题标记很顺手",
                price = 15.6,
                sale_number = 201,
                pic = "food_pic_6",
                category = Goods.GoodsCategory.BOOK,
                discountPrice = 12.9,
                discountTag = "文具周",
                tag = "文具店 推荐",
                stock = 80,
                soldCount = 89
            ),
            Goods(
                id = 7L,
                storeId = 107L,
                name = "羽毛球馆体验次卡",
                message = "可约晚场，适合两人组局",
                price = 48.0,
                sale_number = 64,
                pic = "food_pic_7",
                category = Goods.GoodsCategory.SPORT,
                discountPrice = 39.0,
                discountTag = "夜场优惠",
                tag = "运动馆 到店用",
                stock = 18,
                soldCount = 22
            ),
            Goods(
                id = 8L,
                storeId = 108L,
                name = "贴膜清洁保养包",
                message = "含手机贴膜、镜头膜和清洁套装",
                price = 26.8,
                sale_number = 143,
                pic = "food_pic_8",
                category = Goods.GoodsCategory.DIGITAL,
                discountPrice = 19.9,
                discountTag = "数码补给",
                tag = "数码店 低价",
                stock = 33,
                soldCount = 48
            )
        )

        return List(20) { index ->
            val template = templates[index % templates.size]
            Goods(
                id = (index + 1).toLong(),
                storeId = template.storeId,
                name = template.name,
                message = template.message,
                price = template.price,
                sale_number = template.saleNumber + index * 3,
                pic = template.pic,
                category = template.category,
                discountPrice = template.discountPrice,
                discountTag = template.discountTag,
                tag = template.tag,
                stock = (template.stock - index).coerceAtLeast(6),
                isSoldOut = index % 9 == 0,
                isHot = index % 3 != 1,
                soldCount = template.soldCount + index * 2
            )
        }
    }

    @Volatile
    private var mockSingleGoods: Goods? = null

    fun getSingleVirtualGoods(): Goods {
        if (mockSingleGoods == null) {
            synchronized(this) {
                if (mockSingleGoods == null) {
                    mockSingleGoods = Goods(
                        id = 1000 + (1..999).random().toLong(),
                        storeId = 208L,
                        name = "校园热卖套餐${UUID.randomUUID().toString().substring(0, 4)}",
                        message = "适合学生日常下单的高频商品，支持到店自取或即时送达",
                        price = 19.9 + (0..20).random(),
                        sale_number = 500 + (1..1000).random().toLong(),
                        pic = "food_pic_${(1..10).random()}",
                        category = Goods.GoodsCategory.FOOD,
                        discountPrice = 15.8,
                        discountTag = "店铺推荐",
                        tag = "人气 爆款",
                        stock = 88,
                        isHot = true,
                        soldCount = 126
                    )
                }
            }
        }
        return mockSingleGoods!!
    }
}
