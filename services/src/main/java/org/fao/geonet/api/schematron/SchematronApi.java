/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.schematron;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroupId;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.domain.Schematron_;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.specification.SchematronCriteriaGroupSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration of the schematron validation: display priority of schematrons and the criteria
 * groups deciding when a schematron applies to a record.
 * <p>
 * Group names are free text, so they are passed as request parameters instead of in the path.
 */
@RequestMapping(value = {
    "/{portal}/api/schematron"
})
@Tag(name = "schematron",
    description = "Schematron validation configuration")
@Controller("schematron")
@ReadWriteController
@PreAuthorize("hasAuthority('Administrator')")
public class SchematronApi {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    SchematronRepository schematronRepository;

    @Autowired
    SchematronCriteriaGroupRepository groupRepository;

    @Autowired
    SchematronCriteriaRepository criteriaRepository;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    XmlCacheManager cacheManager;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    ApplicationContext applicationContext;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get schematrons by standard",
        description = "Returns the requirement levels and, for each standard, its schematrons and the criteria types " +
            "that can be used in criteria groups.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SchematronConfiguration getSchematrons(HttpServletRequest request) throws Exception {
        String language = languageUtils.getIso3langCode(request.getLocales());

        Map<String, SchemaSchematrons> schemas = new LinkedHashMap<>();
        for (Schematron schematron : schematronRepository.findAll(SortUtils.createSort(Schematron_.displayPriority))) {
            String schemaName = schematron.getSchemaName();
            Path schemaDir = schemaManager.getSchemaDir(schemaName);
            SchemaSchematrons schema = schemas.get(schemaName);
            if (schema == null) {
                schema = new SchemaSchematrons(schemaName, getCriteriaTypes(schemaDir, language));
                schemas.put(schemaName, schema);
            }

            Element strings = loadTranslations(schemaDir, schematron.getRuleName() + ".xml", language);
            String title = strings.getChildText("schematron.title");
            long groupCount = groupRepository.count(SchematronCriteriaGroupSpecs.hasSchematronId(schematron.getId()));
            schema.schematrons.add(new SchematronInfo(schematron, title == null ? schematron.getRuleName() : title, groupCount));
        }

        return new SchematronConfiguration(Arrays.asList(SchematronRequirement.values()), new ArrayList<>(schemas.values()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Set the display priority of a schematron")
    @PutMapping(value = "/{schematronId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSchematron(
        @PathVariable int schematronId,
        @RequestParam int displayPriority) throws ResourceNotFoundException {
        getSchematron(schematronId);
        schematronRepository.update(schematronId, entity -> entity.setDisplayPriority(displayPriority));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the criteria groups of a schematron")
    @GetMapping(value = "/{schematronId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<SchematronCriteriaGroup> getGroups(@PathVariable int schematronId) throws ResourceNotFoundException {
        getSchematron(schematronId);
        return groupRepository.findAll(SchematronCriteriaGroupSpecs.hasSchematronId(schematronId));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a criteria group to a schematron")
    @PostMapping(value = "/{schematronId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SchematronCriteriaGroup addGroup(
        @PathVariable int schematronId,
        @Parameter(description = "Group name") @RequestParam String name,
        @RequestParam(defaultValue = "REQUIRED") SchematronRequirement requirement) throws ResourceNotFoundException {
        getSchematron(schematronId);
        SchematronCriteriaGroupId id = new SchematronCriteriaGroupId(name, schematronId);
        if (groupRepository.existsById(id)) {
            throw new IllegalArgumentException(String.format("A criteria group named '%s' already exists for this schematron.", name));
        }
        return groupRepository.saveAndFlush(new SchematronCriteriaGroup().setId(id).setRequirement(requirement));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a criteria group",
        description = "Changes the requirement, renames the group or moves it to another schematron.")
    @PutMapping(value = "/{schematronId}/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGroup(
        @PathVariable int schematronId,
        @Parameter(description = "Group name") @RequestParam String name,
        @RequestParam(required = false) SchematronRequirement requirement,
        @RequestParam(required = false) String newName,
        @RequestParam(required = false) Integer newSchematronId) throws ResourceNotFoundException {
        SchematronCriteriaGroup group = getGroup(name, schematronId);
        SchematronCriteriaGroupId newId = new SchematronCriteriaGroupId(
            newName == null ? name : newName,
            newSchematronId == null ? schematronId : newSchematronId);

        if (newId.equals(group.getId())) {
            if (requirement != null) {
                groupRepository.update(group.getId(), entity -> entity.setRequirement(requirement));
            }
            return;
        }

        if (newSchematronId != null) {
            getSchematron(newSchematronId);
        }
        if (groupRepository.existsById(newId)) {
            throw new IllegalArgumentException(String.format("A criteria group named '%s' already exists for this schematron.", newId.getName()));
        }
        SchematronCriteriaGroup newGroup = new SchematronCriteriaGroup()
            .setId(newId)
            .setRequirement(requirement == null ? group.getRequirement() : requirement);
        for (SchematronCriteria criteria : group.getCriteria()) {
            newGroup.addCriteria(criteria.copy());
        }
        groupRepository.deleteById(group.getId());
        groupRepository.saveAndFlush(newGroup);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a criteria group and its criteria")
    @DeleteMapping(value = "/{schematronId}/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(
        @PathVariable int schematronId,
        @Parameter(description = "Group name") @RequestParam String name) throws ResourceNotFoundException {
        groupRepository.deleteById(getGroup(name, schematronId).getId());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a criteria to a criteria group")
    @PostMapping(value = "/{schematronId}/groups/criteria", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SchematronCriteria addCriteria(
        @PathVariable int schematronId,
        @Parameter(description = "Group name") @RequestParam String groupName,
        @RequestParam SchematronCriteriaType type,
        @RequestParam(defaultValue = "") String value,
        @RequestParam String uiType,
        @RequestParam(defaultValue = "") String uiValue) throws ResourceNotFoundException {
        SchematronCriteriaGroup group = getGroup(groupName, schematronId);

        SchematronCriteria criteria = new SchematronCriteria();
        criteria.setType(type);
        criteria.setValue(value);
        criteria.setUiType(uiType);
        criteria.setUiValue(uiValue);
        group.addCriteria(criteria);

        List<SchematronCriteria> saved = groupRepository.saveAndFlush(group).getCriteria();
        return saved.get(saved.size() - 1);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Update a criteria")
    @PutMapping(value = "/criteria/{criteriaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCriteria(
        @PathVariable int criteriaId,
        @RequestParam(required = false) SchematronCriteriaType type,
        @RequestParam(required = false) String value,
        @RequestParam(required = false) String uiType,
        @RequestParam(required = false) String uiValue) throws ResourceNotFoundException {
        getCriteria(criteriaId);
        criteriaRepository.update(criteriaId, entity -> {
            if (type != null) {
                entity.setType(type);
            }
            if (value != null) {
                entity.setValue(value);
            }
            if (uiType != null) {
                entity.setUiType(uiType);
            }
            if (uiValue != null) {
                entity.setUiValue(uiValue);
            }
        });
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a criteria")
    @DeleteMapping(value = "/criteria/{criteriaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCriteria(@PathVariable int criteriaId) throws ResourceNotFoundException {
        SchematronCriteriaGroup group = getCriteria(criteriaId).getGroup();
        // The group owns its criteria (orphan removal), so remove it from the group.
        group.getCriteria().removeIf(c -> c.getId() == criteriaId);
        groupRepository.saveAndFlush(group);
    }

    private Schematron getSchematron(int id) throws ResourceNotFoundException {
        return schematronRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Schematron '%d' not found.", id)));
    }

    private SchematronCriteriaGroup getGroup(String name, int schematronId) throws ResourceNotFoundException {
        return groupRepository.findById(new SchematronCriteriaGroupId(name, schematronId))
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "Criteria group '%s' not found for schematron '%d'.", name, schematronId)));
    }

    private SchematronCriteria getCriteria(int id) throws ResourceNotFoundException {
        return criteriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Criteria '%d' not found.", id)));
    }

    /**
     * Criteria types are defined by each standard in schematron/criteria-type.xml. Their content is
     * free (eg. remote search configuration for autocompletion), so they are returned as converted
     * from XML, plus the generic "always accept" and XPath types.
     */
    private ArrayNode getCriteriaTypes(Path schemaDir, String language) throws IOException, JDOMException {
        List<Element> types = new ArrayList<>();
        Path file = schemaDir.resolve("schematron").resolve("criteria-type.xml");
        if (Files.exists(file)) {
            @SuppressWarnings("unchecked")
            List<Element> schemaTypes = Xml.loadFile(file).getChildren();
            types.addAll(schemaTypes);
        }
        types.add(criteriaType(SchematronCriteriaType.ALWAYS_ACCEPT, null));
        types.add(criteriaType(SchematronCriteriaType.XPATH, "@@value@@"));

        Element labels = loadTranslations(schemaDir, "criteria-type.xml", language);
        ArrayNode result = JSON.createArrayNode();
        for (Element type : types) {
            String name = type.getChildText("name").toUpperCase();
            String label = labels.getChildText(name.toLowerCase());
            type.addContent(new Element("label").setText(label == null ? name : label));
            result.add(JSON.readTree(Xml.getJSON((Element) type.clone())));
        }
        return result;
    }

    private Element criteriaType(SchematronCriteriaType type, String value) {
        Element element = new Element("type")
            .addContent(new Element("type").setText(type.name()))
            .addContent(new Element("name").setText(type.name()));
        if (value != null) {
            element.addContent(new Element("value").setText(value));
        }
        return element;
    }

    private Element loadTranslations(Path schemaDir, String file, String language) throws JDOMException, IOException {
        try {
            return cacheManager.get(applicationContext, true, schemaDir.resolve("loc"), file, language,
                Geonet.DEFAULT_LANGUAGE, true);
        } catch (NoSuchFileException e) {
            // Some standards have no translations for their schematrons or criteria types.
            return new Element("strings");
        }
    }

    public static class SchematronConfiguration {
        public final List<SchematronRequirement> requirements;
        public final List<SchemaSchematrons> schemas;

        SchematronConfiguration(List<SchematronRequirement> requirements, List<SchemaSchematrons> schemas) {
            this.requirements = requirements;
            this.schemas = schemas;
        }
    }

    public static class SchemaSchematrons {
        public final String name;
        public final JsonNode criteriaTypes;
        public final List<SchematronInfo> schematrons = new ArrayList<>();

        SchemaSchematrons(String name, JsonNode criteriaTypes) {
            this.name = name;
            this.criteriaTypes = criteriaTypes;
        }
    }

    public static class SchematronInfo {
        public final int id;
        public final String schemaName;
        public final String ruleName;
        public final String title;
        public final int displayPriority;
        public final long groupCount;

        SchematronInfo(Schematron schematron, String title, long groupCount) {
            this.id = schematron.getId();
            this.schemaName = schematron.getSchemaName();
            this.ruleName = schematron.getRuleName();
            this.title = title;
            this.displayPriority = schematron.getDisplayPriority();
            this.groupCount = groupCount;
        }
    }
}
