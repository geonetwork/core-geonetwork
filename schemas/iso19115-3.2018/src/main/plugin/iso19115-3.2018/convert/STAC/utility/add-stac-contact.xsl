<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

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
