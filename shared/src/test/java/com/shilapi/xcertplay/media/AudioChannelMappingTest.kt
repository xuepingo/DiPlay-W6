package com.shilapi.xcertplay.media

import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioChannelMappingTest {
    @Test
    fun musicDownlinkExperimentChangesOnlyTelephonyInBothMappingModes() {
        for (mode in AudioChannelMappingMode.entries) {
            assertEquals(
                AudioChannelSelection(AudioChannel.PHONE, AudioContentType.SPEECH, AudioManager.STREAM_MUSIC),
                AudioChannelMapper.map("telephony", 100, mode, telephonyDownlinkViaMusic = true),
            )
            for (audioType in listOf("media", "speechRecognition", "alert", "default", "unknown")) {
                assertEquals(
                    AudioChannelMapper.map(audioType, 100, mode),
                    AudioChannelMapper.map(audioType, 100, mode, telephonyDownlinkViaMusic = true),
                )
            }
        }
    }

    @Test
    fun mobileCompatibleMappingMatchesTheOriginalRouting() {
        assertMapped(
            mode = AudioChannelMappingMode.MOBILE_COMPATIBLE,
            audioType = "telephony",
            payloadType = 100,
            channel = AudioChannel.PHONE,
            contentType = AudioContentType.SPEECH,
            streamType = AudioManager.STREAM_VOICE_CALL,
        )
        assertMapped(
            mode = AudioChannelMappingMode.MOBILE_COMPATIBLE,
            audioType = "speechRecognition",
            payloadType = 100,
            channel = AudioChannel.ASSISTANT,
            contentType = AudioContentType.SPEECH,
            streamType = AudioManager.STREAM_VOICE_CALL,
        )
        assertMapped(
            mode = AudioChannelMappingMode.MOBILE_COMPATIBLE,
            audioType = "media",
            payloadType = 100,
            channel = AudioChannel.MEDIA,
            contentType = AudioContentType.MUSIC,
        )
        listOf("default", "alert", "compatibility").forEach { audioType ->
            assertMapped(
                mode = AudioChannelMappingMode.MOBILE_COMPATIBLE,
                audioType = audioType,
                payloadType = 100,
                channel = AudioChannel.NAVIGATION,
                contentType = AudioContentType.SPEECH,
                streamType = AudioChannelMapper.DEFAULT_NAVIGATION_STREAM_TYPE,
            )
        }
    }

    @Test
    fun automotiveMappingUsesTheBusSpecificCarPlayTypes() {
        listOf("media", "compatibility").forEach { audioType ->
            assertMapped(
                mode = AudioChannelMappingMode.AUTOMOTIVE_BUS,
                audioType = audioType,
                payloadType = 100,
                channel = AudioChannel.MEDIA,
                contentType = AudioContentType.MUSIC,
            )
        }
        assertMapped(
            mode = AudioChannelMappingMode.AUTOMOTIVE_BUS,
            audioType = "telephony",
            payloadType = 100,
            channel = AudioChannel.PHONE,
            contentType = AudioContentType.SPEECH,
            streamType = AudioManager.STREAM_VOICE_CALL,
        )
        assertMapped(
            mode = AudioChannelMappingMode.AUTOMOTIVE_BUS,
            audioType = "speechRecognition",
            payloadType = 100,
            channel = AudioChannel.ASSISTANT,
            contentType = AudioContentType.SPEECH,
            streamType = AudioManager.STREAM_VOICE_CALL,
        )
        listOf("default", "alert").forEach { audioType ->
            assertMapped(
                mode = AudioChannelMappingMode.AUTOMOTIVE_BUS,
                audioType = audioType,
                payloadType = 100,
                channel = AudioChannel.NAVIGATION,
                contentType = AudioContentType.SPEECH,
                streamType = AudioChannelMapper.DEFAULT_NAVIGATION_STREAM_TYPE,
            )
        }
    }

    @Test
    fun unknownTypesKeepTheMainHighAudioFallback() {
        assertMapped(
            mode = AudioChannelMappingMode.MOBILE_COMPATIBLE,
            audioType = "unknown",
            payloadType = AudioChannelMapper.STREAM_TYPE_MAIN_HIGH_AUDIO,
            channel = AudioChannel.MEDIA,
            contentType = AudioContentType.MUSIC,
        )
        assertMapped(
            mode = AudioChannelMappingMode.AUTOMOTIVE_BUS,
            audioType = "unknown",
            payloadType = 100,
            channel = AudioChannel.NAVIGATION,
            contentType = AudioContentType.SPEECH,
            streamType = AudioChannelMapper.DEFAULT_NAVIGATION_STREAM_TYPE,
        )
    }

    @Test
    fun navigationStreamTypeIsOnlyAppliedToGuidanceChannels() {
        val custom = 7
        assertEquals(
            AudioChannelSelection(AudioChannel.NAVIGATION, AudioContentType.SPEECH, custom),
            AudioChannelMapper.map("alert", 100, AudioChannelMappingMode.MOBILE_COMPATIBLE, custom),
        )
        assertEquals(
            AudioChannelSelection(AudioChannel.NAVIGATION, AudioContentType.SPEECH, custom),
            AudioChannelMapper.map("default", 100, AudioChannelMappingMode.AUTOMOTIVE_BUS, custom),
        )
        assertEquals(
            AudioChannelSelection(AudioChannel.MEDIA, AudioContentType.MUSIC, AudioManager.STREAM_MUSIC),
            AudioChannelMapper.map("media", 100, AudioChannelMappingMode.AUTOMOTIVE_BUS, custom),
        )
    }

    @Test
    fun volumePercentFollowsTheMappedChannelNotTheAudioTypeLiteral() {
        // CarPlay never sends audioType="navigation"; prompts arrive as default/alert.
        for (mode in AudioChannelMappingMode.entries) {
            for (audioType in listOf("default", "alert", "compatibility", "unknown", "navigation")) {
                val expected = if (
                    mode == AudioChannelMappingMode.AUTOMOTIVE_BUS && audioType == "compatibility"
                ) 250 else 300
                assertEquals(
                    "$mode/$audioType",
                    expected,
                    AudioChannelMapper.volumePercentFor(
                        audioType = audioType,
                        payloadType = 100,
                        mode = mode,
                        mediaVolumePercent = 250,
                        navigationVolumePercent = 300,
                    ),
                )
            }
            assertEquals(
                250,
                AudioChannelMapper.volumePercentFor("media", 100, mode, mediaVolumePercent = 250, navigationVolumePercent = 300),
            )
            // Call / speech recognition keep unity gain regardless of the ladders.
            assertEquals(
                100,
                AudioChannelMapper.volumePercentFor("telephony", 100, mode, mediaVolumePercent = 250, navigationVolumePercent = 300),
            )
            assertEquals(
                100,
                AudioChannelMapper.volumePercentFor("speechRecognition", 100, mode, mediaVolumePercent = 250, navigationVolumePercent = 300),
            )
        }
    }

    private fun assertMapped(
        mode: AudioChannelMappingMode,
        audioType: String,
        payloadType: Int,
        channel: AudioChannel,
        contentType: AudioContentType,
        streamType: Int = AudioManager.STREAM_MUSIC,
    ) {
        assertEquals(
            AudioChannelSelection(channel, contentType, streamType),
            AudioChannelMapper.map(audioType, payloadType, mode),
        )
    }
}
