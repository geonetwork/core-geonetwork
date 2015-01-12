package jeeves.server.overrides;

import org.jdom.Element;

class SetInterceptUrlUpdater extends AbstractInterceptUrlUpdater {

    private final String access;

    public SetInterceptUrlUpdater(Element element) {
        super(element);
        this.access = element.getAttributeValue("access");
    }

    @Override
    protected void update(Iterable<OverridesMetadataSource> sources) {
        boolean found = false;
        for (OverridesMetadataSource overridesMetadataSource : sources) {
            try {
                overridesMetadataSource.setMapping(patternString, access);
                found = true;
            } catch (IllegalArgumentException e) {
                // this is exception is ignored.
            }
        }
        
        if (!found) {
            throw new IllegalArgumentException("Unable to find an existing url mapping "+patternString);
        }
    }

}