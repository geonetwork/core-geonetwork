package org.fao.geonet.util.xml;

/**
 * Wrapper for org.jdom.Namespace to use equals() with both prefix and uri.
 *
 * @author heikki doeleman
 */
public class Namespace {

    private org.jdom.Namespace namespace;

    public Namespace(org.jdom.Namespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Namespace) {
            Namespace n = (Namespace)o;
            return this.namespace.getPrefix().equals(n.namespace.getPrefix()) &&
                    this.namespace.getURI().equals(n.namespace.getURI());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return namespace.getPrefix().hashCode() + namespace.getURI().hashCode();
    }

    public org.jdom.Namespace getNamespace() {
        return this.namespace;
    }

    public String getPrefix() {
        return namespace.getPrefix();
    }

    public String getURI() {
        return namespace.getURI();
    }
}