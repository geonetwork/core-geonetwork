package org.fao.geonet.api.records.model.related;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedResponse")
@XmlRootElement(name = "related")
public class AssociatedResourcesResponse {

    protected Records children;
    protected Records parent;
    protected Records siblings;
    protected Records associated;
    protected Records services;
    protected Records datasets;
    protected Records fcats;
    protected Records hasfeaturecats;
    protected Records sources;
    protected Records hassources;
    protected Records related;

    /**
     * Gets the value of the children property.
     *
     * @return possible object is {@link Records }
     */
    public Records getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setChildren(Records value) {
        this.children = value;
    }

    /**
     * Gets the value of the parent property.
     *
     * @return possible object is {@link Records }
     */
    public Records getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setParent(Records value) {
        this.parent = value;
    }

    /**
     * Gets the value of the siblings property.
     *
     * @return possible object is {@link Records }
     */
    public Records getSiblings() {
        return siblings;
    }

    /**
     * Sets the value of the siblings property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setSiblings(Records value) {
        this.siblings = value;
    }

    /**
     * Gets the value of the associated property.
     *
     * @return possible object is {@link Records }
     */
    public Records getAssociated() {
        return associated;
    }

    /**
     * Sets the value of the associated property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setAssociated(Records value) {
        this.associated = value;
    }

    /**
     * Gets the value of the service property.
     *
     * @return possible object is {@link Records }
     */
    public Records getServices() {
        return services;
    }

    /**
     * Sets the value of the service property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setServices(Records value) {
        this.services = value;
    }

    /**
     * Gets the value of the dataset property.
     *
     * @return possible object is {@link Records }
     */
    public Records getDatasets() {
        return datasets;
    }

    /**
     * Sets the value of the dataset property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setDatasets(Records value) {
        this.datasets = value;
    }

    /**
     * Gets the value of the fcat property.
     *
     * @return possible object is {@link Records }
     */
    public Records getFcats() {
        return fcats;
    }

    /**
     * Sets the value of the fcat property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setFcats(Records value) {
        this.fcats = value;
    }

    public Records getHasfeaturecats() {
        return hasfeaturecats;
    }

    public void setHasfeaturecats(Records hasfeaturecats) {
        this.hasfeaturecats = hasfeaturecats;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link Records }
     */
    public Records getSources() {
        return sources;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setSources(Records value) {
        this.sources = value;
    }

    /**
     * Gets the value of the hassource property.
     *
     * @return possible object is {@link Records }
     */
    public Records getHassources() {
        return hassources;
    }

    /**
     * Sets the value of the hassource property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setHassources(Records value) {
        this.hassources = value;
    }

    /**
     * Gets the value of the related property.
     *
     * @return possible object is {@link Records }
     */
    public Records getRelated() {
        return related;
    }

    /**
     * Sets the value of the related property.
     *
     * @param value allowed object is {@link Records }
     */
    public void setRelated(Records value) {
        this.related = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Records {

        protected Map<String, AssociatedRecord> item;

        public Map<String, AssociatedRecord> getItem() {
            if (item == null) {
                item = new HashMap<String, AssociatedRecord>();
            }
            return this.item;
        }

    }
}
