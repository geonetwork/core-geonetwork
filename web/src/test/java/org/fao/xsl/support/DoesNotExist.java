package org.fao.xsl.support;

import org.jdom.Element;
import org.jdom.filter.Filter;

public class DoesNotExist implements Requirement {

    private final Filter _filter;

    public DoesNotExist(Filter filter)
    {
        this._filter = filter;
    }

    public boolean eval(Element e)
    {
        return !_filter.matches(e) && !e.getDescendants(_filter).hasNext();
    }

    public String toString()
    {
        return "DoesNotExist[" + _filter + "]";
    }

}
