package org.fao.geonet.kernel.security;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.GroupType;
import org.fao.geonet.domain.Language;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseUserUtilsTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private BaseUserUtils baseUserUtils;

    @Test
    public void getOrCreateGroup_createsNewGroupWhenNotFound() {
        String groupName = "NewGroup";
        GroupType groupType = GroupType.Workspace;

        when(groupRepository.findByName(groupName)).thenReturn(null);
        when(languageRepository.findAll()).thenReturn(Collections.emptyList());

        Group result = baseUserUtils.getOrCreateGroup(groupName, groupType);

        assertNotNull(result);
        assertEquals(groupName, result.getName());
        assertEquals(groupType, result.getType());
        verify(groupRepository).save(result);
    }

    @Test
    public void getOrCreateGroup_returnsExistingGroupWhenFound() {
        String groupName = "ExistingGroup";
        GroupType groupType = GroupType.Workspace;
        Group existingGroup = new Group();
        existingGroup.setName(groupName);
        existingGroup.setType(groupType);

        when(groupRepository.findByName(groupName)).thenReturn(existingGroup);

        Group result = baseUserUtils.getOrCreateGroup(groupName, groupType);

        assertEquals(existingGroup, result);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    public void getOrCreateGroup_noWarningWhenRequestedTypeIsNull() {
        String groupName = "ExistingTypedGroup";
        Group existingGroup = new Group();
        existingGroup.setName(groupName);
        existingGroup.setType(GroupType.Workspace);

        when(groupRepository.findByName(groupName)).thenReturn(existingGroup);

        String output = captureStdOut(() -> {
            Group result = baseUserUtils.getOrCreateGroup(groupName); // no type requested
            assertEquals(existingGroup, result);
        });

        assertFalse(output.contains("Using existing group type."));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    public void getOrCreateGroup_logsWarningWhenGroupTypeDiffers() {
        String groupName = "MismatchedGroup";
        GroupType existingType = GroupType.SystemPrivilege;
        GroupType requestedType = GroupType.Workspace;
        Group existingGroup = new Group();
        existingGroup.setName(groupName);
        existingGroup.setType(existingType);

        when(groupRepository.findByName(groupName)).thenReturn(existingGroup);

        String output = captureStdOut(() -> {
            Group result = baseUserUtils.getOrCreateGroup(groupName, requestedType);
            assertEquals(existingGroup, result);
        });

        assertTrue(output.contains("Warning: Group '" + groupName + "' exists with type '" + existingType +
            "', but requested type is '" + requestedType + "'. Using existing group type."));
        verify(groupRepository, never()).save(any(Group.class));
    }

    private String captureStdOut(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outputStream));
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return outputStream.toString();
    }

    @Test
    public void getOrCreateGroup_populatesLabelTranslationsForNewGroup() {
        String groupName = "TranslatedGroup";
        GroupType groupType = GroupType.Workspace;
        Language language = new Language();
        language.setId("en");
        when(groupRepository.findByName(groupName)).thenReturn(null);
        when(languageRepository.findAll()).thenReturn(Collections.singletonList(language));

        Group result = baseUserUtils.getOrCreateGroup(groupName, groupType);

        assertEquals(groupName, result.getLabelTranslations().get("en"));
        verify(groupRepository).save(result);
    }

}
