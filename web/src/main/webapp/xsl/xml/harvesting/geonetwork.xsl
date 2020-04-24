<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="common.xsl"/>

  <xsl:template match="*" mode="site">
    <host>
      <xsl:value-of select="host/value"/>
    </host>
    <node>
      <xsl:value-of select="node/value"/>
    </node>
    <useChangeDateForUpdate>
      <xsl:value-of select="useChangeDateForUpdate/value"/>
    </useChangeDateForUpdate>
    <createRemoteCategory>
      <xsl:value-of select="createRemoteCategory/value"/>
    </createRemoteCategory>
    <mefFormatFull>
      <xsl:value-of select="mefFormatFull"/>
    </mefFormatFull>
    <xslfilter>
      <xsl:value-of select="xslfilter"/>
    </xslfilter>
  </xsl:template>

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
          <keywords>
            <xsl:value-of select="children/keywords/value"/>
          </keywords>
          <digital>
            <xsl:value-of select="children/digital/value"/>
          </digital>
          <hardcopy>
            <xsl:value-of select="children/hardcopy/value"/>
          </hardcopy>
          <anyField>
            <xsl:value-of select="children/anyField/value"/>
          </anyField>
          <anyValue>
            <xsl:value-of select="children/anyValue/value"/>
          </anyValue>
          <source>
            <uuid>
              <xsl:value-of select="children/sourceUuid/value"/>
            </uuid>
            <name>
              <xsl:value-of select="children/sourceName/value"/>
            </name>
          </source>
        </search>
      </xsl:for-each>
    </searches>
  </xsl:template>

  <xsl:template match="*" mode="other">
    <groupsCopyPolicy>
      <xsl:for-each select="children/groupCopyPolicy">
        <group name="{children/name/value}" policy="{children/policy/value}"/>
      </xsl:for-each>
    </groupsCopyPolicy>
  </xsl:template>

</xsl:stylesheet>
