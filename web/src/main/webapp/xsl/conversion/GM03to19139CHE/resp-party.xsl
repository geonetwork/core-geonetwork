<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xalan">
    <xsl:template mode="RespParty"
                  match="GM03Core.Core.MD_IdentificationpointOfContact |
                         GM03Core.Core.MD_Metadatacontact |
                         GM03Comprehensive.Comprehensive.MD_DistributordistributorContact |
                         distributorContact |
                         GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact |
                         GM03Core.Core.CI_ResponsiblePartyparentinfo |
                         GM03Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty |
                         GM03Comprehensive.Comprehensive.MD_UsageuserContactInfo">
        <xsl:variable name="resp-party" select="."/>
        <!-- I don't like this variable.  I think a choice with each
             when being one of the elements in the template match would be better
             and the otherwise should be role
         -->
        <xsl:variable name="roles" select="role |
                            GM03Comprehensive.Comprehensive.MD_DistributordistributorContact/role |
                            GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact/role |
                            ../../../role |
                            ../../GM03Comprehensive.Comprehensive.MD_DistributordistributorContact/role |
                            ../role |
                            ../../GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact/role"/>

            <xsl:for-each select="$roles/GM03Core.Core.CI_RoleCode_[1]">
                    <xsl:apply-templates mode="InnerRespParty" select="$resp-party">
                      <xsl:with-param name="role" select="."/>
                    </xsl:apply-templates>
            </xsl:for-each>

            <xsl:if test="count($roles) > 1">
                <xsl:for-each select="$roles/GM03Core.Core.CI_RoleCode_[ pos != 1]">
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
                  match="GM03Core.Core.MD_IdentificationpointOfContact|
                         GM03Core.Core.MD_Metadatacontact|
                         GM03Comprehensive.Comprehensive.MD_DistributordistributorContact|
                         distributorContact |
                         GM03Comprehensive.Comprehensive.MD_MaintenanceInformationcontact|
                         GM03Core.Core.CI_ResponsiblePartyparentinfo|
                         GM03Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty|
                         GM03Comprehensive.Comprehensive.MD_UsageuserContactInfo">
        <xsl:param name="role"/>
        <che:CHE_CI_ResponsibleParty gco:isoType="gmd:CI_ResponsibleParty">
            <xsl:for-each select="*/GM03Core.Core.CI_ResponsibleParty | GM03Core.Core.CI_ResponsibleParty">
                <xsl:apply-templates mode="text" select="organisationName"/>
                <xsl:for-each select="positionName">
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
                             codeListValue="{value}"/>
                </gmd:role>
            </xsl:for-each>
                  <xsl:for-each select="*/GM03Core.Core.CI_ResponsibleParty | GM03Core.Core.CI_ResponsibleParty">
                <xsl:for-each select="individualFirstName">
                    <che:individualFirstName>
                        <xsl:apply-templates mode="string" select="."/>
                    </che:individualFirstName>
                </xsl:for-each>
                <xsl:for-each select="individualLastName">
                    <che:individualLastName>
                        <xsl:apply-templates mode="string" select="."/>
                    </che:individualLastName>
                </xsl:for-each>
                <xsl:for-each select="organisationAcronym">
                    <che:organisationAcronym>
                        <xsl:apply-templates mode="language" select="."/>
                    </che:organisationAcronym>
                </xsl:for-each>
                <xsl:for-each select="GM03Core.Core.CI_ResponsiblePartyparentinfo">
                    <che:parentResponsibleParty>
                        <xsl:apply-templates mode="RespParty" select="."/>
                    </che:parentResponsibleParty>
                </xsl:for-each>
            </xsl:for-each>
        </che:CHE_CI_ResponsibleParty>
    </xsl:template>

    <xsl:template mode="RespParty" match="GM03Core.Core.CI_ResponsibleParty">
        <gmd:CI_Contact>
            <xsl:apply-templates mode="phone" select="."/>

            <gmd:address>
                <che:CHE_CI_Address gco:isoType="gmd:CI_Address">
                    <xsl:apply-templates mode="address" select="."/>
                </che:CHE_CI_Address>
            </gmd:address>

            <xsl:if test="linkage">
	            <gmd:onlineResource>
	                <xsl:for-each select="linkage">
	                    <gmd:CI_OnlineResource>
	                        <gmd:linkage>
	                            <xsl:apply-templates mode="language"/>
	                        </gmd:linkage>
	                    </gmd:CI_OnlineResource>
	                </xsl:for-each>
	            </gmd:onlineResource>
            </xsl:if>

            <xsl:for-each select="hoursOfService|contactInfo/GM03Core.Core.CI_Contact/hoursOfService">
                <gmd:hoursOfService>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:hoursOfService>
            </xsl:for-each>

            <xsl:for-each select="contactInstructions|contactInfo/GM03Core.Core.CI_Contact/contactInstructions">
                <gmd:contactInstructions>
                    <xsl:apply-templates mode="language"/>
                </gmd:contactInstructions>
            </xsl:for-each>
        </gmd:CI_Contact>
    </xsl:template>

    <xsl:template mode="phone" match="*">
        <gmd:phone>
            <che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">
                <xsl:for-each select="GM03Core.Core.CI_Telephone[numberType='mainNumber']">
                    <gmd:voice>
                        <xsl:apply-templates mode="string" select="number"/>
                    </gmd:voice>
                </xsl:for-each>
                <xsl:for-each select="GM03Core.Core.CI_Telephone[numberType='facsimile']">
                    <gmd:facsimile>
                        <xsl:apply-templates mode="string" select="number"/>
                    </gmd:facsimile>
                </xsl:for-each>
                <xsl:for-each select="GM03Core.Core.CI_Telephone[numberType='directNumber']">
                    <che:directNumber>
                        <xsl:apply-templates mode="string" select="number"/>
                    </che:directNumber>
                </xsl:for-each>
                <xsl:for-each select="GM03Core.Core.CI_Telephone[numberType='mobile']">
                    <che:mobile>
                        <xsl:apply-templates mode="string" select="number"/>
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
            <xsl:for-each select="address/GM03Core.Core.CI_Address/city">
                <gmd:city>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:city>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/administrativeArea">
                <gmd:administrativeArea>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:administrativeArea>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/postalCode">
                <gmd:postalCode>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:postalCode>
            </xsl:for-each>
            <xsl:for-each select="country|address/GM03Core.Core.CI_Address/country">
                <gmd:country>
                    <gco:CharacterString>
                        <xsl:value-of select="."></xsl:value-of>
                    </gco:CharacterString>
                </gmd:country>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/electronicMailAddress|electronicalMailAddress">
                <xsl:for-each select="GM03Core.Core.URL_/value">
                    <gmd:electronicMailAddress>
                        <xsl:apply-templates mode="string" select="."/>
                    </gmd:electronicMailAddress>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/streetName">
                <che:streetName>
                    <xsl:apply-templates mode="string" select="."/>
                </che:streetName>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/streetNumber">
                <che:streetNumber>
                    <xsl:apply-templates mode="string" select="."/>
                </che:streetNumber>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/addressLine">
                <che:addressLine>
                    <xsl:apply-templates mode="string" select="."/>
                </che:addressLine>
            </xsl:for-each>
            <xsl:for-each select="address/GM03Core.Core.CI_Address/postBox">
                <che:postBox>
                    <xsl:apply-templates mode="string" select="."/>
                </che:postBox>
            </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
