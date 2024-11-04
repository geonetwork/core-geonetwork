# Version 3.10.7 {#version-3107}

## New features/fixes

-   Administration

> -   [Create user form issues 2 requests to userselection api that return status code 400.](https://github.com/geonetwork/core-geonetwork/pull/5804)
> -   [Fix parse of logo upload in groups management when the content type includes the charset encoding](https://github.com/geonetwork/core-geonetwork/pull/5460)
> -   [Update user - verify user groups when updating a user by a UserAdmin](https://github.com/geonetwork/core-geonetwork/pull/5560)
> -   [Users without groups assigned are listed in the users lists for any UserAdmin](https://github.com/geonetwork/core-geonetwork/pull/5798)

-   CSW

> -   [Record to CSW Capabilities - extract language iso2code with twoCharLangCode function, update in date element to use the text value](https://github.com/geonetwork/core-geonetwork/pull/5400)

-   Editor

> -   [Associated resource / Feature catalog / Missing list of values](https://github.com/geonetwork/core-geonetwork/pull/5083)
> -   [Fix issue using custom UUIDs](https://github.com/geonetwork/core-geonetwork/pull/5440)
> -   [Fix regression on "Compute extents from keywords" button](https://github.com/geonetwork/core-geonetwork/pull/5455)
> -   [Fix Thumbnail generator](https://github.com/geonetwork/core-geonetwork/pull/5757)
> -   [Fix unhandled error and its xsl regular expression and root element in Schematron title translation](https://github.com/geonetwork/core-geonetwork/pull/5220)
> -   [Fix xpath to match only the main citation title, not other titles](https://github.com/geonetwork/core-geonetwork/pull/5391)
> -   ISO19115-3 / [Add option for PNG images to mcc:fileType Recommended values](https://github.com/geonetwork/core-geonetwork/pull/5456)
> -   ISO19139 / [Fix Conformity checks in INSPIRE strict rules](https://github.com/geonetwork/core-geonetwork/pull/5335)
> -   [Option to disable OGC Capabilities layer processing in the online resource panel](https://github.com/geonetwork/core-geonetwork/pull/5763)
> -   [Remove non required data-translate directive usage in gnMetadataGroupUpdater directive](https://github.com/geonetwork/core-geonetwork/pull/5490)
> -   [Updated schematron name on error messages so that it does not overlap](https://github.com/geonetwork/core-geonetwork/pull/5521)

-   Harvesters

> -   CSW / [Enable preemptive for csw requests with credentials](https://github.com/geonetwork/core-geonetwork/pull/5497)
> -   [Don't allow empty group owner in harvesters.](https://github.com/geonetwork/core-geonetwork/pull/5370)

-   Map viewer

> -   [Add a WMTS error message when the URL can't be parsed](https://github.com/geonetwork/core-geonetwork/pull/5292)
> -   [Fix map height when there is no footer](https://github.com/geonetwork/core-geonetwork/pull/5696)

-   Metadata

> -   [Fix for too small privileges popup](https://github.com/geonetwork/core-geonetwork/pull/5591)
> -   [Fix logo in record view](https://github.com/geonetwork/core-geonetwork/pull/5337)
> -   Formatter / [Avoid request check not modified as unsupported on chrome and edge](https://github.com/geonetwork/core-geonetwork/pull/5406/files)
> -   Formatter / [Fixes some challenges in google-structured-data-test](https://github.com/geonetwork/core-geonetwork/pull/5508)
> -   Formatter / [Full view doesn't work in a draft version of a metadata record](https://github.com/geonetwork/core-geonetwork/pull/5433)
> -   Formatter / [iso19139 full view - codelist elements - display the codelistValue translation only if the element has a text also](https://github.com/geonetwork/core-geonetwork/pull/5793)
> -   Import / [Generic error message in 'Upload a file from URL'](https://github.com/geonetwork/core-geonetwork/pull/5553)
> -   [Index temporal extent period, fix multiple temporal extent display in the metadata detail page](https://github.com/geonetwork/core-geonetwork/pull/5485)
> -   INSPIRE / [Update the filter for INSPIRE Atom dataset feed to support the remote operatesOn indexing format](https://github.com/geonetwork/core-geonetwork/pull/5816)
> -   INSPIRE / [Retrieve INSPIRE Atom feed language from self link element instead of the feed language attribute that is optional](https://github.com/geonetwork/core-geonetwork/pull/5435)
> -   INSPIRE / [INSPIRE geometry fields lead to 0 results in opensearch](https://github.com/geonetwork/core-geonetwork/pull/5434)
> -   INSPIRE / [Atom fixes: filter metadata with schemas based on iso19139 and atom feed parse](https://github.com/geonetwork/core-geonetwork/pull/5472)
> -   INSPIRE / [Local INSPIRE Atom feed xslt fix for resource constraints with multiple gmd:otherConstraints elements](https://github.com/geonetwork/core-geonetwork/pull/5815)
> -   [Remove schema-org annotations from list pages](https://github.com/geonetwork/core-geonetwork/pull/5412)
> -   [Update getMdObjByUuid to use the draft information in the metadata when the workflow is enabled](https://github.com/geonetwork/core-geonetwork/pull/5344)
> -   [Update message if metadata is not found](https://github.com/geonetwork/core-geonetwork/pull/5384)
> -   [Update Thesaurus keywords to ISO19115-3.2018](https://github.com/geonetwork/core-geonetwork/pull/5458)
> -   [Use the configured CSW Service metadata to fill the meta headers for description / keywords for the html content](https://github.com/geonetwork/core-geonetwork/pull/5447)
> -   Workflow / [Bulk publish cancels working copy](https://github.com/geonetwork/core-geonetwork/pull/5420)
> -   Workflow / [Fix draft metadata manager update method to check if should be applied to the draft metadata or the published metadata](https://github.com/geonetwork/core-geonetwork/pull/5470)
> -   Workflow / [Fix for validation result gets lost when approving record and copy validation results when creating a draft.](https://github.com/geonetwork/core-geonetwork/pull/5418)
> -   Workflow / [Search results - metadata with a working copy display a label 'Working copy' if the metadata has a draft copy, even for public users.](https://github.com/geonetwork/core-geonetwork/pull/5805)

-   Other

> -   [Fixes embedded Cross-Site Scripting issues](https://github.com/geonetwork/core-geonetwork/pull/5551)
> -   [Kibana / Only allow access to authenticated users in the catalog](https://github.com/geonetwork/core-geonetwork/pull/5005)
> -   [Log transaction manager notification failures](https://github.com/geonetwork/core-geonetwork/pull/5408)
> -   [Manage url redirects with URL.openConnection](https://github.com/geonetwork/core-geonetwork/pull/5512)
> -   [Protect AccessManager code using user session](https://github.com/geonetwork/core-geonetwork/pull/5365)
> -   [Reset password service fixes/improvements](https://github.com/geonetwork/core-geonetwork/pull/5371)
> -   [Stream attachments to the browser without reading them first](https://github.com/geonetwork/core-geonetwork/pull/5462)
> -   Update doc url to point to 3.10.x documentation
> -   [Update ZipUtil.java to use tempFile configuration to avoid memory issue](https://github.com/geonetwork/core-geonetwork/pull/5526)
> -   [Use maven major, minor, patch, build, qualifier definition for version](https://github.com/geonetwork/core-geonetwork/pull/5451)
> -   [Use \$translateProvider.useSanitizeValueStrategy('escapeParameters') for encoding sanitizing](https://github.com/geonetwork/core-geonetwork/pull/5461)

-   User interface

> -   [Add confirm to delete Category and Cancel edits to metadata](https://github.com/geonetwork/core-geonetwork/pull/5813)
> -   One styling for all scrollbars and increase the width/height
> -   [Use button to recover the password](https://github.com/geonetwork/core-geonetwork/pull/5478)
