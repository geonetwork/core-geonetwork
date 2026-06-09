<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wmc="http://www.opengis.net/context"
                xmlns:wmc11="http://www.opengeospatial.net/context"
                xmlns:ows-context="http://www.opengis.net/ows-context"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"></xsl:param>
  <xsl:param name="viewer_url"></xsl:param>
  <xsl:param name="title"></xsl:param>
  <xsl:param name="abstract"></xsl:param>
  <xsl:param name="map_url"></xsl:param>

  <!-- These are provided by the ImportWmc.java jeeves service -->
  <xsl:param name="currentuser_name"></xsl:param>
  <xsl:param name="currentuser_phone"></xsl:param>
  <xsl:param name="currentuser_mail"></xsl:param>
  <xsl:param name="currentuser_org"></xsl:param>


  <xsl:include href="./resp-party.xsl"/>
  <xsl:include href="./identification.xsl"/>


  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:variable name="isOws" select="count(//ows-context:OWSContext) > 0"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="wmc:ViewContext|wmc11:ViewContext|ows-context:OWSContext">
    <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd"
                     xmlns:gco="http://www.isotc211.org/2005/gco"
    >
      <!-- <fileIdentifier/>  Will be set by UFO -->

      <gmd:language>
        <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                           codeListValue="{$lang}"/>
        <!-- English is default. Not available in Web Map Context or OWS. Selected by user from GUI -->
      </gmd:language>

      <gmd:characterSet>
        <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                                 codeListValue="utf8"/>
      </gmd:characterSet>

      <gmd:hierarchyLevel>
        <gmd:MD_ScopeCode
          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"
          codeListValue="dataset"/>
      </gmd:hierarchyLevel>

      <xsl:for-each select="wmc:General/wmc:ContactInformation|
                            wmc11:General/wmc11:ContactInformation|
                            ows-context:General/ows:ServiceProvider">
        <gmd:contact>
          <gmd:CI_ResponsibleParty>
            <xsl:apply-templates select="." mode="RespParty"/>
          </gmd:CI_ResponsibleParty>
        </gmd:contact>
      </xsl:for-each>

      <!--  Assign a specific user with the info provided by the webservice -->
      <xsl:if test="$currentuser_name != ''">
        <gmd:contact>
          <xsl:call-template name="build-current-user"/>
        </gmd:contact>
      </xsl:if>

      <gmd:dateStamp>
        <gco:DateTime>
          <xsl:value-of select="format-dateTime(current-dateTime(), $df)"/>
        </gco:DateTime>
      </gmd:dateStamp>

      <gmd:metadataStandardName>
        <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
      </gmd:metadataStandardName>

      <gmd:metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </gmd:metadataStandardVersion>

      <gmd:referenceSystemInfo>
        <gmd:MD_ReferenceSystem>
          <gmd:referenceSystemIdentifier>
            <gmd:RS_Identifier>
              <gmd:code>
                <gco:CharacterString>
                  <xsl:value-of select="
                    wmc:General/wmc:BoundingBox/@SRS|
                    wmc11:General/wmc11:BoundingBox/@SRS|
                    ows-context:General/ows:BoundingBox/@crs"/>
                </gco:CharacterString>
              </gmd:code>
            </gmd:RS_Identifier>
          </gmd:referenceSystemIdentifier>
        </gmd:MD_ReferenceSystem>
      </gmd:referenceSystemInfo>

      <gmd:identificationInfo>
        <gmd:MD_DataIdentification>
          <xsl:apply-templates select="." mode="DataIdentification">
            <xsl:with-param name="topic">
              <xsl:value-of select="$topic"/>
            </xsl:with-param>
            <xsl:with-param name="lang">
              <xsl:value-of select="$lang"/>
            </xsl:with-param>
          </xsl:apply-templates>
          <!--  extracts the extent (if not 4326, need to reproject) -->
          <gmd:extent>
            <gmd:EX_Extent>
              <gmd:geographicElement>
                <xsl:apply-templates select=".//*:BoundingBox" mode="BoundingBox"/>
              </gmd:geographicElement>
            </gmd:EX_Extent>
          </gmd:extent>
        </gmd:MD_DataIdentification>
      </gmd:identificationInfo>

      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <gmd:distributionFormat>
            <gmd:MD_Format>
              <gmd:name>
                <gco:CharacterString>OGC:OWS-C</gco:CharacterString>
              </gmd:name>
              <gmd:version gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:version>
            </gmd:MD_Format>
          </gmd:distributionFormat>
          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>

              <!-- Add link to the map -->
              <xsl:if test="$map_url != ''">
                <gmd:onLine>
                  <gmd:CI_OnlineResource>
                    <gmd:linkage>
                      <gmd:URL>
                        <xsl:value-of select="$map_url"/>
                      </gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="if ($isOws) then 'OGC:OWS-C' else 'OGC:WMC'"/>
                      </gco:CharacterString>
                    </gmd:protocol>
                    <gmd:name>
                      <gco:CharacterString>
                        <xsl:value-of
                          select="wmc:General/wmc:Title|wmc11:General/wmc11:Title|ows-context:General/ows:Title"/>
                      </gco:CharacterString>
                    </gmd:name>
                  </gmd:CI_OnlineResource>
                </gmd:onLine>
              </xsl:if>

              <!-- -->
              <xsl:if test="$viewer_url != ''">
                <gmd:onLine>
                  <gmd:CI_OnlineResource>
                    <gmd:linkage>
                      <gmd:URL>
                        <xsl:value-of select="$viewer_url"/>
                      </gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                      <gco:CharacterString>WWW:LINK</gco:CharacterString>
                    </gmd:protocol>
                    <gmd:name>
                      <gco:CharacterString>
                        <xsl:value-of select="wmc:General/wmc:Title|
                          wmc11:General/wmc11:Title|
                          ows-context:General/ows:Title"/>
                      </gco:CharacterString>
                    </gmd:name>
                  </gmd:CI_OnlineResource>
                </gmd:onLine>
              </xsl:if>

              <xsl:for-each
                select="wmc:LayerList/wmc:Layer|ows-context:ResourceList/ows-context:Layer">
                <gmd:onLine>
                  <!-- iterates over the layers -->
                  <!-- Only first URL is used -->
                  <xsl:variable name="layerUrl"
                                select="wmc:Server/wmc:OnlineResource/@xlink:href|
                                        ows-context:Server[1]/ows-context:OnlineResource[1]/@xlink:href"/>
                  <!--  service="urn:ogc:serviceType:WMS">-->
                  <xsl:variable name="layerName" select="wmc:Name/text()|@name"/>
                  <xsl:variable name="layerTitle" select="wmc:Title/text()|ows:Title/text()"/>
                  <xsl:variable name="layerVersion" select="wmc:Server/@version"/>
                  <xsl:variable name="layerProtocol"
                                select="if (ows:Server/@service) then ows:Server/@service else 'OGC:WMS'"/>
                  <gmd:CI_OnlineResource>
                    <gmd:linkage>
                      <gmd:URL>
                        <xsl:value-of select="$layerUrl"/>
                      </gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="$layerProtocol"/>
                      </gco:CharacterString>
                    </gmd:protocol>
                    <gmd:name>
                      <gco:CharacterString>
                        <xsl:value-of select="$layerName"/>
                      </gco:CharacterString>
                    </gmd:name>
                    <gmd:description>
                      <gco:CharacterString>
                        <xsl:value-of select="$layerTitle"/>
                      </gco:CharacterString>
                    </gmd:description>
                  </gmd:CI_OnlineResource>
                </gmd:onLine>
              </xsl:for-each>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
        </gmd:MD_Distribution>
      </gmd:distributionInfo>
      <gmd:dataQualityInfo>
        <gmd:DQ_DataQuality>
          <gmd:scope>
            <gmd:DQ_Scope>
              <gmd:level>
                <gmd:MD_ScopeCode codeListValue="dataset"
                                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"/>
              </gmd:level>
            </gmd:DQ_Scope>
          </gmd:scope>
          <gmd:lineage>
            <gmd:LI_Lineage>
              <gmd:statement gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:statement>
              <xsl:for-each
                select="wmc:LayerList/wmc:Layer|ows-context:ResourceList/ows-context:Layer">
                <!-- Would be good to add link to metadata using uuidref="" -->
                <gmd:source
                  xlink:href="{wmc:MetadataURL/wmc:OnlineResource/@xlink:href|
                                 ows-context:MetadataURL/ows-context:OnlineResource/@xlink:href}"/>
              </xsl:for-each>
            </gmd:LI_Lineage>
          </gmd:lineage>
        </gmd:DQ_DataQuality>
      </gmd:dataQualityInfo>
    </gmd:MD_Metadata>
  </xsl:template>


  <xsl:template match="wmc:BoundingBox|ows:BoundingBox" mode="BoundingBox">
    <xsl:variable name="minx"
                  select="if (ows:LowerCorner) then tokenize(ows:LowerCorner, ' ')[1] else string(./@minx)"/>
    <xsl:variable name="miny"
                  select="if (ows:LowerCorner) then tokenize(ows:LowerCorner, ' ')[2] else string(./@miny)"/>
    <xsl:variable name="maxx"
                  select="if (ows:UpperCorner) then tokenize(ows:UpperCorner, ' ')[1] else string(./@maxx)"/>
    <xsl:variable name="maxy"
                  select="if (ows:UpperCorner) then tokenize(ows:UpperCorner, ' ')[2] else string(./@maxy)"/>
    <xsl:variable name="fromEpsg" select="if (@crs) then string(@crs) else string(./@SRS)"/>
    <xsl:variable name="reprojected"
                  select="java:reprojectCoords($minx,$miny,$maxx,$maxy,$fromEpsg)"/>
    <xsl:copy-of select="saxon:parse($reprojected)"/>
  </xsl:template>

  <xsl:template name="build-current-user">
    <gmd:CI_ResponsibleParty>
      <gmd:individualName>
        <gco:CharacterString>
          <xsl:value-of select="$currentuser_name"/>
        </gco:CharacterString>
      </gmd:individualName>
      <gmd:organisationName>
        <gco:CharacterString>
          <xsl:value-of select="$currentuser_org"/>
        </gco:CharacterString>
      </gmd:organisationName>
      <gmd:contactInfo>
        <gmd:CI_Contact>
          <!--<gmd:phone>
            <gmd:CI_Telephone>
              <gmd:voice>
                <gco:CharacterString>
                  <xsl:value-of select="$currentuser_phone" />
                </gco:CharacterString>
              </gmd:voice>
            </gmd:CI_Telephone>
          </gmd:phone>-->
          <gmd:address>
            <gmd:CI_Address>
              <gmd:electronicMailAddress>
                <gco:CharacterString>
                  <xsl:value-of select="$currentuser_mail"/>
                </gco:CharacterString>
              </gmd:electronicMailAddress>
            </gmd:CI_Address>
          </gmd:address>
        </gmd:CI_Contact>
      </gmd:contactInfo>
      <gmd:role>
        <gmd:CI_RoleCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode"
                codeListValue="author"/>
      </gmd:role>
    </gmd:CI_ResponsibleParty>
  </xsl:template>
</xsl:stylesheet>
