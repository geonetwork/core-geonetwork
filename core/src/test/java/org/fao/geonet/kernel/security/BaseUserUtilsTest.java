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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void getOrCreateGroup_logsWarningWhenGroupTypeDiffers() {
        String groupName = "MismatchedGroup";
        GroupType existingType = GroupType.SystemPrivilege;
        GroupType requestedType = GroupType.Workspace;
        Group existingGroup = new Group();
        existingGroup.setName(groupName);
        existingGroup.setType(existingType);

        when(groupRepository.findByName(groupName)).thenReturn(existingGroup);

        Group result = baseUserUtils.getOrCreateGroup(groupName, requestedType);

        assertEquals(existingGroup, result);
        verify(groupRepository, never()).save(any(Group.class));
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
