package org.fao.geonet.repository;

import org.springframework.data.domain.Sort;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for converting Spring sort objects to and from JPA Order objects.
 * <p/>
 * User: Jesse
 * Date: 9/4/13
 * Time: 7:58 AM
 */
class SortUtils {
    /**
     * Get the property from the order and create a JPA path from it.
     *
     * @param order the order containing the property
     * @param roots the roots that might be the root of the path.  All roots will be tested in order
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
}
