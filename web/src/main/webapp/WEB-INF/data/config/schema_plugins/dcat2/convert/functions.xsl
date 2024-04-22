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
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:mime="java:org.fao.geonet.util.MimeTypeFinder"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- latlon coordinates indexed as numeric. -->
  <xsl:output name="default-serialize-mode" indent="no"
              omit-xml-declaration="yes"/>

  <xsl:include href="../layout/utility-fn.xsl"/>

  <xsl:template match="*" mode="latLon">
    <xsl:variable name="format" select="'##.00'"></xsl:variable>

    <xsl:variable name="geometry" as="node()">
      <xsl:choose>
        <xsl:when test="count(locn:geometry[ends-with(@rdf:datatype,'#wktLiteral')])>0">
          <xsl:copy-of select="node()[name(.)='locn:geometry' and ends-with(@rdf:datatype,'#wktLiteral')][1]"/>
        </xsl:when>
        <xsl:when test="count(locn:geometry[ends-with(@rdf:datatype,'#gmlLiteral')])>0">
          <xsl:copy-of select="node()[name(.)='locn:geometry' and ends-with(@rdf:datatype,'#gmlLiteral')][1]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="locn:geometry[1]"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="bbox" select="gn-fn-dcat2:getBboxCoordinates($geometry)"/>
    <xsl:variable name="bboxCoordinates" select="tokenize(replace($bbox,',','.'), '\|')"/>
    <xsl:if test="count($bboxCoordinates)=4">
      <Field name="westBL" string="{format-number(xs:double($bboxCoordinates[1]) , $format)}"
             store="false" index="true"/>
      <Field name="southBL" string="{format-number(xs:double($bboxCoordinates[2]), $format)}"
             store="false" index="true"/>

      <Field name="eastBL" string="{format-number(xs:double($bboxCoordinates[3]), $format)}"
             store="false" index="true"/>
      <Field name="northBL" string="{format-number(xs:double($bboxCoordinates[4]), $format)}"
             store="false" index="true"/>

      <Field name="geoBox" string="{replace($bbox,',','.')}" store="true" index="false"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="fixSingle">
    <xsl:param name="value"/>

    <xsl:choose>
      <xsl:when test="string-length(string($value))=1">
        <xsl:value-of select="concat('0',$value)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="getMimeTypeFile">
    <xsl:param name="datadir"/>
    <xsl:param name="fname"/>
    <xsl:value-of select="mime:detectMimeTypeFile($datadir,$fname)"/>
  </xsl:template>

  <xsl:template name="getMimeTypeUrl">
    <xsl:param name="linkage"/>
    <xsl:value-of select="mime:detectMimeTypeUrl($linkage)"/>
  </xsl:template>


  <!-- iso3code of default index language -->
  <xsl:variable name="defaultLang">dut</xsl:variable>
  <xsl:variable name="defaultLang-2char">nl</xsl:variable>

  <xsl:template name="interpretLanguage">
    <xsl:param name="input"/>
    <xsl:choose>
      <xsl:when test="ends-with(lower-case($input),'nld')">dut</xsl:when>
      <xsl:when test="ends-with(lower-case($input),'fre')">fre</xsl:when>
      <xsl:when test="ends-with(lower-case($input),'eng')">eng</xsl:when>
      <xsl:when test="ends-with(lower-case($input),'deu')">ger</xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="langId-dcat2">
    <!--xsl:variable name="titleLanguages" select="/*[name(.)='rdf:RDF']/dcat:Catalog/dcat:dataset/dcat:Dataset/dct:title/@xml:lang"/-->
    <!--xsl:variable name="allLanguages" select="distinct-values(/*[name(.)='rdf:RDF']/dcat:Catalog/dcat:dataset/dcat:Dataset//@xml:lang)"/-->
    <xsl:variable name="allCatalogLanguages"
                  select="distinct-values(/*[name(.)='rdf:RDF']/dcat:Catalog/dct:language/skos:Concept/@rdf:about)"/>
    <xsl:variable name="foundDefaultLanguage">
      <xsl:choose>
        <xsl:when test="$allCatalogLanguages[1]">
          <xsl:call-template name="interpretLanguage">
            <xsl:with-param name="input" select="$allCatalogLanguages[1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$foundDefaultLanguage=''">
      <xsl:value-of select="$defaultLang"/>
    </xsl:if>
    <xsl:if test="not($foundDefaultLanguage='')">
      <xsl:value-of select="$foundDefaultLanguage"/>
    </xsl:if>
  </xsl:template>

  <!-- TODO: refactor language code conversions by using the functions in XslUtil.java

  xmlns:java="java:org.fao.geonet.util.XslUtil"
      <xsl:value-of select="lower-case(java:twoCharLangCode($langId-2char))"/>
      			                       java:threeCharLangCode(String)

  -->

  <xsl:template name="langId2to3">
    <xsl:param name="langId-2char"/>
    <xsl:choose>
      <xsl:when test="ends-with($langId-2char,'nl')">dut</xsl:when>
      <xsl:when test="ends-with($langId-2char,'fr')">fre</xsl:when>
      <xsl:when test="ends-with($langId-2char,'en')">eng</xsl:when>
      <xsl:when test="ends-with($langId-2char,'de')">ger</xsl:when>
      <xsl:otherwise>
        <xsl:message select="concat('No mapping found in langId2to3 for langId-2char with value ', $langId-2char)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="langId2toAuth">
    <xsl:param name="langId-2char"/>
    <xsl:choose>
      <xsl:when test="ends-with($langId-2char,'nl')">nld</xsl:when>
      <xsl:when test="ends-with($langId-2char,'fr')">fre</xsl:when>
      <xsl:when test="ends-with($langId-2char,'en')">eng</xsl:when>
      <xsl:when test="ends-with($langId-2char,'de')">deu</xsl:when>
      <xsl:otherwise>
        <xsl:message select="concat('No mapping found in langId2toAuth for langId-2char with value ', $langId-2char)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="langId3to2">
    <xsl:param name="langId-3char"/>
    <xsl:choose>
      <xsl:when test="ends-with($langId-3char,'dut')">nl</xsl:when>
      <xsl:when test="ends-with($langId-3char,'fre')">fr</xsl:when>
      <xsl:when test="ends-with($langId-3char,'eng')">en</xsl:when>
      <xsl:when test="ends-with($langId-3char,'ger')">de</xsl:when>
      <xsl:otherwise>
        <xsl:message select="concat('No mapping found in langId3to2 for langId-3char with value ', $langId-3char)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="defaultTitle">
    <xsl:param name="isoDocLangId"/>


    <xsl:variable name="twoCharLangCode">
      <xsl:call-template name="langId3to2">
        <xsl:with-param name="langId-3char" select="$isoDocLangId"/>
      </xsl:call-template>
    </xsl:variable>


    <xsl:variable name="docLangTitle"
                  select="dct:title[@xml:lang=$twoCharLangCode]"/>
    <xsl:variable name="firstTitle"
                  select="dct:title"/>
    <xsl:choose>
      <xsl:when test="string-length(string($docLangTitle[1])) != 0">
        <xsl:value-of select="$docLangTitle[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string($firstTitle[1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="defaultAbstract">
    <xsl:param name="isoDocLangId"/>

    <xsl:variable name="twoCharLangCode">
      <xsl:call-template name="langId3to2">
        <xsl:with-param name="langId-3char" select="$isoDocLangId"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="docLangAbstract"
                  select="dct:description[@xml:lang=$twoCharLangCode]"/>
    <xsl:variable name="firstAbstract"
                  select="dct:description"/>
    <xsl:choose>
      <xsl:when test="string-length(string($docLangAbstract[1])) != 0">
        <xsl:value-of select="$docLangAbstract[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string($firstAbstract[1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="index-lang-tag-oneval">

    <xsl:param name="tag"/>
    <xsl:param name="langId"/>
    <xsl:param name="isoLangId"/>

    <xsl:variable name="langIdWording" select="$tag[@xml:lang=$langId]"/>
    <xsl:variable name="langIdWordingSkos" select="$tag/skos:Concept/skos:prefLabel[@xml:lang=$langId]"/>
    <xsl:variable name="default" select="$tag[@xml:lang=$isoLangId]"/>
    <xsl:variable name="defaultSkos" select="$tag/skos:Concept/skos:prefLabel[@xml:lang=$isoLangId]"/>
    <xsl:variable name="first" select="$tag[1]"/>
    <xsl:choose>
      <xsl:when test="$langIdWording">
        <xsl:value-of select="$langIdWording[1]"/>
      </xsl:when>
      <xsl:when test="$langIdWordingSkos">
        <xsl:value-of select="$langIdWordingSkos[1]"/>
      </xsl:when>
      <xsl:when test="$default">
        <xsl:value-of select="$default[1]"/>
      </xsl:when>
      <xsl:when test="$defaultSkos">
        <xsl:value-of select="$defaultSkos[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$first"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="index-lang-tag">
    <xsl:param name="tag"/>
    <xsl:param name="field"/>
    <xsl:param name="langId"/>

    <xsl:variable name="langIdWording" select="$tag[@xml:lang=$langId]"/>
    <xsl:variable name="langIdWordingSkos" select="$tag/skos:Concept/skos:prefLabel[@xml:lang=$langId]"/>
    <xsl:variable name="wording" select="$tag"/>

    <xsl:choose>
      <xsl:when test="$langIdWording">
        <xsl:for-each select="$langIdWording">
          <field name="{$field}" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$langIdWordingSkos">
        <xsl:for-each select="$langIdWordingSkos">
          <field name="{$field}" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$wording">
        <xsl:for-each select="$wording">
          <field name="{$field}" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>
