package com.example.grabthisforme.model.Order

import com.example.grabthisforme.model.goos.Goods
import com.example.grabthisforme.model.user.User

class Order(
    var sender : User? = null,
    var orderId: String,
    var buyer: User,
    var goods: Goods
){
    companion object {
        private var buyerList: List<User>? = null
        private var deliveryPersonList: List<User>? = null
        private var orderList: List<Order>? = null

        fun getBuyerList(): List<User> {
            if (buyerList == null) {
                buyerList = listOf(
                    // 买家1
                    User(
                        name = "张三",
                        id = 10001L,
                        headPic = "avatar_zhangsan", // 对应drawable中的头像名称
                        phone = "13800138000",
                        email = "zhangsan@example.com",
                        gender = 1, // 1-男，0-女
                        isVip = true,
                        signature = "爱吃零食，快乐至上～"
                    ),
                    // 买家2
                    User(
                        name = "李四",
                        id = 10002L,
                        headPic = "avatar_lisi",
                        phone = "13900139000",
                        email = "lisi@example.com",
                        gender = 0,
                        isVip = false,
                        signature = "日用品囤货达人"
                    ),
                    // 买家3
                    User(
                        name = "王五",
                        id = 10003L,
                        headPic = "avatar_wangwu",
                        phone = "13700137000",
                        email = "wangwu@example.com",
                        gender = 1,
                        isVip = true,
                        signature = "奶茶续命，快乐翻倍！"
                    ),
                    // 买家4
                    User(
                        name = "赵六",
                        id = 10004L,
                        headPic = "avatar_zhaoliu",
                        phone = "13600136000",
                        email = "zhaoliu@example.com",
                        gender = 0,
                        isVip = false,
                        signature = "生鲜食材，新鲜才好吃～"
                    ),
                    // 买家5
                    User(
                        name = "孙七",
                        id = 10005L,
                        headPic = "avatar_sunqi",
                        phone = "13500135000",
                        email = "sunqi@example.com",
                        gender = 1,
                        isVip = true,
                        signature = "办公文具，一站式采购"
                    )
                )
            }
            return buyerList!!
        }

        fun getDeliveryPersonList(): List<User> {
            if (deliveryPersonList == null) {
                deliveryPersonList = listOf(
                    User(
                        name = "配送员-马八",
                        id = 20001L,
                        headPic = "delivery_avatar_maba",
                        phone = "18800188000",
                        gender = 1,
                        signature = "风雨无阻，准时送达！"
                    ),
                    User(
                        name = "配送员-周九",
                        id = 20002L,
                        headPic = "delivery_avatar_zhoujiu",
                        phone = "18900189000",
                        gender = 0,
                        signature = "用心配送每一份订单～"
                    ),
                    User(
                        name = "配送员-吴十",
                        id = 20003L,
                        headPic = "delivery_avatar_wushi",
                        phone = "18700187000",
                        gender = 1,
                        signature = "高效配送，服务至上"
                    )
                )
            }
            return deliveryPersonList!!
        }
        fun getOrderList(): List<Order> {
            if (orderList == null) {
                // 先初始化依赖的买家和配送员列表
                val buyers = getBuyerList()
                val deliveryPersons = getDeliveryPersonList()

                orderList = listOf<Order>(
                    // 订单1：食品类（零食）
                    Order(
                        sender = deliveryPersons[0], // 分配配送员
                        orderId = "ORD${System.currentTimeMillis() - 100000}", // 唯一订单号（基于时间戳）
                        buyer = buyers[0], // 买家
                        goods = Goods(
                            id = 1001L,
                            name = "薯片+可乐组合装（大份）",
                            message = "薯片要原味，可乐要冰镇", // 商品备注
                            shelf_number = "超市A区-零食架-08号",
                            aim_position = "XX小区3号楼2单元502室",
                            price = 19.9,
                            sale_number = 12000L, // 销量
                            pic = "food_snack",
                            startTime = System.currentTimeMillis(), // 配送开始时间
                            endTime = System.currentTimeMillis() + 3 * 60 * 60 * 1000L // 3小时后截止
                        )
                    ),
                    // 订单2：日用品类
                    Order(
                        sender = deliveryPersons[1],
                        orderId = "ORD${System.currentTimeMillis() - 80000}",
                        buyer = buyers[1],
                        goods = Goods(
                            id = 1002L,
                            name = "抽纸+洗衣液+牙膏（家庭装）",
                            message = "洗衣液要薰衣草香型，牙膏要防蛀款",
                            shelf_number = "便利店B区-日用品架-03号",
                            aim_position = "YY写字楼B座808室",
                            price = 45.5,
                            sale_number = 8500L,
                            pic = "daily_item",
                            startTime = System.currentTimeMillis() + 1 * 60 * 60 * 1000L,
                            endTime = System.currentTimeMillis() + 4 * 60 * 60 * 1000L
                        )
                    ),
                    // 订单3：饮品类
                    Order(
                        sender = deliveryPersons[2],
                        orderId = "ORD${System.currentTimeMillis() - 60000}",
                        buyer = buyers[2],
                        goods = Goods(
                            id = 1003L,
                            name = "珍珠奶茶（大杯）+柠檬茶（中杯）",
                            message = "奶茶少糖少冰，柠檬茶正常糖",
                            shelf_number = "奶茶店-出餐台-12号",
                            aim_position = "ZZ商场5楼美食城",
                            price = 28.0,
                            sale_number = 25000L,
                            pic = "drink",
                            startTime = System.currentTimeMillis() + 30 * 60 * 1000L,
                            endTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000L
                        )
                    ),
                    // 订单4：生鲜类
                    Order(
                        sender = deliveryPersons[0],
                        orderId = "ORD${System.currentTimeMillis() - 40000}",
                        buyer = buyers[3],
                        goods = Goods(
                            id = 1004L,
                            name = "草莓（1斤）+西红柿（2斤）+黄瓜（1斤）",
                            message = "草莓要新鲜的，西红柿选沙瓤的",
                            shelf_number = "生鲜超市-果蔬区-15号",
                            aim_position = "AA小区1号楼1单元101室",
                            price = 32.8,
                            sale_number = 9800L,
                            pic = "fresh_food",
                            startTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000L,
                            endTime = System.currentTimeMillis() + 5 * 60 * 60 * 1000L
                        )
                    ),
                    // 订单5：文具类
                    Order(
                        sender = deliveryPersons[1],
                        orderId = "ORD${System.currentTimeMillis() - 20000}",
                        buyer = buyers[4],
                        goods = Goods(
                            id = 1005L,
                            name = "笔记本（5本）+中性笔（10支）+橡皮（2块）",
                            message = "笔记本要加厚款，中性笔黑色0.5mm",
                            shelf_number = "文具店-办公区-06号",
                            aim_position = "BB小学门口传达室",
                            price = 15.3,
                            sale_number = 6500L,
                            pic = "stationery",
                            startTime = System.currentTimeMillis() + 1 * 60 * 60 * 1000L,
                            endTime = System.currentTimeMillis() + 3 * 60 * 60 * 1000L
                        )
                    )
                )
            }
            return orderList!!
        }
    }
}