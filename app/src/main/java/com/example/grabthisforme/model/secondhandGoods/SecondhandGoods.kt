package com.example.grabthisforme.model.secondhandGoods

import com.example.grabthisforme.model.goods.Goods
import com.example.grabthisforme.model.user.User

class SecondhandGoods(
    saleUser : User? =null,
    id: Long,
    name: String,
    message: String,
    secondhandPrice: Double,
    sale_number: Long,
    pic: String,
    val originalPrice: Double, // 原价（全新价格）
    val quality: String, // 物品成色（如 99 新、95 新、8 新）
    val usedTime: String?, // 使用时长（可选，如 "6 个月"）
) : Goods(id, name, message, secondhandPrice, sale_number, pic, category = Goods.GoodsCategory.CLOTHING){
    companion object SecondhandGoodsMockData {
        // 商品名称数据源（覆盖不同类别，贴合二手场景）
        private val goodsNames = listOf(
            "iPhone 14 128G 蓝色 无磕碰 配件齐全",
            "《Android 开发艺术探索》 无笔迹 品相完好",
            "Nike 运动鞋 42码 仅穿3次 无磨损",
            "美的电饭煲 家用 95新 功能正常",
            "雅诗兰黛小棕瓶 剩余80% 专柜购入",
            "哑铃套装 20kg 几乎全新 自提优先",
            "休闲卫衣 M码 纯棉 无起球",
            "零食大礼包 未拆封 保质期还有6个月",
            "iPad 9代 10.2英寸 64G 电池健康98%",
            "床上四件套 纯棉 1.8m床 仅使用1次",
            "《Java 编程思想》 第4版 少量笔记",
            "Adidas 双肩包 黑色 无污渍 容量大",
            "苏泊尔平底锅 不粘涂层 无划痕",
            "兰蔻粉底液 自然色 剩余70% 无盒",
            "瑜伽垫 10mm 防滑 几乎全新",
            "牛仔外套 L码 宽松款 无破损"
        )

        private val goodsQualities = listOf("99新", "95新", "9新", "85新", "8新")

        private val goodsPicUrls = listOf(
            "ic_goods_default",
            "ic_goods_default",
            "ic_goods_default",
            "ic_goods_default",
            "ic_goods_default"
        )


        fun generateMockData(count: Int): List<SecondhandGoods> {
            val mockList = mutableListOf<SecondhandGoods>()

            for (i in 0 until count) {
                // 随机选取数据，保证每条数据有差异
                val id = i.toLong() + 1 // 商品ID 从1开始递增
                val name = goodsNames[i % goodsNames.size]
                val message = "个人闲置 $name 诚心出，可小刀，非诚勿扰" // 商品描述
                val secondhandPrice = (199 + (i % 50) * 100).toDouble() // 二手价 199~5099 区间
                val saleNumber = (i % 20).toLong() // 销量 0~19 区间
                val pic = goodsPicUrls[i % goodsPicUrls.size] // 商品图片
                val originalPrice = secondhandPrice + (50 + (i % 30) * 50) // 原价 高于二手价
                val quality = goodsQualities[i % goodsQualities.size] // 商品成色
                val usedTime = when (i % 6) { // 使用时长 可选值
                    0 -> "1个月"
                    1 -> "3个月"
                    2 -> "6个月"
                    3 -> "1年"
                    4 -> "2年"
                    else -> null // 部分商品不填使用时长
                }

                // 创建 SecondhandGoods 实例并添加到列表
                val secondhandGoods = SecondhandGoods(
                    id = id,
                    name = name,
                    message = message,
                    secondhandPrice = secondhandPrice,
                    sale_number = saleNumber,
                    pic = pic,
                    originalPrice = originalPrice,
                    quality = quality,
                    usedTime = usedTime
                )

                mockList.add(secondhandGoods)
            }

            return mockList
        }


        fun generateDefaultMockData(): List<SecondhandGoods> {
            return generateMockData(50)
        }
    }
}