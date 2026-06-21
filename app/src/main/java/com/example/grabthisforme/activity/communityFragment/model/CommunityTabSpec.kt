package com.example.grabthisforme.activity.communityFragment.model

data class CommunityTabSpec(
    val title: String,
    val categoryKey: String? = null,
    val mode: CommunityTabMode
)

enum class CommunityTabMode {
    LATEST,
    NEARBY,
    CATEGORY
}

object CommunityTabs {
    const val NEARBY_PLACEHOLDER_KEY = "__nearby__"

    val items: List<CommunityTabSpec> = listOf(
        CommunityTabSpec(title = "最新", mode = CommunityTabMode.LATEST),
        CommunityTabSpec(title = "附近", categoryKey = NEARBY_PLACEHOLDER_KEY, mode = CommunityTabMode.NEARBY),
        CommunityTabSpec(title = "搞笑", categoryKey = "FUNNY", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "吐槽", categoryKey = "GOSSIP", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "分享", categoryKey = "SHARE", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "新鲜", categoryKey = "FRESH", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "二手", categoryKey = "SECOND_HAND", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "交友", categoryKey = "MAKE_FRIENDS", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "游戏", categoryKey = "GAME", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "失物", categoryKey = "LOST_FOUND", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "社团", categoryKey = "CLUB", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "美食", categoryKey = "FOOD", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "避雷", categoryKey = "WARNING", mode = CommunityTabMode.CATEGORY),
        CommunityTabSpec(title = "疑问", categoryKey = "QUESTION", mode = CommunityTabMode.CATEGORY)
    )
}

object CommunityFeedArgs {
    const val TITLE = "community_title"
    const val MODE = "community_mode"
    const val CATEGORY_KEY = "community_category_key"
}
