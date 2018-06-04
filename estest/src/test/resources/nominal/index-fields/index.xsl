<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:output method="xml" indent="yes"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:template match="/">
    <doc>
      <source>source-from-index-xsl</source>
    </doc>
  </xsl:template>
</xsl:stylesheet>
