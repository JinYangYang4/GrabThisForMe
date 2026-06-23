package com.example.grabthisforme.model.post.domain

data class Post(
    val identity: PostIdentity,
    val contentInfo: PostContent,
    val authorInfo: PostAuthor,
    val statsInfo: PostStats
) {
    val postId: String get() = identity.postId
    val content: String get() = contentInfo.text
    val images: List<String> get() = contentInfo.images
    val categoryKey: String get() = contentInfo.categoryKey
    val customTags: List<String> get() = contentInfo.customTags
    val createTime: Long get() = identity.createTime
    val latitude: Double? get() = contentInfo.latitude
    val longitude: Double? get() = contentInfo.longitude
    val country: String get() = contentInfo.country
    val province: String get() = contentInfo.province
    val city: String get() = contentInfo.city
    val district: String get() = contentInfo.district
    val locationLabel: String get() = contentInfo.locationLabel
    val author: PostAuthor get() = authorInfo
    val authorId: Long get() = authorInfo.authorId
    val authorName: String get() = authorInfo.authorName
    val authorAvatarUrl: String get() = authorInfo.authorAvatarUrl
    val likeCount: Int get() = statsInfo.likeCount
    val commentCount: Int get() = statsInfo.commentCount

    constructor(
        postId: String,
        content: String,
        images: List<String> = emptyList(),
        categoryKey: String = "",
        customTags: List<String> = emptyList(),
        createTime: Long,
        author: PostAuthor,
        likeCount: Int,
        commentCount: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        country: String = "",
        province: String = "",
        city: String = "",
        district: String = "",
        locationLabel: String = ""
    ) : this(
        identity = PostIdentity(
            postId = postId,
            createTime = createTime
        ),
        contentInfo = PostContent(
            text = content,
            images = images,
            categoryKey = categoryKey,
            customTags = customTags,
            latitude = latitude,
            longitude = longitude,
            country = country,
            province = province,
            city = city,
            district = district,
            locationLabel = locationLabel
        ),
        authorInfo = PostAuthor(
            authorId = author.authorId,
            authorName = author.authorName,
            authorAvatarUrl = author.authorAvatarUrl
        ),
        statsInfo = PostStats(
            likeCount = likeCount,
            commentCount = commentCount
        )
    )

    constructor(
        postId: String,
        content: String,
        images: List<String> = emptyList(),
        categoryKey: String = "",
        customTags: List<String> = emptyList(),
        createTime: Long,
        authorId: Long,
        authorName: String = "",
        authorAvatarUrl: String = "",
        likeCount: Int,
        commentCount: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        country: String = "",
        province: String = "",
        city: String = "",
        district: String = "",
        locationLabel: String = ""
    ) : this(
        identity = PostIdentity(
            postId = postId,
            createTime = createTime
        ),
        contentInfo = PostContent(
            text = content,
            images = images,
            categoryKey = categoryKey,
            customTags = customTags,
            latitude = latitude,
            longitude = longitude,
            country = country,
            province = province,
            city = city,
            district = district,
            locationLabel = locationLabel
        ),
        authorInfo = PostAuthor(
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl
        ),
        statsInfo = PostStats(
            likeCount = likeCount,
            commentCount = commentCount
        )
    )

    fun hasImages(): Boolean = images.isNotEmpty()
}
