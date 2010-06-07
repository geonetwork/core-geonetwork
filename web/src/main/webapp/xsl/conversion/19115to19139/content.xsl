<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ContInfoTypes">

		<xsl:for-each select="CovDesc">
			<MD_CoverageDescription>
				<xsl:apply-templates select="." mode="CovDesc"/>
			</MD_CoverageDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="FetCatDesc">
			<MD_FeatureCatalogueDescription>
				<xsl:apply-templates select="." mode="FetCatDesc"/>
			</MD_FeatureCatalogueDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ImgDesc">
			<MD_ImageDescription>
				<xsl:apply-templates select="." mode="ImgDesc"/>
			</MD_ImageDescription>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === CovDesc === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="CovDesc">

		<attributeDescription>
			<gco:RecordType><xsl:value-of select="attDesc"/></gco:RecordType>
		</attributeDescription>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<contentType>
			<MD_CoverageContentTypeCode codeList="./resources/codeList.xml#MD_CoverageContentTypeCode" codeListValue="{contentTyp/ContentTypCd/@value}" />
		</contentType>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="covDim">
			<dimension>
				<xsl:apply-templates select="." mode="RangeDimTypes"/>
			</dimension>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RangeDimTypes">

		<xsl:for-each select="RangeDim">
			<MD_RangeDimension>
				<xsl:apply-templates select="." mode="RangeDim"/>
			</MD_RangeDimension>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="Band">
			<MD_Band>
				<xsl:apply-templates select="." mode="Band"/>
			</MD_Band>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RangeDim">

		<xsl:for-each select="seqID">
			<sequenceIdentifier>
				<gco:MemberName>
					<xsl:apply-templates select="." mode="MemberName"/>
				</gco:MemberName>
			</sequenceIdentifier>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dimDescrp">
			<descriptor>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</descriptor>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="MemberName">

		<gco:aName>
			<gco:CharacterString><xsl:value-of select="aName"/></gco:CharacterString>
		</gco:aName>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<gco:attributeType>
			<gco:TypeName>
				<gco:aName>
					<gco:CharacterString><xsl:value-of select="attributeType/aName"/></gco:CharacterString>
				</gco:aName>
			</gco:TypeName>
		</gco:attributeType>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Band">

		<xsl:apply-templates select="." mode="RangeDim"/>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="maxVal">
			<maxValue>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</maxValue>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="minVal">
			<minValue>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</minValue>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="valUnit">
			<units>
				<gml:UnitDefinition>
					<xsl:apply-templates select="." mode="UomLength"/>
				</gml:UnitDefinition>
			</units>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="pkResp">
			<peakResponse>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</peakResponse>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="bitsPerVal">
			<bitsPerValue>
				<gco:Integer><xsl:value-of select="."/></gco:Integer>
			</bitsPerValue>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="toneGrad">
			<toneGradation>
				<gco:Integer><xsl:value-of select="."/></gco:Integer>
			</toneGradation>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="sclFac">
			<scaleFactor>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</scaleFactor>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="offset">
			<offset>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</offset>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="UomLength">

		<gml:name>
			<xsl:value-of select="uomName"/>
		</gml:name>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === FetCatDesc === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="FetCatDesc">

		<xsl:for-each select="compCode">
			<complianceCode>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</complianceCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="catLang">
			<language>
				<gco:CharacterString><xsl:value-of select="languageCode/@value"/></gco:CharacterString>
			</language>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<includedWithDataset>
			<gco:Boolean><xsl:value-of select="incWithDS"/></gco:Boolean>
		</includedWithDataset>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="catFetTypes">
			<featureTypes>
				<xsl:apply-templates select="." mode="GenericNameTypes"/>
			</featureTypes>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="catCitation">
			<featureCatalogueCitation>
				<CI_Citation>
					<xsl:apply-templates select="idCitation" mode="Citation"/>
				</CI_Citation>
			</featureCatalogueCitation>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="GenericNameTypes">

		<xsl:for-each select="LocalName">
			<gco:LocalName><xsl:value-of select="scope"/></gco:LocalName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ScopedName">
			<gco:ScopedName><xsl:value-of select="scope"/></gco:ScopedName>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === ImgDesc === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ImgDesc">

		<xsl:apply-templates select="." mode="CovDesc"/>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="illElevAng">
			<illuminationElevationAngle>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</illuminationElevationAngle>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="illAziAng">
			<illuminationAzimuthAngle>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</illuminationAzimuthAngle>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="imagCond">
			<imagingCondition>
				<MD_ImagingConditionCode codeList="./resources/codeList.xml#MD_ImagingConditionCode" codeListValue="{ImgCondCd/@value}" />
			</imagingCondition>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="imagQuCode">
			<imageQualityCode>
				<xsl:apply-templates select="." mode="MdIdentTypes"/>
			</imageQualityCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="cloudCovPer">
			<cloudCoverPercentage>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</cloudCoverPercentage>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="prcTypCde">
			<processingLevelCode>
				<xsl:apply-templates select="." mode="MdIdentTypes"/>
			</processingLevelCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="cmpGenQuan">
			<compressionGenerationQuantity>
				<gco:Integer><xsl:value-of select="."/></gco:Integer>
			</compressionGenerationQuantity>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="trianInd">
			<triangulationIndicator>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</triangulationIndicator>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="radCalDatAv">
			<radiometricCalibrationDataAvailability>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</radiometricCalibrationDataAvailability>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="camCalInAv">
			<cameraCalibrationInformationAvailability>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</cameraCalibrationInformationAvailability>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="filmDistInAv">
			<filmDistortionInformationAvailability>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</filmDistortionInformationAvailability>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="lensDistInAv">
			<lensDistortionInformationAvailability>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</lensDistortionInformationAvailability>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
