<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                    xmlns:gco="http://www.isotc211.org/2005/gco"
                    xmlns:gml="http://www.opengis.net/gml"
                    xmlns:srv="http://www.isotc211.org/2005/srv"
                    xmlns:ADO="http://www.defence.gov.au/ADO_DM_MDP"
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:date="http://exslt.org/dates-and-times"
                    xmlns:che="http://www.geocat.ch/2008/che"
                    xmlns:java="java:org.fao.geonet.util.XslUtil"
                    xmlns:joda="java:org.fao.geonet.util.JODAISODate"
                    xmlns:mime="java:org.fao.geonet.util.MimeTypeFinder"
                    exclude-result-prefixes="java">

	<!-- ================================================================== -->

	<xsl:template name="fixSingle">
    <xsl:param name="value"/>

    <xsl:choose>
      <xsl:when test="string-length(string($value))=1">
        <xsl:value-of select="concat('0',$value)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

	<!-- ================================================================== -->

	<xsl:template name="getMimeTypeFile">
    <xsl:param name="datadir"/>
    <xsl:param name="fname"/>
		<xsl:value-of select="mime:detectMimeTypeFile($datadir,$fname)"/>
  </xsl:template>

<!-- ==================================================================== -->

	<xsl:template name="getMimeTypeUrl">
    <xsl:param name="linkage"/>
		<xsl:value-of select="mime:detectMimeTypeUrl($linkage)"/>
  </xsl:template>

<!-- ==================================================================== -->
	<xsl:template name="fixNonIso">
		<xsl:param name="value"/>

		<xsl:variable name="now" select="date:date-time()"/>
		<xsl:choose>
		<xsl:when test="$value='' or lower-case($value)='unknown' or lower-case($value)='current' or lower-case($value)='now'">
			<xsl:variable name="miy" select="date:month-in-year($now)"/>
			<xsl:variable name="month">
				<xsl:call-template name="fixSingle">
					<xsl:with-param name="value" select="$miy" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="dim" select="date:day-in-month($now)"/>
			<xsl:variable name="day">
				<xsl:call-template name="fixSingle">
					<xsl:with-param name="value" select="$dim" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:value-of select="concat(date:year($now),'-',$month,'-',$day,'T23:59:59')"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$value"/>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- ==================================================================== -->

	<xsl:template name="newGmlTime">
		<xsl:param name="begin"/>
		<xsl:param name="end"/>


		<xsl:variable name="value1">
			<xsl:call-template name="fixNonIso">
				<xsl:with-param name="value" select="normalize-space($begin)"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="value2">
			<xsl:call-template name="fixNonIso">
				<xsl:with-param name="value" select="normalize-space($end)"/>
			</xsl:call-template>
		</xsl:variable>

		<!-- must be a full ISODateTimeFormat - so parse it and make sure it is 
		     returned as a long format using the joda Java Time library -->
		<xsl:variable name="output" select="joda:parseISODateTimes($value1,$value2)"/>
		<xsl:value-of select="$output"/>
		
	</xsl:template>

    <!-- ================================================================== -->

    <xsl:template name="langId19139">
        <xsl:variable name="tmp">
            <xsl:choose>
                <xsl:when test="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue">
                    <xsl:value-of select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="$defaultLang"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="java:twoCharLangCode(normalize-space(string($tmp)))"></xsl:value-of>
    </xsl:template>

	<xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
	<xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <!-- iso3code of default index language -->
    <xsl:variable name="defaultLang">en</xsl:variable>

    <xsl:template name="defaultTitle">
        <xsl:param name="isoDocLangId"/>
        
        <xsl:variable name="poundLangId" select="concat('#',translate($isoDocLangId,$LOWER, $UPPER))" />

        <xsl:choose>
        <xsl:when    test="string-length(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification']/gmd:citation//gmd:title//gmd:LocalisedCharacterString[@locale=$poundLangId and string-length(.) > 0]]) != 0">
            <xsl:value-of select="string(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification']/gmd:citation//gmd:title//gmd:LocalisedCharacterString[@locale=$poundLangId and string-length(.) > 0]])"></xsl:value-of>
        </xsl:when>
        <xsl:when    test="string-length(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification']/gmd:citation//gmd:title/gco:CharacterString[1]) != 0">
            <xsl:value-of select="string(/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification']/gmd:citation//gmd:title/gco:CharacterString[1])"></xsl:value-of>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="string((/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification']/gmd:citation//gmd:title//gmd:LocalisedCharacterString))"></xsl:value-of>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================================================================== -->

</xsl:stylesheet>
