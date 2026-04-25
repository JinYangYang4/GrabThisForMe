package com.example.grabthisforme.model.secondhandGoods.data.mock

import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods

internal object SecondhandGoodsSampleData {
    private val goodsNames = listOf(
        "iPhone 14 128G 蓝色 无磕碰 配件齐全",
        "Android 开发艺术笔记 无笔迹 品相完好",
        "Nike 运动鞋 42码 仅穿3次 无磨损",
        "美的电饭煲 家用 95新 功能正常",
        "雅诗兰黛小棕瓶 剩余80% 专柜购入",
        "哑铃套装 20kg 几乎全新 自提优先",
        "休闲卫衣 M码 纯棉 无起球",
        "零食大礼包 未拆封 保质期还有3个月",
        "iPad 9代 10.2英寸 64G 电池健康98%",
        "床上四件套 纯棉 1.8m床 仅使用2次",
        "Java 编程思想 第4版 少量笔记",
        "Adidas 双肩包 黑色 无污渍 容量大",
        "苏泊尔平底锅 不粘涂层 无划痕",
        "兰蔻粉底液 自然色 剩余70% 无盒",
        "瑜伽垫 10mm 防滑 几乎全新",
        "牛仔外套 L码 宽松款 无破损"
    )

    private val goodsQualities = listOf("99新", "95新", "9成新", "85新", "8成新")

    private val goodsPicUrls = listOf(
        "ic_goods_default",
        "ic_goods_default",
        "ic_goods_default",
        "ic_goods_default",
        "ic_goods_default"
    )

    fun generateMockData(count: Int): List<SecondhandGoods> {
        val mockList = mutableListOf<SecondhandGoods>()

        for (index in 0 until count) {
            val id = index.toLong() + 1
            val name = goodsNames[index % goodsNames.size]
            val message = "个人闲置 $name，诚心出，可小刀，非诚勿扰"
            val secondhandPrice = (199 + (index % 50) * 100).toDouble()
            val saleNumber = (index % 20).toLong()
            val pic = goodsPicUrls[index % goodsPicUrls.size]
            val originalPrice = secondhandPrice + (50 + (index % 30) * 50)
            val quality = goodsQualities[index % goodsQualities.size]
            val usedTime = when (index % 6) {
                0 -> "1个月"
                1 -> "3个月"
                2 -> "6个月"
                3 -> "1年"
                4 -> "2年"
                else -> null
            }

            mockList.add(
                SecondhandGoods(
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
            )
        }

        return mockList
    }

    fun generateDefaultMockData(): List<SecondhandGoods> {
        return generateMockData(50)
    }
}

