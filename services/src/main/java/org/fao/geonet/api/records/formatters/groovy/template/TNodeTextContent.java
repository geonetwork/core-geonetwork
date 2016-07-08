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

package org.fao.geonet.api.records.formatters.groovy.template;

import com.google.common.base.Optional;

import org.fao.geonet.SystemInfo;

import java.io.IOException;

/**
 * @author Jesse on 11/30/2014.
 */
public class TNodeTextContent extends TNode {
    private final TextBlock textContent;

    public TNodeTextContent(SystemInfo info, TextContentParser parser, TextBlock textContent) throws IOException {
        super(info, parser, "", EMPTY_ATTRIBUTES);
        this.textContent = textContent;
    }

    @Override
    public void render(TRenderContext context) throws IOException {
        textContent.render(context);
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        return Optional.absent();
    }
}
