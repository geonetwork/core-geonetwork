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

package org.fao.geonet.services.util.z3950.provider.GN;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.services.util.z3950.GNXMLQuery;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.DOMOutputter;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.InformationFragment;
import org.jzkit.search.util.RecordModel.InformationFragmentImpl;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.jzkit.search.util.ResultSet.*;

import java.util.List;
import java.util.Observer;

/**
 * interface between JZKit and GN. Retrieves XML content from the GN backend and
 * makes it available to JZkit
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 *
 */
public class GNResultSet extends AbstractIRResultSet implements IRResultSet {

       private GNXMLQuery query;
       private ServiceContext srvxtx;
       private int status;

       private int fragmentcount;

       private MetaSearcher metasearcher;

       public GNResultSet(GNXMLQuery query, Object userInfo, Observer[] observers,
                       ServiceContext srvctx) throws Exception {
               super(observers);
               this.query = query;
               this.srvxtx = srvctx;

               try {

                       GeonetContext gc = (GeonetContext) this.srvxtx
                       .getHandlerContext(Geonet.CONTEXT_NAME);
                       SearchManager searchMan = gc.getBean(SearchManager.class);

                       metasearcher = searchMan.newSearcher(SearcherType.LUCENE,
                                       Geonet.File.SEARCH_Z3950_SERVER);

               } catch (Exception e) {
                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "error constructing GNresult set: " + e);
                       e.printStackTrace();
               }
       }

       public int evaluate(int timeout) {
               try {
                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "INCOMING XML QUERY:\n" + query);

                       Element request = new Element("request");
                       request.addContent(query.toGNXMLRep());

											 List<String> categories = query.getCollections();
											 for (String category : categories) {
											 		if (!category.equals("geonetwork") && !category.equals("Default")) request.addContent(new Element("category").setText(category));
											 }

                       ServiceConfig config = new ServiceConfig();

                       // perform the search and save search results

                       metasearcher.search(this.srvxtx, request, config);

                       // System.out.println("summary:\n" + Xml.getString(s.getSummary()));
                       // // DEBUG

                       // Random number of records.. Set up the result set
                       setFragmentCount(metasearcher.getSize());
                       setTaskStatusCode(IRResultSetStatus.COMPLETE);

               } catch (Throwable e) {
                       Log.error(Geonet.Z3950_SERVER, "error evaluating query.." + e);
                       e.printStackTrace();
               }
               return (getStatus());
       }



       public InformationFragment[] getFragment(int startingFragment, int count,
                       RecordFormatSpecification spec) throws IRResultSetException {
           if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
               Log.debug(Geonet.Z3950_SERVER, "Request for fragment start:"
                               + startingFragment + ", count:" + count);

               InformationFragment fragment[] = new InformationFragment[count];

               ExplicitRecordFormatSpecification rec_spec = new ExplicitRecordFormatSpecification("xml", null, "f");

               try {
                       // build fragment data
                       int from = startingFragment;
                       int to = startingFragment + count - 1;

                       Element request = new Element("request");
                       request.addContent(new Element("from").setText(from + ""));
                       request.addContent(new Element("to").setText(to + ""));
                       ServiceConfig config = new ServiceConfig();

                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "Search request:\n"
                                       + Xml.getString(request));
                       // get result set
                       Element result = this.metasearcher.present(this.srvxtx, request,
                                       config);

                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "Search result:\n"
                                       + Xml.getString(result));

                       // remove summary
                       result.removeChildren("summary");
                       @SuppressWarnings("unchecked")
                    List<Element> list = result.getChildren();

                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "Set name asked:" + spec);

                       // save other records to fragment
                       for (int i = 0; i < count; i++) {
                               Element md = list.get(0);
                               md.detach();

                           if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                               Log.debug(Geonet.Z3950_SERVER, "Returning fragment:\n"
                                               + Xml.getString(md));

                               // add metadata

                               //fragment[i] = new DOMTree("geonetwork", "geonetwork", null,   getRecord(md),rec_spec );
                               //fragment[i].setHitNo(startingFragment+i);
                               
                               DOMOutputter outputter = new DOMOutputter();
                               Document doc = new Document(md);
                               org.w3c.dom.Document doc2 = outputter.output(doc);
                               
                               fragment[i] = new InformationFragmentImpl(startingFragment+i,"geonetwork","geonetwork",null,doc2,rec_spec);
                               //fragment[i] = new InformationFragmentImpl(startingFragment+i,"geonetwork","geonetwork",null,doc,rec_spec);

                               
                               //System.err.println(fragment[i]);

                       }

                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER)) Log.debug(Geonet.Z3950_SERVER, "Fragment returned");
               } catch (Throwable e) {
                   if(Log.isDebugEnabled(Geonet.Z3950_SERVER))
                       Log.debug(Geonet.Z3950_SERVER, "Exception: "+e.getClass().getName()+" "+e);
               }

               return fragment;
       }

       public void asyncGetFragment(int starting_fragment, int count,
                       RecordFormatSpecification spec, IFSNotificationTarget target) throws IRResultSetException  {
               InformationFragment[] result = getFragment(starting_fragment, count,
                               spec);
               target.notifyRecords(result);

       }

       public void close() {
               this.metasearcher.close();
       }

       private void setTaskStatusCode(int i) {
               this.status = i;
       }

       private void setFragmentCount(int i) {
               this.fragmentcount = i;
       }
       
       

       public int getFragmentCount() {
               return this.fragmentcount;
       }

       public int getRecordAvailableHWM() {
               return getFragmentCount();
       }

       public IRResultSetInfo getResultSetInfo() {

               return new IRResultSetInfo("GNDefault", this.fragmentcount, this.status);

       }

       

}
