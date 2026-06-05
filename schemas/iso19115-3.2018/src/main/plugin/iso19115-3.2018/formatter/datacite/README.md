# Conversion from iso19115-3 to Datacite (version 4.6)

See http://schema.datacite.org/meta/kernel-4.6/
or https://datacite-metadata-schema.readthedocs.io/_/downloads/en/4.6/pdf/

This conversion is used in the context of DOI creation.


## DataCite Mandatory Properties

| ID | Property | Obligation |
|----|----------|-----------|
| 1 | Identifier (with mandatory type sub-property) | M |
| 2 | Creator (with optional given name, family name, name identifier and affiliation sub-properties) | M |
| 3 | Title (with optional type sub-properties) | M |
| 4 | Publisher | M |
| 5 | PublicationYear | M |
| 10 | ResourceType (with mandatory general type description subproperty) | M |

The conversion does not check that those elements are present in the record.


`datacite:creator` are any point of contact in identification section with role `author`.
`datacite:publisher` is the first organization in identification or distribution section with role `publisher` or `distributor`.

## DataCite Recommended and Optional Properties

| ID | Property | Obligation |
|----|----------|-----------|
| 6 | Subject | R |
| 7 | Contributor | R |
| 8 | Date | R |
| 9 | Language | O |
| 11 | AlternateIdentifier | O |
| 12 | RelatedIdentifier | R |
| 13 | Size | O |
| 14 | Format | O |
| 15 | Version | O |
| 16 | Rights | O |
| 17 | Description | R |
| 18 | GeoLocation | R |
| 19 | FundingReference | O |
| 20 | RelatedItem | O |


## DataCite unsupported properties

* givenName
* familyName
* schemeURI
* rightsIdentifier
* rightsIdentifierScheme
* geoLocationBox and place only (no point or polygon)
* awardNumber
* awardTitle


## Accessing the formatter

To retrieve a record:
* http://localhost:8080/geonetwork/srv/api/records/ff8d8cd6-c753-4581-99a3-af23fe4c996b/formatters/datacite?output=xml
