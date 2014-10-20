package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.ClosureComparator;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * @author Jesse on 10/20/2014.
 */
public class SorterPathSelect extends Sorter {
    private final Pattern pathPattern;

    public SorterPathSelect(Pattern pathPattern, ClosureComparator comparator) {
    super(comparator);
        this.pathPattern = pathPattern;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult parentElement) {
        StringBuilder path = new StringBuilder(context.getRootPath());
        if (path.length() > 0) {
            path.append(">");
        }
        Handler.createPath(parentElement, path);

        return this.pathPattern.matcher(path.toString()).matches();
    }
}
