package jeeves.server.overrides;

import org.jdom.Element;

public class RemoveInterceptUrlUpdater extends AbstractInterceptUrlUpdater {

    public RemoveInterceptUrlUpdater(Element element) {
        super(element);
    }

    @Override
    protected void update(OverridesMetadataSource overrideSource) {
        overrideSource.removeMapping(pattern);
    }

}
