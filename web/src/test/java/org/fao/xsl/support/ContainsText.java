package org.fao.xsl.support;

import org.jdom.Element;

public class ContainsText implements Requirement
{

    private final String _expected;

    public ContainsText(String expected)
    {
        if (expected == null)
            throw new IllegalArgumentException("expected cannot be null");
        _expected = expected;
    }

    public boolean eval(Element e)
    {
        return e.getTextTrim()!=null && e.getTextTrim().contains(_expected);
    }

    @Override
    public String toString()
    {
        return "contains(text()) = " + _expected;
    }
}