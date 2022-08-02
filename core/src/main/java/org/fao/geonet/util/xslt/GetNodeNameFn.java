package org.fao.geonet.util.xslt;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.fao.geonet.util.XslUtil;

public class GetNodeNameFn extends ExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(XslFn.PREFIX, XslFn.URI, "getNodeName");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN};
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
                String key = ((StringValue) arguments[0]).getStringValue();
                String lang = ((StringValue) arguments[1]).getStringValue();
                boolean withOrganization = ((BooleanValue) arguments[2]).getBooleanValue();
                return StringValue.makeStringValue(
                    XslUtil.getNodeName(key, lang, withOrganization));
            }
        };
    }
}
