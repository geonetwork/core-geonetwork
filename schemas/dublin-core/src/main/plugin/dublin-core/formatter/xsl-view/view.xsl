<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.services.metadata.format.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">

  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/simpledc"/>




  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="simpledc">
    <xsl:value-of select="dc:title"/>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="simpledc">
    <xsl:value-of select="dc:description"/>
  </xsl:template>





  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field" match="*">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="."/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="dc:coverage">

    <xsl:variable name="coverage" select="."/>

    <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
    <xsl:variable name="north" select="substring-before($n,',')"/>
    <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
    <xsl:variable name="south" select="substring-before($s,',')"/>
    <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
    <xsl:variable name="east" select="substring-before($e,',')"/>
    <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
    <xsl:variable name="west" select="if (contains($w, '. '))
                                      then substring-before($w,'. ') else $w"/>
    <xsl:variable name="place" select="substring-after($coverage,'. ')"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($place != '') then $place else ''"/>
      </dt>
      <dd>
        <xsl:copy-of select="gn-fn-render:bbox(
                                xs:double($west),
                                xs:double($south),
                                xs:double($east),
                                xs:double($north))"/>
      </dd>
    </dl>
  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field" match="simpledc">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>







  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value" match="*">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value" match="*[starts-with(., 'http')]">
    <a href="{.}"><xsl:value-of select="."/></a>
  </xsl:template>

  <!-- ... Dates -->
  <xsl:template mode="render-value" match="*[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <xsl:value-of select="format-date(., $dateFormats/date/for[@lang = $language]/text())"/>
  </xsl:template>

  <xsl:template mode="render-value" match="*[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <xsl:value-of select="format-dateTime(., $dateFormats/dateTime/for[@lang = $language]/text())"/>
  </xsl:template>

</xsl:stylesheet>