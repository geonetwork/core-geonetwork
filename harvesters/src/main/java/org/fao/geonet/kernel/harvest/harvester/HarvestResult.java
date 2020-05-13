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

package org.fao.geonet.kernel.harvest.harvester;

public class HarvestResult {
    public int addedMetadata;            // = total
    public int atomicDatasetRecords;        // = md for atomic datasets
    public int badFormat;                //
    public int collectionDatasetRecords;    // = md for collection datasets
    public int couldNotInsert;
    public int datasetUuidExist;    // = uuid already in catalogue
    public int privilegesAppendedOnExistingRecord;
    public int doesNotValidate;            // = 0 cos' not validated
    public int xpathFilterExcluded;
    public int duplicatedResource;
    public int fragmentsMatched;    // = fragments matched in md templates
    public int fragmentsReturned;    // = fragments generated
    public int fragmentsUnknownSchema;    // = fragments with unknown schema
    public int incompatibleMetadata;
    public int layer;                    // = md for data
    public int layerUuidExist;            // = uuid already in catalogue
    public int layerUsingMdUrl;            // = md for data using metadata URL document if ok
    public int locallyRemoved;            // = md removed
    public int recordsBuilt;
    public int recordsUpdated;
    public int schemaSkipped;
    public int serviceRecords;            // = md for services
    public String siteId;
    public int subtemplatesAdded;        // = subtemplates for collection datasets
    public int subtemplatesRemoved;    // = fragments generated
    public int subtemplatesUpdated;
    public int originalMetadata = 0;    //How many metadata was returned on the call
    public int totalMetadata;            // = md for data and service (ie. data + 1)
    public int unchangedMetadata;
    public int unknownSchema;            // = md with unknown schema (should be 0 if no layer loaded using md url)
    public int unretrievable;            // = http connection failed
    public int updatedMetadata = 0;
    public int uuidSkipped;
    public int thumbnails;                // = number of thumbnail generated
    public int thumbnailsFailed;        // = number of thumbnail creation which failed
    /** Number of metadata managed by other harvester. */
    public int managedByOtherHarvester;
}
