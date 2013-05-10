package jeeves.server.overrides;

import org.jdom.Element;

class SetInterceptUrlUpdater extends AbstractInterceptUrlUpdater {

    private final String access;

    public SetInterceptUrlUpdater(Element element) {
        super(element);
        this.access = element.getAttributeValue("access");
    }

    @Override
    protected void update(OverridesMetadataSource overrideSource) {
        overrideSource.setMapping(pattern, access);
    }

}