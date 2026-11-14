package moe.shizuku.manager.callrecorder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingSessionMachineTest {

    private fun snapshot(
        state: RecordingSessionState = RecordingSessionState.Idle,
        generation: Long = 0L,
        durationMs: Long = 0L,
        fileSizeBytes: Long = 0L,
        fileName: String? = null,
        lastError: String? = null
    ) = RecordingSessionSnapshot(state, generation, durationMs, fileSizeBytes, fileName, lastError)

    @Test
    fun validHappyPathTransitions() {
        var s = snapshot()
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StartRequested)
        assertEquals(RecordingSessionState.Starting, s.state)
        assertEquals(1L, s.generation)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.Started())
        assertEquals(RecordingSessionState.Recording, s.state)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.Paused())
        assertEquals(RecordingSessionState.Paused, s.state)
        assertTrue(s.isPaused && s.isRecording)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.Resumed())
        assertEquals(RecordingSessionState.Recording, s.state)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StopRequested)
        assertEquals(RecordingSessionState.Stopping, s.state)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.Stopped())
        assertEquals(RecordingSessionState.Idle, s.state)
        assertEquals(0L, s.durationMs)
        assertEquals(0L, s.fileSizeBytes)
    }

    @Test
    fun failurePathsSetErrorAndClearProgress() {
        listOf(
            RecordingSessionState.Starting,
            RecordingSessionState.Recording,
            RecordingSessionState.Paused,
            RecordingSessionState.Stopping
        ).forEach { st ->
            val out = RecordingSessionMachine.reduce(
                snapshot(state = st, generation = 3, durationMs = 1200, fileSizeBytes = 99),
                RecordingSessionEvent.Failed("boom", generation = 3)
            )
            assertTrue(out.state is RecordingSessionState.Error)
            assertEquals("boom", (out.state as RecordingSessionState.Error).message)
            assertEquals("boom", out.lastError)
            assertEquals(0L, out.durationMs)
            assertEquals(0L, out.fileSizeBytes)
        }
    }

    @Test
    fun errorCanRecoverToIdleOrRestart() {
        val err = snapshot(state = RecordingSessionState.Error("x"), generation = 2, fileName = "a.ogg")
        val reset = RecordingSessionMachine.reduce(err, RecordingSessionEvent.Reset(clearFile = true))
        assertEquals(RecordingSessionState.Idle, reset.state)
        assertEquals(null, reset.fileName)
        val restarted = RecordingSessionMachine.reduce(err, RecordingSessionEvent.StartRequested)
        assertEquals(RecordingSessionState.Starting, restarted.state)
        assertEquals(3L, restarted.generation)
    }

    @Test
    fun invalidCommandsAreRejectedByState() {
        val idle = snapshot()
        assertEquals(RecordingSessionState.Idle, RecordingSessionMachine.reduce(idle, RecordingSessionEvent.Paused()).state)
        assertEquals(RecordingSessionState.Idle, RecordingSessionMachine.reduce(idle, RecordingSessionEvent.Resumed()).state)
        assertEquals(RecordingSessionState.Idle, RecordingSessionMachine.reduce(idle, RecordingSessionEvent.StopRequested).state)
        assertEquals(RecordingSessionState.Idle, RecordingSessionMachine.reduce(idle, RecordingSessionEvent.Started()).state)

        val rec = snapshot(RecordingSessionState.Recording, generation = 1)
        assertEquals(RecordingSessionState.Recording, RecordingSessionMachine.reduce(rec, RecordingSessionEvent.StartRequested).state)
        assertEquals(RecordingSessionState.Recording, RecordingSessionMachine.reduce(rec, RecordingSessionEvent.Resumed()).state)
        assertEquals(RecordingSessionState.Recording, RecordingSessionMachine.reduce(rec, RecordingSessionEvent.Started()).state)

        val paused = snapshot(RecordingSessionState.Paused, generation = 1)
        assertEquals(RecordingSessionState.Paused, RecordingSessionMachine.reduce(paused, RecordingSessionEvent.Paused()).state)
        assertEquals(RecordingSessionState.Paused, RecordingSessionMachine.reduce(paused, RecordingSessionEvent.StartRequested).state)

        val stopping = snapshot(RecordingSessionState.Stopping, generation = 1)
        assertEquals(RecordingSessionState.Stopping, RecordingSessionMachine.reduce(stopping, RecordingSessionEvent.StartRequested).state)
        assertEquals(RecordingSessionState.Stopping, RecordingSessionMachine.reduce(stopping, RecordingSessionEvent.Paused()).state)
    }

    @Test
    fun duplicateAndRepeatedCommandsAreIdempotent() {
        var s = snapshot()
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StartRequested)
        val s2 = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StartRequested)
        assertEquals(s, s2) // rejected
        val rec = snapshot(RecordingSessionState.Recording, 1)
        assertEquals(rec, RecordingSessionMachine.reduce(rec, RecordingSessionEvent.Resumed()))
        val paused = snapshot(RecordingSessionState.Paused, 1)
        assertEquals(paused, RecordingSessionMachine.reduce(paused, RecordingSessionEvent.Paused()))
        val stopping = snapshot(RecordingSessionState.Stopping, 1)
        assertEquals(stopping, RecordingSessionMachine.reduce(stopping, RecordingSessionEvent.StopRequested))
    }

    @Test
    fun staleGenerationCallbacksDoNotMutateNewSession() {
        var old = snapshot(RecordingSessionState.Starting, generation = 1)
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.Started())
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.StopRequested)
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.Stopped())
        var newer = RecordingSessionMachine.reduce(old, RecordingSessionEvent.StartRequested) // gen 2
        newer = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Started())
        val before = newer
        newer = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Progress(500, 10, generation = 1))
        assertEquals(before, newer)
        newer = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Failed("old", generation = 1))
        assertEquals(before, newer)
        val progressed = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Progress(500, 10, generation = 2))
        assertEquals(500L, progressed.durationMs)
        assertEquals(10L, progressed.fileSizeBytes)
    }

    @Test
    fun progressIgnoredWhenNotRecordingOrPaused() {
        val idle = snapshot()
        assertEquals(idle, RecordingSessionMachine.reduce(idle, RecordingSessionEvent.Progress(1, 1, 0)))
        val stopping = snapshot(RecordingSessionState.Stopping, 1)
        assertEquals(stopping, RecordingSessionMachine.reduce(stopping, RecordingSessionEvent.Progress(1, 1, 1)))
    }

    @Test
    fun deterministicStressSequenceIsReproducible() {
        fun run(seed: Int): RecordingSessionSnapshot {
            var rnd = seed
            fun next() = ((1103515245L * rnd + 12345) and 0x7fffffff).toInt().also { rnd = it }
            val events = listOf<(Long) -> RecordingSessionEvent>(
                { RecordingSessionEvent.StartRequested },
                { g -> RecordingSessionEvent.Started(g) },
                { g -> RecordingSessionEvent.Paused(g) },
                { g -> RecordingSessionEvent.Resumed(g) },
                { RecordingSessionEvent.StopRequested },
                { g -> RecordingSessionEvent.Stopped(g) },
                { g -> RecordingSessionEvent.Progress(next().toLong(), next().toLong(), g) },
                { g -> RecordingSessionEvent.Failed("e", g) },
                { RecordingSessionEvent.Reset() }
            )
            var s = snapshot()
            repeat(200) {
                val e = events[next() % events.size](s.generation)
                s = RecordingSessionMachine.reduce(s, e)
                // invariants
                if (s.state is RecordingSessionState.Error) {
                    assertEquals(0L, s.durationMs)
                    assertEquals(0L, s.fileSizeBytes)
                }
                if (s.state is RecordingSessionState.Idle || s.state is RecordingSessionState.Starting) {
                    assertEquals(0L, s.fileSizeBytes)
                }
            }
            return s
        }
        val a = run(42)
        val b = run(42)
        assertEquals(a, b)
        // different seed may differ but still must not crash (covered by run)
        run(7)
    }
}



    @Test
    fun staleTerminalAndControlCallbacksFromOldGenerationAreIgnored() {
        var old = snapshot(RecordingSessionState.Starting, generation = 1)
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.Started(1))
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.StopRequested)
        old = RecordingSessionMachine.reduce(old, RecordingSessionEvent.Stopped(1))
        var newer = RecordingSessionMachine.reduce(old, RecordingSessionEvent.StartRequested) // gen 2
        newer = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Started(2))
        newer = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Paused(2))
        val before = newer
        // stale callbacks from generation 1 must not mutate generation 2 session
        assertEquals(before, RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Started(1)))
        assertEquals(before, RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Paused(1)))
        assertEquals(before, RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Resumed(1)))
        assertEquals(before, RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Stopped(1)))
        assertEquals(before, RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Failed("late", 1)))
        // current-generation callbacks still apply
        val resumed = RecordingSessionMachine.reduce(newer, RecordingSessionEvent.Resumed(2))
        assertEquals(RecordingSessionState.Recording, resumed.state)
        val stopped = RecordingSessionMachine.reduce(resumed, RecordingSessionEvent.StopRequested)
        assertEquals(RecordingSessionState.Stopping, stopped.state)
    }

    @Test
    fun sessionReplacementAfterImmediateStopOrFailureIsClean() {
        // immediate stop from starting
        var s = snapshot()
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StartRequested)
        assertEquals(1L, s.generation)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StopRequested)
        s = RecordingSessionMachine.reduce(s, RecordingSessionEvent.Stopped(1))
        assertEquals(RecordingSessionState.Idle, s.state)
        var s2 = RecordingSessionMachine.reduce(s, RecordingSessionEvent.StartRequested)
        assertEquals(RecordingSessionState.Starting, s2.state)
        assertEquals(2L, s2.generation)
        // failure then replacement
        var f = snapshot(RecordingSessionState.Recording, generation = 5)
        f = RecordingSessionMachine.reduce(f, RecordingSessionEvent.Failed("init", 5))
        assertTrue(f.state is RecordingSessionState.Error)
        val restarted = RecordingSessionMachine.reduce(f, RecordingSessionEvent.StartRequested)
        assertEquals(RecordingSessionState.Starting, restarted.state)
        assertEquals(6L, restarted.generation)
        assertEquals(0L, restarted.durationMs)
        assertEquals(null, restarted.lastError)
    }
