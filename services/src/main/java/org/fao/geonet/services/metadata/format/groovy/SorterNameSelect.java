package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.ClosureComparator;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * @author Jesse on 10/20/2014.
 */
public class SorterNameSelect extends Sorter {
    private final Pattern nameMatcher;

    public SorterNameSelect(Pattern nameMatcher, ClosureComparator comparator) {
        super(comparator);
        this.nameMatcher = nameMatcher;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult result) {
        return nameMatcher.matcher(result.name()).matches();
    }
}
