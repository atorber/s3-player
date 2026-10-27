package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aether S3", appName)
  }

  @Test
  fun `playlist queue storage saves and restores queue accurately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = com.example.data.local.PlaylistQueueStorage(context)
    val tracks = com.example.data.SampleData.getInitialTracks()
    val track1 = tracks.first()
    val track2 = tracks[1]
    val queue = listOf(track1, track2)

    storage.saveQueue(
      queue = queue,
      currentTrack = track2,
      currentPositionMs = 45000L,
      loopMode = com.example.audio.LoopMode.ALL,
      playbackSpeed = 1.25f
    )

    val restored = storage.loadQueue()
    assertEquals(2, restored.queue.size)
    assertEquals(track1.key, restored.queue[0].key)
    assertEquals(track2.key, restored.queue[1].key)
    assertEquals(track2.key, restored.currentTrack?.key)
    assertEquals(45000L, restored.positionMs)
    assertEquals(com.example.audio.LoopMode.ALL, restored.loopMode)
    assertEquals(1.25f, restored.playbackSpeed, 0.001f)

    storage.clear()
    val cleared = storage.loadQueue()
    assertEquals(0, cleared.queue.size)
  }
}
