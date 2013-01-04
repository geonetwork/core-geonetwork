package org.fao.xsl.support;

import org.jdom.Element;

public class EqualAttribute implements Requirement
{
    private final String      _attName;
    private final String      _expected;

    public EqualAttribute(String attName, String expectedValue)
    {
        super();
        _attName = attName;
        _expected = expectedValue;
    }

    @Override
    public String toString()
    {
        if (_expected == null) {
            return "@" + _attName;
        } else {
            return "@" + _attName + " = " + _expected;
        }
    }

    @Override
    public boolean eval(Element e) {
    	if(_expected != null) {
    		return e.getAttributeValue(_attName)!=null && e.getAttributeValue(_attName).equals(_expected);
    	} else {
    		return e.getAttributeValue(_attName)!=null;
    	}
    }
}