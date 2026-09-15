package org.fao.geonet.kernel.mef;


import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.AttachmentsExportLimitExceededException;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MEFLibTest {

    private static final long ATTACHMENTS_SIZE_LIMIT_BYTES = 1000L;

    private static final SettingManager settingManagerMock = mock(SettingManager.class);
    private static final EsSearchManager searchManagerMock = mock(EsSearchManager.class);

    @Test
    public void checkAttachmentsUnderSizeLimit_shouldDoNothing_whenAttachmentsAreUnderSizeLimit() throws AttachmentsExportLimitExceededException {
        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean()))
                .thenReturn(false);
            MEFLib.checkAttachmentsUnderSizeLimit(Set.of(UUID.randomUUID().toString()), false);
        }
    }

    @Test
    public void checkAttachmentsUnderSizeLimit_shouldThrowException_whenSingleRecordAttachmentsExceedLimit(){
        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean()))
                .thenReturn(true);

            AttachmentsExportLimitExceededException exception = assertThrows(AttachmentsExportLimitExceededException.class, () ->
                MEFLib.checkAttachmentsUnderSizeLimit(Set.of(UUID.randomUUID().toString()), false)
            );

            assertEquals("exception.attachmentsExportLimitExceededException.single.description", exception.getDescriptionKey());
        }
    }

    @Test
    public void checkAttachmentsUnderSizeLimit_shouldThrowException_whenMultipleRecordsAttachmentsExceedLimit(){
        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean()))
                .thenReturn(true);

            AttachmentsExportLimitExceededException exception = assertThrows(AttachmentsExportLimitExceededException.class, () ->
                MEFLib.checkAttachmentsUnderSizeLimit(Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), false)
            );

            assertEquals("exception.attachmentsExportLimitExceededException.batch.description", exception.getDescriptionKey());
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldCall_attachmentsExceedExportLimit_withCorrectParameters() {
        Set<String> recordUuids = Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ConfigurableApplicationContext applicationContextMock = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<ApplicationContextHolder> applicationContextHolderMock = mockStatic(ApplicationContextHolder.class, Mockito.CALLS_REAL_METHODS);
                MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {

            applicationContextHolderMock.when(ApplicationContextHolder::get).thenReturn(applicationContextMock);

            when(applicationContextMock.getBean(SettingManager.class)).thenReturn(settingManagerMock);
            when(applicationContextMock.getBean(EsSearchManager.class)).thenReturn(searchManagerMock);

            mefLibMock.when(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean(), any(SettingManager.class), any(EsSearchManager.class)))
                .thenReturn(false);

            MEFLib.attachmentsExceedExportLimit(recordUuids, true);

            mefLibMock.verify(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean(), any(SettingManager.class), any(EsSearchManager.class)), times(1));
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldReturnFalse_whenNoLimitIsConfigured() {
        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.getMaxAttachmentSizeInBytes(any(SettingManager.class)))
                .thenReturn(null);

            boolean result = MEFLib.attachmentsExceedExportLimit(Set.of(UUID.randomUUID().toString()), false, settingManagerMock, searchManagerMock);

            assertFalse(result);
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldReturnFalse_whenAttachmentsAreUnderLimit() {
        EsSearchManager searchManagerMock = mock(EsSearchManager.class);

        when(searchManagerMock.getTotalSizeOfResources(anySet(), anyBoolean())).thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES/2);

        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.getMaxAttachmentSizeInBytes(any(SettingManager.class)))
                .thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES);

            assertFalse(MEFLib.attachmentsExceedExportLimit(Set.of(UUID.randomUUID().toString()), false, settingManagerMock, searchManagerMock));
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldReturnTrue_whenAttachmentsExceedLimit() {
        EsSearchManager searchManagerMock = mock(EsSearchManager.class);

        when(searchManagerMock.getTotalSizeOfResources(anySet(), anyBoolean())).thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES*2);

        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.getMaxAttachmentSizeInBytes(any(SettingManager.class)))
                .thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES);

            assertTrue(MEFLib.attachmentsExceedExportLimit(Set.of(UUID.randomUUID().toString()), false, settingManagerMock, searchManagerMock));
        }
    }

    @Test
    public void getMaxAttachmentSizeInBytes_shouldReturnBytes_whenSettingIsValidNumber() {
        SettingManager settingManager = mock(SettingManager.class);
        when(settingManager.getValueAsLong(anyString())).thenReturn(5L);

        Long result = MEFLib.getMaxAttachmentSizeInBytes(settingManager);

        assertEquals(Long.valueOf(5L * 1024 * 1024), result);
    }

    @Test
    public void getMaxAttachmentSizeInBytes_shouldReturnNull_whenSettingIsNull() {
        SettingManager settingManager = mock(SettingManager.class);
        when(settingManager.getValueAsLong(anyString())).thenReturn(null);

        Long result = MEFLib.getMaxAttachmentSizeInBytes(settingManager);

        assertNull(result);
    }

    @Test
    public void getMaxAttachmentSizeInBytes_shouldReturnNull_whenSettingIsNonNumeric() {
        SettingManager settingManager = mock(SettingManager.class);
        when(settingManager.getValueAsLong(anyString())).thenThrow(new NumberFormatException("invalid"));

        Long result = MEFLib.getMaxAttachmentSizeInBytes(settingManager);

        assertNull(result);
    }

    @Test
    public void getMaxAttachmentSizeInBytes_shouldReturnNull_whenSettingIsNegative() {
        SettingManager settingManager = mock(SettingManager.class);
        when(settingManager.getValueAsLong(anyString())).thenReturn(-10L);

        Long result = MEFLib.getMaxAttachmentSizeInBytes(settingManager);

        assertNull(result);
    }
}
