<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../process/process-utility.xsl"/>

  <!-- Surround the concept with parent based on  related parent xml element
    If no keyword is provided, only thesaurus section is adaded.
    -->
  <xsl:template name="to-dcat2-concept">
    <xsl:variable name="listOfLanguage"
                  select="tokenize(/root/request/lang, ',')"/>

    <xsl:apply-templates mode="to-dcat2-concept" select=".">
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Surround the skos:Concept xml element with a parent xml element on the thesaurus key -->
  <xsl:template mode="to-dcat2-concept"
                match="*[not(/root/request/skipdescriptivekeywords)]">
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="wrapper"
               select="if (/root/request/wrapper)
                       then /root/request/wrapper
                       else 'dcat:keyword'"/>

    <!-- Get thesaurus ID from keyword or from request parameter if no keyword found. -->
    <xsl:variable name="currentThesaurus"
                  select="if (thesaurus/key) then thesaurus/key else /root/request/thesaurus"/>
    <xsl:variable name="keywordThesaurus"
                  select="if ($currentThesaurus = 'external.none.allThesaurus')
                          then replace(./uri, 'http://org.fao.geonet.thesaurus.all/([^@]+)@@@.+', '$1')
                          else $currentThesaurus"/>
    <xsl:variable name="inSchemeURI"
                  select="gn-fn-dcat2:getInSchemeURIByThesaurusId($keywordThesaurus)"/>

    <!-- Loop on all keyword from the same thesaurus -->
    <xsl:variable name="response">
      <gn_replace_all>
        <xsl:for-each select="//keyword">
          <xsl:element name="{$wrapper}">
            <skos:Concept>
              <xsl:attribute name="rdf:about">
                <xsl:value-of select="if (contains(uri, '@@@'))
                                      then substring-after(uri, '@@@')
                                      else uri"/>
              </xsl:attribute>
              <xsl:variable name="keyword" select="."/>
              <xsl:choose>
                <xsl:when test="count($listOfLanguage) > 0">
                  <xsl:for-each select="$listOfLanguage">
                    <xsl:variable name="lang" select="."/>
                    <xsl:if test="$lang!=''">
                      <skos:prefLabel>
                        <xsl:attribute name="xml:lang" select="$lang"/>
                        <xsl:value-of select="$keyword/values/value[@language = $lang]/text()"/>
                      </skos:prefLabel>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <skos:prefLabel>
                    <xsl:value-of select="$keyword/value/text()"/>
                  </skos:prefLabel>
                </xsl:otherwise>
              </xsl:choose>
              <skos:inScheme rdf:resource="{$inSchemeURI}"/>
            </skos:Concept>
          </xsl:element>
        </xsl:for-each>
      </gn_replace_all>
    </xsl:variable>
    <xsl:copy-of select="$response"/>
  </xsl:template>


  <xsl:template name="to-dcat2-concept-reference">
    <xsl:variable name="listOfLanguage"
                  select="tokenize(/root/request/lang, ',')"/>

    <xsl:apply-templates mode="to-dcat2-concept-reference" select=".">
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Surround the skos:Concept xml element with a parent xml element on the thesaurus key -->
  <xsl:template mode="to-dcat2-concept-reference"
                match="*[not(/root/request/skipdescriptivekeywords)]">
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="wrapper"
               select="if (/root/request/wrapper)
                       then /root/request/wrapper
                       else 'dcat:keyword'"/>

    <!-- Get thesaurus ID from keyword or from request parameter if no keyword found. -->
    <xsl:variable name="currentThesaurus"
                  select="if (thesaurus/key) then thesaurus/key else /root/request/thesaurus"/>
    <xsl:variable name="keywordThesaurus"
                  select="if ($currentThesaurus = 'external.none.allThesaurus')
                          then replace(./uri, 'http://org.fao.geonet.thesaurus.all/([^@]+)@@@.+', '$1')
                          else $currentThesaurus"/>
    <xsl:variable name="inSchemeURI"
                  select="gn-fn-dcat2:getInSchemeURIByThesaurusId($keywordThesaurus)"/>

    <!-- Loop on all keyword from the same thesaurus -->
    <xsl:variable name="response">
      <gn_replace_all>
        <xsl:for-each select="//keyword">
          <xsl:element name="{$wrapper}">
            <xsl:attribute name="rdf:resource">
              <xsl:value-of select="if (contains(uri, '@@@'))
                                  then substring-after(uri, '@@@')
                                  else uri"/>
            </xsl:attribute>
          </xsl:element>
        </xsl:for-each>
      </gn_replace_all>
    </xsl:variable>
    <xsl:copy-of select="$response"/>
  </xsl:template>
</xsl:stylesheet>
