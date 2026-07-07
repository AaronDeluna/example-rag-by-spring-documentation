package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.cli.OsType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("inner")
@Tag("unit")
class OsTypeTest {

    @Test
    @DisplayName("detect возвращает LINUX на Linux-системе")
    void detectReturnsLinuxOnLinux() {
        OsType actual = OsType.detect();
        assertEquals(OsType.LINUX, actual);
    }

    @Test
    @DisplayName("detect возвращает не null")
    void detectNeverReturnsNull() {
        OsType actual = OsType.detect();
        assertNotNull(actual);
    }
}
