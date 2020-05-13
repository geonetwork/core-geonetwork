<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

  <!-- inspireThemes is a nodeset consisting of skos:Concept elements -->
  <!-- each containing a skos:definition and skos:prefLabel for each language -->
  <!-- This template finds the provided keyword in the skos:prefLabel elements and
  returns the English one from the same skos:Concept -->
  <xsl:template name="translateInspireThemeToEnglish">
    <xsl:param name="keyword"/>
    <xsl:param name="inspireThemes"/>

    <xsl:value-of select="$inspireThemes/skos:prefLabel[@xml:lang='en' and ../skos:prefLabel = $keyword]/text()"/>
  </xsl:template>

  <xsl:template name="getInspireThemeAcronym">
    <xsl:param name="keyword"/>

    <xsl:value-of select="$inspire-theme/skos:altLabel[../skos:prefLabel = $keyword]/text()"/>
  </xsl:template>

  <xsl:template name="determineInspireAnnex">
    <xsl:param name="keyword"/>
    <xsl:param name="inspireThemes"/>
    <xsl:variable name="englishKeywordMixedCase">
      <xsl:call-template name="translateInspireThemeToEnglish">
        <xsl:with-param name="keyword" select="$keyword"/>
        <xsl:with-param name="inspireThemes" select="$inspireThemes"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="englishKeyword" select="lower-case($englishKeywordMixedCase)"/>
    <!-- Another option could be to add the annex info in the SKOS thesaurus using something
    like a related concept. -->
    <xsl:choose>
      <!-- annex i -->
      <xsl:when test="$englishKeyword='coordinate reference systems' or $englishKeyword='geographical grid systems'
                                    or $englishKeyword='geographical names' or $englishKeyword='administrative units'
                                    or $englishKeyword='addresses' or $englishKeyword='cadastral parcels'
                                    or $englishKeyword='transport networks' or $englishKeyword='hydrography'
                                    or $englishKeyword='protected sites'">
        <xsl:text>i</xsl:text>
      </xsl:when>
      <!-- annex ii -->
      <xsl:when test="$englishKeyword='elevation' or $englishKeyword='land cover'
                                    or $englishKeyword='orthoimagery' or $englishKeyword='geology'">
        <xsl:text>ii</xsl:text>
      </xsl:when>
      <!-- annex iii -->
      <xsl:when test="$englishKeyword='statistical units' or $englishKeyword='buildings'
                                    or $englishKeyword='soil' or $englishKeyword='land use'
                                    or $englishKeyword='human health and safety' or $englishKeyword='utility and governmental services'
                                    or $englishKeyword='environmental monitoring facilities' or $englishKeyword='production and industrial facilities'
                                    or $englishKeyword='agricultural and aquaculture facilities' or $englishKeyword='population distribution — demography'
                                    or $englishKeyword='area management/restriction/regulation zones and reporting units'
                                    or $englishKeyword='natural risk zones' or $englishKeyword='atmospheric conditions'
                                    or $englishKeyword='meteorological geographical features' or $englishKeyword='oceanographic geographical features'
                                    or $englishKeyword='sea regions' or $englishKeyword='bio-geographical regions'
                                    or $englishKeyword='habitats and biotopes' or $englishKeyword='species distribution'
                                    or $englishKeyword='energy resources' or $englishKeyword='mineral resources'">
        <xsl:text>iii</xsl:text>
      </xsl:when>
      <!-- inspire annex cannot be established: leave empty -->
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>