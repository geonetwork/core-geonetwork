<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    >
    
    <xsl:output method="xml"/>
    
    <!-- Return an iso19139 representation of a keyword 
        stored in a skos thesaurus. 
        
        TODO : SKOS.
    -->
    <xsl:template match="/">
        <xsl:for-each select="root/response/keyword">
            <gmd:MD_Keywords>
                <gmd:keyword xsi:type="gmd:PT_FreeText_PropertyType">
	                <!-- multilingual keyword -->
	                <gmd:PT_FreeText>
	                    <xsl:for-each select="value">
	                        <gmd:textGroup>
	                            <gmd:LocalisedCharacterString locale="#{substring(@lang,1,2)}"
	                                ><xsl:value-of select="."/></gmd:LocalisedCharacterString>
	                        </gmd:textGroup>
	                    </xsl:for-each>
	                </gmd:PT_FreeText>
                </gmd:keyword>
                <xsl:if test="@type!='_none_'">
                    <gmd:type>
                        <gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="{@type}"/>
                    </gmd:type>
                </xsl:if>
                <xsl:if test="thesaurus!=''">
                	<gmd:thesaurusName>
                		<gmd:CI_Citation>
                			<gmd:title>
                				<gco:CharacterString><xsl:value-of select="thesaurus"/></gco:CharacterString>
                			</gmd:title>
                			<gmd:date gco:nilReason="missing"/>
               			</gmd:CI_Citation>
               		</gmd:thesaurusName>
                </xsl:if>
            </gmd:MD_Keywords>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
