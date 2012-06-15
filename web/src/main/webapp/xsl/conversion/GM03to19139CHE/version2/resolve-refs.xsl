<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Only one element have to be kept from the root element -->
    <xsl:template match="int:GM03_2Comprehensive.Comprehensive|int:GM03_2Core.Core" mode="ResolveRefs">
        <xsl:element name="{local-name(.)}" namespace="{namespace-uri(.)}">
            <xsl:choose>
                <xsl:when test="int:GM03_2Core.Core.MD_Metadata/parentIdentifier">
                    <xsl:apply-templates select="int:GM03_2Core.Core.MD_Metadata[parentIdentifier]" mode="ResolveRefs">
                        <xsl:with-param name="parent" select="name(.)"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="int:GM03_2Core.Core.MD_Metadata[1]" mode="ResolveRefs">
                        <xsl:with-param name="parent" select="name(.)"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>

            <!-- we add a few N-N links that would introduce a dead loop -->
            <xsl:apply-templates select="int:GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat"
                                 mode="ResolveRefsCopy"/>
        </xsl:element>
    </xsl:template>

    <!-- back references are dropped -->
    <xsl:template match="*[@REF and (starts-with(name(.), 'CI_') or starts-with(name(.), 'DQ_') or starts-with(name(.), 'EX_') or starts-with(name(.), 'LI_') or starts-with(name(.), 'MD_') or starts-with(name(.), 'SV_'))]" mode="ResolveRefs"/>

    <!-- this one does a huge loop in the model, it will be managed manually -->
    <xsl:template match="int:GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat" mode="ResolveRefs">
    </xsl:template>

    <!-- Elements that may contain others -->
    <xsl:template match="*" mode="ResolveRefs">
        <xsl:param name="parent"/>
        <xsl:variable name="selfName" select="name(.)"/>
        <xsl:element name="{local-name(.)}" namespace="{namespace-uri(.)}">
            <xsl:apply-templates select="@*" mode="ResolveRefs"/>
            <xsl:apply-templates mode="ResolveRefs">
                <xsl:with-param name="parent" select="$selfName"/>
            </xsl:apply-templates>

            <xsl:if test="@TID">
                <xsl:variable name="myTID" select="@TID"/>
                <xsl:for-each select="/int:TRANSFER/int:DATASECTION/int:GM03_2Comprehensive.Comprehensive/*|/int:TRANSFER/int:DATASECTION/int:GM03_2Core.Core/*">
                    <xsl:for-each select="*[@REF=$myTID]">
                        <xsl:if test="$parent!=name(..) and (starts-with(name(.), 'CI_') or starts-with(name(.), 'DQ_') or starts-with(name(.), 'EX_') or starts-with(name(.), 'LI_') or starts-with(name(.), 'MD_') or starts-with(name(.), 'SV_'))">
                            <xsl:apply-templates select=".." mode="ResolveRefs">
                                <xsl:with-param name="parent" select="$selfName"/>
                            </xsl:apply-templates>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <!-- manage direct references -->
    <xsl:key name="index" match="/int:TRANSFER/int:DATASECTION/int:GM03_2Comprehensive.Comprehensive//*|/int:TRANSFER/int:DATASECTION/int:GM03_2Core.Core//*" use="@TID"/>
    <xsl:template match="@REF" mode="ResolveRefs">
        <xsl:attribute name="REF">
            <xsl:value-of select="."/>
        </xsl:attribute>
        <xsl:variable name="real" select="key('index', .)"/>
        <xsl:apply-templates select="$real" mode="ResolveRefs">
            <xsl:with-param name="parent" select="name(../..)"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="@*" mode="ResolveRefs">
        <xsl:attribute name="{name(.)}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="text()" mode="ResolveRefs">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <xsl:template match="*" mode="ResolveRefsCopy">
        <xsl:element name="{local-name(.)}" namespace="{namespace-uri(.)}">
            <xsl:apply-templates select="@*" mode="ResolveRefsCopy"/>
            <xsl:apply-templates mode="ResolveRefsCopy"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="int:formatDistributor/@REF" mode="ResolveRefsCopy">
        <xsl:apply-templates mode="ResolveRefs" select="."/>
    </xsl:template>

    <xsl:template match="int:distributorFormat/@REF" mode="ResolveRefsCopy">
        <xsl:apply-templates mode="ResolveRefs" select="."/>
    </xsl:template>

    <xsl:template match="@*" mode="ResolveRefsCopy">
        <xsl:attribute name="{name(.)}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
