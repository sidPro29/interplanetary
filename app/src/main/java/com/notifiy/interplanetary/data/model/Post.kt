package com.notifiy.interplanetary.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("_id")
    val id: String,
    @SerializedName("type")
    val category: String, // video, movie, tvshow
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("videoUrl")
    val videoUrlList: List<String>?,
    @SerializedName("title")
    private val _title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("genres")
    val genres: List<String>?,
    @SerializedName("svp_clip_id")
    val svpClipId: String?
) {
    val title: RenderedContent get() = RenderedContent(_title ?: "Untitled")
    
    // UI backward compatibility
    val portraitPoster: String get() = images?.firstOrNull() ?: ""
    val membershipLevel: List<String> get() = emptyList() // To be implemented with new Plans
    val tag: String get() = tags?.joinToString(", ") ?: ""
    val genre: String get() = genres?.joinToString(", ") ?: ""
    val imageUrl: String get() = portraitPoster
    val videoUrl: String get() = videoUrlList?.firstOrNull() ?: ""

    fun getDisplayImageUrl(): String = portraitPoster
    fun getEffectiveVideoUrl(): String {
        if (!svpClipId.isNullOrEmpty()) {
            return "https://play.webvideocore.net/clip/$svpClipId/playlist.m3u8"
        }
        return videoUrl
    }
}

data class RenderedContent(val rendered: String)
