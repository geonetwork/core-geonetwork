package org.fao.geonet.services.metadata.format.function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.metadata.format.FormatterConstants;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a mapping from the name of a formatter function to the function object.
 *
 * @author Jesse on 10/2/2014.
 */
public class FormatterFunctionMap {
    private final Map<String, FormatterFunction> nameToFunction = Maps.newHashMap();
    private final Set<Namespace> namespaces = Sets.newHashSet(FormatterConstants.GNF_NAMESPACE);

    public Collection<Namespace> getNamespaces() {
        return namespaces;
    }

    public Collection<FormatterFunction> functions() {
        return this.nameToFunction.values();
    }

    public void merge(FormatterFunctionMap toMerge) {
        this.namespaces.addAll(toMerge.namespaces);

        for (FormatterFunction function : toMerge.nameToFunction.values()) {
            if (this.nameToFunction.containsKey(function.getName())) {
                throw new AssertionError("Formatter Function gnf:" + function.getName() + " is defined more than once");
            }
            this.nameToFunction.put(function.getName(), function);

        }
    }

    public FormatterFunction get(String xslFilePath, String formatterName) {
        final FormatterFunction formatterFunction = this.nameToFunction.get(formatterName);
        if (formatterFunction == null) {
            throw new AssertionError("Formatter Function 'gnf:" + formatterName + "' in " + xslFilePath + " was referenced but is not " +
                                     "defined in any of the included formatter function files.");
        }
        return formatterFunction;
    }

    public FormatterFunctionMap copy() {
        final FormatterFunctionMap copy = new FormatterFunctionMap();
        copy.nameToFunction.putAll(this.nameToFunction);
        copy.namespaces.addAll(this.namespaces);

        return copy;
    }

    @SuppressWarnings("unchecked")
    public void loadFrom(String filename, Element xsltFile) {
        Collection<Element> children = xsltFile.getChildren();

        for (Element child : children) {
            if (!child.getName().equals("function") || !Geonet.Namespaces.XSL.getURI().equals(child.getNamespaceURI())) {
                throw new AssertionError("Functions files may only contain xsl functions the file: '" + filename +
                                         "' contains an element: " + child.getQualifiedName());
            }

            final FormatterFunction formatterFunction = new FormatterFunction(filename, child);
            nameToFunction.put(formatterFunction.getName(), formatterFunction);
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");
        for (FormatterFunction function : nameToFunction.values()) {
            builder.append(function.getName()).append(", ");
        }

        builder.append("]");

        return builder.toString();
    }

    void addNamespaces(List<Namespace> namespaces) {
        this.namespaces.addAll(namespaces);
    }

    public void addNamespace(Namespace namespace) {
        this.namespaces.add(namespace);
    }
}
