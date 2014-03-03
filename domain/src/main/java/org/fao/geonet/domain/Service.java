package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.ServiceEntityListenerManager;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * One of the entities responsible for dynamic service configuration. Entity representing a {@link jeeves.interfaces.Service}. Originally
 * they were for CSW virtual services but are generic and could in theory be any arbitrary service.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Services")
@EntityListeners(ServiceEntityListenerManager.class)
@SequenceGenerator(name=Service.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class Service extends GeonetEntity {
    static final String ID_SEQ_NAME = "service_id_seq";
    private int _id;
    private String _name;
    private String _className;
    private String description;
    private Map<String, String> _parameters = new HashMap<String, String>();

    /**
     * Get the id of the service entity. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     *
     * @return the id of the service.
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the service entity. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     *
     * @param id the id of the service entity.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the name of the service.
     *
     * @return the name of the service.
     */
    @Column(nullable = false, unique = true)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the service.
     *
     * @param name the name of the service.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the service class name.
     *
     * @return the service class name.
     */
    @Column(name = "class", length = 1024, nullable = false)
    public String getClassName() {
        return _className;
    }

    /**
     * Set the service class name.
     *
     * @param className the service class name.
     */
    public void setClassName(String className) {
        this._className = className;
    }

    /**
     * Get the description of the service. Maximum length is 1024 characters.
     *
     * @return the description of the service. Maximum length is 1024 characters.
     */
    @Column(length = 1024)
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the service. Maximum length is 1024 characters.
     *
     * @param description the description of the service. Maximum length is 1024 characters.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the init parameters to pass to the service.  The Key is the parameter name the value is the parameter value.
     *
     * @return the init parameters to pass to the service.  The Key is the parameter name the value is the parameter value.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(joinColumns = @JoinColumn(name = "service", insertable = true, updatable = true, unique = false,
            nullable = false), name = "ServiceParameters")
    @MapKeyColumn(name = "name")
    @Column(name = "value", nullable = false, length = 1024)
    public Map<String, String> getParameters() {
        return _parameters;
    }

    /**
     * Set the init parameters to pass to the service. The Key is the parameter name the value is the parameter value.
     *
     * @param parameters the init parameters to pass to the service.  The Key is the parameter name the value is the parameter value.
     */
    public void setParameters(final Map<String, String> parameters) {
        this._parameters = parameters;
    }

}
