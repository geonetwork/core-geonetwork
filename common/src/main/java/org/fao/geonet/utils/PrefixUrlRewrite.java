package org.fao.geonet.utils;

import java.util.Objects;

/**
 * if the url starts with the provided text then write the url with a new prefix.
 *
 * @author Jesse on 11/28/2014.
 */
public class PrefixUrlRewrite implements ResolverRewriteDirective {
    private final String prefix;
    private final String replacement;

    public PrefixUrlRewrite(String prefix, String replacement) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(replacement);
        this.prefix = prefix;
        this.replacement = replacement;
    }

    @Override
    public boolean appliesTo(String href) {
        return href.startsWith(prefix);
    }

    @Override
    public String rewrite(String href) {
        return replacement + href.substring(prefix.length());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrefixUrlRewrite that = (PrefixUrlRewrite) o;

        if (!prefix.equals(that.prefix)) return false;
        if (!replacement.equals(that.replacement)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = prefix.hashCode();
        result = 31 * result + replacement.hashCode();
        return result;
    }
}
