/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

/*
 *    Mapfaces -
 *    http://www.mapfaces.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.ajax4jsf.component;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.event.AjaxEvent;
import org.ajax4jsf.event.AjaxListener;
import org.ajax4jsf.event.AjaxSource;
import org.ajax4jsf.renderkit.RendererUtils;
import org.mapfaces.utils.FacesUtils;

/**
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.3 $ $Date: 2007/02/06 16:23:21 $
 *
 */
public abstract class AjaxActionComponent extends UICommand implements
        AjaxComponent, AjaxSource {

    public static final String FOCUS_DATA_ID = "_A4J.AJAX.focus";

    /*
     * (non-Javadoc)
     *
     * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
     */
    @Override
    public void broadcast(final FacesEvent event) throws AbortProcessingException {
        // perform default
        super.broadcast(event);
        if (event instanceof AjaxEvent) {
            final FacesContext context = getFacesContext();
            // complete re-Render fields. AjaxEvent deliver before render
            // response.
            setupReRender(context);
            // Put data for send in response
            final Object data = getData();
            final AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
            if (null != data) {
                ajaxContext.setResponseData(data);
            }
            String focus = getFocus();
            if (null != focus) {
                // search for component in tree.
                // XXX - use more pourful search, as in h:outputLabel
                // component.
                final UIComponent focusComponent = RendererUtils.getInstance().
                        findComponentFor(this, focus);
                if (null != focusComponent) {
                    focus = focusComponent.getClientId(context);
                }
                ajaxContext.getResponseDataMap().put(FOCUS_DATA_ID, focus);
            }
            ajaxContext.setOncomplete(getOncomplete());
        }
    }

    /**
     * Template method with old signature, for backward compability.
     */
    protected void setupReRender() {
    }

    /**
     * Template methods for fill set of resions to render in subclasses.
     *
     * @param facesContext
     *            TODO
     */
    protected void setupReRender(final FacesContext facesContext) {
        /*GEOMATYS MAPFACES replace reRender attribute by refresh param if it exists*/
        /*BEGIN*/
        if (facesContext.getExternalContext().getRequestMap() != null) {
            final java.util.Map params = getFacesContext().getExternalContext().getRequestParameterMap();
            //if((Boolean) params.get("synchronized")){

            if (params.get("refresh") != null) {
                setReRender((String) params.get("refresh"));
            }
            if (params.get("org.mapfaces.ajax.NO_RERENDER") != null
                    && ((String) params.get("org.mapfaces.ajax.NO_RERENDER")).equals("true")) {
                setReRender(null);
            }
        }
//        System.out.println("\n\nviexwroot findCOmpoennt = " + facesContext.getViewRoot().findComponent((String)getReRender()));
//        System.out.println("\n\nviexwroot findCOmpoenntById = " + FacesUtils.findComponentById(facesContext.getViewRoot(), (String)getReRender()));

        /*END*/
        AjaxContext.getCurrentInstance(facesContext).addRegionsFromComponent(
                this);
        setupReRender();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.faces.component.UIComponentBase#queueEvent(javax.faces.event.FacesEvent)
     */
    @Override
    public void queueEvent(final FacesEvent event) {
        if (event instanceof ActionEvent) {
            if (event.getComponent() == this) {
                if (isImmediate()) {
                    event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                } else if (isBypassUpdates()) {
                    event.setPhaseId(PhaseId.PROCESS_VALIDATIONS);
                } else {
                    event.setPhaseId(PhaseId.INVOKE_APPLICATION);
                }
            }
            // UICommand set Phase ID for all ActionEvents - bypass it.
            getParent().queueEvent(event);
        } else {
            super.queueEvent(event);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ajax4jsf.framework.ajax.AjaxSource#addAjaxListener(org.ajax4jsf.framework.ajax.AjaxListener)
     */
    @Override
    public void addAjaxListener(final AjaxListener listener) {
        addFacesListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ajax4jsf.framework.ajax.AjaxSource#getAjaxListeners()
     */
    @Override
    public AjaxListener[] getAjaxListeners() {
        return (AjaxListener[]) getFacesListeners(AjaxListener.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ajax4jsf.framework.ajax.AjaxSource#removeAjaxListener(org.ajax4jsf.framework.ajax.AjaxListener)
     */
    @Override
    public void removeAjaxListener(final AjaxListener listener) {
        removeFacesListener(listener);

    }

    protected UIComponent getSingleComponent() {
        return this;
    }
}
