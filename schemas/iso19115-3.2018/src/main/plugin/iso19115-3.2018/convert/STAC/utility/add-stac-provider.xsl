<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

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
            </cit:CI_Organisation>
          </cit:party>
        </cit:CI_Responsibility>
      </mri:pointOfContact>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
