package iso19110

import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.Functions
import org.fao.geonet.services.metadata.format.groovy.util.NavBarItem
import org.fao.geonet.services.metadata.format.groovy.util.Summary

/**
 * @author Jesse on 6/24/2015.
 */
class Handlers extends iso19139.Handlers {
    def iso19139RootPackageEl;

    public Handlers(org.fao.geonet.services.metadata.format.groovy.Handlers handlers, Functions f, Environment env) {
        super(handlers, f, env);
        rootEl = 'gfc:FC_FeatureCatalogue'
        packageViews = [rootEl]
        iso19139RootPackageEl = super.rootPackageEl
        rootPackageEl = iso19100RootPackageEl
    }

    private def iso19100RootPackageEl = { el ->
        Summary summary = new Summary(handlers, env, f);

        summary.title = isofunc.isoText(el['gmx:name'])
        summary.abstr = ''
        summary.content = iso19139RootPackageEl(el)
        summary.addNavBarItem(new NavBarItem(f.translate('complete'), null, '.container > .entry:not(.overview)'))
        summary.addNavBarItem(commonHandlers.createXmlNavBarItem())
        summary.addCompleteNavItem = false
        summary.addOverviewNavItem = false

        summary.result
    }

}
