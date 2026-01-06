package org.fao.geonet.kernel.mef;


import org.fao.geonet.api.exception.AttachmentsExportLimitExceededException;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MEFLibTest {

private static final long ATTACHMENTS_SIZE_LIMIT_BYTES = 1000L;

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
                MEFLib.checkAttachmentsUnderSizeLimit(Set.of(UUID.randomUUID().toString()), false)
            );

            assertEquals("exception.attachmentsExportLimitExceededException.batch.description", exception.getDescriptionKey());
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldCall_attachmentsExceedExportLimit_withCorrectParameters() {
        Set<String> recordUuids = Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());


        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.attachmentsExceedExportLimit(anySet(), anyBoolean()))
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

            boolean result = MEFLib.attachmentsExceedExportLimit(Set.of(UUID.randomUUID().toString()), false);

            assertFalse(result);
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldReturnFalse_whenAttachmentsAreUnderLimit() {
        EsSearchManager searchManagerMock = mock(EsSearchManager.class);

        Map<String, List<Map<String, Object>>> recordsAndResources = Map.of(
            UUID.randomUUID().toString(),
            List.of(
                Map.of("size", 50L),
                Map.of("size", 70L)
            ),
            UUID.randomUUID().toString(),
            List.of(
                Map.of("size", 20L),
                Map.of("size", 30L)
            )
        );

        when(searchManagerMock.getResourcesFromIndex(anyString(), anyBoolean()))
            .thenAnswer(invocation -> recordsAndResources.get(invocation.getArgument(0)));

        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.getMaxAttachmentSizeInBytes(any(SettingManager.class)))
                .thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES);

            assertFalse(MEFLib.attachmentsExceedExportLimit(recordsAndResources.keySet(), false));
        }
    }

    @Test
    public void attachmentsExceedExportLimit_shouldReturnTrue_whenAttachmentsExceedLimit() {
        EsSearchManager searchManagerMock = mock(EsSearchManager.class);

        Map<String, List<Map<String, Object>>> recordsAndResources = Map.of(
            UUID.randomUUID().toString(),
            List.of(
                Map.of("size", 500L),
                Map.of("size", 700L)
            ),
            UUID.randomUUID().toString(),
            List.of(
                Map.of("size", 200L),
                Map.of("size", 300L)
            )
        );

        when(searchManagerMock.getResourcesFromIndex(anyString(), anyBoolean()))
            .thenAnswer(invocation -> recordsAndResources.get(invocation.getArgument(0)));

        try (MockedStatic<MEFLib> mefLibMock = mockStatic(MEFLib.class, Mockito.CALLS_REAL_METHODS)) {
            mefLibMock.when(() -> MEFLib.getMaxAttachmentSizeInBytes(any(SettingManager.class)))
                .thenReturn(ATTACHMENTS_SIZE_LIMIT_BYTES);

            assertTrue(MEFLib.attachmentsExceedExportLimit(recordsAndResources.keySet(), false));
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

    @Test
    public void getTotalSizeOfAllAttachments_shouldReturnCorrectTotalSize_whenAllSizesAreValid() {
        List<Map<String, Object>> resources = List.of(
            Map.of("size", 100L),
            Map.of("size", 200L),
            Map.of("size", 300L)
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(600L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldReturnZero_whenResourcesListIsEmpty() {
        List<Map<String, Object>> resources = List.of();

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(0L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldIgnoreNullSizes() {

        Map<String, Object> resourceWithNullSize = new HashMap<>();
        resourceWithNullSize.put("size", null);

        List<Map<String, Object>> resources = List.of(
            Map.of("size", 100L),
            resourceWithNullSize,
            Map.of("size", 300L)
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(400L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldIgnoreResourcesWithMissingSizeKey() {
        List<Map<String, Object>> resources = List.of(
            Map.of("size", 100L),
            Map.of(),
            Map.of("size", 300L)
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(400L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldTreatNonNumericSizesAsZero() {
        List<Map<String, Object>> resources = List.of(
            Map.of("size", 100L),
            Map.of("size", "invalid"),
            Map.of("size", 300L)
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(400L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldReturnZero_whenAllSizesAreNull() {
        Map<String, Object> resourceWithNullSize = new HashMap<>();
        resourceWithNullSize.put("size", null);

        List<Map<String, Object>> resources = List.of(
            resourceWithNullSize,
            resourceWithNullSize
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(0L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldReturnZero_whenAllSizesAreMissing() {
        List<Map<String, Object>> resources = List.of(
            Map.of(),
            Map.of()
        );

        long totalSize = MEFLib.getTotalSizeOfAllAttachments(resources);

        assertEquals(0L, totalSize);
    }

    @Test
    public void getTotalSizeOfAllAttachments_shouldReturnZero_whenResourcesListIsNull() {
        long totalSize = MEFLib.getTotalSizeOfAllAttachments(null);

        assertEquals(0L, totalSize);
    }
}
