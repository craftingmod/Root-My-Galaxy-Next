package dev.busung.s25uroot

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalSuPayloadTest {
    @Test
    fun importedDaemonSurvivesRejectedReplacementAndCanBeRemoved() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Isolated store avoids modifying any payload the user has imported on the test device.
        val store = ImportedPayloadStore("test-su-payload", "ksud", "test_su_payload_name", false)
        val input = File(context.cacheDir, "test-ksud")
        val output = File(context.cacheDir, "test-staged-ksud")
        val bytes = byteArrayOf(0x7f, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), 2, 1)
        try {
            input.writeBytes(bytes)
            // A file URI has no document provider display name, so the store uses its default.
            assertEquals("ksud", store.import(context, Uri.fromFile(input)))
            assertEquals("ksud", store.displayName(context))
            input.writeText("not an ELF")
            assertTrue(runCatching { store.import(context, Uri.fromFile(input)) }.isFailure)
            store.stage(context, output)
            assertArrayEquals(bytes, output.readBytes())
            store.clear(context)
            assertNull(store.file(context))
            assertNull(store.displayName(context))
        } finally {
            store.clear(context)
            input.delete()
            output.delete()
        }
    }
}
