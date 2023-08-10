<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:import href="../layout/utility-fn.xsl"/>


  <!-- Thumbnail base url (mandatory) -->
  <xsl:param name="thumbnail_url"/>
  <!-- Element to use for the file name. -->
  <xsl:param name="thumbnail_desc" select="''"/>
  <xsl:param name="thumbnail_type" select="''"/>

  <!-- Target element to update. The key is based on the concatenation
  of URL+Name -->
  <xsl:param name="updateKey"/>

  <xsl:variable name="separator" select="'\|'"/>

  <xsl:variable name="mainLang"
                select="/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"
                as="xs:string"/>

  <xsl:variable name="useOnlyPTFreeText"
                select="count(//*[lan:PT_FreeText and not(gco:CharacterString)]) > 0"
                as="xs:boolean"/>


  <xsl:template match="mri:MD_DataIdentification|
                      *[@gco:isoType='mri:MD_DataIdentification']|
                      srv:SV_ServiceIdentification">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mri:citation"/>
      <xsl:apply-templates select="mri:abstract"/>
      <xsl:apply-templates select="mri:purpose"/>
      <xsl:apply-templates select="mri:credit"/>
      <xsl:apply-templates select="mri:status"/>
      <xsl:apply-templates select="mri:pointOfContact"/>
      <xsl:apply-templates select="mri:spatialRepresentationType"/>
      <xsl:apply-templates select="mri:spatialResolution"/>
      <xsl:apply-templates select="mri:temporalResolution"/>
      <xsl:apply-templates select="mri:topicCategory"/>
      <xsl:apply-templates select="mri:extent"/>
      <xsl:apply-templates select="mri:additionalDocumentation"/>
      <xsl:apply-templates select="mri:processingLevel"/>
      <xsl:apply-templates select="mri:resourceMaintenance"/>

      <xsl:if test="$updateKey = ''">
        <xsl:call-template name="fill"/>
      </xsl:if>

      <xsl:apply-templates select="mri:graphicOverview"/>

      <xsl:apply-templates select="mri:resourceFormat"/>
      <xsl:apply-templates select="mri:descriptiveKeywords"/>
      <xsl:apply-templates select="mri:resourceSpecificUsage"/>
      <xsl:apply-templates select="mri:resourceConstraints"/>
      <xsl:apply-templates select="mri:associatedResource"/>

      <xsl:apply-templates select="mri:defaultLocale"/>
      <xsl:apply-templates select="mri:otherLocale"/>
      <xsl:apply-templates select="mri:environmentDescription"/>
      <xsl:apply-templates select="mri:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="mri:graphicOverview[concat(
                        */mcc:fileName/gco:CharacterString,
                        */mcc:fileDescription/gco:CharacterString) = normalize-space($updateKey)]">
    <xsl:call-template name="fill"/>
  </xsl:template>


    <xsl:template name="fill">
    <xsl:if test="$thumbnail_url != ''">
      <mri:graphicOverview>
        <mcc:MD_BrowseGraphic>
          <mcc:fileName>
            <xsl:choose>
              <!--Multilingual-->
              <xsl:when test="contains($thumbnail_url, '|')">
                <xsl:for-each select="tokenize($thumbnail_url, $separator)">
                  <xsl:variable name="nameLang"
                                select="substring-before(., '#')"></xsl:variable>
                  <xsl:variable name="nameValue"
                                select="substring-after(., '#')"></xsl:variable>

                  <xsl:if test="$useOnlyPTFreeText = false() and $nameLang = $mainLang">
                    <gco:CharacterString>
                      <xsl:value-of select="$nameValue"/>
                    </gco:CharacterString>
                  </xsl:if>
                </xsl:for-each>

                <lan:PT_FreeText>
                  <xsl:for-each select="tokenize($thumbnail_url, $separator)">
                    <xsl:variable name="nameLang"
                                  select="substring-before(., '#')"></xsl:variable>
                    <xsl:variable name="nameValue"
                                  select="substring-after(., '#')"></xsl:variable>

                    <xsl:if test="$useOnlyPTFreeText = true() or
                                      $nameLang != $mainLang">
                      <lan:textGroup>
                        <lan:LocalisedCharacterString locale="{concat('#', $nameLang)}">
                          <xsl:value-of select="$nameValue"/>
                        </lan:LocalisedCharacterString>
                      </lan:textGroup>
                    </xsl:if>
                  </xsl:for-each>
                </lan:PT_FreeText>
              </xsl:when>
              <xsl:otherwise>
                <gco:CharacterString>
                  <xsl:value-of select="$thumbnail_url"/>
                </gco:CharacterString>
              </xsl:otherwise>
            </xsl:choose>
          </mcc:fileName>
          <xsl:if test="$thumbnail_desc!=''">
            <mcc:fileDescription>
              <xsl:copy-of
                      select="gn-fn-iso19115-3.2018:fillTextElement($thumbnail_desc, $mainLang, $useOnlyPTFreeText)"/>
            </mcc:fileDescription>
          </xsl:if>
          <xsl:if test="$thumbnail_type!=''">
            <mcc:fileType>
              <gco:CharacterString>
                <xsl:value-of select="$thumbnail_type"/>
              </gco:CharacterString>
            </mcc:fileType>
          </xsl:if>
        </mcc:MD_BrowseGraphic>
      </mri:graphicOverview>
    </xsl:if>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>
</xsl:stylesheet>
