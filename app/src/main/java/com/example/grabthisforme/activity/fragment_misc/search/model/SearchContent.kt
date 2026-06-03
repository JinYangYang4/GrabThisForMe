package com.example.grabthisforme.activity.fragment_misc.search.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search")
data class SearchContent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val search_time: Long,
    val content: String,
    val searchType : String = SearchType.SHOPPING
){
    object SearchType {
        const val SHOPPING = "shopping"
        const val COMMUNITY = "community"
        const val FRIEND = "friend"
        const val STORE = "store"
    }
    object SearchRecommendations {

        fun getGuessYouSearch(): List<SearchContent> {
            val suggestions = listOf(
                "2025新款手机", "性价比高的笔记本电脑", "纯棉短袖T恤", "卧室收纳柜",
                "考研英语真题", "补水保湿面膜", "瑜伽垫防滑", "无添加坚果礼盒",
                "无线蓝牙耳机", "牛仔裤男宽松", "厨房置物架", "小学生课外读物",
                "口红哑光显白", "哑铃套装家用", "速食螺蛳粉", "智能手表测心率",
                "连衣裙夏季显瘦", "客厅沙发套", "中性笔按动0.5", "防晒霜50+",
                "羽毛球拍碳素", "每日坚果混合装", "平板电脑2025款", "帆布鞋女百搭",
                "卫生间置物架", "儿童绘本3-6岁", "粉底液持妆不卡粉", "跑步机家用小型",
                "自热火锅懒人", "充电宝大容量便携"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 1000, content = content)
            }
        }

        // 2. 数码产品
        fun getDigitalProducts(): List<SearchContent> {
            val suggestions = listOf(
                "苹果16 Pro Max", "华为Mate70 Pro", "小米笔记本Pro 2025", "联想拯救者Y9000P",
                "AirPods Pro二代", "索尼WH-1000XM6", "三星S25 Ultra", "iPad Pro 12.9英寸",
                "大疆Mini 4无人机", "GoPro Hero13", "机械键盘青轴", "罗技GPW三代鼠标",
                "小米手环10", "华为手表GT5", "金士顿1TB固态硬盘", "闪迪256GU盘",
                "漫步者NeoBuds Pro", "红米K80", "vivo X200", "OPPO Find X8",
                "投影仪家用4K", "显示器27英寸2K", "充电宝20000毫安", "氮化镓充电器65W",
                "蓝牙耳机降噪", "游戏手柄无线", "录音笔高清降噪", "摄像头家用监控",
                "移动硬盘2TB", "耳机收纳盒便携"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 2000, content = content)
            }
        }

        // 3. 服饰鞋帽
        fun getClothingShoes(): List<SearchContent> {
            val suggestions = listOf(
                "纯棉短袖男宽松", "冰丝速干T恤女", "牛仔裤男直筒高腰", "阔腿裤女显瘦",
                "连衣裙夏季碎花", "防晒衣女UPF50+", "冲锋衣男三合一", "卫衣女加绒加厚",
                "羽绒服女中长款", "羊毛大衣男双面呢", "帆布鞋女百搭低帮", "老爹鞋男增高",
                "马丁靴女英伦风", "雪地靴女加绒", "高跟鞋细跟8cm", "运动鞋男减震",
                "内衣女无痕纯棉", "袜子男纯棉中筒", "围巾女羊绒冬季", "帽子男棒球帽",
                "皮带男真皮自动扣", "手套女加绒触屏", "泳衣女显瘦遮肚", "睡衣女纯棉长袖",
                "西装套装男商务", "旗袍改良版夏季", "汉服女齐胸襦裙", "工装裤男宽松",
                "乐福鞋女平底", "拖鞋家用防滑"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = 0, content =  content)
            }
        }

        // 4. 家居用品
        fun getHomeSupplies(): List<SearchContent> {
            val suggestions = listOf(
                "卧室收纳柜实木", "厨房置物架多层", "卫生间置物架壁挂", "客厅沙发套全包",
                "窗帘遮光卧室免打孔", "地毯客厅轻奢防滑", "抱枕靠垫沙发客厅", "床垫乳胶1.8米",
                "枕头护颈椎助睡眠", "被子冬被加厚保暖", "四件套纯棉床单", "浴巾纯棉吸水",
                "拖把免手洗平板", "垃圾桶家用带盖", "洗洁精食品级", "洗衣液持久留香",
                "马桶刷无死角硅胶", "衣架防滑无痕", "收纳箱大号带轮", "碗架沥水厨房",
                "筷子防霉耐高温", "菜板实木抗菌", "锅具套装不粘锅", "保温杯316不锈钢",
                "台灯护眼学习", "香薰机家用静音", "加湿器卧室孕妇婴儿", "吹风机家用大功率",
                "粘毛器可撕式", "清洁湿巾厨房去油"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 4000, content = content)
            }
        }

        // 5. 图书文具
        fun getBooksStationery(): List<SearchContent> {
            val suggestions = listOf(
                "考研英语黄皮书", "高考必刷题数学", "三体全集刘慈欣", "百年孤独正版",
                "小学生课外阅读书", "绘本3-6岁经典", "四大名著完整版", "唐诗三百首幼儿版",
                "中性笔按动0.5黑色", "笔记本子加厚A5", "文件夹透明插页", "便利贴粘性强",
                "马克笔48色双头", "素描纸8K加厚", "橡皮擦无屑干净", "铅笔HB原木",
                "订书机迷你便携", "回形针彩色", "涂改液速干无痕", "尺套装三角板量角器",
                "字帖楷书成人练字", "错题本小学生", "书签定制创意", "文件袋防水A4",
                "计算器学生用多功能", "荧光笔标记笔", "固体胶高粘度", "削笔器手摇",
                "作文纸方格本", "字典新华字典最新版"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 5000, content = content)
            }
        }

        // 6. 美妆护肤
        fun getBeautySkincare(): List<SearchContent> {
            val suggestions = listOf(
                "补水保湿面膜女", "防晒霜50+防紫外线", "粉底液持妆不卡粉", "口红哑光显白",
                "气垫BB霜遮瑕", "隔离霜妆前乳", "散粉定妆控油", "眼影盘大地色",
                "眉笔防水防汗", "睫毛膏纤长卷翘", "眼线笔不晕染", "腮红自然裸妆",
                "高光修容盘", "唇釉镜面水光", "卸妆油温和不刺激", "洗面奶氨基酸控油",
                "爽肤水补水保湿", "乳液清爽不油腻", "面霜抗老紧致", "精华液美白淡斑",
                "眼霜淡化黑眼圈", "身体乳留香持久", "护手霜滋润保湿", "磨砂膏去角质",
                "护发素修复干枯", "发膜免蒸顺滑", "指甲油无毒可撕", "假睫毛自然款",
                "化妆刷套装软毛", "美妆蛋不吃粉"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 6000, content =  content)
            }
        }

        // 7. 运动器材
        fun getSportsEquipment(): List<SearchContent> {
            val suggestions = listOf(
                "瑜伽垫防滑加厚", "哑铃套装家用男", "跑步机家用小型", "动感单车静音",
                "羽毛球拍碳素超轻", "乒乓球拍五星", "篮球斯伯丁7号", "足球成人5号",
                "跳绳计数减肥专用", "拉力器扩胸家用", "健腹轮自动回弹", "握力器练手力",
                "瑜伽球加厚防爆", "弹力带健身阻力带", "护腕运动防扭伤", "护膝跑步专业",
                "游泳镜高清防雾", "游泳帽硅胶不勒头", "拳击手套成人", "沙袋立式家用",
                "飞盘户外专业", "滑板双翘初学者", "轮滑鞋儿童可调", "登山杖碳纤维",
                "护踝运动康复", "深蹲架家用简易", "卧推凳多功能", "计数握力球",
                "筋膜枪肌肉放松", "乒乓球桌折叠家用"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 7000, content = content)
            }
        }

        fun getFoodProducts(): List<SearchContent> {
            val suggestions = listOf(
                "无添加坚果礼盒", "速食螺蛳粉袋装", "自热火锅懒人即食", "每日坚果混合装",
                "牛奶整箱纯牛奶", "酸奶常温风味", "方便面经典口味", "火腿肠即食",
                "薯片原味超大包", "巧克力黑巧85%", "饼干苏打无糖", "面包全麦早餐",
                "大米五常稻花香", "食用油非转基因", "鸡蛋土鸡蛋农家", "面粉高筋烘焙",
                "水果礼盒车厘子", "橙子赣南脐橙", "苹果阿克苏冰糖心", "香蕉进口佳农",
                "零食大礼包整箱", "牛肉干手撕风干", "海苔片儿童即食", "果冻果肉型",
                "茶叶绿茶龙井", "咖啡速溶三合一", "蜂蜜纯正天然", "枸杞宁夏特级",
                "螺蛳粉柳州正宗", "水饺速冻三鲜"
            )
            return suggestions.mapIndexed { index, content ->
                SearchContent(search_time = index.toLong() + 8000, content = content)
            }
        }
    }
}
