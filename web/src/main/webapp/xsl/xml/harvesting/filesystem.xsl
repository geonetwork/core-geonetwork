<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === Filesystem harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <directory>
      <xsl:value-of select="directory/value"/>
    </directory>
    <recurse>
      <xsl:value-of select="recurse/value"/>
    </recurse>
    <nodelete>
      <xsl:value-of select="nodelete/value"/>
    </nodelete>
    <recordType>
      <xsl:value-of select="recordType/value"/>
    </recordType>
    <checkFileLastModifiedForUpdate>
      <xsl:value-of select="checkFileLastModifiedForUpdate/value"/>
    </checkFileLastModifiedForUpdate>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <beforeScript>
      <xsl:value-of select="beforeScript/value"/>
    </beforeScript>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="options"/>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="searches">
    <searches>
      <xsl:for-each select="children/search">
        <search>
          <freeText>
            <xsl:value-of select="children/freeText/value"/>
          </freeText>
          <title>
            <xsl:value-of select="children/title/value"/>
          </title>
          <abstract>
            <xsl:value-of select="children/abstract/value"/>
          </abstract>
          <subject>
            <xsl:value-of select="children/subject/value"/>
          </subject>
        </search>
      </xsl:for-each>
    </searches>
  </xsl:template>

  <!-- ============================================================================================= -->

</xsl:stylesheet>
