# Search records

GeoNetwork is catalog lists records of available datasets.

## Search Catalogue

1.  Enter the desired keywords and search terms into the **Search** field
    at the top of the page and press the **:fontawesome-solid-magnifying-glass: Search** button
    (or use the ++enter++ key), to list search results.

    ![Search field](img/search.png)
    *Search field*

2.  Search for complete words.

    Use **Search** field to enter: `Ocean`

    ![](img/search_results.png)
    *Search results for Ocean*

3.  Search using the wildcard `*` to match the start or end of word. 
    Keep in mind the entire record contents is searched, not only the
    titles and description.
    
    Use **Search** field to enter: `Area*`

    ![](img/search_wildcard.png)
    *Search for start of a word*

4.  The wildcard `*` can also be used multiple times to match part of a word.

    Use **Search** field to enter: `*brass*`

    ![](img/search_partial.png)
    *Search for part of a word*

5. Filtering and exploring [search results](#search-results) is described below.

## Browse Catalogue

1.  Navigate to **:geonetwork-logo: My Geonetwork catalogue** to browse records.
    In a production system the catalogue name and logo will
    match your organisation or project team.

2.  The catalog page can be explored using the quick lists of:

    -   **Latest news**: recently updated records
    -   **Most popular**: frequently used records
    -   **Comments**: records with new comments and discussion

    ![](img/browse_latest.png)
    *Latest news*

3.  Records are displayed as a :fontawesome-solid-table-cells-large: block list, :fontawesome-solid-bars: large list, or :fontawesome-solid-align-justify: small list
    using the toggle on the right.

    Click on any of the listed records to view.

    ![](img/browse_large_list.png)
    *Large list display of records*

4.  The catalog page provides a number of quick searches to browse
    catalog contents:

    -   Use **Browse by Topics** to
        explore records based on subject matter.
    -   Use **Browse by Resources** to
        explore different kinds of content.

    Each option lists "search facets" (shown as small bubbles), click
    on a "search facet" such as `Dataset` to explore.

    ![](img/browse.png)
    *Browse metadata catalogue*

## Search Results

To further explore listed records:

1.  Navigate to the **:fontawesome-solid-magnifying-glass: Search** page (or browse or search the catalogue to
    list search results).
    
    ![](img/search_page.png)
    *Search page*
    
2.  Use the **:fontawesome-solid-magnifying-glass: Filter** section on the right hand side to refine search results
    using additional search facets, keywords, and details such as download format.
    
    Click on the "search facet" `Oceans` to filter the search results to
    matching records.

    ![](img/results_filter.png)
    *Filter results*

3.  Options are provided at the top of the search results to:
    
    * Presentation of matching records (as a **:fontawesome-solid-table-cells-large: Grid**
    or **:fontawesome-solid-bars: List**)
    * Sort the results
    * Manage how many results are shown per page
    * Advance to additional pages of results
    * Quickly select records

    ![](img/browse_results.png)
    *Browse results*

4.  To clear the search results use **:fontawesome-solid-xmark: Clear current search query, filters, and sorts** 
    at any time. This button is located in the **Search** field at the top of the page.

5.  The **:fontawesome-solid-ellipsis-vertical: Advanced** search options are located in the 
    **Search** field at the top of the page.
    
    These options can be used to further refine search results by category, keywords, contact
    or date range.

    ![](img/search_advanced.png)
    *Advanced search options*

4.  Open the **:fontawesome-solid-ellipsis-vertical: Advanced** search options panel.
    
    Use the drop down menu for **Records created in the last** to select `this week`.
    This acts as a short cut to fill in the **From** and **To** calendar fields.
    
    Press the **:fontawesome-solid-magnifying-glass:  Search** button to filter using this date range.

    ![](img/search_record_creation.png)
    *Record updated in the last week*

5.  To search for data in the year `2016` use the advanced search
    options to fill **Resources created in the last** in:
    
    **From**
    :   `2016-01-01`
    
    **To**
    :   `2016-12-31`
    
    Press **Search** button to show data from `2016`.

    ![](img/search_resource_2016.png)
    *Resource updated in 2016*
    
    !!! note

        The **Resource** date filter shows records with data identification
        (creation, publication, revision) dates included within the
        calendar date range.

6.  A slide out map is provided at the bottom of the page, providing
    visual feedback on the extent of each record.

    ![](img/search_map.png)
    *Search map*

    The map can be controlled by by toggling beween two modes:

    -   Pan: Click and drag the map location, using the mouse wheel to
        adjust zoom level.

    -   Bounding Box: Hold ++shift++ and click and drag to define an extent used to filter
        records.
        
        The drop down controls if the extent is used to list
        only records that are withing, or all records that intersect.
        
        ![](img/search_map_bbox.png)
        *Search bounding box intersects*

7.  Records are selected (using the checkbox located next to each one)
    to quickly download or generate a PDF of one or more records.

    ![](img/browse_selection.png)
    *Selected Records*

9.  Additional tips and tricks with search results:

    -   Details on
        [selecting multiple records and exporting](download.md#download-from-search-results)
        as a `ZIP` or `PDF`.
