# Search Fields {#tuto-hookcustomizations-searchfields}

Lucene is the search engine used by GeoNetwork. All Lucene configuration is defined in WEB-INF/config-lucene.xml.

In some cases it's relevant to modify or extend the search fields of the metadata index. For example to add a field (which is then searchable or can be used in a default view) or change the content of the field is created from the metadata (indexation).

## Adding a new field

Each schema contains a file index-fields/default.xsl where the fields stored on the index are defined. This is an xslt that runs over the xml of the metadata and creates the different field tags that Lucene will recognize and process.

``` xml
<xsl:for-each select="gmd:pointOfContact[1]/*/gmd:role/*/@codeListValue">
  <Field name="responsiblePartyRole" string="{string(.)}" store="true" index="true"/>
</xsl:for-each>
```

This fields can then be used as parameters on the Q service, to search by that field.

Remember that after every change on the index configuration, a new index should be rebuilt.

## Summaries

The Q service returns the list of search results, but also can return a summary of the results returned with the parameters "buildSummary=true" and "summaryType=\$summary".

This summary is a list of the most common values for certain fields. This summaries are defined on web/src/main/webapp/WEB-INF/config-summary.xml:

``` xml
<summaryType name="results">
  <item facet="keyword" max="100"/>
</summaryType>
```

The items used on this summaries have to be defined on the upper tag called "facets".

## Boost fields

Sometimes we want some field to be more relevant on the search than others. We can boost those fields inside the tag fieldBoosting .The boost is a positive or negative float value.

``` xml
<fieldBoosting>
  <Field name="_op0" boost="0.0F"/>
  <Field name="_op1" boost="0.0F"/>
  <Field name="_op2" boost="0.0F"/>
  <Field name="_dummy" boost="0.0F"/>
  <Field name="_isTemplate" boost="2.0F"/>
  <Field name="_owner" boost="5.0F"/>
</fieldBoosting>
```

See more on [Configuring search fields](../../../customizing-application/configuring-search-fields.md)
