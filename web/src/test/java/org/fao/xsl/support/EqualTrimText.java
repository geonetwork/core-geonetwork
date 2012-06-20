package org.fao.xsl.support;

import org.jdom.Element;

public class EqualTrimText implements Requirement
{

    private final String _expected;

    public EqualTrimText(String expected)
    {
        if (expected == null)
            throw new IllegalArgumentException("expected cannot be null");
        _expected = expected;
    }

    public boolean eval(Element e)
    {
        return _expected.equals(e.getText().trim());
    }

    @Override
    public String toString()
    {
        return "trim(text()) = " + _expected;
    }
}