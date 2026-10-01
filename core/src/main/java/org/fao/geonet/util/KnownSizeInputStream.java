//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

/**
 * Implemented by {@link java.io.InputStream} (wrappers) that know the total/expected size of the
 * data they will yield, even though that size can't be reliably queried through generic
 * {@link java.io.InputStream} API (eg. {@link java.io.InputStream#available()}, which for
 * network-backed streams only reports how many bytes are currently buffered and ready to read
 * without blocking, NOT the total remaining size of the stream).
 *
 * <p>Consumers that need to declare a content length upfront (eg. object/blob stores) should check
 * for this interface - unwrapping through any {@link java.io.FilterInputStream} decorators that
 * also implement it - instead of relying on {@code available()}, to avoid declaring (and
 * truncating the upload to) an arbitrary, too-small size.
 */
public interface KnownSizeInputStream {

    /**
     * @return the total/expected size of the stream in bytes, or {@code -1} if unknown.
     */
    long getKnownSize();
}

