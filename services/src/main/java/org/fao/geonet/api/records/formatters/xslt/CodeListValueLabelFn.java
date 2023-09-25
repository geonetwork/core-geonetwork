package org.fao.geonet.api.records.formatters.xslt;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.fao.geonet.api.records.formatters.SchemaLocalizations;

public class CodeListValueLabelFn extends GnExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(
            XslFn.PREFIX,
            XslFn.URI,
            "codelist-value-label");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING};
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                Item codeListName = arguments[2].head();
                String codeListNameVal = (codeListName != null) ? codeListName.getStringValue() : null;
                Item codeListValue = arguments[3].head();
                String codeListValueVal = (codeListValue != null) ? codeListValue.getStringValue() : null;
                try {
                    SchemaLocalizations schemaLocalizations = getSchemaLocalizations(arguments);
                    String response = schemaLocalizations.codelistValueLabel(codeListNameVal, codeListValueVal);
                    return StringValue.makeStringValue(response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
