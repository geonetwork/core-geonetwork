<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="/record">
        <xsl:apply-templates select="/record"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/record|*">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="if ((id)[1] and string-length((id)[1]) > 0) then (id)[1] else generate-id()"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="{java-xsl-util:threeCharLangCode(
                                (language)[1])}"/>
          </lan:language>
          <mdb:hierarchyLevel>
            <mdb:MD_ScopeCode codeListValue="series" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode"/>
          </mdb:hierarchyLevel>
          <mdb:hierarchyLevelName>
            <gco:CharacterString>Data collection</gco:CharacterString>
          </mdb:hierarchyLevelName>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                     codeListValue="utf8"/>
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>

      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <mcc:MD_ScopeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
              codeListValue="series"/>
          </mdb:resourceScope>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>
      <!-- Process contacts and providers for metadata contacts -->
      <xsl:choose>
        <xsl:when test="contacts or contact or providers">
          <xsl:for-each select="contacts">
            <xsl:call-template name="map-stac-contact">
              <xsl:with-param name="contactNode" select="."/>
            </xsl:call-template>
          </xsl:for-each>

          <xsl:if test="contact">
            <xsl:call-template name="map-stac-contact">
              <xsl:with-param name="contactNode" select="contact"/>
            </xsl:call-template>
          </xsl:if>

          <xsl:if test="not(contacts) and not(contact) and count(providers) > 0">
            <xsl:for-each select="providers[1]">
              <mdb:contact>
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact"/>
                  </cit:role>
                  <cit:party>
                    <cit:CI_Organisation>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="name"/>
                        </gco:CharacterString>
                      </cit:name>
                      <xsl:if test="url">
                        <cit:contactInfo>
                          <cit:CI_Contact>
                            <cit:onlineResource>
                              <cit:CI_OnlineResource>
                                <cit:linkage>
                                  <gco:CharacterString><xsl:value-of select="url"/></gco:CharacterString>
                                </cit:linkage>
                              </cit:CI_OnlineResource>
                            </cit:onlineResource>
                          </cit:CI_Contact>
                        </cit:contactInfo>
                      </xsl:if>
                      <xsl:if test="description">
                        <cit:individual>
                          <cit:CI_Individual>
                            <cit:name>
                              <gco:CharacterString><xsl:value-of select="description"/></gco:CharacterString>
                            </cit:name>
                          </cit:CI_Individual>
                        </cit:individual>
                      </xsl:if>
                    </cit:CI_Organisation>
                  </cit:party>
                </cit:CI_Responsibility>
              </mdb:contact>
            </xsl:for-each>
          </xsl:if>

        </xsl:when>
      </xsl:choose>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <!-- Handle assets with role "thumbnail" -->
          <xsl:variable name="thumbnailAssets" select="assets/*[roles[contains(., 'thumbnail')]]"/>
          <xsl:if test="count($thumbnailAssets) > 0">
            <mri:graphicOverview>
              <mcc:MD_BrowseGraphic>
                <mcc:fileName>
                  <gco:CharacterString>
                    <xsl:value-of select="$thumbnailAssets[1]/href"/>
                  </gco:CharacterString>
                </mcc:fileName>
                <mcc:fileDescription>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="$thumbnailAssets[1]/title and $thumbnailAssets[1]/description">
                        <xsl:value-of select="concat($thumbnailAssets[1]/title, ' - ', $thumbnailAssets[1]/description)"/>
                      </xsl:when>
                      <xsl:when test="$thumbnailAssets[1]/title">
                        <xsl:value-of select="$thumbnailAssets[1]/title"/>
                      </xsl:when>
                      <xsl:when test="$thumbnailAssets[1]/description">
                        <xsl:value-of select="$thumbnailAssets[1]/description"/>
                      </xsl:when>
                      <xsl:otherwise>Preview image</xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:fileDescription>
                <mcc:fileType>
                  <gco:CharacterString>
                    <xsl:value-of select="$thumbnailAssets[1]/type"/>
                  </gco:CharacterString>
                </mcc:fileType>
              </mcc:MD_BrowseGraphic>
            </mri:graphicOverview>
          </xsl:if>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of select="if ((title)[1] and string-length((title)[1]) > 0) then (title)[1] 
                                        else if ((id)[1] and string-length((id)[1]) > 0) then concat('STAC Collection: ', (id)[1])
                                        else 'STAC Collection'"/>
                </gco:CharacterString>
              </cit:title>
            </cit:CI_Citation>
          </mri:citation>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="if ((description)[1] and string-length((description)[1]) > 0) 
                                    then (description)[1] 
                                    else 'STAC Collection harvested via STAC API'"/>
            </gco:CharacterString>
          </mri:abstract>

          <xsl:choose>
            <xsl:when test="contacts or contact or providers">
              <xsl:call-template name="map-stac-providers"/>
            </xsl:when>
          </xsl:choose>

          <mri:extent>
            <gex:EX_Extent>
              <xsl:choose>
                <xsl:when test="extent/spatial/bbox">
                  <xsl:for-each select="extent/spatial/bbox">
                    <gex:geographicElement>
                      <gex:EX_GeographicBoundingBox>
                        <gex:westBoundLongitude>
                          <gco:Decimal>
                            <xsl:value-of select="if (*[1] and *[1] != 'null') then *[1] else ''"/>
                          </gco:Decimal>
                        </gex:westBoundLongitude>
                        <gex:eastBoundLongitude>
                          <gco:Decimal>
                            <xsl:value-of select="if (*[3] and *[3] != 'null') then *[3] else ''"/>
                          </gco:Decimal>
                        </gex:eastBoundLongitude>
                        <gex:southBoundLatitude>
                          <gco:Decimal>
                            <xsl:value-of select="if (*[2] and *[2] != 'null') then *[2] else ''"/>
                          </gco:Decimal>
                        </gex:southBoundLatitude>
                        <gex:northBoundLatitude>
                          <gco:Decimal>
                            <xsl:value-of select="if (*[4] and *[4] != 'null') then *[4] else ''"/>
                          </gco:Decimal>
                        </gex:northBoundLatitude>
                      </gex:EX_GeographicBoundingBox>
                    </gex:geographicElement>
                  </xsl:for-each>
                </xsl:when>
              </xsl:choose>
              <xsl:if test="extent/temporal/interval">
                <xsl:for-each select="extent/temporal/interval">
                  <gex:temporalElement>
                    <gex:EX_TemporalExtent>
                      <gex:extent>
                        <gml:TimePeriod>
                          <gml:beginPosition>
                            <xsl:value-of select="if (*[1] and *[1] != 'null') then *[1] else ''"/>
                          </gml:beginPosition>
                          <gml:endPosition>
                            <xsl:value-of select="if (*[2] and *[2] != 'null') then *[2] else ''"/>
                          </gml:endPosition>
                        </gml:TimePeriod>
                      </gex:extent>
                    </gex:EX_TemporalExtent>
                  </gex:temporalElement>
                </xsl:for-each>
              </xsl:if>
            </gex:EX_Extent>
          </mri:extent>
          <xsl:variable name="keywords"
                        select="keywords"/>
          <xsl:if test="$keywords">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="$keywords">
                  <mri:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </mri:keyword>
                </xsl:for-each>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:if>

          <mri:resourceConstraints>
            <mco:MD_LegalConstraints>
              <mco:reference>
                <cit:CI_Citation>
                  <cit:title>
                    <gco:CharacterString>
                      <xsl:value-of select="license"/>
                    </gco:CharacterString>
                  </cit:title>
                </cit:CI_Citation>
              </mco:reference>
              <mco:otherConstraints>
                <gco:CharacterString>
                  <xsl:value-of select="license"/>
                </gco:CharacterString>
              </mco:otherConstraints>
            </mco:MD_LegalConstraints>
          </mri:resourceConstraints>

          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <xsl:for-each select="links[rel = 'self']">
            <mrd:onLine>
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <gco:CharacterString>
                    <xsl:value-of select="href"/>
                  </gco:CharacterString>
                </cit:linkage>
                <cit:protocol>
                  <gco:CharacterString>STAC</gco:CharacterString>
                </cit:protocol>
                <cit:name>
                  <gco:CharacterString>
                    <xsl:value-of select="title"/>
                  </gco:CharacterString>
                </cit:name>
                <cit:description>
                  <gco:CharacterString>STAC Collection</gco:CharacterString>
                </cit:description>
              </cit:CI_OnlineResource>
            </mrd:onLine>
          </xsl:for-each>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template name="map-stac-providers">
    <xsl:for-each select="providers">
      <mri:pointOfContact>
        <cit:CI_Responsibility>
          <cit:role>
            <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode">
              <xsl:attribute name="codeListValue">
                <xsl:value-of select="if (roles and roles[1]) then 
                                     (if (roles[1]='licensor') then 'owner'
                                      else if (roles[1]='producer') then 'originator'
                                      else if (roles[1]='processor') then 'processor'
                                      else if (roles[1]='publisher') then 'publisher'
                                      else if (roles[1]='host') then 'distributor'
                                      else 'resourceProvider')
                                    else 'resourceProvider'"/>
              </xsl:attribute>
            </cit:CI_RoleCode>
          </cit:role>
          <cit:party>
            <cit:CI_Organisation>
              <cit:name>
                <gco:CharacterString>
                  <xsl:value-of select="name"/>
                </gco:CharacterString>
              </cit:name>
              <xsl:if test="url">
                <cit:contactInfo>
                  <cit:CI_Contact>
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString><xsl:value-of select="url"/></gco:CharacterString>
                        </cit:linkage>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </cit:CI_Contact>
                </cit:contactInfo>
              </xsl:if>
              <xsl:if test="description">
                <cit:individual>
                  <cit:CI_Individual>
                    <cit:name>
                      <gco:CharacterString><xsl:value-of select="description"/></gco:CharacterString>
                    </cit:name>
                  </cit:CI_Individual>
                </cit:individual>
              </xsl:if>
            </cit:CI_Organisation>
          </cit:party>
        </cit:CI_Responsibility>
      </mri:pointOfContact>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="map-stac-contact">
    <xsl:param name="contactNode"/>
    
    <mdb:contact>
      <cit:CI_Responsibility>
        <cit:role>
          <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact"/>
        </cit:role>
        <cit:party>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <xsl:value-of select="if ($contactNode/organization) then $contactNode/organization else $contactNode/name"/>
              </gco:CharacterString>
            </cit:name>
            <cit:contactInfo>
              <cit:CI_Contact>
                <xsl:if test="$contactNode/emails and $contactNode/emails/value or $contactNode/email">
                  <cit:address>
                    <cit:CI_Address>
                      <xsl:if test="$contactNode/addresses">
                        <xsl:for-each select="$contactNode/addresses">
                          <xsl:for-each select="deliveryPoint">
                            <cit:deliveryPoint>
                              <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                            </cit:deliveryPoint>
                          </xsl:for-each>
                          <xsl:if test="city">
                            <cit:city>
                              <gco:CharacterString><xsl:value-of select="city"/></gco:CharacterString>
                            </cit:city>
                          </xsl:if>
                          <xsl:if test="administrativeArea">
                            <cit:administrativeArea>
                              <gco:CharacterString><xsl:value-of select="administrativeArea"/></gco:CharacterString>
                            </cit:administrativeArea>
                          </xsl:if>
                          <xsl:if test="postalCode">
                            <cit:postalCode>
                              <gco:CharacterString><xsl:value-of select="postalCode"/></gco:CharacterString>
                            </cit:postalCode>
                          </xsl:if>
                          <xsl:if test="country">
                            <cit:country>
                              <gco:CharacterString><xsl:value-of select="country"/></gco:CharacterString>
                            </cit:country>
                          </xsl:if>
                        </xsl:for-each>
                      </xsl:if>
                      
                      <cit:electronicMailAddress>
                        <gco:CharacterString>
                          <xsl:value-of select="if ($contactNode/emails and $contactNode/emails/value) 
                                                then $contactNode/emails[1]/value 
                                                else $contactNode/email"/>
                        </gco:CharacterString>
                      </cit:electronicMailAddress>
                    </cit:CI_Address>
                  </cit:address>
                </xsl:if>
                
                <xsl:if test="$contactNode/phones and $contactNode/phones/value">
                  <cit:phone>
                    <cit:CI_Telephone>
                      <cit:number>
                        <gco:CharacterString><xsl:value-of select="$contactNode/phones[1]/value"/></gco:CharacterString>
                      </cit:number>
                      <cit:numberType>
                        <cit:CI_TelephoneTypeCode codeList="codeListLocation#CI_TelephoneTypeCode" codeListValue="voice"/>
                      </cit:numberType>
                    </cit:CI_Telephone>
                  </cit:phone>
                </xsl:if>
                
                <xsl:if test="$contactNode/links">
                  <xsl:for-each select="$contactNode/links[1]">
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString><xsl:value-of select="href"/></gco:CharacterString>
                        </cit:linkage>
                        <xsl:if test="title">
                          <cit:name>
                            <gco:CharacterString><xsl:value-of select="title"/></gco:CharacterString>
                          </cit:name>
                        </xsl:if>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </xsl:for-each>
                </xsl:if>
              </cit:CI_Contact>
            </cit:contactInfo>
            
            <xsl:if test="$contactNode/name">
              <cit:individual>
                <cit:CI_Individual>
                  <cit:name>
                    <gco:CharacterString><xsl:value-of select="$contactNode/name"/></gco:CharacterString>
                  </cit:name>
                  <xsl:if test="$contactNode/positionName">
                    <cit:positionName>
                      <gco:CharacterString><xsl:value-of select="$contactNode/positionName"/></gco:CharacterString>
                    </cit:positionName>
                  </xsl:if>
                </cit:CI_Individual>
              </cit:individual>
            </xsl:if>
          </cit:CI_Organisation>
        </cit:party>
      </cit:CI_Responsibility>
    </mdb:contact>
  </xsl:template>
</xsl:stylesheet>
