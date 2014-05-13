package org.fao.geonet.guiservices.keywords;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.util.List;

/**
 * Get all the keywords in the identified thesaurus.
 *
 * @author Jesse on 5/12/2014.
 */
public class GetAllInThesaurus implements Service {

    private static String[] LANGS = {"eng", "fre", "ger", "ita"};
    private String thesaurus;
    private volatile Element keywords = null;
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        this.thesaurus = params.getMandatoryValue("thesaurus");
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        if (this.keywords == null) {
            synchronized (this) {
                if (this.keywords == null) {
                    final GeonetContext geonet = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                    final ThesaurusManager thesaurusManager = geonet.getThesaurusManager();
                    final Thesaurus thesaurusByName = thesaurusManager.getThesaurusByName(this.thesaurus);
                    if (thesaurusByName == null) {
                        throw new IllegalArgumentException(this.thesaurus + " is not a valid thesaurus name.  " +
                                                           "Update configuration file.");
                    }

                    final IsoLanguagesMapper languagesMapper = IsoLanguagesMapper.getInstance();
                    final KeywordSearchParamsBuilder paramsBuilder = new KeywordSearchParamsBuilder(languagesMapper);
                    for (String lang : LANGS) {
                        paramsBuilder.addLang(lang);
                    }
                    paramsBuilder.addThesaurus(this.thesaurus);
                    List<KeywordBean> results = paramsBuilder.build().search(thesaurusManager);
                    this.keywords = new Element("keywords");
                    for (KeywordBean result : results) {
                        this.keywords.addContent(result.toElement(thesaurusManager, context, context.getLanguage(), LANGS));
                    }
                }
            }
        }
        return this.keywords;
    }
}
