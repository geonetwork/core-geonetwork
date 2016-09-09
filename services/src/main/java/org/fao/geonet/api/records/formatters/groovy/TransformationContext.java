/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy;

/**
 * Contains properties like the current rootPath that can be used in handlers.
 *
 * @author Jesse on 10/16/2014.
 */
public class TransformationContext {
    private static ThreadLocal<TransformationContext> context = new InheritableThreadLocal<TransformationContext>();
    public final Handlers handlers;
    public final Functions functions;
    public final Environment env;
    private String rootPath;
    private String currentMode = Mode.DEFAULT;

    public TransformationContext(Handlers handlers, Functions functions, Environment env) {
        this.handlers = handlers;
        this.functions = functions;
        this.env = env;
    }

    public static TransformationContext getContext() {
        return context.get();
    }

    public void setThreadLocal() {
        context.set(this);
    }

    /**
     * The path from the root of the metadata document to the "root" element as selected by the
     * roots selectors in {@link org.fao.geonet.api.records.formatters.groovy.Handlers#roots}
     */
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Get the id of the mode currently configured for processing.
     */
    public String getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public Handlers getHandlers() {
        return handlers;
    }

    public Functions getFunctions() {
        return functions;
    }

    public Environment getEnv() {
        return env;
    }
}
