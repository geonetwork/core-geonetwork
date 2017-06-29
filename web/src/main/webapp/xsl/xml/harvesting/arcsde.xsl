<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === ARCSDE harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <server>
      <xsl:value-of select="server/value"/>
    </server>
    <port>
      <xsl:value-of select="port/value"/>
    </port>
    <username>
      <xsl:value-of select="username/value"/>
    </username>
    <password>
      <xsl:value-of select="password/value"/>
    </password>
    <database>
      <xsl:value-of select="database/value"/>
    </database>
    <version>
      <xsl:value-of select="version/value"/>
    </version>
    <connectionType>
      <xsl:value-of select="connectionType/value"/>
    </connectionType>
    <databaseType>
      <xsl:value-of select="databaseType/value"/>
    </databaseType>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
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
