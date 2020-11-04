@file:AutoWired
@file:Suppress("unused", "KDocMissingDocumentation")

package me.schlaubi.lavakord.example

import com.gitlab.kordlib.kordx.commands.annotation.AutoWired
import com.gitlab.kordlib.kordx.commands.argument.text.StringArgument
import com.gitlab.kordlib.kordx.commands.kord.bot
import com.gitlab.kordlib.kordx.commands.kord.model.prefix.kord
import com.gitlab.kordlib.kordx.commands.kord.model.prefix.mention
import com.gitlab.kordlib.kordx.commands.kord.module.module
import com.gitlab.kordlib.kordx.commands.model.command.invoke
import com.gitlab.kordlib.kordx.commands.model.module.ModuleModifier
import com.gitlab.kordlib.kordx.commands.model.prefix.PrefixConfiguration
import com.gitlab.kordlib.kordx.commands.model.prefix.literal
import com.gitlab.kordlib.kordx.commands.model.prefix.or
import com.gitlab.kordlib.kordx.commands.model.prefix.prefix
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import kapt.kotlin.generated.configure
import kotlinx.coroutines.launch
import lavalink.client.io.Lavalink
import lavalink.client.io.Link
import lavalink.client.player.event.TrackStartEvent
import me.schlaubi.lavakord.audio.on
import me.schlaubi.lavakord.connect
import me.schlaubi.lavakord.getLink
import me.schlaubi.lavakord.lavalink
import me.schlaubi.lavakord.rest.loadItem
import java.net.URI

lateinit var lavalink: Lavalink<out Link>

suspend fun main(): Unit = bot(System.getenv("token")) {
    configure()
    lavalink = kord.lavalink {
        autoReconnect = false
    }
    lavalink.addNode(URI.create("ws://localhost:8080"), "youshallnotpass")
}

val prefix: PrefixConfiguration = prefix {
    kord { literal("!") or mention() }
}

fun testModule(): ModuleModifier = module("music-test") {
    command("connect") {
        invoke {
            val guild = guild ?: return@invoke
            val link = guild.getLink(lavalink)

            val voiceState = author.asMember(guild.id).getVoiceState()

            val channelId = voiceState.channelId
            if (channelId == null) {
                respond("Please connect to a voice channel")
                return@invoke
            }

            link.connect(channelId)
        }
    }

    command("leave") {
        invoke {
            val guild = guild ?: return@invoke
            val link = guild.getLink(lavalink)

            if (link.state == Link.State.CONNECTED) {
                link.disconnect()
            } else {
                respond("Not connected to a channel")
            }
        }
    }

    command("play") {
        invoke(StringArgument) { query ->

            val search = if (query.startsWith("http")) {
                query
            } else {
                "ytsearch:$query"
            }

            val guild = guild ?: return@invoke
            val link = guild.getLink(lavalink)
            if (link.state != Link.State.CONNECTED) {
                respond("Not connect to VC!")
                return@invoke
            }

            val player = link.player

            player.on<TrackStartEvent> {
                channel.createMessage(track.info.asString())
            }

            link.loadItem(search, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    player.playTrack(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    player.playTrack(playlist.tracks.first())
                }

                override fun noMatches() {
                    kord.launch {
                        respond("No matches")
                    }
                }

                override fun loadFailed(exception: FriendlyException?) {
                    kord.launch {
                        respond(exception?.message ?: "")
                    }
                }

            })
        }
    }
}

fun AudioTrackInfo.asString(): String {
    return "AudioTrackInfo{" +
            "title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", length=" + length +
            ", identifier='" + identifier + '\'' +
            ", isStream=" + isStream +
            ", uri='" + uri + '\'' +
            '}'
}
