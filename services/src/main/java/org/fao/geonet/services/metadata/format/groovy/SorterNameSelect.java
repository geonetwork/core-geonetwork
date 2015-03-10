package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.ClosureComparator;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * @author Jesse on 10/20/2014.
 */
public class SorterNameSelect extends Sorter {
    private final Pattern namePattern;

    public SorterNameSelect(int priority, Pattern namePattern, ClosureComparator comparator) {
        super(priority, comparator);
        this.namePattern = namePattern;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult result) {
        return namePattern.matcher(result.name()).matches();
    }

    @Override
    protected String extraToString() {
        return ", namePattern ~= /" + namePattern + "/";
    }
}
