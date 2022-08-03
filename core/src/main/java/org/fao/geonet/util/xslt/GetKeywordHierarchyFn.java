package org.fao.geonet.util.xslt;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.fao.geonet.util.XslUtil;

import java.util.List;
import java.util.stream.Collectors;

public class GetKeywordHierarchyFn extends ExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(XslFn.PREFIX, XslFn.URI, "getKeywordHierarchy");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING};
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.STRING_SEQUENCE;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                String keyword = ((StringValue) arguments[0]).getStringValue();
                String thesaurus = ((StringValue) arguments[1]).getStringValue();
                String lang = ((StringValue) arguments[2]).getStringValue();

                List<StringValue> stringValues =
                    XslUtil.getKeywordHierarchy(keyword, thesaurus, lang)
                        .stream()
                        .map(StringValue::makeStringValue)
                        .collect(Collectors.toList());

                return SequenceExtent.makeSequenceExtent(stringValues);
            }
        };
    }
}
