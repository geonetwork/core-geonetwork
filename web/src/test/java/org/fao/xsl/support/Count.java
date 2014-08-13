package org.fao.xsl.support;

import org.jdom.Element;
import org.jdom.filter.Filter;

import java.util.Iterator;

public class Count implements Requirement
{

    private final int    _expected;
    private final Filter _filter;
    private final String msg;

    public Count(int expected, Filter filter)
    {
        this("", expected, filter);
    }

    public Count(String msg, int expected, Filter filter)
    {
        this.msg = msg;
        this._filter = filter;
        _expected = expected;
    }

    @SuppressWarnings("rawtypes")
    public boolean eval(Element e)
    {
        Iterator descendants = e.getDescendants(_filter);
        int count = 0;
        while (descendants.hasNext()) {
            count++;
            descendants.next();
        }

        if(_expected != count) {
            System.err.println(toString() + "\n\tExpected "+_expected+" but got "+count);
        }
        return _expected == count;
    }

    @Override
    public String toString()
    {
        return msg + " count[" + _filter + "] = " + _expected;
    }
}