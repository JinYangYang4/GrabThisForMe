package com.example.grabthisforme.model.post.data.network.dto
/**
 * 分页响应传输对象（泛型）
 * @param items 当前页数据集合，默认空列表
 * @param total 数据总条数，默认 0
 * @param limit 每页条数（分页大小），默认 0
 * @param offset 偏移量（跳过多少条数据），默认 0
 * @param hasMore 是否存在下一页，默认 false
 */
data class PageResponseDto<T>(
    val items: List<T> = emptyList(),
    val total: Long = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val hasMore: Boolean = false
)
