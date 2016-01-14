package org.fao.geonet.kernel.harvest.harvester;

public class HarvestResult {
    public int addedMetadata;			// = total
    public int atomicDatasetRecords;		// = md for atomic datasets
    public int badFormat;		        //
    public int collectionDatasetRecords;	// = md for collection datasets
    public int couldNotInsert;
    public int datasetUuidExist;	// = uuid already in catalogue
    public int doesNotValidate;	        // = 0 cos' not validated
    public int duplicatedResource;
    public int fragmentsMatched;	// = fragments matched in md templates
    public int fragmentsReturned;	// = fragments generated
    public int fragmentsUnknownSchema;	// = fragments with unknown schema
    public int incompatibleMetadata;
    public int layer;			        // = md for data
    public int layerUuidExist;	        // = uuid already in catalogue
    public int layerUsingMdUrl;	        // = md for data using metadata URL document if ok
    public int locallyRemoved;	        // = md removed
    public int recordsBuilt;
    public int recordsUpdated;
    public int schemaSkipped;
    public int serviceRecords;			// = md for services
    public String siteId;
    public int subtemplatesAdded;		// = subtemplates for collection datasets
    public int subtemplatesRemoved;	// = fragments generated
    public int subtemplatesUpdated;
    public int originalMetadata = 0;    //How many metadata was returned on the call
    public int totalMetadata;			// = md for data and service (ie. data + 1)
    public int unchangedMetadata;
    public int unknownSchema;	        // = md with unknown schema (should be 0 if no layer loaded using md url)
    public int unretrievable;	        // = http connection failed
    public int updatedMetadata = 0;
    public int uuidSkipped;
    public int thumbnails;		        // = number of thumbnail generated
    public int thumbnailsFailed;        // = number of thumbnail creation which failed
}
