<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================================= -->

  <xsl:import href="common.xsl"/>

  <!-- ============================================================================================= -->
  <!-- === Web DAV harvesting node -->
  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="site">
    <server>
      <xsl:value-of select="server/value"/>
    </server>
    <port>
      <xsl:value-of select="port/value"/>
    </port>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <xslfilter>
      <xsl:value-of select="xslfilter"/>
    </xslfilter>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="options">
    <validate>
      <xsl:value-of select="validate/value"/>
    </validate>
    <folder>
      <xsl:value-of select="folder/value"/>
    </folder>
    <recurse>
      <xsl:value-of select="recurse/value"/>
    </recurse>
    <useAuthKey>
      <xsl:value-of select="useAuthKey/value"/>
    </useAuthKey>
    <publicKey>
      <xsl:value-of select="publicKey/value"/>
    </publicKey>
    <typeAuthKey>
      <xsl:value-of select="typeAuthKey/value"/>
    </typeAuthKey>
  </xsl:template>

  <!-- ============================================================================================= -->

  <xsl:template match="*" mode="searches"/>

  <!-- ============================================================================================= -->

</xsl:stylesheet>
