<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <!--<xsl:import href="metadata-inspire-constant.xsl"/>-->


  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>


  <xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType" mode="index">

    <!-- Main variables for the document -->
    <xsl:variable name="identifier" as="xs:string?"
                  select="@uuid"/>

    <doc>
      <field name="metadataIdentifier">
        <xsl:value-of select="$identifier"/>
      </field>

      <field name="standardName">ISO19110</field>

      <!-- Indexing record information -->
      <!-- # Date -->
      <!-- TODO improve date formatting maybe using Joda parser
      Select first one because some records have 2 dates !
      eg. fr-784237539-bdref20100101-0105
      -->
      <xsl:for-each select="*:versionDate/*[text() != '' and position() = 1]">
        <field name="dateStamp">
          <xsl:value-of select="if (name() = 'gco:Date' and string-length(.) = 4)
                then concat(., '-01-01T00:00:00Z')
                else if (name() = 'gco:Date' and string-length(.) = 7)
                then concat(., '-01T00:00:00Z')
                else if (name() = 'gco:Date' or string-length(.) = 10)
                then concat(., 'T00:00:00Z')
                else (
                  if (ends-with(., 'Z'))
                  then .
                  else concat(., 'Z')
                )"/>
        </field>
      </xsl:for-each>


      <field name="resourceType">featureCatalog</field>
      <field name="resourceTitle">
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
                              /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
                              /gfc:FC_FeatureType/gfc:typeName/gco:LocalName"/>
      </field>
      <field name="resourceAbstract">
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/gmx:scope/gco:CharacterString|
                              /gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString|
                              /gfc:FC_FeatureType/gfc:definition/gco:CharacterString"/>
      </field>

      <!-- Index attribute table as JSON object -->
      <!-- TODO
      <xsl:variable name="attributes"
                    select=".//gfc:carrierOfCharacteristics"/>
      <xsl:if test="count($attributes) > 0">
        <xsl:variable name="jsonAttributeTable">
          [<xsl:for-each select="$attributes">
          {"name": "<xsl:value-of select="*/gfc:memberName/*/text()"/>",
          "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>",
          "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
          <xsl:if test="*/gfc:listedValue">
            ,"values": [<xsl:for-each select="*/gfc:listedValue">{
            "label": "<xsl:value-of select="*/gfc:label/*/text()"/>",
            "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
            "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>"}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>]
          </xsl:if>}
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>]
        </xsl:variable>
        <Field name="attributeTable" index="true" store="true"
               string="{$jsonAttributeTable}"/>
      </xsl:if>-->
    </doc>
  </xsl:template>
</xsl:stylesheet>
