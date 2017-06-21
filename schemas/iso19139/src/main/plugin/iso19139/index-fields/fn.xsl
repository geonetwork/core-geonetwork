<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 2014-2016 European Environment Agency
  ~
  ~ Licensed under the EUPL, Version 1.1 or – as soon
  ~ they will be approved by the European Commission -
  ~ subsequent versions of the EUPL (the "Licence");
  ~ You may not use this work except in compliance
  ~ with the Licence.
  ~ You may obtain a copy of the Licence at:
  ~
  ~ https://joinup.ec.europa.eu/community/eupl/og_page/eupl
  ~
  ~ Unless required by applicable law or agreed to in
  ~ writing, software distributed under the Licence is
  ~ distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  ~ either express or implied.
  ~ See the Licence for the specific language governing
  ~ permissions and limitations under the Licence.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:daobs="http://daobs.org"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:function name="daobs:search-in" as="node()*">
    <xsl:param name="list" as="node()*"/>
    <xsl:param name="value" as="xs:string"/>

    <!-- Convert accent, lower case -->
    <xsl:copy-of select="$list[
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space(.)),'NFKD'),'\p{Mn}',''),'NFKC') =
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($value)),'NFKD'),'\p{Mn}',''),'NFKC')]"/>
  </xsl:function>

  <xsl:function name="daobs:search-in-contains" as="node()*">
    <xsl:param name="list" as="node()*"/>
    <xsl:param name="value" as="xs:string"/>

    <!-- Convert accent, lower case -->
    <xsl:copy-of select="$list[contains(
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($value)),'NFKD'),'\p{Mn}',''),'NFKC'),
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space(.)),'NFKD'),'\p{Mn}',''),'NFKC')
                      )]"/>
  </xsl:function>

  <!-- Compare ignoring case, accents -->
  <xsl:function name="daobs:compare" as="xs:boolean">
    <xsl:param name="this" as="xs:string"/>
    <xsl:param name="that" as="xs:string"/>

    <xsl:value-of select="normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($this)),'NFKD'),'\p{Mn}',''),'NFKC') =
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($that)),'NFKD'),'\p{Mn}',''),'NFKC')"/>
  </xsl:function>

  <xsl:function name="daobs:contains" as="xs:boolean">
    <xsl:param name="this" as="xs:string"/>
    <xsl:param name="that" as="xs:string"/>

    <xsl:value-of select="contains(
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($this)),'NFKD'),'\p{Mn}',''),'NFKC'),
                    normalize-unicode(replace(normalize-unicode(
                      lower-case(normalize-space($that)),'NFKD'),'\p{Mn}',''),'NFKC'))"/>
  </xsl:function>
</xsl:stylesheet>
