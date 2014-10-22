package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

/**
 * @author Jesse on 10/16/2014.
 */
public class HandlerFunctionSelect extends Handler {
    private final Closure selectFunction;

    public HandlerFunctionSelect(Closure select, int priority, Closure handlerFunction) {
        super(priority, handlerFunction);
        this.selectFunction = select;
    }

    @Override
    public boolean select(TransformationContext rootPath, GPathResult elem) {
        final int maximumNumberOfParameters = this.selectFunction.getMaximumNumberOfParameters();
        boolean matches;
        switch (maximumNumberOfParameters) {
            case 0:
                matches = (boolean) this.selectFunction.call();
                break;
            case 1:
                matches = (boolean) this.selectFunction.call(elem);
                break;
            case 2:
                StringBuilder path = new StringBuilder();
                createPath(elem, path);
                matches = (boolean) this.selectFunction.call(elem, path.toString());
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " + maximumNumberOfParameters);
        }

        return matches;
    }

    @Override
    protected String handlerExtraToString() {
        return "";
    }

}
