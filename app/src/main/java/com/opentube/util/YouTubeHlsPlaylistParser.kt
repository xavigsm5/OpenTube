package com.opentube.util

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist.Rendition
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParserFactory
import androidx.media3.exoplayer.upstream.ParsingLoadable
import java.io.InputStream

/**
 * A YouTube HLS playlist parser which adds role flags to audio formats with track types.
 * Copied from LibreTube
 */
@OptIn(UnstableApi::class)
class YouTubeHlsPlaylistParser : ParsingLoadable.Parser<HlsPlaylist> {

    class Factory : HlsPlaylistParserFactory {
        override fun createPlaylistParser() = YouTubeHlsPlaylistParser()

        override fun createPlaylistParser(
            multivariantPlaylist: HlsMultivariantPlaylist,
            previousMediaPlaylist: HlsMediaPlaylist?
        ) = YouTubeHlsPlaylistParser(multivariantPlaylist, previousMediaPlaylist)
    }

    private val hlsPlaylistParser: HlsPlaylistParser

    private constructor() {
        this.hlsPlaylistParser = HlsPlaylistParser()
    }

    private constructor(
        multivariantPlaylist: HlsMultivariantPlaylist,
        previousMediaPlaylist: HlsMediaPlaylist?
    ) {
        this.hlsPlaylistParser = HlsPlaylistParser(multivariantPlaylist, previousMediaPlaylist)
    }

    override fun parse(uri: Uri, inputStream: InputStream): HlsPlaylist {
        val hlsPlaylist = hlsPlaylistParser.parse(uri, inputStream)
        if (hlsPlaylist !is HlsMultivariantPlaylist) {
            return hlsPlaylist
        }

        val hlsMultivariantPlaylist: HlsMultivariantPlaylist = hlsPlaylist

        return HlsMultivariantPlaylist(
            hlsMultivariantPlaylist.baseUri,
            hlsMultivariantPlaylist.tags,
            hlsMultivariantPlaylist.variants,
            hlsMultivariantPlaylist.videos,
            getAudioRenditionsWithTrackTypeSet(hlsMultivariantPlaylist.audios),
            hlsMultivariantPlaylist.subtitles,
            hlsMultivariantPlaylist.closedCaptions,
            hlsMultivariantPlaylist.muxedAudioFormat,
            hlsMultivariantPlaylist.muxedCaptionFormats,
            hlsMultivariantPlaylist.hasIndependentSegments,
            hlsMultivariantPlaylist.variableDefinitions,
            hlsMultivariantPlaylist.sessionKeyDrmInitData
        )
    }

    private fun getAudioRenditionsWithTrackTypeSet(
        hlsMultivariantPlaylistAudios: List<Rendition>
    ): List<Rendition> {
        return hlsMultivariantPlaylistAudios.map {
            val pathSegments = it.url?.pathSegments ?: return@map it
            val sgoapPathParameterNameIndex = pathSegments.indexOf(SGOAP_PATH_PARAMETER)

            if (sgoapPathParameterNameIndex == -1) {
                return@map it
            }

            val sgoapPathParameterValueIndex = sgoapPathParameterNameIndex + 1

            if (sgoapPathParameterValueIndex == pathSegments.size) {
                return@map it
            }

            Rendition(
                it.url,
                createAudioFormatFromAccountValue(
                    pathSegments[sgoapPathParameterValueIndex],
                    it.format
                ),
                it.groupId,
                it.name
            )
        }
    }

    private fun createAudioFormatFromAccountValue(
        sgoapPathParameterValue: String,
        audioFormat: Format
    ): Format {
        XTAGS_ACONT_VALUE_REGEX.find(sgoapPathParameterValue)?.groupValues?.get(1)
            ?.let { acontValue ->
                return audioFormat.buildUpon()
                    .setRoleFlags(getFullAudioRoleFlags(audioFormat.roleFlags, acontValue))
                    .build()
            }

        return audioFormat
    }

    private fun getFullAudioRoleFlags(existingRoleFlags: Int, acontValue: String): Int {
        var roleFlags = existingRoleFlags
        
        when (acontValue) {
            "original" -> roleFlags = roleFlags or androidx.media3.common.C.ROLE_FLAG_MAIN
            "dubbed" -> roleFlags = roleFlags or androidx.media3.common.C.ROLE_FLAG_DUB
            "descriptive" -> roleFlags = roleFlags or androidx.media3.common.C.ROLE_FLAG_DESCRIBES_VIDEO
        }
        
        return roleFlags
    }

    companion object {
        private const val SGOAP_PATH_PARAMETER = "sgoap"
        private val XTAGS_ACONT_VALUE_REGEX = Regex("xtags=.*acont=(.[^:]+)")
    }
}
