package org.fao.geonet.kernel.schema;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jesse on 2/6/14.
 */
public class SchematronCriteriaGroup {
    private String name;
    private List<SchematronCriteria> criteriaList = new ArrayList<SchematronCriteria>();
    private SchematronRequirement requirement;
    private int schematronId;

    /**
     * Get the name/id of this group. This is only shown to the administrator
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name/id of this group.
     *
     * @param name the group name/id
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the schematron criteria that of this group.
     *
     * @return the schematron criteria that of this group.
     */
    public List<SchematronCriteria> getCriteriaList() {
        return criteriaList;
    }

    /**
     * Set the schematron criteria that of this group.
     * <p/>
     * Use {@link #addCriteria(SchematronCriteria)} for adding criteria to this group rather than adding
     * the criteria to this list.
     * @param criteriaList the schematron criteria that of this group.
     */
    public void setCriteriaList(List<SchematronCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    /**
     * Get the requirement value if this criteria group is applicable for the metadata.
     *
     * @return the requirement.
     */
    public SchematronRequirement getRequirement() {
        return requirement;
    }

    /**
     * Set the level requirement for this group.
     *
     *
     * @param requirement
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setRequirement(SchematronRequirement requirement) {
        this.requirement = requirement;
        return this;
    }
    /**
     * Get the schematron this group applies to.
     *
     * @return the schematron
     */
    public int getSchematronId() {
        return schematronId;
    }

    /**
     * Set the schematron this group applies to.
     *
     *
     * @param schematronId
     *            the schematron to set
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setSchematronId(int schematronId) {
        this.schematronId = schematronId;
        return this;
    }

    public Element asXml() {
        Element results = new Element("criteriaGroup").addContent(
                Arrays.asList(
                        new Element("name").setText(name),
                        new Element("requirement").setText(requirement.name())
                )
        );

        Element criterialist = new Element("criteria");

        results.addContent(criterialist);
        for (SchematronCriteria criteria : criteriaList) {
            Element criteriaEl = new Element("criteria");
            criteriaEl.addContent(Arrays.asList(
                    new Element("type").setText(criteria.getType().name()),
                    new Element("value").setText(criteria.getValue())
            ));

            criterialist.addContent(criteriaEl);
        }

        return results;
    }
}
