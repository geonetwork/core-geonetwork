package org.fao.geonet.repository;

import org.fao.geonet.domain.Localized;
import org.jdom.Element;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Common repository interface for all Repositories that load entities that extend the Localised abstract class.
 *
 * User: Jesse
 * Date: 9/9/13
 * Time: 2:58 PM
 */
public interface LocalizedEntityRepository<T extends Localized, ID extends Serializable> {

    /**
     * Load all entities and convert each to XML of the form:
     *     <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     * @return
     */
    @Nonnull
    Element findAllAsXml();
}
