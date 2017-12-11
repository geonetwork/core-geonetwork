<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <!-- All URL parameters could be available as params -->

  <!-- Define the view to be rendered as defined in
  the config-editor.xml file of the schema. -->
  <xsl:param name="view" select="'default'"/>

  <!-- Choose the type of HTML to return:
  * html render a full HTML page
  * div render a div element to be embedded in an existing webpage. -->
  <xsl:param name="root" select="'html'"/>

  <!-- Define the full portal link. By default, it will link
  to the catalog.search main page of the catalog. To configure a custom
  use {{uuid}} to be replaced by the record UUID.
  eg. http://another.portal.org/${uuid}
  -->
  <xsl:param name="portalLink" select="''"/>


  <!-- TODO: schema is not part of the XML -->
  <xsl:variable name="schema"
                select="/root/info/record/datainfo/schemaid"/>
  <xsl:variable name="source"
                select="/root/info/record/sourceinfo/sourceid"/>
  <xsl:variable name="metadataId"
                select="/root/info/record/id"/>
  <xsl:variable name="metadataUuid"
                select="/root/info/record/uuid"/>

  <xsl:variable name="schemaCodelists">
    <null/>
  </xsl:variable>

  <xsl:variable name="metadata"
                select="/root/undefined"/>
  <xsl:variable name="language"
                select="/root/lang/text()"/>
  <xsl:variable name="baseUrl"
                select="/root/url"/>
  <xsl:variable name="nodeUrl"
                select="/root/gui/nodeUrl"/>

  <!-- Date formating -->
  <xsl:variable name="dateFormats">
    <dateTime>
      <for lang="eng" default="true">[H1]:[m01]:[s01] on [D1] [MNn] [Y]</for>
      <for lang="fre">[H1]:[m01]:[s01] le [D1] [MNn] [Y]</for>
    </dateTime>
    <date>
      <for lang="eng" default="true">[D1] [MNn] [Y]</for>
      <for lang="fre">[D1] [MNn] [Y]</for>
    </date>
  </xsl:variable>


  <xsl:variable name="schemaStrings"
                select="/root/schemas/*[name() = $schema]/strings"/>

  <!-- Get params from requests parameters or use the first view configured -->
  <xsl:variable name="viewConfig" select="$configuration/editor/views/view[@name = $view]"/>

  <!-- Flat mode is defined in the first tab of the view -->
  <xsl:variable name="isFlatMode"
                select="$viewConfig/tab[1]/@mode = 'flat'"/>
</xsl:stylesheet>
