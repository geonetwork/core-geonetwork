package org.fao.geonet.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.JDOMException;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.utils.Xml;

public class LangUtils {

    /**
     * Find all the translations for a given key in the <type>.xml file.  normally you will want 
     * 'type' to == 'string'.  In fact the 2 parameter method can be used for this.
     * 
     * @param type the type of translations file, typically strings
     * @param key the key to look up.  may contain / but cannot start with one.  for example: categories/water
     * @return
     */
    public static Map<String, String> translate(ServiceContext context, String type, String key) throws JDOMException, IOException {
        String appPath = context.getAppPath();
        XmlCacheManager cacheManager = context.getXmlCacheManager();
        File loc = new File(appPath, "loc");
        String typeWithExtension = "xml"+File.separator+type+".xml";
        Map<String, String> translations = new HashMap<String, String>();
        
        for (File file : loc.listFiles()) {
            if(file.isDirectory() && new File(file, typeWithExtension).exists()) {
                Element xml = cacheManager.get(context, true, loc.getAbsolutePath(), typeWithExtension, file.getName(), file.getName());
                String translation = Xml.selectString(xml, key);
                if(translation != null && !translation.trim().isEmpty()) {
                    translations.put(file.getName(), translation);
                }
            }
        }
        
        return translations;
    }
    /**
     * same as translate(context, "string", key)
     */
    public static Map<String, String> translate(ServiceContext context, String key) throws JDOMException, IOException {
        return translate(context, "strings", key);
    }
    
    /**
     * Find all the translations for a given key in the <table>Des table in the database.
     * 
     * @param table Table to look for (translation are stored in <table>Des)
     * @param key the key to look up.
     * @return
     * @throws Exception 
     */
    public static Map<String, String> dbtranslate(ServiceContext context, String table, String key) throws Exception {
       String sqlSelect = String.format("SELECT langid, label FROM %sDes WHERE idDes=?", table);
       Map<String, String> translations = new HashMap<String, String>();
       Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
       Element result = dbms.select(sqlSelect, Integer.parseInt(key));
       for (Object o : result.getChildren()) {
           Element record = (Element) o;
           translations.put(record.getChildText("langid"), record.getChildText("label"));
       }
       return translations;
    }
}
