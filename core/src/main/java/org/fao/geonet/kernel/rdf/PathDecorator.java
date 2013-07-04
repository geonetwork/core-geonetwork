package org.fao.geonet.kernel.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Namespace;

public class PathDecorator extends Selector {
    private Selector wrapped;

    public PathDecorator(String id, String path, Selector wrapped, Namespace... namespaces) {
        super(id, path, namespaces);
        this.wrapped = wrapped;
    }

    public PathDecorator(Selector path) {
        super(path);
        this.wrapped = path;
    }

    @Override
    public Collection<Namespace> getNamespaces() {
        List<Namespace> ns = new ArrayList<Namespace>(wrapped.getNamespaces());
        ns.addAll(super.getNamespaces());
        return ns;
    }

    @Override
    public String getVariable() {
        return wrapped.getVariable();
    }

    @Override
    public String getPath() {
        return wrapped.getPath();
    }
    
}
