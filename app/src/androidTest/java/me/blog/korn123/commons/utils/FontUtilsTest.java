package me.blog.korn123.commons.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2017-11-06.
 */

public class FontUtilsTest {

    private static Context sContext;

    @BeforeClass
    public static void init() {
        sContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_03() {
        assertEquals("NanumPen", FontUtils.fontFileNameToDisplayName(sContext, "NanumPen.ttf"));
    }

    @Test
    public void test_04() {
        assertEquals("consola", FontUtils.fontFileNameToDisplayName(sContext, "consola.ttf"));
    }

}
