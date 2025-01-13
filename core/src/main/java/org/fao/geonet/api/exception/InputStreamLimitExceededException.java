//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.api.exception;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Custom exception to be thrown when the size of a remote file to be uploaded to the store exceeds the maximum upload size.
 */
public class InputStreamLimitExceededException extends MaxUploadSizeExceededException {
    private final long remoteFileSize;

    /**
     * Create a new InputStreamLimitExceededException with an unknown remote file size.
     *
     * @param maxUploadSize the maximum upload size allowed
     */
    public InputStreamLimitExceededException(long maxUploadSize) {
        this(maxUploadSize, -1L);
    }

    /**
     * Create a new InputStreamLimitExceededException with a known remote file size.
     *
     * @param maxUploadSize the maximum upload size allowed
     * @param remoteFileSize the size of the remote file
     */
    public InputStreamLimitExceededException(long maxUploadSize, long remoteFileSize) {
        super(maxUploadSize);
        this.remoteFileSize = remoteFileSize;
    }

    /**
     * Get the size of the remote file.
     *
     * @return the size of the remote file or -1 if the size is unknown
     */
    public long getRemoteFileSize() {
        return this.remoteFileSize;
    }
}
