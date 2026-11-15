package com.nuvio.app.features.tmdb

import com.nuvio.app.core.logging.InAppLogger
import com.nuvio.app.features.addons.httpGetText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A trending title's artwork, for the screens shown before a profile exists. */
internal data class TmdbTrendingArt(
    val title: String,
    val posterUrl: String,
    val backdropUrl: String?,
)

/**
 * This week's trending posters from TMDB, fetched once per launch. The welcome wall and the
 * profile picker use them as the picture of what the app holds; with no key or no network they
 * get an empty list and draw plain tiles instead.
 */
internal object TmdbTrendingArtwork {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cached: List<TmdbTrendingArt>? = null

    suspend fun load(): List<TmdbTrendingArt> {
        cached?.let { return it }
        val apiKey = TmdbSettingsRepository.effectiveApiKey().takeIf(String::isNotBlank) ?: return emptyList()
        return mutex.withLock {
            cached ?: fetch(apiKey).also { if (it.isNotEmpty()) cached = it }
        }
    }

    private suspend fun fetch(apiKey: String): List<TmdbTrendingArt> =
        runCatching {
            val payload = httpGetText(buildTmdbUrl(endpoint = "trending/all/week", apiKey = apiKey))
            json.decodeFromString<TrendingResponse>(payload).results.mapNotNull { item ->
                val poster = item.posterPath?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                TmdbTrendingArt(
                    title = item.title ?: item.name.orEmpty(),
                    posterUrl = "https://image.tmdb.org/t/p/w342$poster",
                    backdropUrl = item.backdropPath
                        ?.takeIf(String::isNotBlank)
                        ?.let { "https://image.tmdb.org/t/p/w780$it" },
                )
            }
        }.onFailure { error ->
            InAppLogger.warn("Metadata/TMDB", "trending artwork failed: ${InAppLogger.throwableSummary(error)}")
        }.getOrDefault(emptyList())

    @Serializable
    private data class TrendingResponse(
        val results: List<TrendingItem> = emptyList(),
    )

    @Serializable
    private data class TrendingItem(
        val title: String? = null,
        val name: String? = null,
        @SerialName("poster_path") val posterPath: String? = null,
        @SerialName("backdrop_path") val backdropPath: String? = null,
    )
}
