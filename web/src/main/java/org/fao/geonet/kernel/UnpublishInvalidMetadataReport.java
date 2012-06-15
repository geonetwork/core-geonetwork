package org.fao.geonet.kernel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.UnpublishInvalidMetadataJob.Record;
import org.jdom.Element;

public class UnpublishInvalidMetadataReport implements Service {

    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";
    private static final String ALL = "all";
    private static final Set<String> INCLUDE_OPTIONS = new HashSet<String>(Arrays.asList(ALL,AUTO,MANUAL));

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        List includes = params.getChildren("include");
        HashSet<String> includeSet = new HashSet<String>();
        Iterator iter = includes.iterator();
        while(iter.hasNext()) {
            Element next = (Element) iter.next();
            String text = next.getTextTrim().toLowerCase();
            includeSet.add(text);
            if(!INCLUDE_OPTIONS.contains(text)) {
                throw new IllegalArgumentException("The legal values for the includes parameter are: "+INCLUDE_OPTIONS);
            } 
        }
        Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
        
        List<Record> records = UnpublishInvalidMetadataJob.values(dbms, 100, 0);
        
        Element report = new Element("report");
        
        Element autoUnpublishedToday = new Element(AUTO+"Unpublish");
        Element manualUnpublishedToday = new Element(MANUAL+"Unpublish");
        Element all = new Element(ALL+"Elements");
        
        if (includeSet.isEmpty() || includeSet.contains(AUTO)) {
            report.addContent(autoUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(MANUAL)) {
            report.addContent(manualUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(ALL)) {
            report.addContent(all);
        }
        
        for(Record todayRecord : records) {
            all.addContent(todayRecord.toElement());
            
            if(UnpublishInvalidMetadataJob.AUTOMATED_ENTITY.equals(todayRecord.entity)) {
                autoUnpublishedToday.addContent(todayRecord.toElement());
            } else {
                manualUnpublishedToday.addContent(todayRecord.toElement());
            }  
        }
        
        return report;
    }

}
