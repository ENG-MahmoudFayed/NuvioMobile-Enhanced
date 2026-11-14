package com.nuvio.app.features.home

import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.details.seriesPrimaryAction
import com.nuvio.app.features.watched.WatchedRepository
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import com.nuvio.app.features.watchprogress.WatchProgressRepository

internal data class HomePlayTarget(
    val type: String,
    val videoId: String,
    val parentMetaId: String,
    val parentMetaType: String,
    val title: String,
    val logo: String?,
    val poster: String?,
    val background: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
    val episodeThumbnail: String?,
    val pauseDescription: String?,
    val resumePositionMs: Long?,
)

/**
 * Resolves what the home hero's Play button starts, matching the details page's primary action:
 * a series resumes or continues with its next episode, a movie resumes where it was left.
 * Returns null when the metadata cannot be loaded or a series has no playable episode.
 */
internal suspend fun resolveHomePlayTarget(preview: MetaPreview): HomePlayTarget? {
    val meta = MetaDetailsRepository.peek(preview.type, preview.id)
        ?: runCatching { MetaDetailsRepository.fetch(preview.type, preview.id) }.getOrNull()
        ?: return null
    val progressEntries = WatchProgressRepository.uiState.value.entries
    val hasEpisodes = meta.videos.any { it.season != null || it.episode != null }

    if (meta.type == "series" || hasEpisodes) {
        val watchedUiState = WatchedRepository.uiState.value
        val action = meta.seriesPrimaryAction(
            entries = progressEntries,
            watchedItems = watchedUiState.items,
            todayIsoDate = CurrentDateProvider.todayIsoDate(),
            preferFurthestEpisode = ContinueWatchingPreferencesRepository.uiState.value.upNextFromFurthestEpisode,
            watchedKeys = watchedUiState.watchedKeys,
            allowRewatch = true,
        ) ?: return null
        val video = meta.videoForSeriesAction(action)
        return HomePlayTarget(
            type = meta.type,
            videoId = video?.id?.takeIf { it.isNotBlank() } ?: action.videoId,
            parentMetaId = meta.id,
            parentMetaType = meta.type,
            title = meta.name,
            logo = meta.logo,
            poster = meta.poster,
            background = meta.background,
            seasonNumber = action.seasonNumber,
            episodeNumber = action.episodeNumber,
            episodeTitle = action.episodeTitle,
            episodeThumbnail = action.episodeThumbnail,
            pauseDescription = video?.overview,
            resumePositionMs = action.resumePositionMs,
        )
    }

    val movieProgress = WatchProgressRepository.uiState.value
        .byVideoIdForContent(meta.id)[meta.id]
        ?.takeUnless { it.isCompleted }
    return HomePlayTarget(
        type = meta.type,
        videoId = meta.id,
        parentMetaId = meta.id,
        parentMetaType = meta.type,
        title = meta.name,
        logo = meta.logo,
        poster = meta.poster,
        background = meta.background,
        seasonNumber = null,
        episodeNumber = null,
        episodeTitle = null,
        episodeThumbnail = null,
        pauseDescription = meta.description,
        resumePositionMs = movieProgress?.lastPositionMs,
    )
}
