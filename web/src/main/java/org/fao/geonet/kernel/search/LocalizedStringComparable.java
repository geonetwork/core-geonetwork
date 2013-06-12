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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_comparator == null) ? 0 : _comparator.hashCode());
        result = prime * result + ((_wrapped == null) ? 0 : _wrapped.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalizedStringComparable other = (LocalizedStringComparable) obj;
        
        return compareTo(other) == 0;
    }
    
    
}