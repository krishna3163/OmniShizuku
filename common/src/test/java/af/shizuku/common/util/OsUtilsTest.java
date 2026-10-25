package af.shizuku.common.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class OsUtilsTest {

    @Test
    public void testGetPid() {
        // Given the constraints of Robolectric and modern Java reflection, the best approach
        // is to verify the method works without crashing, returns a consistent non-negative value,
        // and corresponds to the Robolectric environment's behavior.

        int pid = OsUtils.getPid();

        // The process ID should be >= 0 (Robolectric might return 0 if not fully shadowed, or an actual PID)
        org.junit.Assert.assertTrue("PID should be >= 0", pid >= 0);

        // We can also verify that multiple calls return the same cached value (since it's a static final field)
        int secondPidCall = OsUtils.getPid();
        assertEquals("Multiple calls to getPid() should return the same value", pid, secondPidCall);

        // We can also verify that it returns the exact value of the process PID at class load
        // Note: in a fresh Robolectric test environment, OsUtils.getPid() has the value
        // that android.system.Os.getpid() returned when OsUtils was initialized.
        int currentProcessPid = android.os.Process.myPid();

        // In standard Robolectric, android.system.Os.getpid() and android.os.Process.myPid()
        // may both return 0, so they should be equal.
        assertEquals("PID should match the current process PID", currentProcessPid, pid);
    }
}
