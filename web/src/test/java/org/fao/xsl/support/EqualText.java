package org.fao.xsl.support;

import org.jdom.Element;

public class EqualText implements Requirement
{

    private final String _expected;

    public EqualText(String expected)
    {
        if (expected == null)
            throw new IllegalArgumentException("expected cannot be null");
        _expected = expected;
    }

    public boolean eval(Element e)
    {
        return _expected.equals(e.getText());
    }

    @Override
    public String toString()
    {
        return "text() = " + _expected;
    }
}