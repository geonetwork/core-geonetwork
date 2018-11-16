<?xml version="1.0" encoding="UTF-8"?>
<!--
  Jerico next migration process
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>

  <xsl:variable name="uuid"
                select="/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>


  <!-- Add INSPIRE theme block if not set already after the last keyword block. -->
  <xsl:variable name="hasInspireThemeSection"
                select="count(//gmd:descriptiveKeywords[
    .//gmd:code/*/text() = 'geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme'
    ]) > 0"/>

  <xsl:variable name="hasInspireThemeOGF"
                select="count(//gmd:descriptiveKeywords[
    .//gmd:code/*/text() = 'geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme'
     and .//gmd:keyword/* = 'Oceanographic geographical features']) > 0"/>


  <!-- INSPIRE Theme section does not exist, insert it with default theme. -->
  <xsl:template
    match="gmd:descriptiveKeywords[$hasInspireThemeSection = false() and
                                  name(following-sibling::*[1]) != 'gmd:descriptiveKeywords']">
    <xsl:copy-of select="."/>

    <gmd:descriptiveKeywords>           
      <gmd:MD_Keywords>
        <gmd:keyword>    
          <gco:CharacterString>Oceanographic geographical features</gco:CharacterString>   
        </gmd:keyword>
        <gmd:type>     
          <gmd:MD_KeywordTypeCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>      
        </gmd:type>
        <gmd:thesaurusName>        
          <gmd:CI_Citation>     
            <gmd:title>              
              <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>           
            </gmd:title>
            <gmd:date>             
              <gmd:CI_Date>                    
                <gmd:date>                    
                  <gco:Date>2008-06-01</gco:Date>                  
                </gmd:date>
                <gmd:dateType>                    
                  <gmd:CI_DateTypeCode
                    codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>              
                </gmd:dateType>
              </gmd:CI_Date>   
            </gmd:date>    
            <gmd:identifier>              
              <gmd:MD_Identifier>              
                <gmd:code>              
                  <gmx:Anchor
                    xlink:href="https://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.theme.httpinspireeceuropaeutheme-theme">
                    geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme
                  </gmx:Anchor>                  
                </gmd:code>        
              </gmd:MD_Identifier>       
            </gmd:identifier>
          </gmd:CI_Citation>
        </gmd:thesaurusName>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>

  <!-- INSPIRE Theme section exist, but OGF is not set, adding it. -->
  <xsl:template
    match="gmd:descriptiveKeywords/gmd:MD_Keywords[
                $hasInspireThemeOGF = false()
                and .//gmd:code/*/text() = 'geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme']">
    <xsl:copy>
      <!-- Copy existing non null theme -->
      <xsl:apply-templates select="gmd:keyword[not(@gco:nilReason)]"/>
      <!-- Add OGF -->
      <gmd:keyword>    
        <gco:CharacterString>Oceanographic geographical features</gco:CharacterString>   
      </gmd:keyword>

      <xsl:apply-templates select="gmd:type|gmd:thesaurusName"/>
    </xsl:copy>
  </xsl:template>


  <!-- Add lineage -->
  <xsl:template match="gmd:lineage/*/gmd:statement">
    <xsl:copy>
      <gco:CharacterString>not available</gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata[not(gmd:dataQualityInfo)]">
    <xsl:message><xsl:value-of select="$uuid"/> No DQ section, statement not updated.</xsl:message>
  </xsl:template>


  <!-- Remove empty scale denominator -->
  <xsl:template match="gmd:spatialResolution[*/gmd:equivalentScale/*/gmd:denominator/gco:Integer = '']"/>
  <xsl:template match="gmd:spatialResolution[count(*/gmd:equivalentScale/*) = 0]"/>


  <!-- Add metadata contact -->
  <xsl:variable name="hasFredKaanAlready"
                select="count(.//gmd:MD_Metadata/gmd:contact[*/gmd:individualName/gco:CharacterString = 'Fred Kaan']) > 0"/>
  <xsl:template match="gmd:MD_Metadata/gmd:contact[not($hasFredKaanAlready) and name(following-sibling::*[1]) != 'gmd:contact']">
    <xsl:copy-of select="."/>
    <gmd:contact>
      <gmd:CI_ResponsibleParty>
        <gmd:individualName>
          <gco:CharacterString>Fred Kaan</gco:CharacterString>
        </gmd:individualName>
        <gmd:organisationName>
          <gco:CharacterString>MARIS</gco:CharacterString>
        </gmd:organisationName>
        <gmd:contactInfo>
          <gmd:CI_Contact> 
            <gmd:address>      
              <gmd:CI_Address>           
                <gmd:electronicMailAddress>           
                  <gco:CharacterString>info@maris.nl</gco:CharacterString>
                </gmd:electronicMailAddress>
              </gmd:CI_Address>
            </gmd:address>
          </gmd:CI_Contact>
        </gmd:contactInfo>
        <gmd:role>
          <gmd:CI_RoleCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode"
            codeListValue="pointOfContact"/>
        </gmd:role>
      </gmd:CI_ResponsibleParty>
    </gmd:contact>
  </xsl:template>

  <!-- Replace resource constraints bloc -->
  <xsl:template match="gmd:resourceConstraints" priority="2">
    <gmd:resourceConstraints>
      <gmd:MD_LegalConstraints>
        <gmd:useLimitation>
          <gco:CharacterString>Creative Commons license to apply : Attribution (BY) : http://creativecommons.org/licenses/?lang=en
          </gco:CharacterString>
        </gmd:useLimitation>
        <gmd:accessConstraints>
          <gmd:MD_RestrictionCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
            codeListValue="copyright"/>
        </gmd:accessConstraints>
        <gmd:useConstraints>
          <gmd:MD_RestrictionCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
            codeListValue="copyright"/>
        </gmd:useConstraints>
        <gmd:otherConstraints gco:nilReason="missing">
          <gco:CharacterString/>
        </gmd:otherConstraints>
      </gmd:MD_LegalConstraints>
    </gmd:resourceConstraints>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
