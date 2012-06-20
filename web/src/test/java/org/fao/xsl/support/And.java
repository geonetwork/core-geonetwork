package org.fao.xsl.support;

import java.util.Arrays;

import org.jdom.Element;

public class And implements Requirement
{

    private final Requirement[] _filters;

    public And(Requirement... filter)
    {
        this._filters = filter;
    }

    public boolean eval(Element e)
    {
        for (Requirement f : _filters) {
            if (!f.eval(e)) {
                return false;
            }
        }
        return true;
    }

    public String toString()
    {
        return "Exists[" + Arrays.toString(_filters) + "]";
    }
}