package org.fao.geonet.kernel.mef;


import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.AttachmentsExportLimitExceededException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
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

    /**
     * Regression tests for {@link MEFLib#getFilesElement}, the info.xml reader's single point of
     * truth for locating a file's changeDate/mimetype whether the archive uses the MEF 3.0
     * unified {@code <store>} element or the pre-3.0 {@code <public>}/{@code <private>}
     * elements. Previously only exercised indirectly through other integration tests.
     */
    @Test
    public void getFilesElement_shouldReturnFilesFilteredByAccess_whenStoreElementPresent() {
        Element info = new Element("info");
        Element store = new Element("store");
        store.addContent(new Element("file").setAttribute("name", "a.txt").setAttribute("access", "public"));
        store.addContent(new Element("file").setAttribute("name", "b.txt").setAttribute("access", "private"));
        info.addContent(store);

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");
        List<Element> privateFiles = MEFLib.getFilesElement(info, "private");

        assertEquals(1, publicFiles.size());
        assertEquals("a.txt", publicFiles.get(0).getAttributeValue("name"));
        assertEquals(1, privateFiles.size());
        assertEquals("b.txt", privateFiles.get(0).getAttributeValue("name"));
    }

    @Test
    public void getFilesElement_shouldReturnLegacyFiles_whenNoStoreElementButLegacyElementPresent() {
        Element info = new Element("info");
        Element legacyPublic = new Element("public");
        legacyPublic.addContent(new Element("file").setAttribute("name", "a.txt"));
        info.addContent(legacyPublic);

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        assertEquals(1, publicFiles.size());
        assertEquals("a.txt", publicFiles.get(0).getAttributeValue("name"));
    }

    @Test
    public void getFilesElement_shouldReturnEmptyList_whenNeitherStoreNorLegacyElementPresent() {
        Element info = new Element("info");
        info.addContent(new Element("general"));

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        assertNotNull("Must never return null, so callers can iterate without a null-check", publicFiles);
        assertTrue(publicFiles.isEmpty());
    }

    @Test
    public void getFilesElement_shouldPreferStoreElement_whenBothStoreAndLegacyElementsPresent() {
        Element info = new Element("info");
        Element store = new Element("store");
        store.addContent(new Element("file").setAttribute("name", "from-store.txt").setAttribute("access", "public"));
        info.addContent(store);
        Element legacyPublic = new Element("public");
        legacyPublic.addContent(new Element("file").setAttribute("name", "from-legacy.txt"));
        info.addContent(legacyPublic);

        List<Element> publicFiles = MEFLib.getFilesElement(info, "public");

        assertEquals(1, publicFiles.size());
        assertEquals("from-store.txt", publicFiles.get(0).getAttributeValue("name"));
    }

    /**
     * Regression tests for {@link MEFLib#buildInfoFiles}, the info.xml writer's counterpart to
     * {@link #getFilesElement}.
     */
    @Test
    public void buildInfoFiles_shouldIncludeAccessAndMimetypeAttributes_whenResourceHasThem() {
        MetadataResource resource = mock(MetadataResource.class);
        when(resource.getFilename()).thenReturn("thumbnail.gif");
        when(resource.getLastModification()).thenReturn(new Date(0));
        when(resource.getVisibility()).thenReturn(MetadataResourceVisibility.PUBLIC);
        when(resource.getMimeType()).thenReturn("image/gif");

        Element store = MEFLib.buildInfoFiles("store", Collections.singletonList(resource));

        assertEquals("store", store.getName());
        @SuppressWarnings("unchecked")
        List<Element> files = store.getChildren("file");
        assertEquals(1, files.size());
        Element file = files.get(0);
        assertEquals("thumbnail.gif", file.getAttributeValue("name"));
        assertEquals("public", file.getAttributeValue("access"));
        assertEquals("image/gif", file.getAttributeValue("mimetype"));
    }

    @Test
    public void buildInfoFiles_shouldOmitMimetypeAttribute_whenResourceHasNoMimetype() {
        MetadataResource resource = mock(MetadataResource.class);
        when(resource.getFilename()).thenReturn("file.bin");
        when(resource.getLastModification()).thenReturn(new Date(0));
        when(resource.getVisibility()).thenReturn(MetadataResourceVisibility.PRIVATE);
        when(resource.getMimeType()).thenReturn(null);

        Element store = MEFLib.buildInfoFiles("store", Collections.singletonList(resource));

        @SuppressWarnings("unchecked")
        List<Element> files = store.getChildren("file");
        Element file = files.get(0);
        assertEquals("private", file.getAttributeValue("access"));
        assertNull("No mimetype attribute should be written when the resource has none", file.getAttributeValue("mimetype"));
    }

    @Test
    public void buildInfoFiles_shouldReturnEmptyElement_whenResourceListIsNull() {
        Element store = MEFLib.buildInfoFiles("store", null);

        assertEquals("store", store.getName());
        @SuppressWarnings("unchecked")
        List<Element> files = store.getChildren("file");
        assertTrue(files.isEmpty());
    }
}
