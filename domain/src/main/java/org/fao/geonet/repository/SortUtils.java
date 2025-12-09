/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.repository;

import org.springframework.data.domain.Sort;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for converting Spring sort objects to and from JPA Order objects and for creating sort
 * objects
 * <p/>
 * User: Jesse Date: 9/4/13 Time: 7:58 AM
 */
public class SortUtils {
    /**
     * Get the property from the order and create a JPA path from it.
     *
     * @param order the order containing the property
     * @param roots the roots that might be the root of the path.  All roots will be tested in
     *              order
     * @return the path
     */
    public static Path<?> toJPAPath(final Sort.Order order, final Root<?>... roots) {
        String[] pathParts = order.getProperty().split("\\.");

        Path<?> currentPathElement = null;
        for (String part : pathParts) {
            if (currentPathElement == null) {
                for (Root<?> root : roots) {
                    try {
                        currentPathElement = root.get(part);
                    } catch (IllegalArgumentException e) {
                        // this root doesn't have property try another.
                        continue;
                    }
                }
            } else {
                currentPathElement = currentPathElement.get(part);
            }
        }

        return currentPathElement;
    }

    /**
     * Convert the Spring Sort to JPA order objects.
     *
     * @param cb    a Criteria Builder
     * @param sort  the sort object to convert
     * @param roots the possible roots for the sort properties.
     * @return the list of orders created from the sort object.
     */
    public static List<Order> sortToJpaOrders(CriteriaBuilder cb, Sort sort, Root<?>... roots) {
        ArrayList<Order> orders = new ArrayList<Order>();
        for (Sort.Order order : sort) {
            final Path<?> path = toJPAPath(order, roots);
            if (order.isAscending()) {
                orders.add(cb.asc(path));
            } else {
                orders.add(cb.desc(path));
            }
        }
        return orders;
    }

    /**
     * Construct a path string from the id attributes.  The path string is '.' separated and is used
     * in sorting and JPA Query Language queries.
     *
     * @param attributes the attributes that make up the path from root to end attribute.
     * @return a '.' separated path.
     */
    public static String createPath(SingularAttribute<?, ?>... attributes) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < attributes.length; i++) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(attributes[i].getName());
        }
        return builder.toString();
    }

    /**
     * Create a sort object from the path objects.  This only creates a sort with a single path.
     * For multiple paths do:
     * <p/>
     * <p> new Sort(createPath(attributes1...), createPath(attributes2...),...) </p>
     *
     * @param attributes the attributes to use for building a sort.
     * @return a sort object from the path objects
     */
    public static Sort createSort(SingularAttribute<?, ?>... attributes) {
        return Sort.by(createPath(attributes));
    }

    public static Sort createSort(Sort.Direction direction, SingularAttribute<?, ?>... attributes) {
        return Sort.by(direction, createPath(attributes));
    }
}
