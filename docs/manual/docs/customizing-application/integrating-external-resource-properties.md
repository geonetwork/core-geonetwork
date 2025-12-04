# Integrating External Resource Properties

Add custom resource metadata from an external service directly into GeoNetwork's catalog index.

## Quick Start

### 1. Configure GeoNetwork

Add these properties or environment variables to your `application.properties` or environment variables:

```properties
resources.external.additional.properties.url.template=https://your-api.example.com/resources?uuid={uuid}&approved={approved}
resources.external.additional.properties.identifier.field.name=externalId
```

```bash
export RESOURCES_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE=https://your-api.example.com/resources?uuid={uuid}&approved={approved}
export RESOURCES_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME=externalId
```

**Property Explanations:**

- **`url.template`** - The URL of your external API endpoint
  - Use `{uuid}` as a placeholder for the metadata record UUID
  - Use `{approved}` as a placeholder for the approved status (true/false)
  - Example: `https://api.example.com/resources?uuid={uuid}&approved={approved}`
  
- **`identifier.field.name`** - The field name in your API response that contains the resource identifier
  - This identifier is used to match your external properties with GeoNetwork resources
  - Must match the field name you return in your JSON response
  - Example: if your response has `"externalId": "resource-123"`, set this to `externalId`

### 2. Build Your External API Endpoint

Your endpoint must:
- Accept `uuid` and `approved` query parameters
- Return a JSON array of objects
- Include an identifier field (matching your configured field name) to match with resources

**Endpoint Requirements:**

```
GET /resources?uuid={metadata-uuid}&approved={true|false}
Authorization: Bearer <token>  (if authentication required)
```

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

## How It Works

1. When metadata is indexed, GeoNetwork calls your external API with the UUID and approved flag
2. Your API returns properties for all the resources associated with that UUID
3. GeoNetwork matches resources by identifier and merges the external properties
4. The enriched data is indexed and searchable

**Properties are merged automatically during metadata indexing** - no additional setup required once configured.
