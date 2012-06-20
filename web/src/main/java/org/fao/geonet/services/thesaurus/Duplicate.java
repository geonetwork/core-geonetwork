package org.fao.geonet.services.thesaurus;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

public class Duplicate implements Service {

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {

        String uuid = Util.getParam(params, "uuid");
        String thesaurusName = Util.getParam(params, "thesaurus");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager thesaurusManager = gc.getThesaurusManager();
        Thesaurus thesaurus = thesaurusManager.getThesaurusByName(thesaurusName);
        KeywordsSearcher ks = new KeywordsSearcher(thesaurusManager);
        
        KeywordBean bean = ks.searchById(uuid, thesaurusName, "*", true);
        if(bean == null) {
            return new Element("uuid").setText(uuid);
        }
        if(!(bean.getLanguages().contains("en") || bean.getLanguages().contains("eng"))) {
            thesaurus.addElement(bean.getNameSpaceCode(), bean.getRelativeCode(), bean.getValue(), bean.getDefinition(), "eng");
        }
        
        if(!(bean.getLanguages().contains("it") || bean.getLanguages().contains("ita"))) {
            thesaurus.addElement(bean.getNameSpaceCode(), bean.getRelativeCode(), bean.getValue(), bean.getDefinition(), "ita");
        }
        
        if(!(bean.getLanguages().contains("fr") || bean.getLanguages().contains("fre") || bean.getLanguages().contains("fra"))) {
            thesaurus.addElement(bean.getNameSpaceCode(), bean.getRelativeCode(), bean.getValue(), bean.getDefinition(), "fre");
        }
        
        if(!(bean.getLanguages().contains("de") || bean.getLanguages().contains("deu") || bean.getLanguages().contains("ger"))) {
            thesaurus.addElement(bean.getNameSpaceCode(), bean.getRelativeCode(), bean.getValue(), bean.getDefinition(), "ger");
        }
        return new Element("uuid").setText(uuid);
    }

}
