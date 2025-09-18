# Portal configuration

Portals can be configured under the `sources` section in `admin console` --> `settings`.

![](img/portal-access.png)

There are 3 types of sources:

-   the main catalogue,
-   sub-portal, which can be a subset of the main catalogue
-   harvesters.

![](img/portal-types.png)

The main catalogue corresponds to the current installation.

When harvesting, at least one source is created to represent the harvester. When harvesting from another GeoNetwork node, sources from the target catalogue are also harvested to keep track of the true origin of records. This only applies to the GeoNetwork protocols which use MEF ([Metadata Exchange Format (MEF)](../../annexes/mef-format.md)) which contain the source catalogue information.

The last type is sub-portal, which is described in details below.

## Configuring a sub-portal

Sub-portals can be used to create a space in the main catalogue only focusing on a subset of records.

A sub-portal is defined by:

![](img/portal-subportal.png)

When creating a sub-portal with an identifier `ìnspire` for example, a new entry point on your catalogue will be available: <http://localhost:8080/geonetwork/inspire/>. Accessing the catalogue through it will only provide access to records matching the filter defined for this sub-portal. The `search filter` parameter value uses the Lucene query parser syntax (see <http://lucene.apache.org/java/2_9_1/queryparsersyntax.html>) and is applied to all searches.

User privileges apply as in the main instance.

The logo and name of the sub-portal will be displayed instead of the main instance information in case the option `Display in the portal switcher` is selected:

![](img/portal-header.png)

A CSW service is also available for this sub-portal <http://localhost:8080/geonetwork/inspire/eng/csw> (and replacing the virtual CSW feature).

A sub-portal can also use a specific user interface configuration.

The list of sub-portal available is at <http://localhost:8080/geonetwork/srv/api/sources>

![](img/portal-list.png)

### Configuring a Datahub interface for a sub-portal

If the [Datahub Integration Plugin](../../install-guide/plugins.md#datahub-integration-gn-datahub-integration) is installed, a Datahub interface can be configured for a sub-portal. Two settings are available for this:

* Datahub enabled (checkbox)
* Datahub configuration (text field in TOML format)

![datahub-config.png](img/datahub-config.png)

Please refer to the [GeoNetwork-UI Configuration guide](https://geonetwork.github.io/geonetwork-ui/main/docs/guide/configure.html) for a complete documentation on what each setting does.

!!! note

    If no configuration is given for a portal or sub-portal, the default configuration will be used.


## Example of usage

To find terms available for filtering you can query a record in your ElasticSearch instance: 

`POST /gn-records*/_search
{"query":{"query_string":{"query":"+uuid:\"EXT_DS120344\""}}}`

This shows elements such as keywords are grouped together under a "tag" element. A tag element in a multilingual catalogue looks something like this:
```
"tag" : [
            {
              "default" : "BIRDS",
              "langeng" : "BIRDS"
            },
            {
              "default" : "MARINE",
              "langeng" : "MARINE",
              "key" : "https://registry.geonetwork-opensource.org/theme/nrw-keywords/marine-uc"
            },
...
]
```

You can use the Query string syntax search `+tag.default:"BIRDS"` as the filter for a sub-portal.

You can use the `+` + (this term must be present) and `-` (this term must not be present).

**ElasticSearch 8 documentation**: [Query String Syntax](https://www.elastic.co/guide/en/elasticsearch/reference/8.18/query-dsl-query-string-query.html#query-string-syntax).

<details>
<summary>Example of filters:</summary>

<h4>Filter by Source Catalogue and Organization</h4>

Records from a specific source catalogue and organization:
<br><code>sourceCatalogue:972b12aa-08dc-4d29-8a10-ff44ecef1dda AND OrgForResourceObject.default:("My organisation")</code>
<br>

Records from a specific organization:
<br><code>+OrgForResourceObject.default:("My organization name")</code>
<br>

<h4>Filter by Organization Name and Keywords</h4>

Records from "My agency" with open data keywords:
<br><code>OrgObject.\\*: "My agency" AND keyword:("OPEN DATA" OR "open data" OR "opendata")</code>
<br>

Records from either "New York" or "GN":
<br><code>OrgObject.\\*:"New York" OR OrgObject.\\*:"GN"</code>
<br>

<h4>Filter by Harvester</h4>

Records harvested by a specific harvester:
<br><code>+harvesterUuid: e04c8cba-64d9-4495-9c38-fc91119cfa1a</code>
<br>

Records from a harvester, excluding under development status:
<br><code>+harvesterUuid: b37387af-89e8-4a37-a882-1c2105bdf859 AND NOT cl_status.key: underDevelopment</code>
<br>

<h4>Filter by Topic Category</h4>

Filter by topic key named biota:
<br><code>+cl_topic.key:"biota"</code>
<br>

Climatology, Meteorology, Atmosphere:
<br><code>+cl_topic.key:(society OR transportation)</code>
<br>

<b>NOT</b> Society topic:
<br><code>-cl_topic.key:"society"</code>
<br>

<h4>Filter by Group</h4>

Records owned by group 108:
<br><code>+groupOwner:(108)</code>
<br>

Records owned by group 104 or 105:
<br><code>groupOwner:(104 OR 105)</code>
<br>

<h4>Filter by Tag (Country Example)</h4>

Records tagged with Thailand, Cambodia, or Laos:
<br><code>+(tag.default:Thailand OR tag.default:Cambodia)</code>
<br>

Records with any tag containing "Geo":
<br><code>+tag.\\*:"Geo"</code>
<br>

<h4>Filter by Record Owner</h4>

Records owned by "<owner>":
<br><code>+recordOwner:"<owner>"</code>
<br>

<h4>Filter by Thesaurus Keyword Presence</h4>
Filter records referencing any keyword from a specific thesaurus:
<br><code>+th_otherKeywords-theme:*</code>
<br>
(where `otherKeywords-theme` is the thesaurus identifier)

For INSPIRE themes:
<br><code>+th_httpinspireeceuropaeutheme-theme:*</code>
<br>

<h4>Filter by Specific Keyword</h4>

By default translation:
<br><code>+tag.default:inspire</code>
<br>

By tag key:
<br><code>+tag.key:"http://inspire.ec.europa.eu/theme/ps"</code>
<br>

By Slovak translation:
<br><code>+tag.langslo:jaskyne</code>
<br>

<h4>Free Text Search</h4>

Search for "Open?ata":
<br><code>+any.default:Open?ata</code>
<br>

Regex search for "OpenData" (case-insensitive):
<br><code>+any.default:/[Oo]pen[Dd]ata/</code>
<br>

<h4>Filter by Contact Organization (for data)</h4>

All organizations starting with "GN":
<br><code>+OrgForResourceObject.default:GN*</code>
<br>

</details>

### Creating an INSPIRE directive sub-portal

For the INSPIRE directive, a catalogue administrator needs to publish an entry point providing access only to INSPIRE related records. An INSPIRE sub-portal can be created with a filter on keywords `+thesaurusName:"GEMET - INSPIRE themes, version 1.0"`.

### Creating a sub-portal for partners

Some organizations need to open the catalogue to a set of partners. In such cases, each partner generally accesses the catalogue and creates their records in dedicated groups. A good example is <https://www.geocat.ch/> providing a main search filter `catalogue`.

![](img/portal-geocatch.png)

The concept of a sub-portal allows the possibility of creating a dedicated URL for each partner. The header can contain the partner identification with name and logo. Optionally the user interface can also be customized (see [User Interface Configuration](user-interface-configuration.md)).

To setup this kind of configuration, the basic principle is to have:

-   One group for each partner with one or more users
-   One sub-portal for each partner with a filter matching records in that group

To configure this, apply the following steps:

-   Create a group for the partner eg. `oca` (see [Creating group](../managing-users-and-groups/creating-group.md)).
-   Create at least one user for the partner (see [Creating user](../managing-users-and-groups/creating-user.md)). The user must be member of the group `oca`. If you want the user to be able to configure the sub-portal (eg. change the name, choose a logo), the user must have at least the `UserAdmin` profile for the group `oca`.
-   Create a sub-portal. This can have the same name as the group, eg. `oca` but this is not essential. The filter can be created using the fact that a record published in the group `oca` should be in this sub-portal, using the syntax `+_groupPublished:oca`. Once created the sub-portal is accessible at <http://localhost:8080/geonetwork/oca>.
-   (Optional) Link the sub-portal to a user admin group to allow `UserAdmin` to configure their sub-portal.

With this type configuration, ie. one partner = one group = one sub-portal and users are only member of one group, then when connecting to a partner sub-portal:

-   user will only see records published in that group in the entire application
-   when creating new records, no group selection is provided because user is member of one group only

![](img/portal-oca-newrecord.png)

Remember that a record is visible in the `oca` sub-portal because it is published in the group `oca`:

![](img/portal-oca-privileges.png)

If the `publish` operation is removed from `oca` group, then records will not longer be visible in that sub-portal.

In some situations, you also want to share templates among partners. There are 2 options for this:

-   Publish the template in all partner's groups. The main drawback in this case is that if a new group is added, the templates need to be published to that new group.
-   Create a dedicated group for shared records eg. `sharedGroup`. Publish templates in that shared space. Alter the sub-portal filter to match either the partner group or the shared one. `+groupPublished:(oca OR sharedGroup)`.
