package net.youapps.wallpaper_apis.pb.obj

import kotlinx.serialization.Serializable

@Serializable
data class PixabayPage(
    val mediaType: String? = null,
    val minHeight: Int? = null,
    val minWidth: Int? = null,
    val order: String? = null,
    val orientation: String? = null,
    val page: Int? = null,
    val pageType: String? = null,
    val pages: Int? = null,
    val perPage: Int? = null,
    val query: String? = null,
    val results: List<PixabayResult> = emptyList(),
    val retouchInCanvaMobileVariant: Int? = null,
    val safesearch: String? = null,
    val searchType: String? = null,
    val showAds: String? = null,
    val sponsorLinkHref: String? = null,
    val sponsorViewMoreHref: String? = null,
    val suggest: String? = null,
    val total: Int? = null,
)