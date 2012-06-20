<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int xalan">
    <xsl:template mode="RespParty"
                  match="int:GM03_2Core.Core.MD_IdentificationpointOfContact |
                         int:GM03_2Core.Core.MD_Metadatacontact |
                         int:GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact |
                         int:distributorContact |
                         int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact |
                         int:GM03_2Core.Core.CI_ResponsiblePartyparentinfo |
                         int:GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty |
                         int:GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo">
        <xsl:variable name="resp-party" select="."/>
        <!-- I don't like this variable.  I think a choice with each
             when being one of the elements in the template match would be better
             and the otherwise should be role
         -->
        <xsl:variable name="roles" select="int:role |
                            int:GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact/int:role |
                            int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact/int:role |
                            ../../../int:role |
                            ../../int:GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact/int:role |
                            ../int:role |
                            ../../int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact/int:role"/>

            <xsl:for-each select="$roles/int:GM03_2Core.Core.CI_RoleCode_[1]">
                    <xsl:apply-templates mode="InnerRespParty" select="$resp-party">
                      <xsl:with-param name="role" select="."/>
                    </xsl:apply-templates>
            </xsl:for-each>

            <xsl:if test="count($roles) > 1">
                <xsl:for-each select="$roles/int:GM03_2Core.Core.CI_RoleCode_[ pos != 1]">
                        <xsl:element name="{name(..)}">
                            <xsl:apply-templates mode="InnerRespParty" select="$resp-party">
                              <xsl:with-param name="role" select="."/>
                            </xsl:apply-templates>
                        </xsl:element>
                </xsl:for-each>
            </xsl:if>
    </xsl:template>

    <!-- This template does the actual work of copying the responsible party.
         The other template with the same match (but mode="RespParty") calls this
         based with each role
    -->
    <xsl:template mode="InnerRespParty"
                  match="int:GM03_2Core.Core.MD_IdentificationpointOfContact|
                         int:GM03_2Core.Core.MD_Metadatacontact|
                         int:GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact|
                         int:distributorContact |
                         int:GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact|
                         int:GM03_2Core.Core.CI_ResponsiblePartyparentinfo|
                         int:GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty|
                         int:GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo">
        <xsl:param name="role"/>
        <che:CHE_CI_ResponsibleParty gco:isoType="gmd:CI_ResponsibleParty">
            <xsl:for-each select="*/int:GM03_2Core.Core.CI_ResponsibleParty | int:GM03_2Core.Core.CI_ResponsibleParty">
                <xsl:apply-templates mode="text" select="int:organisationName"/>
                <xsl:for-each select="int:positionName">
                    <gmd:positionName>
                        <xsl:apply-templates mode="language"/>
                    </gmd:positionName>
                </xsl:for-each>
                <gmd:contactInfo>
                    <xsl:apply-templates select="." mode="RespParty"/>
                </gmd:contactInfo>
            </xsl:for-each>
            <xsl:for-each select="$role">
                <gmd:role>
                    <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode"
                             codeListValue="{int:value}"/>
                </gmd:role>
            </xsl:for-each>
                  <xsl:for-each select="*/int:GM03_2Core.Core.CI_ResponsibleParty | int:GM03_2Core.Core.CI_ResponsibleParty">
                <xsl:for-each select="int:individualFirstName">
                    <che:individualFirstName>
                        <xsl:apply-templates mode="string" select="."/>
                    </che:individualFirstName>
                </xsl:for-each>
                <xsl:for-each select="int:individualLastName">
                    <che:individualLastName>
                        <xsl:apply-templates mode="string" select="."/>
                    </che:individualLastName>
                </xsl:for-each>
                <xsl:for-each select="int:organisationAcronym">
                    <che:organisationAcronym>
                        <xsl:apply-templates mode="language" select="."/>
                    </che:organisationAcronym>
                </xsl:for-each>
                <xsl:for-each select="int:GM03_2Core.Core.CI_ResponsiblePartyparentinfo">
                    <che:parentResponsibleParty>
                        <xsl:apply-templates mode="RespParty" select="."/>
                    </che:parentResponsibleParty>
                </xsl:for-each>
            </xsl:for-each>
        </che:CHE_CI_ResponsibleParty>
    </xsl:template>

    <xsl:template mode="RespParty" match="int:GM03_2Core.Core.CI_ResponsibleParty">
        <gmd:CI_Contact>
            <xsl:apply-templates mode="phone" select="."/>

            <gmd:address>
                <che:CHE_CI_Address gco:isoType="gmd:CI_Address">
                    <xsl:apply-templates mode="address" select="."/>
                </che:CHE_CI_Address>
            </gmd:address>

            <xsl:if test="int:linkage">
	            <gmd:onlineResource>
	                <xsl:for-each select="int:linkage">
	                    <gmd:CI_OnlineResource>
	                        <gmd:linkage>
	                            <xsl:apply-templates mode="language"/>
	                        </gmd:linkage>
	                    </gmd:CI_OnlineResource>
	                </xsl:for-each>
	            </gmd:onlineResource>
            </xsl:if>

            <xsl:for-each select="int:hoursOfService|int:contactInfo/int:GM03_2Core.Core.CI_Contact/int:hoursOfService">
                <gmd:hoursOfService>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:hoursOfService>
            </xsl:for-each>

            <xsl:for-each select="int:contactInstructions|int:contactInfo/int:GM03_2Core.Core.CI_Contact/int:contactInstructions">
                <gmd:contactInstructions>
                    <xsl:apply-templates mode="language"/>
                </gmd:contactInstructions>
            </xsl:for-each>
        </gmd:CI_Contact>
    </xsl:template>

    <xsl:template mode="phone" match="*">
        <gmd:phone>
            <che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">
                <xsl:for-each select="int:GM03_2Core.Core.CI_Telephone[int:numberType='mainNumber']">
                    <gmd:voice>
                        <xsl:apply-templates mode="string" select="int:number"/>
                    </gmd:voice>
                </xsl:for-each>
                <xsl:for-each select="int:GM03_2Core.Core.CI_Telephone[int:numberType='facsimile']">
                    <gmd:facsimile>
                        <xsl:apply-templates mode="string" select="int:number"/>
                    </gmd:facsimile>
                </xsl:for-each>
                <xsl:for-each select="int:GM03_2Core.Core.CI_Telephone[int:numberType='directNumber']">
                    <che:directNumber>
                        <xsl:apply-templates mode="string" select="int:number"/>
                    </che:directNumber>
                </xsl:for-each>
                <xsl:for-each select="int:GM03_2Core.Core.CI_Telephone[int:numberType='mobile']">
                    <che:mobile>
                        <xsl:apply-templates mode="string" select="int:number"/>
                    </che:mobile>
                </xsl:for-each>
            </che:CHE_CI_Telephone>
        </gmd:phone>
    </xsl:template>

    <xsl:template mode="RespParty" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">RespParty</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ====================================================================================== -->

    <xsl:template mode="address" match="*">
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:city">
                <gmd:city>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:city>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:administrativeArea">
                <gmd:administrativeArea>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:administrativeArea>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:postalCode">
                <gmd:postalCode>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:postalCode>
            </xsl:for-each>
            <xsl:for-each select="int:country|address/int:GM03_2Core.Core.CI_Address/int:country">
                <gmd:country>
                    <gco:CharacterString>
                        <xsl:value-of select="."></xsl:value-of>
                    </gco:CharacterString>
                </gmd:country>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:electronicMailAddress|int:electronicalMailAddress">
                <xsl:for-each select="int:GM03_2Core.Core.URL_/int:value">
                    <gmd:electronicMailAddress>
                        <xsl:apply-templates mode="string" select="."/>
                    </gmd:electronicMailAddress>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:streetName">
                <che:streetName>
                    <xsl:apply-templates mode="string" select="."/>
                </che:streetName>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:streetNumber">
                <che:streetNumber>
                    <xsl:apply-templates mode="string" select="."/>
                </che:streetNumber>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:addressLine">
                <che:addressLine>
                    <xsl:apply-templates mode="string" select="."/>
                </che:addressLine>
            </xsl:for-each>
            <xsl:for-each select="int:address/int:GM03_2Core.Core.CI_Address/int:postBox">
                <che:postBox>
                    <xsl:apply-templates mode="string" select="."/>
                </che:postBox>
            </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
