package dev.schlaubi.lavakord.rest

import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.audio.player.Track

/**
 * Maps a [List] of [TrackResponse.PartialTrack]s to a List of [Track]s.
 *
 * @see TrackResponse.PartialTrack.toTrack
 */
public suspend fun List<TrackResponse.PartialTrack>.mapToTrack(): List<Track> = map { it.toTrack() }

/**
 * Loads an audio item from this [Link].
 *
 * See: [Lavalink doc](https://github.com/Frederikam/Lavalink/blob/master/IMPLEMENTATION.md#track-loading-api)
 *
 * @see TrackResponse
 * @see Node.loadItem
 */
public suspend fun Link.loadItem(query: String): TrackResponse = node.loadItem(query)

/**
 * Loads an audio item from this [Link].
 *
 * See: [Lavalink doc](https://github.com/Frederikam/Lavalink/blob/master/IMPLEMENTATION.md#track-loading-api)
 *
 * @see TrackResponse
 */
public suspend fun Node.loadItem(query: String): TrackResponse = get {
    path("loadtracks")
    parameters.append("identifier", query)
}
