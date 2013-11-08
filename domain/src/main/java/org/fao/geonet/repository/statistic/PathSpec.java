package org.fao.geonet.repository.statistic;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 * Interface for defining a path.
 * User: Jesse
 * Date: 10/2/13
 * Time: 6:43 PM
 */
public interface PathSpec<E, T> {
    Path<T> getPath(Root<E> root);
}
