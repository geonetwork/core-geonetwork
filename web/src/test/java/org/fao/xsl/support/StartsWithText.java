package org.fao.xsl.support;

import org.jdom.Element;

public class StartsWithText implements Requirement
{

    private final String _expected;

    public StartsWithText(String expected)
    {
        if (expected == null)
            throw new IllegalArgumentException("expected cannot be null");
        _expected = expected;
    }

    public boolean eval(Element e)
    {
        return e.getTextTrim()!=null && e.getTextTrim().startsWith(_expected);
    }

    @Override
    public String toString()
    {
        return "startsWith(text()) = " + _expected;
    }
}