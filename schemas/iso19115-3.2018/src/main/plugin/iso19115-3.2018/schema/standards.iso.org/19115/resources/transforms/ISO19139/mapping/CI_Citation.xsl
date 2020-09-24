<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:srvold="http://www.isotc211.org/2005/srv"
                xmlns:gml30="http://www.opengis.net/gml"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/1.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/1.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">

    <xsl:import href="CI_ResponsibleParty.xsl"/>
    <xsl:import href="../utility/dateTime.xsl"/>

    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b>December 5, 2014 </xd:p>
            <xd:p>These templates transform ISO 19139 CI_Citation XML content into ISO 19115-3 CI_Citation. They are designed to be imported as a template library</xd:p>
            <xd:p>Version December 5, 2014</xd:p>
            <xd:p><xd:b>Author:</xd:b>thabermann@hdfgroup.org</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="gmd:CI_Citation" mode="from19139to19115-3">
        <xsl:element name="cit:CI_Citation">
            <xsl:apply-templates mode="from19139to19115-3"/>
            <!-- Special attention is required for CI_ResponsibleParties that are included in the 
                CI_Citation only for a URL. These are currently identified as those 
                with no name elements (individualName, organisationName, or positionName)
            -->
            <xsl:for-each
                select=".//gmd:CI_ResponsibleParty[count(gmd:individualName/gco:CharacterString) + count(gmd:organisationName/gco:CharacterString) + count(gmd:positionName/gco:CharacterString) = 0]">
                <xsl:call-template name="CI_ResponsiblePartyToOnlineResource"/>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="gmd:CI_Citation/gmd:date" mode="from19139to19115-3">
        <cit:date>
            <xsl:apply-templates select="@*" mode="from19139to19115-3"/>
            <xsl:choose>
                <xsl:when test="normalize-space()=''"/>
                <xsl:otherwise>
                    <cit:CI_Date>
                        <cit:date>
                            <xsl:choose>
                                <xsl:when test="descendant::gmd:date/@gcoold:nilReason">
                                    <!-- added this to get gmd:dates with gco:nilReason and dateTypes -->
                                    <xsl:attribute name="gco:nilReason" select="descendant::gmd:date/@gcoold:nilReason"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="writeDateTime"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </cit:date>
                        <xsl:for-each select="descendant::gmd:dateType">
                            <xsl:call-template name="writeCodelistElement">
                                <xsl:with-param name="elementName" select="'cit:dateType'"/>
                                <xsl:with-param name="codeListName" select="'cit:CI_DateTypeCode'"/>
                                <xsl:with-param name="codeListValue" select="gmd:CI_DateTypeCode/@codeListValue"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </cit:CI_Date>
                </xsl:otherwise>
            </xsl:choose>
        </cit:date>
    </xsl:template>

    <xsl:template match="gmd:CI_Citation/gmd:title" mode="from19139to19115-3">
        <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'cit:title'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="."/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="gmd:CI_Citation/gmd:editionDate" mode="from19139to19115-3">
        <cit:editionDate>
            <xsl:call-template name="writeDateTime"/>
        </cit:editionDate>
    </xsl:template>

    <!-- The collectiveTitle element was dropped from CI_Citation in 19115-1 -->
    <xsl:template match="gmd:CI_Citation/gmd:collectiveTitle" mode="from19139to19115-3"/>
</xsl:stylesheet>
