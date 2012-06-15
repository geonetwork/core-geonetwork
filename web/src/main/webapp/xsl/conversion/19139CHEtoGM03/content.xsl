<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml"
                exclude-result-prefixes="che gco gmd gml">

    <xsl:template mode="Content" match="gmd:MD_CoverageDescription|che:CHE_MD_CoverageDescription">
        <GM03_2Comprehensive.Comprehensive.MD_CoverageDescription TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="Content" select="gmd:attributeDescription"/>
            <xsl:apply-templates mode="text" select="gmd:contentType"/>
            <xsl:apply-templates mode="Content" select="gmd:dimension"/>
            <xsl:apply-templates mode="text" select="che:filmType"/>
            <xsl:apply-templates mode="text" select="che:focalDistance"/>
        </GM03_2Comprehensive.Comprehensive.MD_CoverageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_FeatureCatalogueDescription|gmd:MD_FeatureCatalogueDescription">
	    <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    	<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:if test="gmd:language">
                <language>
                    <xsl:for-each select="gmd:language/gco:CharacterString">
                        <CodeISO.LanguageCodeISO_>
                            <value>
                                    <xsl:call-template name="lang3_to_lang2">
                                    <xsl:with-param name="lang3" select="translate(.,$ucletters,$lcletters)"/>
                                </xsl:call-template>
                            </value>
                        </CodeISO.LanguageCodeISO_>
                    </xsl:for-each>
                </language>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:includedWithDataset"/>
            <xsl:apply-templates mode="text" select="gmd:complianceCode"/>
            <xsl:if test="gmd:featureTypes">
                <featureTypes>
                    <xsl:for-each select="gmd:featureTypes/*">
                        <GM03_2Comprehensive.Comprehensive.GenericName_>
                            <value>
                                <xsl:value-of select="."/>
                            </value>
                        </GM03_2Comprehensive.Comprehensive.GenericName_>
                    </xsl:for-each>
                </featureTypes>
            </xsl:if>
            <xsl:choose>
	            <xsl:when test="che:modelType">
	                <modelType><xsl:value-of select="che:modelType/che:CHE_MD_modelTypeCode/@codeListValue"/></modelType>
	            </xsl:when>
	            <xsl:otherwise>
	                <modelType>other</modelType>
	            </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates mode="Content" select="che:dataModel"/>
            <xsl:apply-templates mode="Content" select="che:class"/>
            <xsl:apply-templates mode="Content" select="che:domain"/>
            <xsl:apply-templates mode="text" select="che:portrayalCatalogueURL"/>
            <xsl:apply-templates mode="Content" select="gmd:featureCatalogueCitation"/>
        </GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription>
    </xsl:template>

 
    <xsl:template mode="Content" match="gmd:dimension">
        <GM03_2Comprehensive.Comprehensive.dimensionMD_CoverageDescription TID="x{generate-id(.)}">
            <dimension REF="?">
                <xsl:apply-templates mode="Content"/>
            </dimension>
            <BACK_REF name="MD_CoverageDescription"/>
        </GM03_2Comprehensive.Comprehensive.dimensionMD_CoverageDescription>
    </xsl:template>
 

    <xsl:template mode="Content" match="che:class">
        <GM03_2Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription TID="x{generate-id(.)}">
            <class REF="?">
                <xsl:apply-templates mode="Content"/>
            </class>
            <BACK_REF name="MD_FeatureCatalogueDescription"/>
        </GM03_2Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:MD_Band">
        <GM03_2Comprehensive.Comprehensive.MD_Band TID="x{generate-id(.)}">
            <xsl:apply-templates mode="Content" select="gmd:sequenceIdentifier/gco:MemberName/gco:aName"/>
            <xsl:apply-templates mode="text" select="gmd:descriptor"/>
            <xsl:apply-templates mode="text" select="gmd:maxValue"/>
            <xsl:apply-templates mode="text" select="gmd:minValue"/>
            <xsl:apply-templates mode="Content" select="gmd:units"/>
            <xsl:apply-templates mode="text" select="gmd:peakResponse"/>
            <xsl:apply-templates mode="text" select="gmd:bitsPerValue"/>
            <xsl:apply-templates mode="text" select="gmd:toneGradation"/>
            <xsl:apply-templates mode="text" select="gmd:scaleFactor"/>
            <xsl:apply-templates mode="text" select="gmd:offset"/>
        </GM03_2Comprehensive.Comprehensive.MD_Band>
    </xsl:template>
    
    <xsl:template mode="Content" match="gmd:MD_RangeDimension">
        <GM03_2Comprehensive.Comprehensive.MD_RangeDimension TID="x{generate-id(.)}">
            <xsl:apply-templates mode="Content" select="gmd:sequenceIdentifier/gco:MemberName/gco:aName"/>
            <xsl:apply-templates mode="text" select="gmd:descriptor"/>
        </GM03_2Comprehensive.Comprehensive.MD_RangeDimension>
    </xsl:template>
    
    <xsl:template mode="Content" match="gco:aName">
        <sequenceIdentifier><xsl:value-of select="."/></sequenceIdentifier>
    </xsl:template>
    
    <xsl:template mode="Content" match="che:CHE_MD_Class">
        <GM03_2Comprehensive.Comprehensive.MD_Class TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="text" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:baseClass"/>
            <xsl:apply-templates mode="Content" select="che:subClass"/>
            <xsl:apply-templates mode="Content" select="che:attribute"/>
        </GM03_2Comprehensive.Comprehensive.MD_Class>
    </xsl:template>

    <xsl:template mode="Content" match="che:attribute">
        <GM03_2Comprehensive.Comprehensive.MD_Attribute TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="text" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:namedType"/> 
            <BACK_REF name="MD_AbstractClass"/>
            <xsl:apply-templates mode="Content" select="che:anonymousType"/>
        </GM03_2Comprehensive.Comprehensive.MD_Attribute>
    </xsl:template>

	<xsl:template mode="Content" match="che:domain">
		<GM03_2Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription TID="x{generate-id(.)}">
			<domain REF="?">
				<xsl:apply-templates mode="Content" select="che:CHE_MD_CodeDomain"/>
			</domain>
			<BACK_REF name="MD_FeatureCatalogueDescription"/>
		</GM03_2Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription>
	</xsl:template>

    <xsl:template mode="Content" match="che:type">
      <type REF="?">
        <GM03_2Comprehensive.Comprehensive.MD_Type TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:type"/>
            <xsl:apply-templates mode="Content" select="che:value/che:CHE_MD_CodeValue"/>
        </GM03_2Comprehensive.Comprehensive.MD_Type>
      </type>
    </xsl:template>
    
    <xsl:template mode="Content" match="che:anonymousType">
        <anonymousType REF="?">
            <xsl:apply-templates mode="Content" select="che:CHE_MD_Type"/>
        </anonymousType>
    </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_Type">
		<GM03_2Comprehensive.Comprehensive.MD_Type TID="x{generate-id(.)}">
			<xsl:apply-templates mode="text" select="che:type" />
			<xsl:apply-templates mode="Content" select="che:value/che:CHE_MD_CodeValue" />
		</GM03_2Comprehensive.Comprehensive.MD_Type>
	</xsl:template>
    
    <xsl:template mode="Content" match="che:CHE_MD_CodeValue">
        <GM03_2Comprehensive.Comprehensive.MD_CodeValue TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="text" select="che:code"/>
            <xsl:apply-templates mode="text" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:subValue/che:CHE_MD_CodeValue"/>
            <xsl:choose>
            <xsl:when test="name(..) = 'che:value'">
                <BACK_REF name="MD_Type"/>
            </xsl:when>
            <xsl:otherwise>
                <BACK_REF name="MD_CodeValue"/>
            </xsl:otherwise>
            </xsl:choose>
        </GM03_2Comprehensive.Comprehensive.MD_CodeValue>
    </xsl:template>

	<xsl:template mode="Content" match="che:CHE_MD_CodeDomain">
		<GM03_2Comprehensive.Comprehensive.MD_CodeDomain TID="x{generate-id(.)}">
			<xsl:apply-templates mode="text" select="che:name" />
			<xsl:apply-templates mode="text" select="che:description" />
		    <xsl:apply-templates mode="Content" select="che:baseDomain" />
			<xsl:apply-templates mode="Content" select="che:type" />
		</GM03_2Comprehensive.Comprehensive.MD_CodeDomain>
	</xsl:template>

	<xsl:template mode="Content" match="che:baseDomain">
	   <baseDomain REF="?">
		  <xsl:apply-templates mode="Content" select="che:CHE_MD_CodeDomain" />
		</baseDomain>
	</xsl:template>

    <xsl:template mode="Content" match="che:namedType">
        <xsl:if test="./*">
            <GM03_2Comprehensive.Comprehensive.MD_AttributenamedType TID="x{generate-id(.)}">
                <BACK_REF name="MD_Attribute"/>
	            <namedType REF="?">
	                <xsl:apply-templates mode="Content"/>
	           </namedType>
	        </GM03_2Comprehensive.Comprehensive.MD_AttributenamedType>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:featureCatalogueCitation">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>
    
    <xsl:template mode="Content" match="gmd:units">
        <units><xsl:value-of select=".//gml:catalogSymbol"/></units>
    </xsl:template>
    <xsl:template mode="Content" match="che:dataModel">
        <xsl:choose>
        <xsl:when test="normalize-space(text()) = ''">
            <dataModel>
             <GM03_2Core.Core.PT_FreeURL>
                <URLGroup>
                    <GM03_2Core.Core.PT_URLGroup>
                        <language><xsl:value-of select="$defaultLanguage"/></language>
                        <plainURL/>
                    </GM03_2Core.Core.PT_URLGroup>
              </URLGroup>
            </GM03_2Core.Core.PT_FreeURL>
            </dataModel>
        </xsl:when>
        <xsl:otherwise>
        <xsl:apply-templates mode="text" select="."/>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="Content" match="che:modelType">
        <xsl:apply-templates mode="text" select="."/>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:CI_Citation">
        <GM03_2Comprehensive.Comprehensive.CI_Citation TID="x{generate-id(.)}">
        <xsl:apply-templates mode="textGroup" select="gmd:title"/>
        <xsl:apply-templates mode="text" select="gmd:edition"/>
        <xsl:apply-templates mode="text" select="gmd:editionDate"/>
        <xsl:apply-templates mode="groupEnum" select=".">
        	<xsl:with-param name="element">presentationForm</xsl:with-param>
        	<xsl:with-param name="newName">GM03_2Comprehensive.Comprehensive.CI_PresentationFormCode_</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:ISBN"/>
        <xsl:apply-templates mode="text" select="gmd:ISSN"/>
        <xsl:apply-templates mode="groupText" select=".">
            <xsl:with-param name="element">alternateTitle</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:collectiveTitle"/>
        <xsl:apply-templates mode="text" select="gmd:otherCitationDetails"/>
        <BACK_REF name="MD_FeatureCatalogueDescription"/>
        <xsl:apply-templates mode="RefSystem" select="gmd:series/gmd:CI_Series"/>

        <xsl:apply-templates mode="RefSystem" select="gmd:date"/>

        <!-- not mapped -->
        <xsl:apply-templates mode="DataIdentification" select="gmd:identifier"/>
        <xsl:apply-templates mode="RefSystem" select="gmd:citedResponsibleParty"/>
        </GM03_2Comprehensive.Comprehensive.CI_Citation>
            
    </xsl:template>


    <xsl:template mode="Content" match="gmd:attributeDescription">
        <attributeDescription><xsl:value-of select="gco:RecordType"/></attributeDescription>
    </xsl:template>

    <xsl:template mode="Content" match="che:filmType">
        <filmType><xsl:value-of select="che:CHE_MD_FilmTypeCode/@codeListValue"/></filmType>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:MD_ImageDescription|che:CHE_MD_ImageDescription">
        <GM03_2Comprehensive.Comprehensive.MD_ImageDescription TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="Content" select="gmd:attributeDescription"/>
            <xsl:apply-templates mode="text" select="gmd:contentType"/>
             <xsl:apply-templates mode="Content" select="che:filmType"/>
             <xsl:apply-templates mode="text" select="che:focalDistance"/>
            <xsl:apply-templates mode="text" select="gmd:illuminationElevationAngle"/>
            <xsl:apply-templates mode="text" select="gmd:illuminationAzimuthAngle"/>
            <xsl:apply-templates mode="text" select="gmd:imagingCondition"/>
            <xsl:apply-templates mode="text" select="gmd:cloudCoverPercentage"/>
            <xsl:apply-templates mode="text" select="gmd:compressionGenerationQuantity"/>
            <xsl:apply-templates mode="text" select="gmd:triangulationIndicator"/>
            <xsl:apply-templates mode="text" select="gmd:radiometricCalibrationDataAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:cameraCalibrationInformationAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:filmDistortionInformationAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:lensDistortionInformationAvailability"/>

            <imageQualityCode REF="?">
                <xsl:apply-templates mode="Extent" select="gmd:imageQualityCode/gmd:MD_Identifier"/>
            </imageQualityCode>
            <processingLevelCode REF="?">
                <xsl:apply-templates mode="Extent" select="gmd:processingLevelCode/gmd:MD_Identifier"/>
            </processingLevelCode>
        </GM03_2Comprehensive.Comprehensive.MD_ImageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="*" priority="-100">
        <ERROR>Unknown Content element <xsl:value-of select="local-name(..)"/>/<xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
</xsl:stylesheet>