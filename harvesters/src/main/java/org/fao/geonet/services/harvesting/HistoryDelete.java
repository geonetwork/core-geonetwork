package org.fao.geonet.services.harvesting;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class HistoryDelete implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
        Collection<Integer> ids = Collections2.transform(params.getChildren("id"), new Function<Object, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nonnull Object input) {
                return Integer.valueOf(((Element)input).getText());
            }
        });

        List<Element> files = params.getChildren("file");

        int nrRecs = context.getBean(HarvestHistoryRepository.class).deleteAllById(ids);

		return new Element(Jeeves.Elem.RESPONSE).setText(nrRecs+"");
	}
}
