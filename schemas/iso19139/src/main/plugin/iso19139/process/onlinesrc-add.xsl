<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
attached it to the metadata for data.
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:date="http://exslt.org/dates-and-times">

	<!-- ============================================================================= -->

	<xsl:param name="uuidref"/>
	<xsl:param name="extra_metadata_uuid"/>
	<xsl:param name="protocol" select="'WWW:LINK-1.0-http--link'"/>
	<xsl:param name="url"/>
	<xsl:param name="name"/>
	<xsl:param name="desc"/>
	
	<!-- ============================================================================= -->

	<xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:variable name="mainLang">
      <xsl:value-of select="gmd:language/gmd:LanguageCode/@codeListValue" />
    </xsl:variable>

    <xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of
				select="gmd:fileIdentifier|
		    gmd:language|
		    gmd:characterSet|
		    gmd:parentIdentifier|
		    gmd:hierarchyLevel|
		    gmd:hierarchyLevelName|
		    gmd:contact|
		    gmd:dateStamp|
		    gmd:metadataStandardName|
		    gmd:metadataStandardVersion|
		    gmd:dataSetURI|
		    gmd:locale|
		    gmd:spatialRepresentationInfo|
		    gmd:referenceSystemInfo|
		    gmd:metadataExtensionInfo|
		    gmd:identificationInfo|
		    gmd:contentInfo"/>

      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <xsl:copy-of
            select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat"/>
          <xsl:copy-of
            select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor"/>
          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:unitsOfDistribution"/>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:transferSize"/>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:onLine"/>


              <!-- Add all online source from the target metadata to the
              current one -->
              <xsl:if test="//extra">
                <xsl:for-each select="//extra//gmd:onLine">
                  <gmd:onLine>
                    <xsl:if test="$extra_metadata_uuid">
                      <xsl:attribute name="uuidref" select="$extra_metadata_uuid"/>
                    </xsl:if>
                    <xsl:copy-of select="*"/>
                  </gmd:onLine>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$url">

                <!-- In case the protocol is an OGC protocol
                the name parameter may contains a list of layers
                separated by comma.
                In that case on one online element is added per
                layer/featureType.
                -->
                <xsl:choose>
                  <xsl:when test="starts-with($protocol, 'OGC:')">

                    <xsl:for-each select="tokenize($name, ',')">
                      <xsl:variable name="pos" select="position()"/>
                      <gmd:onLine>
                        <xsl:if test="$uuidref">
                          <xsl:attribute name="uuidref" select="$uuidref"/>
                        </xsl:if>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="$url"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>
                              <xsl:value-of select="$protocol"/>
                            </gco:CharacterString>
                          </gmd:protocol>

                          <xsl:if test=". != ''">
                            <gmd:name>
                              <gco:CharacterString>
                                <xsl:value-of select="."/>
                              </gco:CharacterString>
                            </gmd:name>
                          </xsl:if>

                          <xsl:if test="tokenize($desc, ',')[position() = $pos] != ''">
                            <gmd:description>
                              <gco:CharacterString>
                                <xsl:value-of select="tokenize($desc, ',')[position() = $pos]"/>
                              </gco:CharacterString>
                            </gmd:description>
                          </xsl:if>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- ... the name is simply added in the newly
                    created online element. -->

                    <xsl:variable name="separator" select="'\|'"/>
                    <xsl:variable name="useOnlyPTFreeText">
                      <xsl:value-of select="count(//*[gmd:PT_FreeText and not(gco:CharacterString)]) > 0" />
                    </xsl:variable>

                    <gmd:onLine>
                      <xsl:if test="$uuidref">
                        <xsl:attribute name="uuidref" select="$uuidref"/>
                      </xsl:if>
                      <gmd:CI_OnlineResource>
                        <gmd:linkage>
                          <gmd:URL>
                            <xsl:value-of select="$url"/>
                          </gmd:URL>
                        </gmd:linkage>

                        <xsl:if test="$protocol != ''">
                          <gmd:protocol>
                            <gco:CharacterString>
                              <xsl:value-of select="$protocol"/>
                            </gco:CharacterString>
                          </gmd:protocol>
                        </xsl:if>

                        <xsl:if test="$name != ''">
                          <gmd:name>
                            <xsl:choose>

                              <!--Multilingual-->
                              <xsl:when test="contains($name, '|')">
                                <xsl:for-each select="tokenize($name, $separator)">
                                  <xsl:variable name="nameLang" select="substring-before(., '#')"></xsl:variable>
                                  <xsl:variable name="nameValue" select="substring-after(., '#')"></xsl:variable>
                                  <xsl:if test="$useOnlyPTFreeText = 'false' and $nameLang = $mainLang">
                                    <gco:CharacterString>
                                      <xsl:value-of select="$nameValue"/>
                                    </gco:CharacterString>
                                  </xsl:if>
                                </xsl:for-each>

                                <gmd:PT_FreeText>
                                  <xsl:for-each select="tokenize($name, $separator)">
                                    <xsl:variable name="nameLang" select="substring-before(., '#')"></xsl:variable>
                                    <xsl:variable name="nameValue" select="substring-after(., '#')"></xsl:variable>

                                    <xsl:if test="$useOnlyPTFreeText = 'true' or $nameLang != $mainLang">
                                      <gmd:textGroup>
                                        <gmd:LocalisedCharacterString locale="{concat('#', $nameLang)}"><xsl:value-of select="$nameValue" /></gmd:LocalisedCharacterString>
                                      </gmd:textGroup>
                                    </xsl:if>

                                  </xsl:for-each>
                                </gmd:PT_FreeText>
                              </xsl:when>
                              <xsl:otherwise>
                                <gco:CharacterString>
                                  <xsl:value-of select="$name"/>
                                </gco:CharacterString>
                              </xsl:otherwise>
                            </xsl:choose>
                          </gmd:name>
                        </xsl:if>

                        <xsl:if test="$desc != ''">
                          <gmd:description>
                            <xsl:choose>
                              <xsl:when test="contains($desc, '|')">
                                <xsl:for-each select="tokenize($desc, $separator)">
                                  <xsl:variable name="descLang" select="substring-before(., '#')"></xsl:variable>
                                  <xsl:variable name="descValue" select="substring-after(., '#')"></xsl:variable>
                                  <xsl:if test="$useOnlyPTFreeText = 'false' and $descLang = $mainLang">
                                    <gco:CharacterString>
                                      <xsl:value-of select="$descValue"/>
                                    </gco:CharacterString>
                                  </xsl:if>
                                </xsl:for-each>

                                <gmd:PT_FreeText>
                                  <xsl:for-each select="tokenize($desc, $separator)">
                                    <xsl:variable name="descLang" select="substring-before(., '#')"></xsl:variable>
                                    <xsl:variable name="descValue" select="substring-after(., '#')"></xsl:variable>
                                      <xsl:if test="$useOnlyPTFreeText = 'true' or $descLang != $mainLang">
                                        <gmd:textGroup>
                                          <gmd:LocalisedCharacterString locale="{concat('#', $descLang)}"><xsl:value-of select="$descValue" /></gmd:LocalisedCharacterString>
                                        </gmd:textGroup>
                                      </xsl:if>
                                  </xsl:for-each>
                                </gmd:PT_FreeText>
                              </xsl:when>
                              <xsl:otherwise>
                                <gco:CharacterString>
                                  <xsl:value-of select="$desc"/>
                                </gco:CharacterString>
                              </xsl:otherwise>
                            </xsl:choose>
                          </gmd:description>
                        </xsl:if>
                        <!-- TODO may be relevant to add the function -->
                      </gmd:CI_OnlineResource>
                    </gmd:onLine>
                  </xsl:otherwise>
                </xsl:choose>


              </xsl:if>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:offLine"
              />
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
          <xsl:copy-of
            select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[position() > 1]"
          />
        </gmd:MD_Distribution>

      </gmd:distributionInfo>
			
			<xsl:copy-of
				select="gmd:dataQualityInfo|
			gmd:portrayalCatalogueInfo|
			gmd:metadataConstraints|
			gmd:applicationSchemaInfo|
			gmd:metadataMaintenance|
			gmd:series|
			gmd:describes|
			gmd:propertyType|
			gmd:featureType|
			gmd:featureAttribute"/>
			
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
