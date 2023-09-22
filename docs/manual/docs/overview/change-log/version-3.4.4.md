# Version 3.4.4 {#version-344}

!!! warning

    The migration from 3.4.3 and earlier to 3.4.4 may cause a reset of the *Template for advanced search form* setting in the UI configuration.


## New features

-   Added Slovak language ([PR 3086](https://github.com/geonetwork/core-geonetwork/pull/3086))
-   Improved error message when a region was not found in a thesaurus ([PR 3069](https://github.com/geonetwork/core-geonetwork/pull/3069))
-   Improved batch editor labels in english ([PR 3000](https://github.com/geonetwork/core-geonetwork/pull/3000))
-   Excluded servlet-related dependencies ([PR 2287](https://github.com/geonetwork/core-geonetwork/pull/2287))

## Fixes

-   Fixed issue with multiselect directive in users administration ([issue 3055](https://github.com/geonetwork/core-geonetwork/issues/3055))
-   Fixed issue where a record with user feedbacks could not be deleted ([issue 3122](https://github.com/geonetwork/core-geonetwork/issues/3122))
-   Fixed publication date for registries ([PR 3150](https://github.com/geonetwork/core-geonetwork/pull/3150))
-   Fixed CSW response when inserting records ([issue 3104](https://github.com/geonetwork/core-geonetwork/issues/3104))
-   Fixed WFS layer support in simple view ([issue 3116](https://github.com/geonetwork/core-geonetwork/issues/3116))
-   Fixed `hideTimeInCalendar` option for editor layout ([issue 3058](https://github.com/geonetwork/core-geonetwork/issues/3058))
-   Fixed an issue with MapServer password reset ([issue 3140](https://github.com/geonetwork/core-geonetwork/issues/3140))
-   Fixed thesaurus upload that would sometime give an unclear error ([issue 3131](https://github.com/geonetwork/core-geonetwork/issues/3131))
-   Fixed loading WMTS sometime failing in projections other than EPSG:3857 ([issue 3124](https://github.com/geonetwork/core-geonetwork/pull/3124))
-   Fixed an error related to thesaurus encoding when uploading from a registry
-   Fixed an issue when adding WMS layers with an url ending with `?` ([issue 3088](https://github.com/geonetwork/core-geonetwork/issues/3088))
-   Fixed temporal extent rendering in the editor ([PR 2868](https://github.com/geonetwork/core-geonetwork/pull/2868))
-   Fixed an `OutOfMemoryError` when listing groups using service admin.group.list ([PR 3079](https://github.com/geonetwork/core-geonetwork/pull/3079))
-   Fixed issue with INSPIRE related fields being indexed when the thesaurus was not present ([issue 2719](https://github.com/geonetwork/core-geonetwork/issues/2719))
-   Fixed adding a layer to the map when the URL contained extra parameters like GetCapabilities ([PR 2846](https://github.com/geonetwork/core-geonetwork/pull/2846))
-   Fixed rendering of the menubar on small screens ([issue 2934](https://github.com/geonetwork/core-geonetwork/issues/2934))
-   Fixed favicon uploading ([issue 2992](https://github.com/geonetwork/core-geonetwork/issues/2992))

and more \... see [3.4.4 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A3.4.4+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=milestone%3A3.4.4+is%3Aclosed+is%3Apr) for full details.
