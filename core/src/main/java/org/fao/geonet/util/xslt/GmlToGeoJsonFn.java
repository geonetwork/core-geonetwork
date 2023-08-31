package org.fao.geonet.util.xslt;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.fao.geonet.util.XslUtil;

public class GmlToGeoJsonFn extends ExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(
            XslFn.PREFIX,
            XslFn.URI,
            "gmlToGeoJson");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN, SequenceType.SINGLE_INTEGER};
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
                String gml =arguments[0].head().getStringValue();
                Boolean applyPrecisionModel = arguments[1].head().effectiveBooleanValue();
                Integer numberOfDecimals = Integer.valueOf(arguments[2].head().getStringValue());
                return StringValue.makeStringValue(XslUtil.gmlToGeoJson(gml,applyPrecisionModel, numberOfDecimals));
            }
        };
    }
}
