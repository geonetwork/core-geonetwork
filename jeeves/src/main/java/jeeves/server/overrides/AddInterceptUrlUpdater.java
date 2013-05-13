package jeeves.server.overrides;

import org.jdom.Element;

class AddInterceptUrlUpdater extends AbstractInterceptUrlUpdater {

    private final String access;
    private int position;

    public AddInterceptUrlUpdater(Element element) {
        super(element);
        String postAttVal = element.getAttributeValue("position");
        if (postAttVal == null) {
            postAttVal = "0";
        }
        this.position = Integer.parseInt(postAttVal);
        this.access = element.getAttributeValue("access");
    }

    @Override
    protected void update(Iterable<OverridesMetadataSource> sources) {
        sources.iterator().next().addMapping(patternString, pattern, access, position);
    }

}