package io.github.wasabithumb.yandisk4j.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDVendorTest {

    @Test
    void growShrink() {
        IDVendor vendor = new IDVendor(4);

        for (int i=0; i < 6; i++) {
            assertEquals(i, vendor.next());
        }

        vendor.free(3);
        vendor.free(5);
        vendor.free(4);
        vendor.free(0);

        assertEquals(0, vendor.next());
        assertEquals(3, vendor.next());
        assertEquals(4, vendor.next());
    }

}