package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class TranslatorCachingWrapperTest {
    private Translator mockTranslator;
    private Translator cachingWrapper;

    @Before
    public void setup() {
        mockTranslator = mockTranslator("testKey", "testValue");
        cachingWrapper = new TranslatorCachingWrapper(mockTranslator);
    }

    @Test
    public void testCacheMiss() {
        String value = cachingWrapper.translate("testKey");
        assertEquals("testValue", value);
        verify(mockTranslator, times(1)).translate("testKey");
    }

    @Test
    public void testCacheHit() {
        String value = cachingWrapper.translate("testKey");
        value = cachingWrapper.translate("testKey");
        assertEquals("testValue", value);
        verify(mockTranslator, times(1)).translate("testKey");
    }

    private Translator mockTranslator(String testKey, String testValue) {
        Translator mockTranslator = mock(Translator.class);
        when(mockTranslator.translate(testKey)).thenReturn(testValue);
        return mockTranslator;
    }

}
