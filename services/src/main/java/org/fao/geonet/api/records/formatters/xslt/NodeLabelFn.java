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

public class NodeLabelFn extends GnExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(
            XslFn.PREFIX,
            XslFn.URI,
            "nodeLabel");
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 3;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 4;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.OPTIONAL_STRING};
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
                Item nodeName = arguments[2].head();
                String nodeNameVal = (nodeName != null) ? nodeName.getStringValue() : null;
                Item parentNodeName = arguments[3].head();
                String parentNodeNameVal = (parentNodeName != null) ? parentNodeName.getStringValue() : null;
                try {
                    SchemaLocalizations schemaLocalizations = getSchemaLocalizations(arguments);
                    String response = schemaLocalizations.nodeLabel(nodeNameVal, parentNodeNameVal);
                    return StringValue.makeStringValue(response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
