package org.fao.geonet.util.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map from original namespaces to normalized namespaces. Uses a wrapper for org.jdom.Namespace as keys, in order to
 * distinguish namespaces with different prefixes and the same uri.
 *
 * @author heikki doeleman
 */
public class NormalizedNamespaceMap {
    private Map<Namespace, org.jdom.Namespace> namespaceMap = new HashMap<Namespace, org.jdom.Namespace>();

    public org.jdom.Namespace getNormalizedNamespace(Namespace namespace) {
        return namespaceMap.get(namespace);
    }

    public org.jdom.Namespace getNormalizedNamespaceByURI(String uri) {
        for(org.jdom.Namespace normalizedNamespace : namespaceMap.values()) {
            if(normalizedNamespace.getURI().equals(uri)) {
                return normalizedNamespace;
            }
        }
        return null;
    }

    public String getNormalizedNamespaceURI(String normalizedPrefix) {
        for(org.jdom.Namespace ns : namespaceMap.values()) {
            if(ns.getPrefix().equals(normalizedPrefix)) {
                return ns.getURI();
            }
        }
        return null;
    }

    public void put(Namespace namespace, org.jdom.Namespace normalized) {
        namespaceMap.put(namespace, normalized);
    }

    public int size() {
        return namespaceMap.size();
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *         is not supported by this map
     */
    public void clear() {
        namespaceMap.clear();
    }

    public List<String> normalizedPrefixes() {
        List<String> normalizedPrefixes = new ArrayList<String>();
        for(org.jdom.Namespace ns : namespaceMap.values()) {
            normalizedPrefixes.add(ns.getPrefix());
        }
        return normalizedPrefixes;
    }

    /**
     * Returns a {@link java.util.Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<Namespace> keySet() {
        return namespaceMap.keySet();
    }

    /**
     * Returns a {@link java.util.Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map
     */
    public Collection<org.jdom.Namespace> values() {
        return namespaceMap.values();
    }

}