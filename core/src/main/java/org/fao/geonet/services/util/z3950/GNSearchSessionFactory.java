//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet.services.util.z3950;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.configuration.api.ConfigurationException;
import org.jzkit.search.ExplainInformationDTO;
import org.jzkit.search.impl.SearchSessionFactoryImpl;
import org.jzkit.search.util.Profile.AttrMappingDBO;
import org.jzkit.search.util.Profile.CrosswalkDBO;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import javax.annotation.*;

/**
 * Overloaded JZKit Search Factory with added explain operation functionality
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 */
public class GNSearchSessionFactory extends SearchSessionFactoryImpl {

    protected ApplicationContext appl_ctx;


    public void setApplicationContext(ApplicationContext ctx) {
        super.setApplicationContext(ctx);
        this.appl_ctx = ctx;
    }

    public ExplainInformationDTO explain() {

        ExplainInformationDTO result = new ExplainInformationDTO();

        Configuration directory = (Configuration) appl_ctx.getBean("JZKitConfig");
        try {
            // Populate explain information

            // if we used ValidIndices we would use this code

               /*

               CollectionDescriptionDBO cd = directory.lookupCollectionDescription("Default");
               if (cd!= null ) {

                       Map<String, AttrValue> mappings = cd.getSearchServiceDescription().getServiceSpecificTranslations();
                       Map<String,String> reverse_mappings = new HashMap<String, String>();
                       for (String key: mappings.keySet()) {
                               reverse_mappings.put(mappings.get(key).toString(), key);
                       }

                       AttributeSetDBO attrs = cd.getSearchServiceDescription().getValidAttrs().get("AccessPoint");




                       List<GNExplainInfoDTO> list = new ArrayList<GNExplainInfoDTO>();
                       for (AttrValue val: attrs.getAttrs() ) {

                               GNExplainInfoDTO exl = new GNExplainInfoDTO();
                               exl.addMapping(val.getValue(), val.getNamespaceIdentifier());

                               String reverse_key = val.getNamespaceIdentifier()+":"+val.getValue();
                               if (reverse_mappings.containsKey(reverse_key)) {
                                       String[] temp = reverse_mappings.get(reverse_key).split("\\.");
                                       if (temp.length==3) exl.addMapping(temp[1]+"."+temp[2], temp[0]);
                                       if (temp.length==2) exl.addMapping(temp[1], temp[0]);
                               }

                               list.add(exl);
                       }

                       result.setDatabaseInfo(list);

               }

               */

            // but we use Crosswalks (the info on what we actually accept is in the geo profile, but we suppose
            // that the crosswalks will eventually lead to a valid attribute)

            // this should really be done differently but unfortunately there is
            // no way to know if a mapping is for an attribute (can be a relation, too)
            // we take as indicator the fact that are is a "1"
            Pattern p = Pattern.compile(".*\\.1\\.([0-9]+)$");

            @SuppressWarnings("unchecked")
            Iterator<CrosswalkDBO> it = directory.enumerateCrosswalks();

            List<GNExplainInfoDTO> list = new ArrayList<GNExplainInfoDTO>();
            while (it.hasNext()) {


                CrosswalkDBO cw = it.next();

                for (String key : cw.getMappings().keySet()) {

                    AttrMappingDBO attrmaping = cw.getMappings().get(key);

                    if (attrmaping.getTargetAttrs().isEmpty()) {
                        continue;
                    }

                    // namespace is not important, just important that it is there (for the patternmatching)
                    String attrString = attrmaping.getTargetAttrs().iterator().next().getWithDefaultNamespace("geo");

                    // find out which are the actual attribute mappings (this is ugly)
                    // "geo:1.4" matches, "something:1.3443", too, but not "gagh" or "geo.2.22"
                    Matcher m = p.matcher(attrString);

                    if (m.find()) {
                        String id = m.group(1);

                        GNExplainInfoDTO exl = new GNExplainInfoDTO(id);
                        exl.addMapping(attrmaping.getSourceAttrValue(), cw.getSourceNamespace());

                        list.add(exl);

                    }

                }

            }

            result.setDatabaseInfo(list);

        } catch (ConfigurationException ce) {
            Log.error(Geonet.SRU, "GNSearchSessionFactory - explain, error: " + ce.getMessage(), ce);
        }


        return result;

    }


}
