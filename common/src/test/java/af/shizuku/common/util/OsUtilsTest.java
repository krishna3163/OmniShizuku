package af.shizuku.common.util;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class OsUtilsTest {

    @Test
    public void testGetUid() {
        int uid = OsUtils.getUid();
        assertTrue("UID should be non-negative", uid >= 0);
    }

    @Test
    public void testGetPid() {
        int pid = OsUtils.getPid();
        assertTrue("PID should be non-negative", pid >= 0);
    }
}
