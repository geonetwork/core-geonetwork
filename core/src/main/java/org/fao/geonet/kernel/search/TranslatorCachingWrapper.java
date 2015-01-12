//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import java.util.concurrent.ConcurrentHashMap;

public class TranslatorCachingWrapper implements Translator {

    private static final long serialVersionUID = 1L;

    private static final int CONCURRENCY_LEVEL = 1;
    private static final float LOAD_FACTOR = 0.9f;
    private static final int INITIAL_CAPACITY = 16;

    private Translator wrappedTranslator;

    private ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<String, String>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);

    public TranslatorCachingWrapper(Translator wrappedTranslator) {
        this.wrappedTranslator = wrappedTranslator;
    }

    @Override
    public String translate(String key) {
        String value = cache.get(key);

        if (value == null) {
            value = wrappedTranslator.translate(key);
            cache.putIfAbsent(key, value);
        }

        return value;
    }

}
