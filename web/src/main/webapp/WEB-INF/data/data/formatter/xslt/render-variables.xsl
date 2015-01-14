<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <!-- TODO: schema is not part of the XML -->
  <xsl:variable name="schema"
                select="/root/info/record/datainfo/schemaid"/>
  <xsl:variable name="metadataId"
                select="/root/info/record/id"/>

  <xsl:variable name="schemaCodelists"><null/></xsl:variable>

  <xsl:variable name="metadata"
                select="/root/undefined"/>
  <xsl:variable name="language"
                select="/root/lang/text()"/>


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
                select="/root/schemas/*[name() = $schema]/*[3]"/>

  <!-- Get params from requests parameters or use the first view configured -->
  <xsl:param name="view" select="$configuration/editor/views/view[1]/@name"/>
  <xsl:variable name="viewConfig" select="$configuration/editor/views/view[@name = $view]"/>

  <!-- Flat mode is defined in the first tab of the view -->
  <xsl:variable name="isFlatMode"
                select="$viewConfig/tab[1]/@mode = 'flat'"/>
</xsl:stylesheet>