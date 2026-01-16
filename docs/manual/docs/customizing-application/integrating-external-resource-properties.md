# Integrating External Resource Properties

Add custom resource metadata from an external service directly into GeoNetwork's catalog index.

**Note:** This feature is currently only available when using JCloud-based storage.

## Quick Start

### 1. Configure GeoNetwork

Add these properties or environment variables to your GeoNetwork configuration:

```properties
jcloud.external.resources.properties.url=https://your-api.example.com/resources?uuid={uuid}&approved={approved}
jcloud.external.resources.properties.identifier.field.name=externalId
```

```bash
export JCLOUD_EXTERNAL_RESOURCES_PROPERTIES_URL=https://your-api.example.com/resources?uuid={uuid}&approved={approved}
export JCLOUD_EXTERNAL_RESOURCES_PROPERTIES_IDENTIFIER_FIELD_NAME=externalId
```

**Property Explanations:**

- **`jcloud.external.resources.properties.url`** - The URL of your external API endpoint
  - Use `{uuid}` as a placeholder for the metadata record UUID
  - Use `{approved}` as a placeholder for the approved status (true/false)
  - Example: `https://api.example.com/resources?uuid={uuid}&approved={approved}`
  - Environment variable: `JCLOUD_EXTERNAL_RESOURCES_PROPERTIES_URL`
  
- **`jcloud.external.resources.properties.identifier.field.name`** - The field name in your API response that contains the resource identifier
  - This identifier is used to match your external properties with GeoNetwork resources
  - Must match the field name you return in your JSON response
  - Example: if your response has `"externalId": "resource-123"`, set this to `externalId`
  - Environment variable: `JCLOUD_EXTERNAL_RESOURCES_PROPERTIES_IDENTIFIER_FIELD_NAME`

### 2. Build Your External API Endpoint

Your endpoint must:
- Accept `uuid` and `approved` query parameters
- Return a JSON array of objects
- Include an identifier field (matching your configured field name) to match with resources

**Endpoint Requirements:**

```
GET /resources?uuid={metadata-uuid}&approved={true|false}
Authorization: Bearer <token>  (if authentication is configured)
```

**Authentication:**

- When configured, GeoNetwork will automatically log in as a service account before calling your API
- The authentication token is automatically included in the `Authorization` header
- If service account login fails, the request will not proceed (error will be thrown)
- If no security provider is configured, requests are made without authentication

**Expected Response:**

```json
[
  {
    "externalId": "resource-001",
    "department": "Planning",
    "owner": "John Doe",
    "budget": "$50,000",
    "lastReviewDate": "2024-12-01",
    "status": "Active"
  },
  {
    "externalId": "resource-002",
    "department": "Environmental",
    "owner": "Jane Smith",
    "budget": "$30,000",
    "status": "Review"
  }
]
```

**Response Format Rules:**
- Must be a JSON array
- Each object must contain your configured identifier field
- Field values should be simple types (strings, numbers, booleans)
- Return empty array `[]` if no properties exist for the UUID
- HTTP 200+ status code for success

### 3. Verify in Catalog

After indexing, resources will contain the `externalAdditionalProperties` field with your custom properties:

```json
[
  {
    "lastModification": "2025-10-28T15:43:03.000+00:00",
    "metadataResourceExternalManagementProperties": {
      "id": "resource-001",
      "url": "http://example.com/resource/resource-001",
      "validationStatus": "INCOMPLETE"
    },
    "size": 112339,
    "url": "http://localhost:8084/catalogue/srv/api/records/37aecae5-7783-4274-b595-df02aa003ac3/attachments/Sample1.pdf",
    "version": "1",
    "visibility": "PUBLIC",
    "externalAdditionalProperties": {
      "department": "Planning",
      "owner": "John Doe",
      "budget": "$50,000",
      "lastReviewDate": "2024-12-01",
      "status": "Active"
    }
  },
  {
    "lastModification": "2025-10-28T15:43:03.000+00:00",
    "metadataResourceExternalManagementProperties": {
      "id": "resource-002",
      "url": "http://example.com/resource/resource-002",
      "validationStatus": "INCOMPLETE"
    },
    "size": 112339,
    "url": "http://localhost:8084/catalogue/srv/api/records/37aecae5-7783-4274-b595-df02aa003ac3/attachments/Sample2.pdf",
    "version": "1",
    "visibility": "PUBLIC",
    "externalAdditionalProperties": {
      "department": "Environmental",
      "owner": "Jane Smith",
      "budget": "$30,000",
      "status": "Review"
    }
  }
]
```

**Note:** The `metadataResourceExternalManagementProperties.id` matches the identifier field from your API response, allowing GeoNetwork to correctly merge the external properties.

## How It Works

1. When metadata is indexed, GeoNetwork calls your external API with the UUID and approved flag
2. Your API returns properties for all the resources associated with that UUID
3. GeoNetwork matches resources by identifier and merges the external properties
4. The enriched data is indexed and searchable

**Properties are merged automatically during metadata indexing** - no additional setup required once configured.

## Technical Implementation

The external resource properties feature is implemented in the JCloud storage module:

**Configuration:**
- Properties are configured in `config-jcloud-overrides.properties` or via environment variables
- Configuration is managed by `JCloudConfiguration` class which reads the properties

**Processing Flow:**
1. `JCloudStore.getResourcesForIndexing()` is called during metadata indexing
2. `ResourcesExternalAdditionalPropertiesService.getAdditionalPropertiesMap()` makes the authenticated API call
3. The service uses `SecurityProviderUtil` for automatic service account authentication when available
4. Returned properties are matched by identifier and merged into resource metadata
5. The enriched resources are indexed into the catalog

**Key Classes:**
- `JCloudConfiguration` - Holds configuration properties
- `JCloudStore` - Orchestrates resource retrieval and property merging
- `ResourcesExternalAdditionalPropertiesService` - Handles external API calls and authentication


