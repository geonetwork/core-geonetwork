//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.dispatchers;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.transaction.TransactionStatus;

import java.nio.file.Path;
import java.util.Vector;

//=============================================================================

/**
 * A container class for a service. It collect the method and the filter
 */
public class ServiceInfo {

    private String match;
    private String sheet;
    private boolean cache = false;

    private Vector<Service> vServices = new Vector<Service>();
    private Vector<OutputPage> vOutputs = new Vector<OutputPage>();
    private Vector<ErrorPage> vErrors = new Vector<ErrorPage>();

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public void setMatch(String match) {
        if (match != null && match.trim().equals(""))
            match = null;

        this.match = match;
    }

    //--------------------------------------------------------------------------

    public void setSheet(String sheet) {
        if (sheet != null && sheet.trim().equals(""))
            sheet = null;

        this.sheet = sheet;
    }

    //--------------------------------------------------------------------------

    public void setCache(String cache) {
        this.cache = "yes".equals(cache);
    }

    //--------------------------------------------------------------------------

    public boolean isCacheSet() {
        return cache;
    }

    //--------------------------------------------------------------------------

    public void addService(Service s) {
        vServices.add(s);
    }

    //--------------------------------------------------------------------------

    /**
     * Adds to the engine the output page of a service
     */

    public void addOutputPage(OutputPage page) {
        vOutputs.add(page);
    }

    //--------------------------------------------------------------------------

    public void addErrorPage(ErrorPage page) {
        vErrors.add(page);
    }

    //--------------------------------------------------------------------------

    public Element execServices(final Element params, final ServiceContext context) throws Exception {
        return TransactionManager.runInTransaction("ServiceManager.dispatch", context.getApplicationContext(),
            TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
            TransactionManager.CommitBehavior.ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS,
            false, new TransactionTask<Element>() {
                @Override
                public Element doInTransaction(TransactionStatus transaction) throws Throwable {
                    return noTransactionExec(params, context);
                }
            });
    }

    private Element noTransactionExec(Element params, ServiceContext context) throws Exception {
        if (params == null)
            params = new Element(Jeeves.Elem.REQUEST);

        //--- transform input request using a given stylesheet

        params = transformInput(params);

        if (vServices.size() == 0) {
            params.setName(Jeeves.Elem.RESPONSE);
            return params;
        }

        Element response = params;

        for (Service service : vServices) {
            response = execService(service, response, context);
        }

        //--- we must detach the element from its parent because the output dispatcher
        //--- links it to the root element
        //--- note that caching is not allowed in any case

        response.detach();

        return response;
    }

    //--------------------------------------------------------------------------

    public OutputPage findOutputPage(Element response) throws Exception {
        for (OutputPage page : vOutputs) {
            if (page.matches(response))
                return page;
        }

        return null;
    }

    //--------------------------------------------------------------------------

    public ErrorPage findErrorPage(String id) {
        for (ErrorPage page : vErrors) {
            if (page.matches(id))
                return page;
        }

        return null;
    }

    //---------------------------------------------------------------------------

    /**
     * Returns true if the service input has elements that match this page
     */

    public boolean matches(Element request) throws Exception {
        if (match == null)
            return true;
        else
            return Xml.selectBoolean(request, match);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private Element transformInput(Element request) throws Exception {
        if (sheet == null)
            return request;

        GeonetworkDataDirectory geonetworkDataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        Path styleSheet = geonetworkDataDirectory.resolveWebResource(Jeeves.Path.XSL).resolve(sheet);

        ServiceManager.info("Transforming input with stylesheet : " + styleSheet);

        try {
            Element result = Xml.transform(request, styleSheet);
            ServiceManager.info("End of input transformation");

            return result;
        } catch (Exception e) {
            ServiceManager.error("Exception during transformation");
            ServiceManager.error("  (C) message is : " + e.getMessage());

            Throwable t = e;

            while (t.getCause() != null) {
                ServiceManager.error("  (C) message is : " + t.getMessage());
                t = t.getCause();
            }

            throw e;
        }
    }

    //--------------------------------------------------------------------------
    private Element execService(final Service service, final Element params, final ServiceContext context) throws Exception {
        try {
            Element response = service.exec(params, context);

            if (response == null) {
                response = new Element(Jeeves.Elem.RESPONSE);
            }

            //--- commit resources and return response
            return response;
        } catch (Exception e) {
            //--- in case of exception we have to abort all resources
            ServiceManager.error("Exception when executing service");
            ServiceManager.error(" (C) Exc : " + e);

            throw e;
        }
    }

    public Vector<Service> getServices() {
        return vServices;
    }
}

