<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml">

    <xsl:template mode="Content"
                  match="GM03Comprehensive.Comprehensive.MD_CoverageDescription">
        <che:CHE_MD_CoverageDescription>
            <xsl:apply-templates mode="Content" select="attributeDescription"/>
            <xsl:apply-templates mode="Content" select="contentType"/>
            <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.dimensionMD_CoverageDescription"/>
            <xsl:apply-templates mode="Content" select="filmType"/>
            <xsl:apply-templates mode="integerCHE" select="focalDistance"/>
        </che:CHE_MD_CoverageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="filmType">
        <che:filmType>
            <che:CHE_MD_FilmTypeCode codeList="?" codeListValue="{.}"/>
        </che:filmType>
    </xsl:template>

    <xsl:template mode="Content" match="attributeDescription">
        <gmd:attributeDescription>
            <gco:RecordType>
                <xsl:value-of select="."/>
            </gco:RecordType>
        </gmd:attributeDescription>
    </xsl:template>

    <xsl:template mode="Content" match="contentType">
        <gmd:contentType>
            <gmd:MD_CoverageContentTypeCode
                    codeList="./resources/codeList.xml#MD_CoverageContentTypeCode"
                    codeListValue="{.}"/>
        </gmd:contentType>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.dimensionMD_CoverageDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="dimension">
        <gmd:dimension>
            <xsl:apply-templates mode="Content"/>
        </gmd:dimension>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_Band">
        <gmd:MD_Band>
            <xsl:apply-templates mode="Content" select="sequenceIdentifier"/>
            <xsl:apply-templates mode="text" select="descriptor"/>
            <xsl:apply-templates mode="real" select="maxValue"/>
            <xsl:apply-templates mode="real" select="minValue"/>
            <xsl:apply-templates mode="Content" select="units"/>
            <xsl:apply-templates mode="real" select="peakResponse"/>
            <xsl:apply-templates mode="integer" select="bitsPerValue"/>
            <xsl:apply-templates mode="integer" select="toneGradation"/>
            <xsl:apply-templates mode="real" select="scaleFactor"/>
            <xsl:apply-templates mode="real" select="offset"/>
        </gmd:MD_Band>
    </xsl:template>

    <xsl:template mode="Content" match="sequenceIdentifier">
        <gmd:sequenceIdentifier>
            <gco:MemberName>
                <!-- TODO: what to put here? -->
                <gco:aName>
                    <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                </gco:aName>
                <gco:attributeType>
                    <gco:TypeName>
                        <gco:aName>
                            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                        </gco:aName>
                    </gco:TypeName>
                </gco:attributeType>
            </gco:MemberName>
        </gmd:sequenceIdentifier>
    </xsl:template>

    <xsl:template mode="Content" match="units">
        <xsl:variable name="unit" select="text()"/>
        <gmd:units>
            <xsl:copy-of select="document('units.xml')//gml:dictionaryEntry/*[@gml:id=$unit]"/>
        </gmd:units>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_ImageDescription">
        <che:CHE_MD_ImageDescription>
        <!--<MD_ImageDescription>-->
            <xsl:apply-templates mode="Content" select="attributeDescription"/>
            <xsl:apply-templates mode="Content" select="contentType"/>
            <xsl:apply-templates mode="Content" select="dimension"/>

            <xsl:apply-templates mode="real" select="illuminationElevationAngle"/>
            <xsl:apply-templates mode="real" select="illuminationAzimuthAngle"/>
            <xsl:apply-templates mode="Content" select="imagingCondition"/>
            <xsl:apply-templates mode="Content" select="imageQualityCode"/>
            <xsl:apply-templates mode="real" select="cloudCoverPercentage"/>
            <xsl:apply-templates mode="Content" select="processingLevelCode"/>
            <xsl:apply-templates mode="integer" select="compressionGenerationQuantity"/>
            <xsl:apply-templates mode="boolean" select="triangulationIndicator"/>
            <xsl:apply-templates mode="boolean" select="radiometricCalibrationDataAvailability"/>
            <xsl:apply-templates mode="boolean" select="cameraCalibrationInformationAvailability"/>
            <xsl:apply-templates mode="boolean" select="filmDistortionInformationAvailability"/>
            <xsl:apply-templates mode="boolean" select="lensDistortionInformationAvailability"/>

            <xsl:apply-templates mode="Content" select="filmType"/>
            <xsl:apply-templates mode="integerCHE" select="focalDistance"/>
        <!--</MD_ImageDescription>-->
        </che:CHE_MD_ImageDescription>

    </xsl:template>

    <xsl:template mode="Content" match="imagingCondition">
        <gmd:imagingCondition>
            <gmd:MD_ImagingConditionCode codeList="./resources/codeList.xml#MD_ImagingConditionCode" codeListValue="{./@value|.}" />
        </gmd:imagingCondition>
    </xsl:template>

    <xsl:template mode="Content" match="imageQualityCode|processingLevelCode">
        <xsl:element name="{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:apply-templates mode="Identifier"/>
        </xsl:element>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_RangeDimension">
        <gmd:MD_RangeDimension>
            <xsl:apply-templates mode="Content" select="sequenceIdentifier"/>
            <xsl:apply-templates mode="text" select="descriptor"/>
        </gmd:MD_RangeDimension>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content"
                  match="GM03Comprehensive.Comprehensive.MD_FeatureCatalogueDescription">
        <che:CHE_MD_FeatureCatalogueDescription gco:isoType="gmd:MD_FeatureCatalogueDescription">
            <xsl:for-each select="complianceCode">
                <gmd:complianceCode>
                    <xsl:apply-templates mode="boolean" select="text()"/>
                </gmd:complianceCode>
            </xsl:for-each>

            <xsl:for-each select="language">
                <xsl:apply-templates mode="language" select="."/>
            </xsl:for-each>

            <xsl:for-each select="includedWithDataset">
                <gmd:includedWithDataset>
                    <xsl:apply-templates mode="boolean" select="text()"/>
                </gmd:includedWithDataset>
            </xsl:for-each>

            <xsl:for-each select="featureTypes">
                <xsl:for-each select="GM03Comprehensive.Comprehensive.GenericName_/value">
                    <gmd:featureTypes>
                        <gco:LocalName><xsl:value-of select="."/></gco:LocalName>
                    </gmd:featureTypes>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:for-each select="GM03Comprehensive.Comprehensive.CI_Citation">
                <gmd:featureCatalogueCitation>
                    <xsl:apply-templates mode="Citation" select="."/>
                </gmd:featureCatalogueCitation>
            </xsl:for-each>

            <xsl:for-each select="dataModel">
                <che:dataModel xsi:type="che:PT_FreeURL_PropertyType">
                    <xsl:choose>
                        <xsl:when test="GM03Core.Core.PT_FreeURL">
                            <xsl:apply-templates mode="language" select="GM03Core.Core.PT_FreeURL"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <gmd:URL><xsl:value-of select="."/></gmd:URL>
                        </xsl:otherwise>
                    </xsl:choose>
                </che:dataModel>
            </xsl:for-each>
            <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription"/>
            <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription"/>
            <xsl:choose>
	            <xsl:when test="modelType">
	               <xsl:apply-templates mode="Content" select="modelType"/>
	            </xsl:when>
	            <xsl:otherwise>
			        <che:modelType>
			            <che:CHE_MD_modelTypeCode codeListValue="FeatureDescription" codeList="./resources/codeList.xml#che:CHE_MD_modelTypeCode" />
			        </che:modelType>
		        </xsl:otherwise>
	        </xsl:choose>
            <xsl:apply-templates mode="Content" select="GM03_2Comprehensive.Comprehensive.CI_Citation"/>
        </che:CHE_MD_FeatureCatalogueDescription>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="domain">
        <che:domain>
            <xsl:apply-templates mode="Content"/>
        </che:domain>
    </xsl:template>

    <xsl:template mode="Content" match="modelType">
        <che:modelType>
            <che:CHE_MD_modelTypeCode codeListValue="{.}" codeList="./resources/codeList.xml#che:CHE_MD_modelTypeCode" />
        </che:modelType>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_CodeDomain">
        <che:CHE_MD_CodeDomain>
            <xsl:apply-templates mode="textCHE" select="name"/>
            <xsl:apply-templates mode="textCHE" select="description"/>
            <xsl:apply-templates mode="Content" select="type"/>
            <xsl:apply-templates mode="Content" select="subDomain"/>
            <xsl:apply-templates mode="Content" select="baseDomain"/>
        </che:CHE_MD_CodeDomain>
    </xsl:template>

    <xsl:template mode="Content" match="subDomain|baseDomain">
        <xsl:element name="che:{local-name()}">
            <xsl:apply-templates mode="Content"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="Content" match="type">
        <che:type>
            <xsl:apply-templates mode="Content"/>
        </che:type>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_Type">
        <!--<che:CHE_MD_Type>-->
            <xsl:apply-templates mode="textCHE" select="type"/>
            <xsl:for-each select="GM03Comprehensive.Comprehensive.MD_CodeValue">
                <che:value>
                    <xsl:apply-templates mode="Content" select="."/>
                </che:value>
            </xsl:for-each>
        <!--</che:CHE_MD_Type>-->
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_CodeValue">
        <che:CHE_MD_CodeValue>
            <xsl:apply-templates mode="textCHE" select="name"/>
            <xsl:apply-templates mode="textCHE" select="code"/>
            <xsl:apply-templates mode="textCHE" select="description"/>
            <xsl:for-each select="GM03Comprehensive.Comprehensive.MD_CodeValue">
                <che:subValue>
                    <xsl:apply-templates mode="Content" select="."/>
                </che:subValue>
            </xsl:for-each>
        </che:CHE_MD_CodeValue>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="class">
        <che:class>
            <xsl:apply-templates mode="Content"/>
        </che:class>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_Class">
        <che:CHE_MD_Class>
            <xsl:apply-templates mode="textCHE" select="name"/>
            <che:description>
                <xsl:apply-templates mode="language" select="description/*"/>
            </che:description>
            <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.MD_Attribute"/>
            <xsl:apply-templates mode="Content" select=".//baseClass"/>   <!-- TODO -->
            <xsl:apply-templates mode="Content" select=".//subClass"/>   <!-- TODO -->
        </che:CHE_MD_Class>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_Attribute">
        <che:attribute>
            <xsl:apply-templates mode="textCHE" select="name"/>
            <xsl:apply-templates mode="textCHE" select="description"/>
            <che:namedType>
                <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.MD_AttributenamedType"/>
            </che:namedType>
            <xsl:apply-templates mode="Content" select="anonymousType"/>
        </che:attribute>
    </xsl:template>

    <xsl:template mode="Content" match="GM03Comprehensive.Comprehensive.MD_AttributenamedType">
            <xsl:apply-templates mode="Content" select="namedType/GM03Comprehensive.Comprehensive.MD_CodeDomain"/>
    </xsl:template>

    <xsl:template mode="Content" match="anonymousType">
        <che:anonymousType>
            <che:CHE_MD_Type>
                <xsl:apply-templates mode="textCHE" select="GM03Comprehensive.Comprehensive.MD_Type/type"/>
                <che:value>
                    <xsl:apply-templates mode="Content" select="GM03Comprehensive.Comprehensive.MD_Type/GM03Comprehensive.Comprehensive.MD_CodeValue"/>
                </che:value>
            </che:CHE_MD_Type>
        </che:anonymousType>
    </xsl:template>

    <xsl:template mode="Content" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Content</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
