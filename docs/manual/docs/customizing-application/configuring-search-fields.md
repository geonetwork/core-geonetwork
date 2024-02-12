# Configuring search fields

In some cases it's relevant to modify or extend the search fields of the metadata index. For example to add a field (which is then searchable or can be used in a default view) or change the content of the field created from the metadata (indexation).

In each schema-plugin you can define how the new field is filled from the metadata content in the file (see `schemas/iso19139/src/main/plugin/iso19139/index-fields/default.xsl`).

[Lucene](http://lucene.apache.org/java/docs/index.html>) is the search engine used by GeoNetwork. All Lucene configuration is defined in `WEB-INF/config-lucene.xml`.


## Add a search field

Indexed fields are defined on a per-schema basis on the schema folder (eg. WEB-INF/data/config/schema_plugins/iso19139/index-fields) in default.xsl file.
This file define for each search criteria the corresponding element in a metadata record. For example, indexing the title
of an ISO19139 record:

``` xml
<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
                       gmd:citation/gmd:CI_Citation/
                       gmd:title/gco:CharacterString">
    <Field name="mytitle" string="{string(.)}" store="true" index="true"/>
</xsl:for-each>
```

Usually, if the field is only for searching and should not be displayed in search results the store attribute could
be set to false.

Once the field added to the index, user could query using it as a search criteria in the different kind
of search services. For example using: http://localhost:8080/geonetwork/srv/en/q?mytitle=africa

If user wants this field to be tokenized, it should be added to the tokenized section of `WEB-INF/config-lucene.xml`:

``` xml
<tokenized>
  <Field name="mytitle"/>
```

If user wants this field to be returned in search results for the search service, then the field should be added to the Lucene configuration in the dumpFields section:

``` xml
    <dumpFields>
      <field name="mytitle" tagName="mytitle"/>
```

### Boosting documents and fields

Document and field boosting allows catalogue administrator to be able to customize default Lucene scoring in order to promote certain types of records.

A common use case is when the catalogue contains lot of series for aggregating datasets. 

Not promoting the series could make the series "useless" even if those records contains important content.

Boosting this type of document allows to promote series and guide the end-user from series to related records (through the relation navigation).

In that case, the following configuration allows boosting series and minor importance of records part of a series:

``` xml
<boostDocument name="org.fao.geonet.kernel.search.function.ImportantDocument">
  <Param name="fields" type="java.lang.String" value="type,parentUuid"/>
  <Param name="values" type="java.lang.String" value="series,NOTNULL"/>
  <Param name="boosts" type="java.lang.String" value=".2F,-.3F"/>
</boostDocument>
```  

The boost is a positive or negative float value.

This feature has to be used by expert users to alter default search behavior scoring according to catalogue content. It needs tuning and experimentation to not promote too much some records. During testing, if search results looks different while being logged or not, it could be relevant to ignore some internal fields in boost computation which may alter scoring according to current user. 

Example configuration:

``` xml
<fieldBoosting>
  <Field name="_op0" boost="0.0F"/>
  <Field name="_op1" boost="0.0F"/>
  <Field name="_op2" boost="0.0F"/>
  <Field name="_dummy" boost="0.0F"/>
  <Field name="_isTemplate" boost="0.0F"/>
  <Field name="_owner" boost="0.0F"/>
</fieldBoosting>
```

### Boosting search results

By default, Lucene compute score according to search criteria and the corresponding result set and the index content. In case of search with no criteria, Lucene will return top docs in index order (because none are more relevant than others).

In order to change the score computation, a boost function could be defined. Boosting query needs to be loaded in classpath. A sample boosting class is available. RecencyBoostingQuery will promote recently modified documents:

``` xml
<boostQuery name="org.fao.geonet.kernel.search.function.RecencyBoostingQuery">
  <Param name="multiplier" type="double" value="2.0"/>
  <Param name="maxDaysAgo" type="int" value="365"/>
  <Param name="dayField" type="java.lang.String" value="_changeDate"/>
</boostQuery>
```
