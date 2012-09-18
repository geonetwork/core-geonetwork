package org.fao.geonet.util.xml;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.util.NormalizeNamespaceException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalize namespace across XML documents.
 *
 * @author heikki doeleman
 */
public class NamespaceNormalizer {

    //
    // normalize namespaces
    //

    /**
     * Returns a generated prefix which is "xxx" and a number appended.
     *
     * @param i a number
     * @return generated prefix
     */
    private static String generatePrefix(int i) {
        String prefix = "xxx"+ ++i;
        //System.out.println("generated prefix: " + prefix );
        return prefix;
    }

    /**
     * Returns a map of namespaces to normalized namespaces in one XML document.
     *
     * @param e XML document
     * @param normalizedNamespaceMap mapping between original and normalized namespaces
     * @return map of original namespaces to normalized namespaces
     */
    protected static NormalizedNamespaceMap createNormalizedNamespaceMap(Element e, NormalizedNamespaceMap normalizedNamespaceMap) {

        //System.out.println("checking element " + e.getName());

        //
        // check element namespace
        //

        //System.out.println("element namespace " + e.getNamespace().toString());

        // namespace not yet in map
        if(StringUtils.isNotEmpty(e.getNamespaceURI()) && normalizedNamespaceMap.getNormalizedNamespace(new org.fao.geonet.util.xml.Namespace(e.getNamespace())) == null) {
            // add namespace to map
            normalizedNamespaceMap.put(new org.fao.geonet.util.xml.Namespace(e.getNamespace()), Namespace.getNamespace(generatePrefix(normalizedNamespaceMap.size()), e.getNamespaceURI()));
        }

        //
        // check additional namespaces
        //

        List additionalNamespaces = e.getAdditionalNamespaces();
        for(Object additionalNamespace : additionalNamespaces) {
            Namespace ns = (Namespace) additionalNamespace;
            //System.out.println("additional namespace " + ns.toString());

            // namespace not yet in map
            if(StringUtils.isNotEmpty(ns.getURI()) && normalizedNamespaceMap.getNormalizedNamespace(new org.fao.geonet.util.xml.Namespace(ns)) == null) {
                // add namespace to map
                normalizedNamespaceMap.put(new org.fao.geonet.util.xml.Namespace(ns), Namespace.getNamespace(generatePrefix(normalizedNamespaceMap.size()), ns.getURI()));
            }
        }

        //
        // recursively check child elements
        //

        List children = e.getChildren();
        for(Object aChildren : children) {
            Element child = (Element) aChildren;
            createNormalizedNamespaceMap(child, normalizedNamespaceMap);
        }

        return normalizedNamespaceMap;
    }

    /**
     * Returns a map of namespaces to normalized namespaces in one or more XML documents.
     *
     * @param elements XML documents
     * @return map of namespace URIs to (generated) prefixes
     */
    protected static NormalizedNamespaceMap createNormalizedNamespaceMapOverMultipleDocuments(Element... elements) {
        NormalizedNamespaceMap normalizedNamespaceMap = new NormalizedNamespaceMap();
        for(Element e : elements) {
            createNormalizedNamespaceMap(e, normalizedNamespaceMap);
        }
        return normalizedNamespaceMap;
    }

    /**
     * Returns a map of namespace prefixes to namespace uris for all namespaces in an XML element.
     *
     * @param e XML element
     * @return map of namespace URIs to prefixes
     */
    protected static Map<String, String> createURI2PrefixMap(Element e) {
        Map<String, String> namespacePrefixURIMapping = new HashMap<String, String>();
        //
        // add element namespace, if present
        //
        if(StringUtils.isNotEmpty(e.getNamespaceURI())) {
            namespacePrefixURIMapping.put(e.getNamespaceURI(), e.getNamespacePrefix());
        }
        //
        // add additional namespaces
        //
        List additionalNamespaces = e.getAdditionalNamespaces();
        for(Object additionalNamespace : additionalNamespaces) {
            Namespace ns = (Namespace) additionalNamespace;
            if(namespacePrefixURIMapping.get(ns.getURI()) == null) {
                namespacePrefixURIMapping.put(ns.getURI(), ns.getPrefix());
            }
        }
        return namespacePrefixURIMapping;
    }

    /**
     *
     * @param e XML document
     * @param normalizedNamespaceMap map of URIs to normalized prefixes
     */
    @SuppressWarnings("unchecked")
    private static void normalizeNamespace(Element e, NormalizedNamespaceMap normalizedNamespaceMap) throws NormalizeNamespaceException {
        //
        // add normalized declarations
        //

        //
        // element namespace
        //
        org.jdom.Namespace org = e.getNamespace();
        if(!StringUtils.isEmpty(org.getURI())) {
            String normalizedPrefix = normalizedNamespaceMap.getNormalizedNamespaceByURI(org.getURI()).getPrefix();
            if(StringUtils.isEmpty(normalizedPrefix)) {
                throw new NormalizeNamespaceException("Inconsistent namespace map: no generated prefix found for URI " + org.getURI());
            }
            Namespace normalizedNamespace = Namespace.getNamespace(normalizedPrefix, org.getURI());
            e.setNamespace(normalizedNamespace);
            e.addNamespaceDeclaration(org);
        }

        //
        // attribute namespaces
        //
        List attributes = e.getAttributes();
        for(Object o: attributes) {
            Attribute a = (Attribute) o;
            org.jdom.Namespace orgA = a.getNamespace();
            if(!StringUtils.isEmpty(orgA.getURI())) {
                String normalizedPrefix = normalizedNamespaceMap.getNormalizedNamespaceByURI(orgA.getURI()).getPrefix();
                if(StringUtils.isEmpty(normalizedPrefix)) {
                    throw new NormalizeNamespaceException("Inconsistent namespace map: no generated prefix found for URI " + org.getURI());
                }
                Namespace normalizedNamespace = Namespace.getNamespace(normalizedPrefix, orgA.getURI());
                a.setNamespace(normalizedNamespace);
                //e.addNamespaceDeclaration(orgA);
            }
        }

        //
        // additional namespaces
        //

        List orgNSs = e.getAdditionalNamespaces();
        // copy to new list because that one is immutable
        List orgNSList = new ArrayList();
        orgNSList.addAll(orgNSs);
        for(Object orgNSO : orgNSList) {
            Namespace orgNS = (Namespace) orgNSO;
           // e.removeNamespaceDeclaration(orgNS);
            String normalizedPrefix = normalizedNamespaceMap.getNormalizedNamespaceByURI(orgNS.getURI()).getPrefix();
            if(StringUtils.isEmpty(normalizedPrefix)) {
                throw new NormalizeNamespaceException("Inconsistent namespace map: no generated prefix found for URI " + orgNS.getURI());
            }
            Namespace normalizedNamespace = Namespace.getNamespace(normalizedPrefix, orgNS.getURI());
            e.addNamespaceDeclaration(normalizedNamespace);
        }
        //
        // recursively process children
        //
        for(Object child : e.getChildren()) {
            normalizeNamespace((Element)child, normalizedNamespaceMap);
        }

    }

    /**
     * De-normalizes namespaces for one XML document.
     *
     * @param e XML document
     */
    private static void denormalizeNamespace(Element e, List<Namespace> namespaces) throws NormalizeNamespaceException {
        //
        // remove normalized namespace declarations
        //

        //System.out.println("denormalizeNamespace for " + e.getName() + " " + Xml.getString(e)) ;

        List<Namespace> namespacesInScope = new ArrayList<Namespace>();
        namespacesInScope.addAll(namespaces);
        namespacesInScope.addAll(e.getAdditionalNamespaces());

        //for(Namespace n : namespacesInScope) {
        //    System.out.println("namespace in scope: " + n.toString());
        //}

        //
        // element namespace
        //
        org.jdom.Namespace norm = e.getNamespace();
        if(!StringUtils.isEmpty(norm.getURI())) {

            //System.out.println("norm: " + norm);

            Namespace originalNamespace = null;
            for(Namespace n : namespacesInScope) {
                if(n.getURI().equals(norm.getURI()) && !n.getPrefix().startsWith("xxx")) {
                    originalNamespace = n;
                }
            }
            if(originalNamespace != null) {
                //System.out.println("setting originalNamespace " + originalNamespace);
                e.setNamespace(originalNamespace);
            }
            else {
                //System.out.println("originalNamespace is null, setting default namespace");
                Namespace def = Namespace.getNamespace(norm.getURI());
                e.setNamespace(def);
            }
        }

        //
        // attributes' namespace
        //
        //
        // attribute namespaces
        //
        List attributes = e.getAttributes();
        for(Object o: attributes) {
            Attribute a = (Attribute) o;
            org.jdom.Namespace normA = a.getNamespace();
            if(!StringUtils.isEmpty(normA.getURI())) {

                Namespace originalNamespace = null;
                for(Namespace n : namespacesInScope) {
                    if(n.getURI().equals(normA.getURI()) && !n.getPrefix().startsWith("xxx")) {
                        originalNamespace = n;
                    }
                }
                if(originalNamespace != null) {
                    //System.out.println("setting originalNamespace " + originalNamespace);
                    a.setNamespace(originalNamespace);
                }
                else {
                    //System.out.println("originalNamespace is null, setting default namespace");
                    Namespace def = Namespace.getNamespace(normA.getURI());
                    a.setNamespace(def);
                }
            }
        }

        //
        // additional namespaces
        //

        List normNSs = e.getAdditionalNamespaces();
        // copy to new list because that one is immutable
        List normNSList = new ArrayList();
        normNSList.addAll(normNSs);
        for(Object normNSO : normNSList) {
            Namespace normNS = (Namespace) normNSO;
            //System.out.println("additional namespace: " + normNS);
            e.removeNamespaceDeclaration(normNS);
            if(!normNS.getPrefix().startsWith("xxx")) {
                e.addNamespaceDeclaration(normNS);
            }
        }
        //System.out.println("after removing normalized namespaces: " + Xml.getString(e));

        //
        // recursively process children
        //
        for(Object child : e.getChildren()) {
            denormalizeNamespace((Element)child, namespacesInScope);
        }

    }

    /**
     * Normalizes namespaces between one or more XML documents.
     *
     * @param elements XML documents
     */
    public static NormalizedNamespaceMap normalize(Element... elements) throws NormalizeNamespaceException {
        NormalizedNamespaceMap normalizedNamespaceMap = createNormalizedNamespaceMapOverMultipleDocuments(elements);
        for(Element e : elements) {
            //System.out.print("\n\n\n\n\nBefore normalization:\n" + Xml.getString(e) + "\n\n");
            normalizeNamespace(e, normalizedNamespaceMap);
            /*
            for(String normalizedPrefix : normalizedNamespaceMap.normalizedPrefixes()) {
                boolean toAdd = true;
                List additionalNamespaces = e.getAdditionalNamespaces();
                for(Object o : additionalNamespaces) {
                    Namespace ns = (Namespace) o;
                    if(ns.getPrefix().equals(normalizedPrefix)) {
                        toAdd = false;
                        break;
                    }
                }
                if(toAdd) {
                    e.addNamespaceDeclaration(Namespace.getNamespace(normalizedPrefix, "http://www.fao.org/geonetwork"));
                }
            }
            */
            //System.out.print("\n\n\n\n\nAfter normalization:\n" + Xml.getString(e) + "\n\n");
            //Xml.loadString()
        }
        return normalizedNamespaceMap;
    }

    /**
     * Denormalizes namespaces for one or more XML documents.
     *
     * @param elements XML documents
     */
    public static void denormalize(Element... elements) throws NormalizeNamespaceException {
        for(Element e : elements) {
            //System.out.println("\n\n\n\n\n\n\n**** denormalizing document:\n" + Xml.getString(e)+"\n\n");
            denormalizeNamespace(e, new ArrayList<Namespace>());
            List additionalNamespaces = e.getAdditionalNamespaces();
            List fuck = new ArrayList(additionalNamespaces);
            for(Object fuckyou : fuck) {
                Namespace ns = (Namespace) fuckyou;
                if(ns.getPrefix().startsWith("xxx")) {
                    e.removeNamespaceDeclaration(ns);
                }
            }
        }
    }

}