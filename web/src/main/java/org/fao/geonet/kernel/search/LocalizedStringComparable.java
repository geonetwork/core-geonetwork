package org.fao.geonet.kernel.search;

import java.text.Collator;
import java.util.Locale;

class LocalizedStringComparable implements Comparable<LocalizedStringComparable>
{
    public final String _wrapped;
    private final Collator _comparator;

    public LocalizedStringComparable(String wrapped, Locale locale)
    {
        this._wrapped = wrapped;
        _comparator = java.text.Collator.getInstance(locale);
    }

    public int compareTo(LocalizedStringComparable anotherString)
    {
        return _comparator.compare(_wrapped, anotherString._wrapped);
    }
}