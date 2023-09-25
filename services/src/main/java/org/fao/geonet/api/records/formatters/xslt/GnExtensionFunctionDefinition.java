package org.fao.geonet.api.records.formatters.xslt;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.records.formatters.SchemaLocalizations;
import org.jdom.JDOMException;

import java.io.IOException;

public abstract class GnExtensionFunctionDefinition extends ExtensionFunctionDefinition {
    protected static SchemaLocalizations getSchemaLocalizations(Sequence[] arguments) throws XPathException, IOException, JDOMException {
        Item schema = arguments[0].head();
        String schemaVal = (schema != null) ? schema.getStringValue() : null;
        Item lang = arguments[1].head();
        String langVal = (lang != null && StringUtils.isNotEmpty(lang.getStringValue())) ? lang.getStringValue() : null;

        SchemaLocalizations schemaLocalizations;
        if (langVal == null) {
            schemaLocalizations = SchemaLocalizations.create(schemaVal);
        } else {
            schemaLocalizations = SchemaLocalizations.create(schemaVal, langVal);
        }
        return schemaLocalizations;
    }
}
