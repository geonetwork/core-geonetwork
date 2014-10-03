package org.fao.geonet.services.metadata.format.function;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;
import java.util.regex.Matcher;

import static org.fao.geonet.services.metadata.format.FormatterConstants.GNF_NAMESPACE;

/**
 * Represents one of the shared formatter functions and its dependencies.
 *
 * @author Jesse on 10/2/2014.
 */
public class FormatterFunction {
    private static final String GNF_PREFIX = "gnf:";
    private static final int PREFIX_LENGTH = GNF_PREFIX.length();
    private final String name;
    private final String definition;
    private final List<String> dependencies = Lists.newArrayList();

    public FormatterFunction(String xslFilePath, Element functionEl) {
        String prefixedName = functionEl.getAttributeValue("name");

        this.name = getFunctionName(xslFilePath, functionEl, prefixedName);
        this.definition = Xml.getString(functionEl);

        final Matcher matcher = FormatterFunctionManager.FUNCTION_FINDER.matcher(this.definition);
        while(matcher.find()) {
            this.dependencies.add(matcher.group(1));
        }
    }

    private String getFunctionName(String xslFilePath, Element functionEl, String prefixedName) {
        if (!prefixedName.startsWith(GNF_PREFIX)) {
            throw new AssertionError("Only functions with the prefix: 'gnf' are permitted.  The function: " +
                                     prefixedName + " referenced in xsl file: " + xslFilePath + " does not satisfy these requirements");
        }

        if (!hasCorrectGnfNamespace(functionEl)) {
            throw new AssertionError("Only functions with the gnf namespace must be bound to " + GNF_NAMESPACE.getURI() +
                                     ".  The function: " + prefixedName + " does not satisfy these requirements");
        }

        return prefixedName.substring(PREFIX_LENGTH);
    }

    private boolean hasCorrectGnfNamespace(Element functionEl) {
        if (functionEl.getNamespace("gnf") == null) {
            return hasCorrectGnfNamespace(functionEl.getParentElement());
        }
        return GNF_NAMESPACE.getURI().equals(functionEl.getNamespace("gnf").getURI());
    }


    public String getName() {
        return name;
    }

    public void addTo(String xslFilePath, FormatterFunctionMap functions, StringBuilder functionFile) {
        functionFile.append(this.definition).append("\n\n");
        for (String dep : this.dependencies) {
            FormatterFunction depFunc = functions.get(xslFilePath, dep);
            if (depFunc == null) {
                throw new AssertionError(
                        "Dependency '" + dep + "' of formatter function: " + this.name + " is not available in the" +
                        " available functions for the current formatter." + xslFilePath + "  Options are: " + functions);
            }
            depFunc.addTo(xslFilePath, functions, functionFile);
        }
    }

    @VisibleForTesting
    void assertValidDependencies(String xslFilePath, FormatterFunctionMap formatterFunction) {
        for (String dependency : this.dependencies) {
            formatterFunction.get(xslFilePath, dependency);
        }
    }
}
