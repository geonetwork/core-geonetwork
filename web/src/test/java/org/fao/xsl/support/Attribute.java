package org.fao.xsl.support;

import org.jdom.Element;
import org.jdom.filter.Filter;

public class Attribute implements Filter
{
    private static final long serialVersionUID = 1L;
    private final String      _elemName;
    private final String      _attName;
    private final String      _expected;

    public Attribute(String elemName, String attName, String expectedValue)
    {
        super();
        _elemName = elemName;
        _attName = attName;
        _expected = expectedValue;
    }

    public Attribute(String attName)
    {
        this(null, attName, null);
    }

    public boolean matches(Object arg0)
    {
        if (arg0 instanceof Element) {
            Element e = (Element) arg0;
            if (_elemName!=null && !e.getName().equals(_elemName)) {
                return false;
            }
            if (_expected == null) {
                return e.getAttributeValue(_attName) != null;
            } else {
                boolean result = _expected.equals(e.getAttributeValue(_attName));
                if (_elemName != null && !result) {
                    //System.out.println("Expected " + toString() + " but got " + e.getAttributeValue(_attName));
                }
                return result;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String e = _elemName == null ? "" : _elemName;
        if (_expected == null) {
            return e + "@" + _attName;
        } else {
            return e + "@" + _attName + " = " + _expected;
        }
    }

}