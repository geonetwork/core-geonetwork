package org.fao.xsl.support;

import org.jdom.Element;

public class Not implements Requirement
{
    private final Requirement _req;

    public Not(Requirement req)
    {
        super();
        _req = req;
    }

    public boolean eval(Element e)
    {
        return !_req.eval(e);
    }

    @Override
    public String toString()
    {
        return "not(" + _req + ")";
    }
}