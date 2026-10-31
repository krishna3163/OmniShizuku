package net.youapps.wallpaper_apis.pb.obj

import kotlinx.serialization.Serializable

@Serializable
data class PixabayResult(
    val alt: String = "",
    val attributionHtml: String? = null,
    val canvaRetouchUrl: String? = null,
    val commentCount: Int? = null,
    val commentHref: String? = null,
    val description: String? = null,
    val downloadCount: Int? = null,
    val height: Int = 0,
    val href: String = "",
    val id: Int = 0,
    val isAiGenerated: Boolean = false,
    val isEditorsChoice: Boolean = false,
    val isLowQuality: Boolean = false,
    val likeCount: Int? = null,
    val likeHref: String? = null,
    val mediaDescriptiveType: String? = null,
    val mediaSubType: Int? = null,
    val mediaType: String? = null,
    val name: String = "",
    val nsfw: Boolean = false,
    val qualityStatus: Int? = null,
    val sources: Map<String, String> = emptyMap(),
    val tagLinks: String? = null,
    val tagList: List<List<String>> = emptyList(),
    val title: String? = null,
    val uploadDate: String? = null,
    val user: PixabayUser? = null,
    val vector: Boolean = false,
    val viewCount: Int? = null,
    val width: Int = 0
)