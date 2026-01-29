# Integrating External Resource Properties

Add custom metadata properties to resources stored in JCloud-based storage. These properties are stored as metadata in the cloud storage and are automatically indexed into GeoNetwork's catalog.

## Quick Start

### 1. Configure GeoNetwork

Add this property or environment variable to your GeoNetwork configuration to specify which metadata fields from cloud storage should be included in the catalog index:

```properties
jcloud.additional.properties=field1,field2,field3
```

```bash
export JCLOUD_ADDITIONAL_PROPERTIES=field1,field2,field3
```

**Property Explanation:**

- **`jcloud.additional.properties`** - Comma-separated list of metadata field names to copy from cloud storage into the catalog index
  - Each field name must correspond to a metadata property that you store with your resources in cloud storage
  - Example: `jcloud.additional.properties=department,owner,budget,status`
  - Environment variable: `JCLOUD_ADDITIONAL_PROPERTIES`
  - Properties are optional - if not configured, only standard resource metadata is indexed

### 2. Store Metadata with Your Resources

When uploading resources to cloud storage, attach metadata properties with the names you specified in the configuration:

**Example using blob metadata:**
```
Blob Name: documents/report.pdf

Metadata Properties:
  - department: "Planning"
  - owner: "John Doe"
  - budget: "$50,000"
  - status: "Active"
```

**Metadata Requirements:**
- Property names must match exactly those configured in `jcloud.additional.properties`
- Property values should be simple types (strings, numbers, booleans)
- Properties are optional - resources without configured properties will simply not include those fields

### 3. Verify in Catalog

After indexing, resources will contain the custom properties in the `additionalProperties` field of the `metadataResourceExternalManagementProperties` object:

```json
{
  "lastModification": "2025-10-28T15:43:03.000+00:00",
  "metadataResourceExternalManagementProperties": {
    "id": "resource-001",
    "url": "http://example.com/resource/resource-001",
    "validationStatus": "INCOMPLETE",
    "additionalProperties": {
      "department": "Planning",
      "owner": "John Doe",
      "budget": "$50,000",
      "status": "Active"
    }
  },
  "size": 112339,
  "url": "http://localhost:8084/catalogue/srv/api/records/37aecae5-7783-4274-b595-df02aa003ac3/attachments/Sample1.pdf",
  "version": "1",
  "visibility": "PUBLIC"
}
```

## How It Works

1. When resources are retrieved for indexing, GeoNetwork reads the metadata properties stored with each blob in cloud storage
2. For any properties that match the configured `jcloud.additional.properties` names, the values are extracted
3. These properties are stored in the `additionalProperties` map within the `MetadataResourceExternalManagementProperties` object
4. The enriched resource data is indexed and becomes searchable in the catalog

**Properties are extracted automatically during metadata indexing** - no additional setup required once configured and metadata is stored with your resources.

