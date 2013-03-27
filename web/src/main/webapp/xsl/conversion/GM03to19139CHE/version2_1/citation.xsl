<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template mode="Citation" match="int:GM03_2_1Comprehensive.Comprehensive.CI_Citation|int:GM03_2_1Core.Core.CI_Citation|int:citation">
        <gmd:CI_Citation>
            <xsl:for-each select="int:title">
                <gmd:title>
                    <xsl:apply-templates mode="language"/>
                </gmd:title>
            </xsl:for-each>
            <xsl:for-each select="int:alternateTitle/int:GM03_2_1Core.Core.PT_FreeText">
                <gmd:alternateTitle>
                    <xsl:apply-templates mode="language" select="."/>
                </gmd:alternateTitle>
            </xsl:for-each>
            <xsl:if test="not(int:GM03_2_1Core.Core.CI_Date) and not(int:alternateTitle/int:GM03_2_1Core.Core.PT_FreeText)">
                <gmd:date/>
            </xsl:if>
            <xsl:for-each select="int:GM03_2_1Core.Core.CI_Date">
                <gmd:date>
                    <xsl:apply-templates select="." mode="CitationDate"/>
                </gmd:date>
            </xsl:for-each>
            <xsl:for-each select="int:edition">
                <gmd:edition>
                    <xsl:apply-templates mode="string" select="."/>
                </gmd:edition>
            </xsl:for-each>
            <xsl:for-each select="int:editionDate">
                <gmd:editionDate>
                    <xsl:apply-templates mode="date" select="."/>
                </gmd:editionDate>
            </xsl:for-each>
            <xsl:for-each select="int:identifier|int:GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier/int:identifier">
                <gmd:identifier>
                    <xsl:apply-templates mode="Identifier"/>
                </gmd:identifier>
            </xsl:for-each>
            <xsl:for-each select="int:GM03_2_1Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty">
                <gmd:citedResponsibleParty>
                    <xsl:apply-templates mode="RespParty" select="."/>
                </gmd:citedResponsibleParty>
            </xsl:for-each>
            <xsl:for-each select="int:presentationForm">
                <gmd:presentationForm>
                    <gmd:CI_PresentationFormCode codeList="./resources/codeList.xml#CI_PresentationFormCode"
                                             codeListValue="{int:GM03_2_1Comprehensive.Comprehensive.CI_PresentationFormCode_/int:value}"/>
                </gmd:presentationForm>
            </xsl:for-each>
            <xsl:for-each select="int:series/int:GM03_2_1Comprehensive.Comprehensive.CI_Series">
                <gmd:series>
                    <gmd:CI_Series>
                        <xsl:apply-templates mode="text" select="int:name"/>
                        <xsl:apply-templates mode="text" select="int:issueIdentification"/>
                        <xsl:apply-templates mode="text" select="int:page"/>
                    </gmd:CI_Series>
                </gmd:series>
            </xsl:for-each>
            <xsl:for-each select="int:otherCitationDetails">
                <gmd:otherCitationDetails>
                    <xsl:apply-templates mode="language"/>
                </gmd:otherCitationDetails>
            </xsl:for-each>
            <xsl:for-each select="int:collectiveTitle">
                <gmd:collectiveTitle>
                    <xsl:apply-templates mode="language"/>
                </gmd:collectiveTitle>
            </xsl:for-each>
            <xsl:for-each select="int:ISBN">
                <gmd:ISBN>
                    <xsl:apply-templates mode="string"/>
                </gmd:ISBN>
            </xsl:for-each>
            <xsl:for-each select="int:ISSN">
                <gmd:ISSN>
                    <xsl:apply-templates mode="string"/>
                </gmd:ISSN>
            </xsl:for-each>
        </gmd:CI_Citation>
    </xsl:template>

    <xsl:template mode="Citation" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Citation</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ========================================================================== -->

    <xsl:template mode="Citation" match="int:GM03_2_1Core.Core.CI_Date">
        <gmd:date>
            <xsl:apply-templates select="." mode="CitationDate"/>
        </gmd:date>
    </xsl:template>

    <xsl:template mode="CitationDate" match="int:GM03_2_1Core.Core.CI_Date">
        <gmd:CI_Date>
            <xsl:apply-templates mode="CitationDate"/>
        </gmd:CI_Date>
    </xsl:template>

    <xsl:template mode="CitationDate" match="int:date">
        <gmd:date>
            <xsl:apply-templates mode="date" select="."/>
        </gmd:date>
    </xsl:template>

    <xsl:template mode="CitationDate" match="int:dateType">
        <gmd:dateType>
            <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode" codeListValue="{.}" />
        </gmd:dateType>
    </xsl:template>

    <xsl:template mode="CitationDate" match="int:CI_Citation"/>

    <xsl:template mode="CitationDate" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">CitationDate</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ========================================================================== -->

    <xsl:template mode="Identifier" match="int:GM03_2_1Core.Core.MD_Identifier">
        <xsl:if test="int:code">
            <gmd:MD_Identifier>
                <xsl:for-each select="int:code">
                    <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
                        <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>
                        <xsl:apply-templates mode="language" select="."/>
                    </gmd:code>
                </xsl:for-each>
            </gmd:MD_Identifier>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="Identifier" match="int:GM03_2_1Comprehensive.Comprehensive.RS_Identifier">
        <xsl:apply-templates mode="MdIdent" select="." />
    </xsl:template>

    <xsl:template mode="Identifier" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Identifier</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
