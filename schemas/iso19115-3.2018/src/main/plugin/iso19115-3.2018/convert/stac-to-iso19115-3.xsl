<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <xsl:import href="protocol-mapping.xsl"/>
  <xsl:import href="odstheme-mapping.xsl"/>
  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/> 

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="/record">
        <xsl:apply-templates select="/record"/>
      </xsl:when>
      <xsl:when test="/*[local-name()='collections'] or /*[local-name()='collection']">
        <xsl:apply-templates select="/*"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/record|*">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:choose>
                <xsl:when test="(datasetid|dataset/dataset_id|id)[1] and string-length((datasetid|dataset/dataset_id|id)[1]) > 0">
                  <xsl:value-of select="(datasetid|dataset/dataset_id|id)[1]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="generate-id()"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="{java-xsl-util:threeCharLangCode(
                                (metas/language|dataset/metas/default/metadata_languages|language)[1])}"/>
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
          <!-- Determine if we have metadata contacts from STAC contacts extension -->
          <xsl:variable name="hasMetadataContacts" select="count(contacts) > 0 or contact"/>
          
          <!-- Process contacts list from STAC contacts extension -->
          <xsl:for-each select="contacts">
            <mdb:contact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:choose>
                          <xsl:when test="organization">
                            <xsl:value-of select="organization"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="name"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:contactInfo>
                      <cit:CI_Contact>
                        <xsl:if test="emails and emails/value">
                          <cit:address>
                            <cit:CI_Address>
                              <xsl:if test="addresses">
                                <xsl:for-each select="addresses">
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
                                <gco:CharacterString><xsl:value-of select="emails[1]/value"/></gco:CharacterString>
                              </cit:electronicMailAddress>
                            </cit:CI_Address>
                          </cit:address>
                        </xsl:if>
                        <xsl:if test="phones and phones/value">
                          <cit:phone>
                            <cit:CI_Telephone>
                              <cit:number>
                                <gco:CharacterString><xsl:value-of select="phones[1]/value"/></gco:CharacterString>
                              </cit:number>
                              <cit:numberType>
                                <cit:CI_TelephoneTypeCode codeList="codeListLocation#CI_TelephoneTypeCode" codeListValue="voice"/>
                              </cit:numberType>
                            </cit:CI_Telephone>
                          </cit:phone>
                        </xsl:if>
                        <xsl:if test="links">
                          <xsl:for-each select="links[1]">
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
                    <xsl:if test="name">
                      <cit:individual>
                        <cit:CI_Individual>
                          <cit:name>
                            <gco:CharacterString><xsl:value-of select="name"/></gco:CharacterString>
                          </cit:name>
                          <xsl:if test="positionName">
                            <cit:positionName>
                              <gco:CharacterString><xsl:value-of select="positionName"/></gco:CharacterString>
                            </cit:positionName>
                          </xsl:if>
                        </cit:CI_Individual>
                      </cit:individual>
                    </xsl:if>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mdb:contact>
          </xsl:for-each>
          
          <!-- Process single contact object from STAC if present -->
          <xsl:if test="contact">
            <mdb:contact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:choose>
                          <xsl:when test="contact/organization">
                            <xsl:value-of select="contact/organization"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="contact/name"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:contactInfo>
                      <cit:CI_Contact>
                        <xsl:if test="contact/emails and contact/emails/value">
                          <cit:address>
                            <cit:CI_Address>
                              <xsl:if test="contact/addresses">
                                <xsl:for-each select="contact/addresses">
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
                                <gco:CharacterString><xsl:value-of select="contact/emails[1]/value"/></gco:CharacterString>
                              </cit:electronicMailAddress>
                            </cit:CI_Address>
                          </cit:address>
                        </xsl:if>
                        <xsl:if test="contact/phones and contact/phones/value">
                          <cit:phone>
                            <cit:CI_Telephone>
                              <cit:number>
                                <gco:CharacterString><xsl:value-of select="contact/phones[1]/value"/></gco:CharacterString>
                              </cit:number>
                              <cit:numberType>
                                <cit:CI_TelephoneTypeCode codeList="codeListLocation#CI_TelephoneTypeCode" codeListValue="voice"/>
                              </cit:numberType>
                            </cit:CI_Telephone>
                          </cit:phone>
                        </xsl:if>
                        <xsl:if test="contact/links">
                          <xsl:for-each select="contact/links[1]">
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
                        <!-- Support for email as a direct string rather than an array -->
                        <xsl:if test="contact/email and not(contact/emails)">
                          <cit:address>
                            <cit:CI_Address>
                              <cit:electronicMailAddress>
                                <gco:CharacterString><xsl:value-of select="contact/email"/></gco:CharacterString>
                              </cit:electronicMailAddress>
                            </cit:CI_Address>
                          </cit:address>
                        </xsl:if>
                      </cit:CI_Contact>
                    </cit:contactInfo>
                    <xsl:if test="contact/name">
                      <cit:individual>
                        <cit:CI_Individual>
                          <cit:name>
                            <gco:CharacterString><xsl:value-of select="contact/name"/></gco:CharacterString>
                          </cit:name>
                          <xsl:if test="contact/positionName">
                            <cit:positionName>
                              <gco:CharacterString><xsl:value-of select="contact/positionName"/></gco:CharacterString>
                            </cit:positionName>
                          </xsl:if>
                        </cit:CI_Individual>
                      </cit:individual>
                    </xsl:if>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mdb:contact>
          </xsl:if>
          
          <!-- Process first provider as metadata contact if no specific metadata contacts -->
          <xsl:if test="not($hasMetadataContacts) and count(providers) > 0">
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
          
          <!-- Fallback: If no metadata contacts, providers, or single contact, use first contact from list as a default -->
          <xsl:if test="not($hasMetadataContacts) and count(providers) = 0 and count(contacts) > 0">
            <mdb:contact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:choose>
                          <xsl:when test="contacts[1]/organization">
                            <xsl:value-of select="contacts[1]/organization"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="contacts[1]/name"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gco:CharacterString>
                    </cit:name>
                    <xsl:if test="contacts[1]/emails and contacts[1]/emails/value">
                      <cit:contactInfo>
                        <cit:CI_Contact>
                          <cit:address>
                            <cit:CI_Address>
                              <cit:electronicMailAddress>
                                <gco:CharacterString><xsl:value-of select="contacts[1]/emails[1]/value"/></gco:CharacterString>
                              </cit:electronicMailAddress>
                            </cit:CI_Address>
                          </cit:address>
                        </cit:CI_Contact>
                      </cit:contactInfo>
                    </xsl:if>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mdb:contact>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <!-- Legacy/fallback contact processing when no contacts or providers are present -->
          <mdb:contact>
            <cit:CI_Responsibility>
              <cit:role>
                <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="publisher"/>
              </cit:role>
              <cit:party>
                <cit:CI_Organisation>
                  <cit:name>
                    <gco:CharacterString>
                      <xsl:value-of select="(metas/publisher|dataset/metas/default/publisher)[1]"/>
                    </gco:CharacterString>
                  </cit:name>
                  <cit:contactInfo>
                    <cit:CI_Contact>
                      <cit:address>
                        <cit:CI_Address>
                          <cit:electronicMailAddress>
                            <gco:CharacterString>
                              <xsl:value-of select="author_email"/>
                            </gco:CharacterString>
                          </cit:electronicMailAddress>
                        </cit:CI_Address>
                      </cit:address>
                    </cit:CI_Contact>
                  </cit:contactInfo>
                </cit:CI_Organisation>
              </cit:party>
            </cit:CI_Responsibility>
          </mdb:contact>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:call-template name="build-date">
        <xsl:with-param name="tag" select="'mdb:dateInfo'"/>
      </xsl:call-template>

      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

      <xsl:apply-templates select="dataset/metas/default/records_count"
                           mode="ods-to-iso"/>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <!-- Handle QL, ql, or thumbnail in assets -->
          <xsl:if test="assets/QL or assets/ql or assets/thumbnail">
            <mri:graphicOverview>
              <mcc:MD_BrowseGraphic>
                <mcc:fileName>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="assets/QL">
                        <xsl:value-of select="assets/QL/href"/>
                      </xsl:when>
                      <xsl:when test="assets/ql">
                        <xsl:value-of select="assets/ql/href"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="assets/thumbnail/href"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:fileName>
                <mcc:fileDescription>
                  <gco:CharacterString>
                    <xsl:choose>
                      <!-- Handle cases for uppercase QL -->
                      <xsl:when test="assets/QL/title and assets/QL/description">
                        <xsl:value-of select="concat(assets/QL/title, ' - ', assets/QL/description)"/>
                      </xsl:when>
                      <xsl:when test="assets/QL/title">
                        <xsl:value-of select="assets/QL/title"/>
                      </xsl:when>
                      <xsl:when test="assets/QL/description">
                        <xsl:value-of select="assets/QL/description"/>
                      </xsl:when>
                      <!-- Handle cases for lowercase ql -->
                      <xsl:when test="assets/ql/title and assets/ql/description">
                        <xsl:value-of select="concat(assets/ql/title, ' - ', assets/ql/description)"/>
                      </xsl:when>
                      <xsl:when test="assets/ql/title">
                        <xsl:value-of select="assets/ql/title"/>
                      </xsl:when>
                      <xsl:when test="assets/ql/description">
                        <xsl:value-of select="assets/ql/description"/>
                      </xsl:when>
                      <!-- Handle cases for thumbnail -->
                      <xsl:when test="assets/thumbnail/title and assets/thumbnail/description">
                        <xsl:value-of select="concat(assets/thumbnail/title, ' - ', assets/thumbnail/description)"/>
                      </xsl:when>
                      <xsl:when test="assets/thumbnail/title">
                        <xsl:value-of select="assets/thumbnail/title"/>
                      </xsl:when>
                      <xsl:when test="assets/thumbnail/description">
                        <xsl:value-of select="assets/thumbnail/description"/>
                      </xsl:when>
                      <xsl:otherwise>Preview image</xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:fileDescription>
                <mcc:fileType>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="assets/QL">
                        <xsl:value-of select="assets/QL/type"/>
                      </xsl:when>
                      <xsl:when test="assets/ql">
                        <xsl:value-of select="assets/ql/type"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="assets/thumbnail/type"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:fileType>
              </mcc:MD_BrowseGraphic>
            </mri:graphicOverview>
          </xsl:if>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:choose>
                    <xsl:when test="(metas/title|dataset/metas/default/title|title)[1] and 
                                   string-length((metas/title|dataset/metas/default/title|title)[1]) > 0">
                      <xsl:value-of select="(metas/title|dataset/metas/default/title|title)[1]"/>
                    </xsl:when>
                    <xsl:when test="(id|datasetid|dataset/dataset_id)[1] and 
                                   string-length((id|datasetid|dataset/dataset_id)[1]) > 0">
                      <xsl:value-of select="concat('STAC Collection: ', (id|datasetid|dataset/dataset_id)[1])"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="'STAC Collection'"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </gco:CharacterString>
              </cit:title>

              <xsl:call-template name="build-date">
                <xsl:with-param name="tag" select="'cit:date'"/>
              </xsl:call-template>

              <xsl:apply-templates select="dataset/dataset_id"
                                   mode="ods-to-iso"/>
              
              <xsl:call-template name="ensure-identifier"/>
              
            </cit:CI_Citation>
          </mri:citation>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:choose>
                <xsl:when test="(metas/description|dataset/metas/default/description|description)[1] and 
                               string-length((metas/description|dataset/metas/default/description|description)[1]) > 0">
                  <xsl:value-of select="(metas/description|dataset/metas/default/description|description)[1]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'STAC Collection harvested via STAC API'"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:CharacterString>
          </mri:abstract>

          <xsl:for-each select="dataset/metas/default/attributions[. != 'null']">
            <mri:credit>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mri:credit>
          </xsl:for-each>
          <!-- Process contacts and providers -->
          <xsl:choose>
            <xsl:when test="contacts or contact or providers">
              <!-- Process STAC providers -->
              <xsl:call-template name="map-stac-providers"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Fallback when no contacts or providers are available -->
              <xsl:if test="not(organization)">
                <mri:pointOfContact>
                  <cit:CI_Responsibility>
                    <cit:role>
                      <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator">publisher
                      </cit:CI_RoleCode>
                    </cit:role>
                    <cit:party>
                      <cit:CI_Organisation>
                        <cit:name>
                          <gco:CharacterString>
                            <xsl:value-of select="metas/publisher|dataset/metas/default/publisher"/>
                          </gco:CharacterString>
                        </cit:name>
                        <xsl:if test="author_email">
                          <cit:contactInfo>
                            <cit:CI_Contact>
                              <cit:address>
                                <cit:CI_Address>
                                  <cit:electronicMailAddress>
                                    <gco:CharacterString>
                                      <xsl:value-of select="author_email"/>
                                    </gco:CharacterString>
                                  </cit:electronicMailAddress>
                                </cit:CI_Address>
                              </cit:address>
                            </cit:CI_Contact>
                          </cit:contactInfo>
                        </xsl:if>
                      </cit:CI_Organisation>
                    </cit:party>
                  </cit:CI_Responsibility>
                </mri:pointOfContact>
              </xsl:if>
              <xsl:for-each select="organization">
                <mri:pointOfContact>
                  <cit:CI_Responsibility>
                    <cit:role>
                      <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact">pointOfContact
                      </cit:CI_RoleCode>
                    </cit:role>
                    <cit:party>
                      <cit:CI_Organisation>
                        <cit:name>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </cit:name>
                        <cit:contactInfo>
                          <cit:CI_Contact>
                            <cit:address>
                              <cit:CI_Address>
                                <cit:electronicMailAddress>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../author_email"/>
                                  </gco:CharacterString>
                                </cit:electronicMailAddress>
                              </cit:CI_Address>
                            </cit:address>
                          </cit:CI_Contact>
                        </cit:contactInfo>
                      </cit:CI_Organisation>
                    </cit:party>
                  </cit:CI_Responsibility>
                </mri:pointOfContact>
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>

          <xsl:apply-templates select="dataset/metas/dcat/creator"
                               mode="ods-to-iso"/>

          <xsl:variable name="odsThemes"
                        select="metas/theme|dataset/metas/default/theme"/>
          <xsl:if test="count($odsThemes) > 0">
            <xsl:for-each select="distinct-values($odsThemeToIsoTopic[theme = $odsThemes]/name())">
              <mri:topicCategory>
                <mri:MD_TopicCategoryCode>
                  <xsl:value-of select="."/>
                </mri:MD_TopicCategoryCode>
              </mri:topicCategory>
            </xsl:for-each>
          </xsl:if>

          <mri:extent>
            <gex:EX_Extent>
              <xsl:choose>
                <xsl:when test="extent/spatial/bbox">
                  <xsl:for-each select="extent/spatial/bbox">
                    <gex:geographicElement>
                      <gex:EX_GeographicBoundingBox>
                        <gex:westBoundLongitude>
                          <gco:Decimal>
                            <xsl:choose>
                              <xsl:when test="*[1] and *[1] != 'null'">
                                <xsl:value-of select="*[1]"/>
                              </xsl:when>
                              <xsl:otherwise>-180</xsl:otherwise>
                            </xsl:choose>
                          </gco:Decimal>
                        </gex:westBoundLongitude>
                        <gex:eastBoundLongitude>
                          <gco:Decimal>
                            <xsl:choose>
                              <xsl:when test="*[3] and *[3] != 'null'">
                                <xsl:value-of select="*[3]"/>
                              </xsl:when>
                              <xsl:otherwise>180</xsl:otherwise>
                            </xsl:choose>
                          </gco:Decimal>
                        </gex:eastBoundLongitude>
                        <gex:southBoundLatitude>
                          <gco:Decimal>
                            <xsl:choose>
                              <xsl:when test="*[2] and *[2] != 'null'">
                                <xsl:value-of select="*[2]"/>
                              </xsl:when>
                              <xsl:otherwise>-90</xsl:otherwise>
                            </xsl:choose>
                          </gco:Decimal>
                        </gex:southBoundLatitude>
                        <gex:northBoundLatitude>
                          <gco:Decimal>
                            <xsl:choose>
                              <xsl:when test="*[4] and *[4] != 'null'">
                                <xsl:value-of select="*[4]"/>
                              </xsl:when>
                              <xsl:otherwise>90</xsl:otherwise>
                            </xsl:choose>
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
                            <xsl:choose>
                              <xsl:when test="*[1] and *[1] != 'null'">
                                <xsl:value-of select="*[1]"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:attribute name="indeterminatePosition">unknown</xsl:attribute>
                              </xsl:otherwise>
                            </xsl:choose>
                          </gml:beginPosition>
                          <gml:endPosition>
                            <xsl:choose>
                              <xsl:when test="*[2] and *[2] != 'null'">
                                <xsl:value-of select="*[2]"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:attribute name="indeterminatePosition">now</xsl:attribute>
                              </xsl:otherwise>
                            </xsl:choose>
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
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString>
                          </gco:CharacterString>
                        </cit:linkage>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </cit:CI_Citation>
                </mco:reference>
                <mco:accessConstraints>
                  <mco:MD_RestrictionCode codeListValue="otherRestrictions"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                </mco:accessConstraints>
                <mco:useConstraints>
                  <mco:MD_RestrictionCode codeListValue="otherRestrictions"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                </mco:useConstraints>
                <mco:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="license"/>
                  </gco:CharacterString>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>

          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                  codeListValue="{java-xsl-util:threeCharLangCode((metas/language|dataset/metas/default/language)[1])}"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <xsl:if test="count(fields|dataset/fields) > 0">
        <mdb:contentInfo>
          <mrc:MD_FeatureCatalogue>
            <mrc:featureCatalogue>
              <gfc:FC_FeatureCatalogue>
                <gfc:producer></gfc:producer>
                <gfc:featureType>
                  <gfc:FC_FeatureType>
                    <gfc:typeName>
                      <xsl:value-of select="(metas/title|dataset/metas/default/title)[1]"/>
                    </gfc:typeName>
                    <gfc:isAbstract>
                      <gco:Boolean>false</gco:Boolean>
                    </gfc:isAbstract>
                    <xsl:for-each select="fields|dataset/fields">
                      <gfc:carrierOfCharacteristics>
                        <gfc:FC_FeatureAttribute>
                          <gfc:memberName>
                            <xsl:value-of select="name"/>
                          </gfc:memberName>
                          <gfc:definition>
                            <gco:CharacterString>
                              <xsl:value-of select="label"/>
                              <xsl:if test="description[. != 'null']">
                                -
                                <xsl:value-of select="description"/>
                              </xsl:if>
                            </gco:CharacterString>
                          </gfc:definition>
                          <gfc:cardinality>1</gfc:cardinality>
                          <gfc:valueType>
                            <gco:TypeName>
                              <gco:aName>
                                <gco:CharacterString>
                                  <xsl:value-of select="type"/>
                                </gco:CharacterString>
                              </gco:aName>
                            </gco:TypeName>
                          </gfc:valueType>
                        </gfc:FC_FeatureAttribute>
                      </gfc:carrierOfCharacteristics>
                    </xsl:for-each>
                    <gfc:featureCatalogue/>
                  </gfc:FC_FeatureType>
                </gfc:featureType>
              </gfc:FC_FeatureCatalogue>
            </mrc:featureCatalogue>
          </mrc:MD_FeatureCatalogue>
        </mdb:contentInfo>
      </xsl:if>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <xsl:for-each-group select="links/mimetype" group-by=".">
            <mrd:distributionFormat>
              <mrd:MD_Format>
                <mrd:formatSpecificationCitation>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:title>
                  </cit:CI_Citation>
                </mrd:formatSpecificationCitation>
              </mrd:MD_Format>
            </mrd:distributionFormat>
          </xsl:for-each-group>
          <xsl:if test="metas/records_count > 0">
            <xsl:call-template name="dataFormat">
              <xsl:with-param name="format">csv</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="dataFormat">
              <xsl:with-param name="format">json</xsl:with-param>
            </xsl:call-template>
            <xsl:if test="count(features[. = 'geo']) > 0">
              <xsl:call-template name="dataFormat">
                <xsl:with-param name="format">geojson</xsl:with-param>
              </xsl:call-template>
              <xsl:if test="metas/records_count &lt; 5000">
                <xsl:call-template name="dataFormat">
                  <xsl:with-param name="format">shapefile</xsl:with-param>
                </xsl:call-template>
              </xsl:if>
            </xsl:if>
          </xsl:if>


          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <xsl:for-each select="attachments|dataset/attachments">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="href"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="type"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="id"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>
                        <xsl:value-of select="title"/>
                      </gco:CharacterString>
                    </cit:description>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>
              
              <xsl:for-each select="links">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="href"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="rel"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="@title|title"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>
                        <xsl:value-of select="type"/>
                      </gco:CharacterString>
                    </cit:description>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

              <xsl:for-each select="assets/*">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="href"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="type"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="name(.)"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>
                        <xsl:value-of select="description|title"/>
                      </gco:CharacterString>
                    </cit:description>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

              <xsl:variable name="count"
                            select="metas/records_count|dataset/metas/default/records_count"/>
              <xsl:if test="$count > 0">
                <xsl:call-template name="dataLink">
                  <xsl:with-param name="format">csv</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="dataLink">
                  <xsl:with-param name="format">json</xsl:with-param>
                </xsl:call-template>
                <xsl:if test="count(.//features[. = 'geo']) > 0">
                  <xsl:call-template name="dataLink">
                    <xsl:with-param name="format">geojson</xsl:with-param>
                  </xsl:call-template>
                  <xsl:if test="$count &lt; 5000">
                    <xsl:call-template name="dataLink">
                      <xsl:with-param name="format">shp</xsl:with-param>
                    </xsl:call-template>
                  </xsl:if>
                </xsl:if>
              </xsl:if>

            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>

              <xsl:for-each select="dataset/metas/default/references[. != 'null']">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        WWW:LINK
                      </gco:CharacterString>
                    </cit:protocol>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>
            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>

      <mdb:resourceLineage>
        <mrl:LI_Lineage>
          <mrl:statement>
            <gco:CharacterString>
              <xsl:value-of select="dataset/metas/dcat/dataquality[. != 'null']"/>
            </gco:CharacterString>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                  codeListValue="series"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>
        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template match="*[local-name()='collections']">
    <xsl:apply-templates select="*[local-name()='collections']/*"/>
  </xsl:template>

  <xsl:template match="*[local-name()='collection']">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <xsl:apply-templates select="." mode="process-collection"/>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template match="*" mode="process-collection">
    <xsl:apply-templates select="."/>
    
    <xsl:if test="extent">
      <mri:extent>
        <gex:EX_Extent>
          <xsl:if test="extent/spatial/bbox">
            <xsl:for-each select="extent/spatial/bbox/array">
              <gex:geographicElement>
                <gex:EX_GeographicBoundingBox>
                  <gex:westBoundLongitude>
                    <gco:Decimal><xsl:value-of select="array[1]"/></gco:Decimal>
                  </gex:westBoundLongitude>
                  <gex:eastBoundLongitude>
                    <gco:Decimal><xsl:value-of select="array[3]"/></gco:Decimal>
                  </gex:eastBoundLongitude>
                  <gex:southBoundLatitude>
                    <gco:Decimal><xsl:value-of select="array[2]"/></gco:Decimal>
                  </gex:southBoundLatitude>
                  <gex:northBoundLatitude>
                    <gco:Decimal><xsl:value-of select="array[4]"/></gco:Decimal>
                  </gex:northBoundLatitude>
                </gex:EX_GeographicBoundingBox>
              </gex:geographicElement>
            </xsl:for-each>
          </xsl:if>
          
          <xsl:if test="extent/temporal/interval">
            <xsl:for-each select="extent/temporal/interval/array">
              <gex:temporalElement>
                <gex:EX_TemporalExtent>
                  <gex:extent>
                    <gml:TimePeriod>
                      <gml:beginPosition>
                        <xsl:choose>
                          <xsl:when test="array[1] and array[1] != 'null'">
                            <xsl:value-of select="array[1]"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:attribute name="indeterminatePosition">unknown</xsl:attribute>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gml:beginPosition>
                      <gml:endPosition>
                        <xsl:choose>
                          <xsl:when test="array[2] and array[2] != 'null'">
                            <xsl:value-of select="array[2]"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:attribute name="indeterminatePosition">now</xsl:attribute>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gml:endPosition>
                    </gml:TimePeriod>
                  </gex:extent>
                </gex:EX_TemporalExtent>
              </gex:temporalElement>
            </xsl:for-each>
          </xsl:if>
        </gex:EX_Extent>
      </mri:extent>
    </xsl:if>
  </xsl:template>

  <xsl:template name="dataLink">
    <xsl:param name="format"/>

    <mrd:onLine>
      <cit:CI_OnlineResource>
        <cit:linkage>
          <gco:CharacterString>
            <xsl:value-of select="concat(nodeUrl,
                                   '/api/explore/v2.1/catalog/datasets/',
                                   (datasetid|dataset/dataset_id)[1],
                                   '/exports/', $format, '?use_labels=true')"/>
          </gco:CharacterString>
        </cit:linkage>
        <cit:protocol>
          <gco:CharacterString>
            <xsl:value-of select="$format-protocol-mapping/entry[format=lower-case($format)]/protocol"/>
          </gco:CharacterString>
        </cit:protocol>
        <cit:name>
          <gco:CharacterString>
            <xsl:value-of select="$format"/>
          </gco:CharacterString>
        </cit:name>
        <cit:description>
          <gco:CharacterString>
            <xsl:value-of select="$format"/>
          </gco:CharacterString>
        </cit:description>
        <cit:function>
          <cit:CI_OnLineFunctionCode
            codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
            codeListValue="download"/>
        </cit:function>
      </cit:CI_OnlineResource>
    </mrd:onLine>
  </xsl:template>

  <xsl:template name="dataFormat">
    <xsl:param name="format"/>
    <mrd:distributionFormat>
      <mrd:MD_Format>
        <mrd:formatSpecificationCitation>
          <cit:CI_Citation>
            <cit:title>
              <gco:CharacterString>
                <xsl:value-of select="$format"/>
              </gco:CharacterString>
            </cit:title>
          </cit:CI_Citation>
        </mrd:formatSpecificationCitation>
      </mrd:MD_Format>
    </mrd:distributionFormat>
  </xsl:template>
  <xsl:template name="build-date">
    <xsl:param name="tag"/>
    <xsl:for-each select="metas/modified[. != 'null']|
                                          metas/data_processed[. != 'null']|
                                          dataset/metas/default/modified[. != 'null']|
                                          dataset/metas/default/data_processed[. != 'null']">

      <xsl:variable name="type"
                    select="if(name() = 'data_processed') then 'revision' else 'publication'"/>
      <xsl:element name="{$tag}">
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:value-of select="."/>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="{$type}"/>
          </cit:dateType>
        </cit:CI_Date>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>


  <xsl:template match="dataset/dataset_id"
                mode="ods-to-iso">
    <cit:identifier>
      <mcc:MD_Identifier>
        <mcc:code>
          <gco:CharacterString>
            <xsl:value-of select="."/>
          </gco:CharacterString>
        </mcc:code>
      </mcc:MD_Identifier>
    </cit:identifier>
  </xsl:template>
  
  <!-- Add a fallback identifier if none is provided -->
  <xsl:template name="ensure-identifier">
    <xsl:if test="not(dataset/dataset_id) and not(datasetid) and not(id)">
      <cit:identifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="generate-id()"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </cit:identifier>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="ods-to-iso"/>

  <!-- Template to handle STAC providers -->
  <xsl:template name="map-stac-providers">
    <xsl:for-each select="providers">
      <mri:pointOfContact>
        <cit:CI_Responsibility>
          <cit:role>
            <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode">
              <xsl:attribute name="codeListValue">
                <xsl:choose>
                  <xsl:when test="roles and roles[1]">
                    <xsl:choose>
                      <xsl:when test="roles[1]='licensor'">owner</xsl:when>
                      <xsl:when test="roles[1]='producer'">originator</xsl:when>
                      <xsl:when test="roles[1]='processor'">processor</xsl:when>
                      <xsl:when test="roles[1]='publisher'">publisher</xsl:when>
                      <xsl:when test="roles[1]='host'">distributor</xsl:when>
                      <xsl:otherwise>resourceProvider</xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise>resourceProvider</xsl:otherwise>
                </xsl:choose>
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
</xsl:stylesheet>
