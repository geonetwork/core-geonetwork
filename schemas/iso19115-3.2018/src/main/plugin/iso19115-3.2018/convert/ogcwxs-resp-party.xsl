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

<xsl:stylesheet xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:ows2="http://www.opengis.net/ows/2.0"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="RespParty">
    <cit:role>
      <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode"
                       codeListValue="pointOfContact"/>
    </cit:role>
    <cit:party>
      <cit:CI_Organisation>

        <xsl:for-each
          select="(ContactPersonPrimary/ContactOrganization|
                  wms:ContactPersonPrimary/wms:ContactOrganization|
                  wcs:organisationName|
                  ows:ProviderName|
                  ows11:ProviderName|
                  ows2:ProviderName)[. != '']">
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </cit:name>
        </xsl:for-each>

        <cit:contactInfo>
          <cit:CI_Contact>


            <xsl:variable name="phone"
                          select="(ContactVoiceTelephone|wms:ContactVoiceTelephone|
      ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice|
      ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Voice|
      ows2:ServiceContact/ows2:ContactInfo/ows2:Phone/ows2:Voice)[. != '']"/>
            <xsl:variable name="fax"
                          select="(ContactFacsimileTelephone|wms:ContactFacsimileTelephone|
      ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile|
      ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Facsimile|
            ows2:ServiceContact/ows2:ContactInfo/ows2:Phone/ows2:Facsimile)[. != '']"/>

            <xsl:if test="$phone or $fax">
              <cit:phone>
                <cit:CI_Telephone>
                  <xsl:for-each select="$phone">
                    <cit:number>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:number>
                    <cit:numberType>
                      <cit:CI_TelephoneTypeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_TelephoneTypeCode"
                                                codeListValue="voice"/>
                    </cit:numberType>
                  </xsl:for-each>

                  <xsl:for-each select="$fax">
                    <cit:number>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:number>
                    <cit:numberType>
                      <cit:CI_TelephoneTypeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_TelephoneTypeCode"
                                                codeListValue="facsimilie"/>
                    </cit:numberType>
                  </xsl:for-each>
                </cit:CI_Telephone>
              </cit:phone>
            </xsl:if>


            <cit:address>
              <cit:CI_Address>

                <xsl:for-each select="(Address|wms:Address|ows:DeliveryPoint|ows11:DeliveryPoint|ows2:DeliveryPoint)[. != '']">
                  <cit:deliveryPoint>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:deliveryPoint>
                </xsl:for-each>


                <xsl:for-each select="(City|wms:City|wcs:address/wcs:city|ows:City|ows11:City|ows2:City)[. != '']">
                  <cit:city>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:city>
                </xsl:for-each>


                <xsl:for-each
                  select="(StateOrProvince|wms:StateOrProvince|ows:AdministrativeArea|ows11:AdministrativeArea|ows2:AdministrativeArea)[. != '']">
                  <cit:administrativeArea>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:administrativeArea>
                </xsl:for-each>


                <xsl:for-each select="(PostCode|wms:PostCode|ows:PostalCode|ows11:PostalCode|ows2:PostalCode)[. != '']">
                  <cit:postalCode>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:postalCode>
                </xsl:for-each>


                <xsl:for-each select="(Country|wms:Country|wcs:address/wcs:country|ows:Country|ows11:Country|ows2:Country)[. != '']">
                  <cit:country>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:country>
                </xsl:for-each>

                <xsl:for-each
                  select="(ContactElectronicMailAddress|wms:ContactElectronicMailAddress|wcs:address/wcs:electronicMailAddress|ows:ElectronicMailAddress|ows11:ElectronicMailAddress|ows2:ElectronicMailAddress)[. != '']">
                  <cit:electronicMailAddress>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:electronicMailAddress>
                </xsl:for-each>
              </cit:CI_Address>
            </cit:address>

            <xsl:variable name="url" select="//Service/OnlineResource/@xlink:href|
      //wms:Service/wms:OnlineResource/@xlink:href|
      ows:ProviderSite/@xlink:href|
      ows11:ProviderSite/@xlink:href|
      ows2:ProviderSite/@xlink:href"/>
            <xsl:if test="$url != ''">
              <cit:onlineResource>
                <cit:CI_OnlineResource>
                  <cit:linkage>
                    <gco:CharacterString>
                      <xsl:value-of select="$url"/>
                    </gco:CharacterString>
                  </cit:linkage>
                </cit:CI_OnlineResource>
              </cit:onlineResource>
            </xsl:if>
          </cit:CI_Contact>
        </cit:contactInfo>
        <cit:individual>
          <cit:CI_Individual>
            <xsl:for-each
              select="(ContactPersonPrimary/ContactPerson|
      wms:ContactPersonPrimary/wms:ContactPerson|
      wcs:individualName|
      ows:ServiceContact/ows:IndividualName|
      ows11:ServiceContact/ows11:IndividualName|
      ows2:ServiceContact/ows2:IndividualName)[. != '']">
              <cit:name>
                <gco:CharacterString>
                  <xsl:value-of select="."/>
                </gco:CharacterString>
              </cit:name>
            </xsl:for-each>


            <xsl:for-each
              select="(ContactPosition|wms:ContactPosition|
      wcs:positionName|
      ows:ServiceContact/ows:PositionName|
      ows11:ServiceContact/ows11:PositionName|
      ows2:ServiceContact/ows2:PositionName)[. != '']">
              <cit:positionName>
                <gco:CharacterString>
                  <xsl:value-of select="."/>
                </gco:CharacterString>
              </cit:positionName>
            </xsl:for-each>
          </cit:CI_Individual>
        </cit:individual>
      </cit:CI_Organisation>
    </cit:party>
  </xsl:template>
</xsl:stylesheet>
