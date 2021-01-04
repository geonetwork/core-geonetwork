<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
	xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:gml="http://www.opengis.net/gml/3.2"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="#all">

	<xsl:include href="metadata-utils.xsl"/>
	<xsl:include href="metadata-geo.xsl"/>
  <xsl:include href="metadata-fop.xsl"/>
  <xsl:include href="metadata-subtemplates.xsl"/>
  
	<!-- main template - the way into processing iso19115-3.2018 -->
	<xsl:template name="metadata-iso19115-3.2018">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<xsl:apply-templates mode="iso19115-3.2018" select="." >
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="embedded" select="$embedded" />
		</xsl:apply-templates>
	</xsl:template>

	<!-- =================================================================== -->
	<!-- default: in simple mode just a flat list -->
	<!-- =================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<!-- do not show empty elements in view mode -->
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="flat"   select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
				</xsl:apply-templates>
			</xsl:when>

			<xsl:otherwise>
				<xsl:variable name="empty">
					<xsl:apply-templates mode="iso19115-3.2018IsEmpty" select="."/>
				</xsl:variable>

				<xsl:if test="$empty!=''">
					<xsl:apply-templates mode="element" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="false()"/>
						<xsl:with-param name="flat"   select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
					</xsl:apply-templates>
				</xsl:if>

			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>


	<!--=====================================================================-->
	<!-- these elements should not be displayed 
		* do not display graphicOverview managed by GeoNetwork (ie. having a 
		fileDescription set to thumbnail or large_thumbnail). Those thumbnails
		are managed in then thumbnail popup. Others could be valid URL pointing to
		an image available on the Internet.
	-->
	<!--=====================================================================-->

	<xsl:template mode="iso19115-3.2018"
		match="mdb:graphicOverview[mcc:MD_BrowseGraphic/mcc:fileDescription/gco:CharacterString='thumbnail' or mcc:MD_BrowseGraphic/mcc:fileDescription/gco:CharacterString='large_thumbnail']"
		priority="20" />

	<!-- Do not try do display element with no children in view mode -->
	<!-- Usually this should not happen because GeoNetwork will add default children like gco:CharacterString. 
		 Fixed #299
		 TODO : metadocument contains geonet:element which is probably not required ?
	-->
	<xsl:template mode="iso19115-3.2018" priority="199" match="*[@gco:nilReason='missing' and geonet:element and count(*)=1]"/>

	<xsl:template mode="iso19115-3.2018" priority="199" match="*[geonet:element and count(*)=1 and text()='']"/>
	
	<!-- ===================================================================== -->
	<!-- these elements should be boxed -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="mdb:identificationInfo|mdb:distributionInfo|mri:descriptiveKeywords|mri:thesaurusName|mdb:spatialRepresentationInfo|mri:pointOfContact|mdb:dataQualityInfo|mdb:resourceLineage|mdb:referenceSystemInfo|mri:equivalentScale|msr:projection|mdb:extent|cit:extent|gex:geographicBox|gex:EX_TemporalExtent|mrd:MD_Distributor|srv:containsOperations|srv:SV_CoupledResource|mdb:metadataConstraints">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ===================================================================== -->
	<!-- some gco: elements and gcx:MimeFileType are swallowed -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="*[gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gcx:MimeFileType]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="gco:ScopedName|gco:LocalName">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:variable name="text">
			<xsl:call-template name="getElementText">
				<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema"   select="$schema"/>
			<xsl:with-param name="edit"     select="$edit"/>
			<xsl:with-param name="title"    select="'Name'"/>
			<xsl:with-param name="text"     select="$text"/>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ==================================================================== -->

	<!-- GML time interval -->
	<xsl:template mode="iso19115-3.2018" match="gml:timeInterval">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:choose>
			<xsl:when test="$edit">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="title"    select="/root/gui/schemas/iso19139/labels/element[@name='gml:timeInterval']/label"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="text">
					<xsl:choose>
						<xsl:when test="@radix and @factor"><xsl:value-of select=". * @factor div @radix"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
						<xsl:when test="@factor"><xsl:value-of select=". * @factor"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
						<xsl:when test="@radix"><xsl:value-of select=". div @radix"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
						<xsl:otherwise><xsl:value-of select="."/>&#160;<xsl:value-of select="@unit"/></xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="title"    select="/root/gui/schemas/iso19139/labels/element[@name='gml:timeInterval']/label"/>
					<xsl:with-param name="text"     select="$text"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ==================================================================== -->

	<!-- Display element attributes only in edit mode 
		* GML time interval 
	-->
	<xsl:template mode="simpleAttribute" match="gml:timeInterval/@*" priority="99">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:choose>
			<xsl:when test="$edit">
				<xsl:call-template name="simpleAttribute">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="simpleAttribute" match="@xsi:type" priority="99"/>

	<!-- ================================================================= -->
	<!-- some elements that have both attributes and content               -->
	<!-- ================================================================= -->

	<xsl:template mode="iso19115-3.2018" match="gml:identifier|gml:axisDirection|gml:descriptionReference">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema"   select="$schema"/>
			<xsl:with-param name="edit"   	select="$edit"/>
			<xsl:with-param name="content">

				<!-- existing attributes -->
				<xsl:apply-templates mode="simpleElement" select="@*">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<!-- existing content -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- codelists -->
	<!-- ================================================================= -->

	<xsl:template mode="iso19115-3.2018" match="*[*/@codeList]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:call-template name="iso19115-3.2018Codelist">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018Codelist">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:apply-templates mode="iso19115-3.2018GetAttributeText" select="*/@codeListValue">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19115-3.2018GetAttributeText" match="@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:variable name="name"     select="local-name(..)"/>
		<xsl:variable name="qname"    select="name(..)"/>
		<xsl:variable name="value"    select="../@codeListValue"/>

		<xsl:choose>
			<xsl:when test="$qname='lan:LanguageCode'">
				<xsl:apply-templates mode="iso19115-3.2018" select="..">
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<!--
					Get codelist from profile first and use use default one if not
					available.
				-->
				<xsl:variable name="codelistProfile">
					<xsl:choose>
						<xsl:when test="starts-with($schema,'iso19115-3.2018.')">
							<xsl:copy-of
								select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $qname]/*" />
						</xsl:when>
						<xsl:otherwise />
					</xsl:choose>
				</xsl:variable>

				<xsl:variable name="codelistCore">
					<xsl:choose>
						<xsl:when test="normalize-space($codelistProfile)!=''">
							<xsl:copy-of select="$codelistProfile" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of
								select="/root/gui/schemas/*[name(.)='iso19115-3.2018']/codelists/codelist[@name = $qname]/*" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>

				<xsl:variable name="codelist" select="exslt:node-set($codelistCore)" />
				<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />

				<xsl:choose>
					<xsl:when test="$edit=true()">
						<!-- codelist in edit mode -->
						<select class="md" name="_{../geonet:element/@ref}_{name(.)}" id="_{../geonet:element/@ref}_{name(.)}" size="1">
							<!-- Check element is mandatory or not -->
							<xsl:if test="../../geonet:element/@min='1' and $edit">
								<xsl:attribute name="onchange">validateNonEmpty(this);</xsl:attribute>
							</xsl:if>
							<xsl:if test="$isXLinked">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
							<option name=""/>
							<xsl:for-each select="$codelist/entry[not(@hideInEditMode)]">
								<xsl:sort select="label"/>
								<option>
									<xsl:if test="code=$value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
									<xsl:value-of select="label"/>
								</option>
							</xsl:for-each>
						</select>
					</xsl:when>
					<xsl:otherwise>
						<!-- codelist in view mode -->
						<xsl:if test="normalize-space($value)!=''">
							<b><xsl:value-of select="$codelist/entry[code = $value]/label"/></b>
							<xsl:value-of select="concat(': ',$codelist/entry[code = $value]/description)"/>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19115-3.2018" match="mdb:metadataIdentifier[position() = 1]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="false()"/>
			<xsl:with-param name="text">
				<xsl:choose>
					<xsl:when test="normalize-space(mcc:MD_Identifier/mcc:code/gco:*)=''">
						<span class="info">
							- <xsl:value-of select="/root/gui/strings/setOnSave"/> - 
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="mcc:MD_Identifier/mcc:code/gco:*"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19115-3.2018" match="cit:date[gco:DateTime|gco:Date]|cit:editionDate" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="text">
						<xsl:variable name="ref" select="gco:DateTime/geonet:element/@ref|gco:Date/geonet:element/@ref"/>
						<xsl:variable name="format">
							<xsl:choose>
								<xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
								<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
							</xsl:choose>
						</xsl:variable>

						<xsl:call-template name="calendar">
							<xsl:with-param name="ref" select="$ref"/>
							<xsl:with-param name="date" select="gco:DateTime/text()|gco:Date/text()"/>
							<xsl:with-param name="format" select="$format"/>
						</xsl:call-template>

					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="iso19139String">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ===================================================================== -->
	<!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="gml:*[gml:beginPosition|gml:endPosition]|gml:TimeInstant[gml:timePosition]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="$edit=true() and (name(.)='gml:beginPosition' or name(.)='gml:endPosition' or name(.)='gml:timePosition')">
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="text">
							<xsl:variable name="ref" select="geonet:element/@ref"/>
							<xsl:variable name="format"><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:variable>

							<xsl:call-template name="calendar">
								<xsl:with-param name="ref" select="$ref"/>
								<xsl:with-param name="date" select="text()"/>
								<xsl:with-param name="format" select="$format"/>
							</xsl:call-template>

						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:when test="name(.)='gml:timeInterval'">
					<xsl:apply-templates mode="iso19115-3.2018" select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="text">
							<xsl:value-of select="text()"/>
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<!-- =================================================================== -->
	<!-- subtemplates -->
	<!-- =================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="*[geonet:info/isTemplate='s']" priority="3">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="@gco:isoType"/>

	<!-- ==================================================================== -->
	<!-- Metadata -->
	<!-- ==================================================================== -->

	<xsl:template mode="iso19115-3.2018" match="mdb:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded"/>

		<xsl:variable name="dataset" select="mdb:metadataScope/mcc:MD_ScopeCode/@codeListValue='dataset' or normalize-space(mdb:metadataScope/mcc:MD_ScopeCode/@codeListValue)=''"/>

		<!-- thumbnail -->
		<tr>
			<td valign="middle" colspan="2">
				<xsl:if test="$currTab='metadata' or $currTab='identification' or /root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat">
					<div style="float:left;width:70%;text-align:center;">
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:call-template name="thumbnail">
							<xsl:with-param name="metadata" select="$metadata"/>
						</xsl:call-template>
					</div>
				</xsl:if>
				<xsl:if test="/root/gui/config/editor-metadata-relation">
					<div style="float:right;">
						<xsl:call-template name="relatedResources19115-3.2018">
							<xsl:with-param name="edit" select="$edit"/>
						</xsl:call-template>
					</div>
				</xsl:if>
			</td>
		</tr>

		<xsl:choose>

			<!-- metadata tab -->
			<xsl:when test="$currTab='metadata'">
				<xsl:call-template name="iso19115-3.2018Metadata">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- identification tab -->
			<xsl:when test="$currTab='identification'">
				<xsl:apply-templates mode="elementEP" select="mdb:identificationInfo|geonet:child[string(@name)='identificationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- maintenance tab -->
			<xsl:when test="$currTab='maintenance'">
				<xsl:apply-templates mode="elementEP" select="mdb:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- constraints tab -->
			<xsl:when test="$currTab='constraints'">
				<xsl:apply-templates mode="elementEP" select="mdb:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- spatial tab -->
			<xsl:when test="$currTab='spatial'">
				<xsl:apply-templates mode="elementEP" select="mdb:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- refSys tab -->
			<xsl:when test="$currTab='refSys'">
				<xsl:apply-templates mode="elementEP" select="mdb:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- distribution tab -->
			<xsl:when test="$currTab='distribution'">
				<xsl:apply-templates mode="elementEP" select="mdb:distributionInfo|geonet:child[string(@name)='distributionInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- embedded distribution tab -->
			<xsl:when test="$currTab='distribution2'">
				<xsl:apply-templates mode="elementEP" select="mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- dataQuality tab -->
			<xsl:when test="$currTab='dataQuality'">
				<xsl:apply-templates mode="elementEP" select="mdb:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- lineage tab -->
			<xsl:when test="$currTab='resourceLineage'">
				<xsl:apply-templates mode="elementEP" select="mdb:resourceLineage|geonet:child[string(@name)='resourceLineage']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- appSchInfo tab -->
			<xsl:when test="$currTab='appSchInfo'">
				<xsl:apply-templates mode="elementEP" select="mdb:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- porCatInfo tab -->
			<xsl:when test="$currTab='porCatInfo'">
				<xsl:apply-templates mode="elementEP" select="mdb:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- contentInfo tab -->
			<xsl:when test="$currTab='contentInfo'">
				<xsl:apply-templates mode="elementEP" select="mdb:contentInfo|geonet:child[string(@name)='contentInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- extensionInfo tab -->
			<xsl:when test="$currTab='extensionInfo'">
				<xsl:apply-templates mode="elementEP" select="mdb:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- ISOMinimum tab -->
			<xsl:when test="$currTab='ISOMinimum'">
				<xsl:call-template name="iso19115-3.2018tabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="false()"/>
				</xsl:call-template>
			</xsl:when>

			<!-- ISOCore tab -->
			<xsl:when test="$currTab='ISOCore'">
				<xsl:call-template name="iso19115-3.2018tabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="true()"/>
				</xsl:call-template>
			</xsl:when>

			<!-- ISOAll tab -->
			<xsl:when test="$currTab='ISOAll'">
				<xsl:call-template name="iso19115-3.2018Complete">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- default -->
			<xsl:otherwise>
				<xsl:call-template name="iso19115-3.2018Simple">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018tabs">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="dataset"/>
		<xsl:param name="core"/>

		<!-- dataset or resource info in its own box -->

		<xsl:for-each select="mdb:identificationInfo/*">
			<xsl:call-template name="complexElementGuiWrapper">
				<xsl:with-param name="title">
					<xsl:choose>
						<xsl:when test="$dataset=true()">
							<xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='mri:MD_DataIdentification']/label"/>
						</xsl:when>
						<xsl:when test="local-name(.)='SV_ServiceIdentification' or contains(@gco:isoType, 'SV_ServiceIdentification')">
							<xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='srv:SV_ServiceIdentification']/label"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="'Resource Identification'"/><!-- FIXME i18n-->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
				<xsl:with-param name="content">

					<xsl:apply-templates mode="elementEP" select="mri:citation/cit:CI_Citation/cit:title|mri:citation/cit:CI_Citation/geonet:child[string(@name)='title']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mri:citation/cit:CI_Citation/cit:date|cit:citation/cit:CI_Citation/geonet:child[string(@name)='date']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mri:abstract|geonet:child[string(@name)='abstract']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mri:pointOfContact|geonet:child[string(@name)='pointOfContact']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mri:descriptiveKeywords|geonet:child[string(@name)='descriptiveKeywords']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:if test="$core and $dataset">
						<xsl:apply-templates mode="elementEP" select="mri:spatialRepresentationType|geonet:child[string(@name)='spatialRepresentationType']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mri:spatialResolution|geonet:child[string(@name)='spatialResolution']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mri:temporalResolution|geonet:child[string(@name)='temporalResolution']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:if>

					<xsl:apply-templates mode="elementEP" select="mri:defaultLocale|geonet:child[string(@name)='defaultLocale']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mri:topicCategory|geonet:child[string(@name)='topicCategory']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:if test="$dataset">
						<xsl:for-each select="mri:extent/gex:EX_Extent">
							<xsl:call-template name="complexElementGuiWrapper">
								<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:EX_Extent']/label"/>
								<xsl:with-param name="content">
									<xsl:apply-templates mode="elementEP" select="*">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
								</xsl:with-param>
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="realname"   select="'gmd:EX_Extent'"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:if>

				</xsl:with-param>
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="realname"   select="name(.)"/>
			</xsl:call-template>
		</xsl:for-each>

		<xsl:if test="$core and $dataset">

		<!-- scope and lineage in their own box -->

			<xsl:call-template name="complexElementGuiWrapper">
				<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:LI_Lineage']/label"/>
				<xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:LI_Lineage']/label)"/>
				<xsl:with-param name="content">

					<xsl:for-each select="mdb:dataQualityInfo/dqm:DQ_DataQuality">
						<xsl:apply-templates mode="elementEP" select="dqm:scope|geonet:child[string(@name)='scope']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:resourceLineage|geonet:child[string(@name)='resourceLineage']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:for-each>

				</xsl:with-param>
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="group" select="/root/gui/strings/dataQualityTab"/>
				<xsl:with-param name="edit" select="$edit"/>
				<xsl:with-param name="realname"   select="'gmd:dataQualityInfo'"/>
			</xsl:call-template>

			<!-- referenceSystemInfo in its own box -->

			<xsl:call-template name="complexElementGuiWrapper">
				<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label"/>
				<xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label)"/>
				<xsl:with-param name="content">

				<xsl:for-each select="mdb:referenceSystemInfo/mrs:MD_ReferenceSystem">
					<xsl:apply-templates mode="elementEP" select="mrs:referenceSystemIdentifier/mcc:MD_Identifier/mcc:code|mrs:referenceSystemIdentifier/mcc:MD_Identifier/geonet:child[string(@name)='code']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mrs:referenceSystemIdentifier/mcc:MD_Identifier/mcc:codeSpace|mrs:referenceSystemIdentifier/mcc:MD_Identifier/geonet:child[string(@name)='codeSpace']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:for-each>

				</xsl:with-param>
				<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="group" select="/root/gui/strings/refSysTab"/>
      	<xsl:with-param name="edit" select="$edit"/>
				<xsl:with-param name="realname"   select="'gmd:referenceSystemInfo'"/>
			</xsl:call-template>

			<!-- distribution Format and onlineResource(s) in their own box -->

    	<xsl:call-template name="complexElementGuiWrapper">
    		<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:distributionInfo']/label"/>
    		<xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:distributionInfo']/label)"/>
      	<xsl:with-param name="content">

				<xsl:for-each select="mdb:distributionInfo">
        	<xsl:apply-templates mode="elementEP" select="*/mrd:distributionFormat|*/geonet:child[string(@name)='distributionFormat']">
          	<xsl:with-param name="schema" select="$schema"/>
          	<xsl:with-param name="edit"   select="$edit"/>
        	</xsl:apply-templates>

        	<xsl:apply-templates mode="elementEP" select="*/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine|*/mrd:transferOptions/mrd:MD_DigitalTransferOptions/geonet:child[string(@name)='onLine']">
          	<xsl:with-param name="schema" select="$schema"/>
          	<xsl:with-param name="edit"   select="$edit"/>
        	</xsl:apply-templates>
				</xsl:for-each>

      	</xsl:with-param>
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="group" select="/root/gui/strings/distributionTab"/>
      	<xsl:with-param name="edit" select="$edit"/>
      	<xsl:with-param name="realname" select="'mdb:distributionInfo'"/>
    	</xsl:call-template>
			
		</xsl:if>

		<!-- metadata info in its own box -->

		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_Metadata']/label"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_Metadata']/label)"/>
			<xsl:with-param name="content">

				<xsl:apply-templates mode="elementEP" select="mdb:metadataIdentifier|geonet:child[string(@name)='metadataIdentifier']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:parentMetadata|geonet:child[string(@name)='parentMetadata']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:metadataScope|geonet:child[string(@name)='metadataScope']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<!-- metadata contact info in its own box -->

				<xsl:for-each select="mdb:contact">

					<xsl:call-template name="complexElementGuiWrapper">
						<xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:contact']/label"/>
						<xsl:with-param name="content">

							<xsl:apply-templates mode="elementEP" select="*/cit:individualName|*/geonet:child[string(@name)='individualName']">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

							<xsl:apply-templates mode="elementEP" select="*/cit:organisationName|*/geonet:child[string(@name)='organisationName']">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

							<xsl:apply-templates mode="elementEP" select="*/cit:positionName|*/geonet:child[string(@name)='positionName']">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

							<xsl:if test="$core and $dataset">
								<xsl:apply-templates mode="elementEP" select="*/cit:contactInfo|*/geonet:child[string(@name)='contactInfo']">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
							</xsl:if>

							<xsl:apply-templates mode="elementEP" select="*/cit:role|*/geonet:child[string(@name)='role']">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

						</xsl:with-param>
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="group" select="/root/gui/strings/metadata"/>
						<xsl:with-param name="edit" select="$edit"/>
					</xsl:call-template>

				</xsl:for-each>

				<!-- more metadata elements -->

				<xsl:apply-templates mode="elementEP" select="mdb:dateInfo|geonet:child[string(@name)='dateInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:if test="$core and $dataset">
					<xsl:apply-templates mode="elementEP" select="mdb:metadataStandard|geonet:child[string(@name)='metadataStandard']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="mdb:metadataProfile|geonet:child[string(@name)='metadataProfile']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:if>

			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="group" select="/root/gui/strings/metadataTab"/>
			<xsl:with-param name="edit" select="$edit"/>
		</xsl:call-template>

	</xsl:template>

	<!-- ================================================================== -->
	<!-- complete mode we just display everything - tab = complete          -->
	<!-- ================================================================== -->

	<xsl:template name="iso19115-3.2018Complete">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="mdb:identificationInfo|geonet:child[string(@name)='identificationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:contentInfo|geonet:child[string(@name)='contentInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:distributionInfo|geonet:child[string(@name)='distributionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:resourceLineage|geonet:child[string(@name)='resourceLineage']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="'Metadata Info'"/>
			<xsl:with-param name="content">

				<xsl:apply-templates mode="elementEP" select="mdb:metadataIdentifier|geonet:child[string(@name)='metadataIdentifier']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:defaultLocale|geonet:child[string(@name)='defaultLocale']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:otherLocale|geonet:child[string(@name)='otherLocale']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:parentMetadata|geonet:child[string(@name)='parentMetadata']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:metadataScope|geonet:child[string(@name)='metadataScope']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:contact|geonet:child[string(@name)='contact']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:metadataStandard|geonet:child[string(@name)='metadataStandard']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:metadataProfile|geonet:child[string(@name)='metadataProfile']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:alternativeMetadataReference|geonet:child[string(@name)='alternativeMetadataReference']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="mdb:metadataLinkage|geonet:child[string(@name)='metadataLinkage']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="group" select="/root/gui/strings/metadataTab"/>
			<xsl:with-param name="edit" select="$edit"/>
		</xsl:call-template>

	<!-- metadata Extension Information - dead last because its boring and
		 can clutter up the rest of the metadata record! -->

		<xsl:apply-templates mode="elementEP" select="mdb:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

	</xsl:template>


	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018Metadata">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
		<xsl:variable name="validationLink">
			<xsl:call-template name="validationLink">
				<xsl:with-param name="ref" select="$ref"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/metadata"/>
			<xsl:with-param name="validationLink" select="$validationLink"/>

			<xsl:with-param name="helpLink">
			  <xsl:call-template name="getHelpLink">
			      <xsl:with-param name="name" select="name(.)"/>
			      <xsl:with-param name="schema" select="$schema"/>
			  </xsl:call-template>
			</xsl:with-param>

			<xsl:with-param name="edit" select="true()"/>
			<xsl:with-param name="content">

				<!-- if the parent is root then display fields not in tabs -->
				<xsl:choose>
					<xsl:when test="name(..)='root'">
						<xsl:apply-templates mode="elementEP" select="mdb:metadataIdentifier|geonet:child[string(@name)='metadataIdentifier']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:defaultLocale|geonet:child[string(@name)='defaultLocale']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:otherLocale|geonet:child[string(@name)='otherLocale']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:parentMetadata|geonet:child[string(@name)='parentMetadata']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:metadataScope|geonet:child[string(@name)='metadataScope']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:dateInfo|geonet:child[string(@name)='dateInfo']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:metadataStandard|geonet:child[string(@name)='metadataStandard']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:metadataProfile|geonet:child[string(@name)='metadataProfile']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:alternativeMetadataReference|geonet:child[string(@name)='alternativeMetadataReference']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="mdb:metadataLinkage|geonet:child[string(@name)='metadataLinkage']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

					</xsl:when>
					<!-- otherwise, display everything because we have embedded MD_Metadata -->
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="*">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>

			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>

	</xsl:template>

	<!-- ============================================================================= -->
	<!--
	simple mode; ISO order is:
	- mdb:metadataIdentifier
	- mdb:defaultLocale
	- mdb:parentMetadata
	- mdb:metadataScope
	- mdb:contact
	- mdb:dateInfo
	- mdb:metadataStandard
	- mdb:metadataProfile
	- mdb:alternativeMetadataReference
	- mdb:otherLocale
	- mdb:metadataLinkage
	- mdb:spatialRepresentationInfo
	- mdb:referenceSystemInfo
	- mdb:metadataExtensionInfo
	- mdb:identificationInfo
	- mdb:contentInfo
	- mdb:distributionInfo
	- mdb:dataQualityInfo
	- mdb:resourceLineage
	- mdb:portrayalCatalogueInfo
	- mdb:metadataConstraints
	- mdb:applicationSchemaInfo
	- mdb:metadataMaintenance
	-->
	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018Simple">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="mdb:identificationInfo|geonet:child[string(@name)='identificationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:distributionInfo|geonet:child[string(@name)='distributionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:resourceLineage|geonet:child[string(@name)='resourceLineage']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/metadata"/>
			<xsl:with-param name="content">
				<xsl:call-template name="iso19115-3.2018Simple2">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>

		<xsl:apply-templates mode="elementEP" select="mdb:contentInfo|geonet:child[string(@name)='contentInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018Simple2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataIdentifier|geonet:child[string(@name)='metadataIdentifier']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:defaultLocale|geonet:child[string(@name)='defaultLocale']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:otherLocale|geonet:child[string(@name)='otherLocale']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:parentMetadata|geonet:child[string(@name)='parentMetadata']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataScope|geonet:child[string(@name)='metadataScope']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataStandard|geonet:child[string(@name)='metadataStandard']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataProfile|geonet:child[string(@name)='metadataProfile']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:alternativeMetadataReference|geonet:child[string(@name)='alternativeMetadataReference']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataLinkage|geonet:child[string(@name)='metadataLinkage']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="mdb:contact|geonet:child[string(@name)='contact']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

	</xsl:template>

	<!-- ===================================================================== -->
	<!-- === iso19115-3.2018 brief formatting === -->
	<!-- ===================================================================== -->

	<xsl:template name="iso19115-3.2018Brief">
		<metadata>
			<xsl:choose>
				<xsl:when test="geonet:info/isTemplate='s'">
					<xsl:call-template name="iso19115-3.2018-subtemplate"/>
					<xsl:copy-of select="geonet:info" copy-namespaces="no"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="iso19115-3.2018-brief"/>
				</xsl:otherwise>
			</xsl:choose>
		</metadata>
	</xsl:template>


	<xsl:template name="iso19115-3.2018-brief">
		<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
		<xsl:variable name="info" select="geonet:info"/>
		<xsl:variable name="id" select="$info/id"/>
		<xsl:variable name="uuid" select="$info/uuid"/>

		<xsl:if test="normalize-space(mdb:parentMetadata/mcc:MD_Identifier/mcc:code/*)!=''">
			<parentId><xsl:value-of select="mdb:parentMetadata/mcc:MD_Identifier/mcc:code/*"/></parentId>
		</xsl:if>

		<xsl:variable name="langId">
			<xsl:call-template name="getLangId19115-3.2018">
				<xsl:with-param name="langGui" select="/root/gui/language"/>
				<xsl:with-param name="md" select="."/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:apply-templates mode="briefster" select="mdb:identificationInfo/*">
			<xsl:with-param name="id" select="$id"/>
			<xsl:with-param name="langId" select="$langId"/>
			<xsl:with-param name="info" select="$info"/>
		</xsl:apply-templates>

		<xsl:for-each select="mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine/cit:CI_OnlineResource">
			<xsl:variable name="protocol" select="cit:protocol[1]/gco:CharacterString"/>
			<xsl:variable name="linkage"  select="normalize-space(cit:linkage/*)"/>
			<xsl:variable name="name">
				<xsl:choose>
					<xsl:when test="cit:name/gcx:MimeFileType">
						<xsl:value-of select="cit:name/gcx:MimeFileType/text()"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="cit:name">
							<xsl:call-template name="localised19115-3.2018">
								<xsl:with-param name="langId" select="$langId"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:variable name="mimeType" select="normalize-space(cit:name/gcx:MimeFileType/@type)"/>

			<xsl:variable name="desc">
				<xsl:for-each select="cit:description">
					<xsl:call-template name="localised19115-3.2018">
						<xsl:with-param name="langId" select="$langId"/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:variable>

			<xsl:if test="string($linkage)!=''">

				<xsl:element name="link">
					<xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
					<xsl:attribute name="href"><xsl:value-of select="$linkage"/></xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
					<xsl:attribute name="protocol"><xsl:value-of select="$protocol"/></xsl:attribute>
					<xsl:attribute name="type" select="geonet:protocolMimeType($linkage, $protocol, $mimeType)"/>
				</xsl:element>

			</xsl:if>

			<!-- Generate a KML output link for a WMS service -->
			<xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($name)!=''">

				<xsl:element name="link">
					<xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
					</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
					<xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
				</xsl:element>
			</xsl:if>

			<!-- The old links still in use by some systems. Deprecated -->
			<xsl:choose>
				<!-- no protocol, but URL is for a WMS service -->
				<xsl:when test="(not(string($protocol)) and contains(upper-case($linkage),'SERVICE=WMS') and not(string($name)))">
					<link type="wms">
						<xsl:value-of select="concat('javascript:addWMSServerLayers(&#34;' ,  $linkage  ,  '&#34;)' )"/>
					</link>
				</xsl:when>
				<!-- no protocol, but URL is for a WMS service -->
				<xsl:when test="(not(string($protocol)) and contains(upper-case($linkage),'SERVICE=WMS') and string($name)!='')">
					<link type="wms">
						<xsl:value-of select="concat('javascript:addWMSLayer([[&#34;' , $name , '&#34;,&#34;' ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;,&#34;',$id,'&#34;]])')"/>
					</link>
				</xsl:when>
				<xsl:when test="matches($protocol,'^WWW:DOWNLOAD-.*-http--download.*') and not(contains($linkage,$download_check))">
					<link type="download"><xsl:value-of select="$linkage"/></link>
				</xsl:when>
				<xsl:when test="starts-with($protocol,'ESRI:AIMS-') and contains($protocol,'-get-image') and string($linkage)!='' and string($name)!=''">
					<link type="arcims">
						<xsl:value-of select="concat('javascript:runIM_addService(&#34;'  ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;, 1)' )"/>
					</link>
				</xsl:when>
				<xsl:when test="(starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!='') or ($protocol = 'OGC:WMS' and string($linkage)!='' and string($name)!='')">
					<link type="wms">
						<xsl:value-of select="concat('javascript:addWMSLayer([[&#34;' , $name , '&#34;,&#34;' ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;,&#34;',$id,'&#34;]])')"/>
					</link>
					<link type="googleearth">
						<xsl:value-of select="concat(/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
					</link>
				</xsl:when>
				<xsl:when test="(starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and not(string($name))) or ($protocol = 'OGC:WMS' and string($linkage)!='' and not(string($name)))">
					<link type="wms">
						<xsl:value-of select="concat('javascript:addWMSServerLayers(&#34;' ,  $linkage  ,  '&#34;)' )"/>
					</link>
				</xsl:when>
				<xsl:when test="(starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-capabilities') and string($linkage)!='') or ($protocol = 'OGC:WMS' and string($name)='' and string($linkage)!='')">
					<link type="wms">
						<xsl:value-of select="concat('javascript:addWMSServerLayers(&#34;' ,  $linkage  ,  '&#34;)' )"/>
					</link>
				</xsl:when>
				<xsl:when test="string($linkage)!=''">
					<link type="url"><xsl:value-of select="$linkage"/></link>
				</xsl:when>

			</xsl:choose>
		</xsl:for-each>

		<xsl:for-each select="mdb:contact/*">
			<xsl:variable name="role" select="cit:role/*/@codeListValue"/>
			<xsl:if test="normalize-space($role)!=''">
				<responsibleParty role="{geonet:getCodeListValue(/root/gui/schemas, 'iso19139', 'gmd:CI_RoleCode', $role)}" appliesTo="metadata">
					<xsl:apply-templates mode="responsiblepartysimple" select="."/>
				</responsibleParty>
			</xsl:if>
		</xsl:for-each>

		<metadatacreationdate>
			<xsl:value-of select="mdb:dateStamp/*"/>
		</metadatacreationdate>

		<geonet:info>
			<xsl:copy-of select="geonet:info/*[name(.)!='edit']"/>
			<xsl:choose>
				<xsl:when test="/root/gui/env/harvester/enableEditing='false' and geonet:info/isHarvested='y'">
					<edit>false</edit>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="geonet:info/edit"/>
				</xsl:otherwise>
			</xsl:choose>
			<!--
				Internal category could be define using different informations
				in a metadata record (according to standard). This could be improved.
				This type of categories could be added to Lucene index also in order
				to be queriable.
				Services and datasets are at least the required internal categories
				to be distinguished for INSPIRE requirements (hierarchyLevel could be
				use also). TODO
			-->
			<category internal="true">
				<xsl:choose>
					<xsl:when test="mdb:identificationInfo/srv:SV_ServiceIdentification">service</xsl:when>
					<xsl:otherwise>dataset</xsl:otherwise>
				</xsl:choose>
			</category>
		</geonet:info>
	</xsl:template>

	<xsl:template mode="briefster" match="mri:MD_DataIdentification">
		<xsl:param name="id"/>
		<xsl:param name="langId"/>
		<xsl:param name="info"/>

		<xsl:if test="mri:citation/*/cit:title">
			<title>
				<xsl:apply-templates mode="localised19115-3.2018" select="mri:citation/*/cit:title">
					<xsl:with-param name="langId" select="$langId"></xsl:with-param>
				</xsl:apply-templates>
			</title>
		</xsl:if>

		<xsl:if test="mri:citation/*/cit:date/*/cit:dateType/*[@codeListValue='creation']">
			<datasetcreationdate>
				<xsl:value-of select="mri:citation/*/cit:date/*/cit:date/gco:DateTime"/>
			</datasetcreationdate>
		</xsl:if>

		<xsl:if test="mri:abstract">
			<abstract>
				<xsl:apply-templates mode="localised19115-3.2018" select="mri:abstract">
					<xsl:with-param name="langId" select="$langId"></xsl:with-param>
				</xsl:apply-templates>
			</abstract>
		</xsl:if>

		<xsl:for-each select=".//mri:keyword[not(@gco:nilReason)]">
			<keyword>
				<xsl:apply-templates mode="localised19115-3.2018" select=".">
					<xsl:with-param name="langId" select="$langId"></xsl:with-param>
				</xsl:apply-templates>
			</keyword>
		</xsl:for-each>

		<xsl:for-each select="mri:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox">
			<geoBox>
				<westBL><xsl:value-of select="gex:westBoundLongitude"/></westBL>
				<eastBL><xsl:value-of select="gex:eastBoundLongitude"/></eastBL>
				<southBL><xsl:value-of select="gex:southBoundLatitude"/></southBL>
				<northBL><xsl:value-of select="gex:northBoundLatitude"/></northBL>
			</geoBox>
		</xsl:for-each>

		<xsl:for-each select="*/mco:MD_Constraints/*">
			<Constraints preformatted="true">
				<xsl:apply-templates mode="iso19115-3.2018" select=".">
					<xsl:with-param name="schema" select="$info/schema"/>
					<xsl:with-param name="edit" select="false()"/>
				</xsl:apply-templates>
			</Constraints>
			<Constraints preformatted="false">
				<xsl:copy-of select="."/>
			</Constraints>
		</xsl:for-each>

		<xsl:for-each select="*/mco:MD_SecurityConstraints/*">
			<SecurityConstraints preformatted="true">
				<xsl:apply-templates mode="iso19115-3.2018" select=".">
					<xsl:with-param name="schema" select="$info/schema"/>
					<xsl:with-param name="edit" select="false()"/>
				</xsl:apply-templates>
			</SecurityConstraints>
			<SecurityConstraints preformatted="false">
				<xsl:copy-of select="."/>
			</SecurityConstraints>
		</xsl:for-each>

		<xsl:for-each select="*/mco:MD_LegalConstraints/*">
			<LegalConstraints preformatted="true">
				<xsl:apply-templates mode="iso19115-3.2018" select=".">
					<xsl:with-param name="schema" select="$info/schema"/>
					<xsl:with-param name="edit" select="false()"/>
				</xsl:apply-templates>
			</LegalConstraints>
			<LegalConstraints preformatted="false">
				<xsl:copy-of select="."/>
			</LegalConstraints>
		</xsl:for-each>

		<xsl:for-each select="mri:extent/*/gex:temporalElement/*/gex:extent/gml:TimePeriod">
			<temporalExtent>
				<begin><xsl:apply-templates mode="brieftime" select="gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition"/></begin>
				<end><xsl:apply-templates mode="brieftime" select="gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition"/></end>
			</temporalExtent>
		</xsl:for-each>

		<xsl:if test="not($info/server)">
			<xsl:for-each select="mri:graphicOverview/mcc:MD_BrowseGraphic">
				<xsl:variable name="fileName"  select="mcc:fileName/gco:CharacterString"/>
				<xsl:if test="$fileName != ''">
					<xsl:variable name="fileDescr" select="mcc:fileDescription/gco:CharacterString"/>

					<xsl:choose>
						<!-- the thumbnail is an url -->
						<xsl:when test="contains($fileName ,'://')">
							<xsl:choose>
								<xsl:when test="string($fileDescr)='thumbnail'">
									<image type="thumbnail"><xsl:value-of select="$fileName"/></image>
								</xsl:when>
								<xsl:when test="string($fileDescr)='large_thumbnail'">
									<image type="overview"><xsl:value-of select="$fileName"/></image>
								</xsl:when>
								<xsl:otherwise>
									<image type="unknown"><xsl:value-of select="$fileName"/></image>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>

						<!-- small thumbnail -->
						<xsl:when test="string($fileDescr)='thumbnail'">
							<xsl:choose>
								<xsl:when test="$info/smallThumbnail">
									<image type="thumbnail">
										<xsl:value-of select="concat($info/smallThumbnail, $fileName)"/>
									</image>
								</xsl:when>
								<xsl:otherwise>
									<image type="thumbnail">
										<xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"/>
									</image>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>

						<!-- large thumbnail -->

						<xsl:when test="string($fileDescr)='large_thumbnail'">
							<xsl:choose>
								<xsl:when test="$info/largeThumbnail">
									<image type="overview">
										<xsl:value-of select="concat($info/largeThumbnail, $fileName)"/>
									</image>
								</xsl:when>
								<xsl:otherwise>
									<image type="overview">
										<xsl:value-of select="concat(/root/gui/locService,'/graphover.show?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"/>
									</image>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>

		<xsl:for-each-group select="mri:pointOfContact/*" group-by="cit:party/cit:CI_Organisation/cit:name/gco:CharacterString">
			<xsl:variable name="roles" select="string-join(current-group()/cit:role/*/geonet:getCodeListValue(/root/gui/schemas, 'iso19139', 'gmd:CI_RoleCode', @codeListValue), ', ')"/>
			<xsl:if test="normalize-space($roles)!=''">
				<responsibleParty role="{$roles}" appliesTo="resource">
					<xsl:if test="descendant::*/gcx:FileName">
						<xsl:attribute name="logo"><xsl:value-of select="descendant::*/gcx:FileName/@src"/></xsl:attribute>
					</xsl:if>
					<xsl:apply-templates mode="responsiblepartysimple" select="."/>
				</responsibleParty>
			</xsl:if>
		</xsl:for-each-group>
	</xsl:template>

	<!-- helper to create a very simplified view of a CI_ResponsibleParty block -->

	<xsl:template mode="responsiblepartysimple" match="*">
		<xsl:for-each select=".//gco:CharacterString|.//cit:URL">
			<xsl:if test="normalize-space(.)!=''">
				<xsl:element name="{local-name(..)}">
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template mode="brieftime" match="*">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:value-of select="@indeterminatePosition"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="."/>
				<xsl:if test="@indeterminatePosition">
					<xsl:value-of select="concat(' (Qualified by indeterminatePosition',': ',@indeterminatePosition,')')"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- In order to add profil specific tabs 
		add a template in this mode.

		To add some more tabs.
		<xsl:template name="iso19139.profileIdCompleteTab">
		<xsl:param name="tabLink"/>
		<xsl:param name="schema"/>

		Load iso19139 complete tab if needed
		<xsl:call-template name="iso19139CompleteTab">
		<xsl:with-param name="tabLink" select="$tabLink"/>
		<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>

		Add Extra tabs
		<xsl:call-template name="mainTab">
		<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/tab"/>
		<xsl:with-param name="default">profileId</xsl:with-param>
		<xsl:with-param name="menu">
		<item label="profileIdTab">profileId</item>
		</xsl:with-param>
		</xsl:call-template>
		</xsl:template>
	-->

	<!-- ============================================================================= -->
	<!-- iso19115-3.2018 complete tab template	-->
	<!-- ============================================================================= -->

	<xsl:template name="iso19115-3.2018CompleteTab">
		<xsl:param name="tabLink"/>
		<xsl:param name="schema"/>

		<xsl:if test="/root/gui/env/metadata/enableIsoView = 'true'">
			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'groups'"/> <!-- just a non-existing tab -->
				<xsl:with-param name="text"    select="/root/gui/strings/byGroup"/>
				<xsl:with-param name="tabLink" select="''"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'ISOMinimum'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/isoMinimum"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'ISOCore'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/isoCore"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'ISOAll'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/isoAll"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>
		</xsl:if>

		<xsl:if test="/root/gui/config/metadata-tab/advanced">
			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'packages'"/> <!-- just a non-existing tab -->
				<xsl:with-param name="text"    select="/root/gui/strings/byPackage"/>
				<xsl:with-param name="tabLink" select="''"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'metadata'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/metadata"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'identification'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/identificationTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'maintenance'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/maintenanceTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'constraints'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/constraintsTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'spatial'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/spatialTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'refSys'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/refSysTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'distribution'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/distributionTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'dataQuality'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/dataQualityTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'resourceLineage'"/>
				<xsl:with-param name="text"    select="/root/gui/schemas/iso19115-3.2018/strings/resourceLineageTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'appSchInfo'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/appSchInfoTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'porCatInfo'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/porCatInfoTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'contentInfo'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/contentInfoTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>

			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'extensionInfo'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/extensionInfoTab"/>
				<xsl:with-param name="indent"  select="'&#xA0;&#xA0;&#xA0;'"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>
		</xsl:if>		
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- utilities -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19115-3.2018IsEmpty" match="*|@*|text()">
		<xsl:choose>
			<!-- normal element -->
			<xsl:when test="*">
				<xsl:apply-templates mode="iso19115-3.2018IsEmpty"/>
			</xsl:when>
			<!-- text element -->
			<xsl:when test="text()!=''">txt</xsl:when>
			<!-- empty element -->
			<xsl:otherwise>
				<!-- attributes? -->
				<xsl:for-each select="@*">
					<xsl:if test="string-length(.)!=0">att</xsl:if>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================
		Multilingual metadata:
		=====================================================================
		* ISO 19139 define how to store multilingual content in a metadata
		record.
		1) A record is defined by a main language set in 
mdb:MD_Metadata/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode 
    element. All gco:CharacterString are then defined in that language. 
		2) In order to add translation editor
		should add a mdb:defaultLocale element in mdb:MD_Metadata:  
		<mdb:defaultLocale>
			<lan:PT_Locale id="FR">
				<lan:language>
					<lan:LanguageCode codeList="#FR" codeListValue="fra"/>
				</lan:language>
				<lan:characterEncoding/>
			</lan:PT_Locale>
		</mdb:defaultLocale>
		3) Once declared in mdb:defaultLocale, all gco:CharacterString could
		be translated using the following mechanism:
			* add xsi:type attribute (@see DataManager.updatedLocalizedTextElement)
			* add lan:PT_FreeText element linked to locale using the locale attribute.
		<cit:title xsi:type="gmd:PT_FreeText_PropertyType">
			<gco:CharacterString>Template for Vector data in ISO19139
				(preferred!)</gco:CharacterString>
			<lan:PT_FreeText>
				<lan:textGroup>
					<lan:LocalisedCharacterString locale="#FR">Modèle de saisie pour les données vecteurs en ISO19139</lan:LocalisedCharacterString>
				</lan:textGroup>
			</lan:PT_FreeText>
		</cit:title>

		=====================================================================		
		Editor principles:
		=====================================================================		
		* available locales in metadata records are not displayed in view
		mode, only used in editing mode in order to add multilingual content.
	-->
	<xsl:template mode="iso19115-3.2018" match="mdb:defaultLocale|geonet:child[string(@name)='defaultLocale']" priority="1">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		<xsl:choose>
			<xsl:when test="$edit = true()">
				<xsl:variable name="content">
					<xsl:apply-templates mode="elementEP" select="*/lan:language|*/geonet:child[string(@name)='language']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:variable>

				<xsl:apply-templates mode="complexElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"    select="$edit"/>
					<xsl:with-param name="content" select="$content"/>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>

	</xsl:template>



	<!--
		=====================================================================				
		* All elements having gco:CharacterString or gmd:PT_FreeText elements
		have to display multilingual editor widget. Even if default language
		is set, an element could have gmd:PT_FreeText and no gco:CharacterString
		(ie. no value for default metadata language) .
	-->
	<xsl:template mode="iso19115-3.2018"
		match="*[gco:CharacterString or lan:PT_FreeText]|
		gco:aName[gco:CharacterString]"
		>
		<xsl:param name="schema" />
		<xsl:param name="edit" />

		<!-- Define a rows variable if form element as
			to be a textarea instead of a simple text input.
			This parameter define the number of rows of the textarea. -->
		<xsl:variable name="rows">
			<xsl:choose>
				<xsl:when test="name(.)='mri:abstract'">10</xsl:when>
				<xsl:when test="name(.)='mri:supplementalInformation'
					or name(.)='mri:purpose'
					or name(.)='*:statement'">5</xsl:when>
				<xsl:when test="name(.)='*:description'
					or name(.)='*:specificUsage'
					or name(.)='*:explanation'
					or name(.)='*:evaluationMethodDescription'
					or name(.)='*:measureDescription'
					or name(.)='*:maintenanceNote'
					or name(.)='mri:credit'
					or name(.)='mco:otherConstraints'
					or name(.)='*:handlingDescription'
					or name(.)='*:userNote'
					or name(.)='*:checkPointDescription'
					or name(.)='*:evaluationMethodDescription'
					or name(.)='*:measureDescription'
					">3</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:call-template name="localizedCharStringField_19115-3.2018">
			<xsl:with-param name="schema" select="$schema" />
			<xsl:with-param name="edit" select="$edit" />
			<xsl:with-param name="rows" select="$rows" />
		</xsl:call-template>
	</xsl:template>



	<!-- =====================================================================				
		* Anyway some elements should not be multilingual.

		Use this template to define which elements
		are not multilingual.
		If an element is not multilingual and require
		a specific widget (eg. protocol list), create
		a new template for this new element.

		!!! WARNING: this is not defined in ISO19139. !!!
		This list of element mainly focus on identifier (eg. postal code)
		which are usually not multilingual. The list has been defined
		based on ISO profil for Switzerland recommendations. Feel free
		to adapt this list according to your needs.
	-->
	<xsl:template mode="iso19115-3.2018"
		match="
		*:identifier[gco:CharacterString]|
		*:postalCode[gco:CharacterString]|
		*:city[gco:CharacterString]|
		*:administrativeArea[gco:CharacterString]|
		*:voice[gco:CharacterString]|
		*:facsimile[gco:CharacterString]|
		*:MD_ScopeDescription/*:dataset[gco:CharacterString]|
		*:MD_ScopeDescription/*:other[gco:CharacterString]|
		*:hoursOfService[gco:CharacterString]|
		*:applicationProfile[gco:CharacterString]|
		*:CI_Series/*:page[gco:CharacterString]|
		mcc:MD_BrowseGraphic/mcc:fileName[gco:CharacterString]|
		mcc:MD_BrowseGraphic/mcc:fileType[gco:CharacterString]|
		*:unitsOfDistribution[gco:CharacterString]|
		*:amendmentNumber[gco:CharacterString]|
		*:specification[gco:CharacterString]|
		*:fileDecompressionTechnique[gco:CharacterString]|
		*:turnaround[gco:CharacterString]|
		*:fees[gco:CharacterString]|
		*:userDeterminedLimitations[gco:CharacterString]|
		mcc:MD_Identifier/mcc:codeSpace[gco:CharacterString]|
		mcc:MD_Identifier/mcc:version[gco:CharacterString]|
		*:edition[gco:CharacterString]|
		*:ISBN[gco:CharacterString]|
		*:ISSN[gco:CharacterString]|
		*:errorStatistic[gco:CharacterString]|
		*:schemaAscii[gco:CharacterString]|
		*:softwareDevelopmentFileFormat[gco:CharacterString]|
		*:MD_ExtendedElementInformation/*:shortName[gco:CharacterString]|
		*:MD_ExtendedElementInformation/*:condition[gco:CharacterString]|
		*:MD_ExtendedElementInformation/*:maximumOccurence[gco:CharacterString]|
		*:MD_ExtendedElementInformation/*:domainValue[gco:CharacterString]|
		*:densityUnits[gco:CharacterString]|
		*:MD_RangeDimension/*:descriptor[gco:CharacterString]|
		*:classificationSystem[gco:CharacterString]|
		*:checkPointDescription[gco:CharacterString]|
		*:transformationDimensionDescription[gco:CharacterString]|
		*:orientationParameterDescription[gco:CharacterString]|
		srv:SV_OperationChainMetadata/srv:name[gco:CharacterString]|
		srv:SV_OperationMetadata/srv:invocationName[gco:CharacterString]|
		srv:serviceTypeVersion[gco:CharacterString]|
		srv:operationName[gco:CharacterString]|
		srv:identifier[gco:CharacterString]
		"
		priority="100">
		<xsl:param name="schema" />
		<xsl:param name="edit" />

		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>


	<!-- =====================================================================
		Multilingual editor widget is composed of input box
		with a list of languages defined in current metadata record. 

		Metadata languages are:
		* the main language (gmd:MD_Metadata/gmd:language) and
		* all languages defined in gmd:locale section. 

		Change this template to defined another multilingual widget.
	-->
	<xsl:template name="localizedCharStringField_19115-3.2018" >
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		<xsl:param name="rows" select="1" />

		<xsl:variable name="langId">
			<xsl:call-template name="getLangId19115-3.2018">
				<xsl:with-param name="langGui" select="/root/gui/language" />
				<xsl:with-param name="md"
					select="ancestor-or-self::*[name(.)='mdb:MD_Metadata' or contains(@gco:isoType,'MD_Metadata')]" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="widget">
			<xsl:if test="$edit=true()">
				<xsl:variable name="tmpFreeText">
					<xsl:call-template name="PT_FreeText_Tree_19115-3.2018" />
				</xsl:variable>

				<xsl:variable name="ptFreeTextTree" select="exslt:node-set($tmpFreeText)" />

				<xsl:variable name="mainLang"
				  select="string(/root/*/mdb:defaultLocal/lan:language/lan:LanguageCode/@codeListValue)" />
				<xsl:variable name="mainLangId">
					<xsl:call-template name="getLangIdFromMetadata19115-3.2018">
						<xsl:with-param name="lang" select="$mainLang" />
						<xsl:with-param name="md"
							select="ancestor-or-self::*[name(.)='mdb:MD_Metadata' or contains(@gco:isoType,'MD_Metadata')]" />
					</xsl:call-template>
				</xsl:variable>


				<table><tr><td>
					<!-- Match gco:CharacterString element which is in default language or
						process a PT_FreeText with a reference to the main metadata language. -->
					<xsl:choose>
						<xsl:when test="gco:*">
							<xsl:for-each select="gco:*">
								<xsl:call-template name="getElementText">
									<xsl:with-param name="schema" select="$schema" />
									<xsl:with-param name="edit" select="'true'" />
									<xsl:with-param name="rows" select="$rows" />
								</xsl:call-template>
							</xsl:for-each>
						</xsl:when>
						<xsl:when test="gco:*">
							<xsl:for-each select="gco:*">
								<xsl:call-template name="getElementText">
									<xsl:with-param name="schema" select="$schema" />
									<xsl:with-param name="edit" select="'true'" />
									<xsl:with-param name="rows" select="$rows" />
								</xsl:call-template>
							</xsl:for-each>
						</xsl:when>
						<xsl:when test="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale=$mainLangId]">
							<xsl:for-each select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale=$mainLangId]">
								<xsl:call-template name="getElementText">
									<xsl:with-param name="schema" select="$schema" />
									<xsl:with-param name="edit" select="'true'" />
									<xsl:with-param name="rows" select="$rows" />
								</xsl:call-template>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:for-each select="$ptFreeTextTree//lan:LocalisedCharacterString[@locale=$mainLangId]">
								<xsl:call-template name="getElementText">
									<xsl:with-param name="schema" select="$schema" />
									<xsl:with-param name="edit" select="'true'" />
									<xsl:with-param name="rows" select="$rows" />
								</xsl:call-template>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>

					<xsl:for-each select="$ptFreeTextTree//lan:LocalisedCharacterString[@locale!=$mainLangId]">
						<xsl:call-template name="getElementText">
							<xsl:with-param name="schema" select="$schema" />
							<xsl:with-param name="edit" select="'true'" />
							<xsl:with-param name="visible" select="'false'" />
							<xsl:with-param name="rows" select="$rows" />
						</xsl:call-template>
					</xsl:for-each>
				</td>
				<td align="left">
					<xsl:choose>
						<xsl:when test="$ptFreeTextTree//lan:LocalisedCharacterString">
							<!-- Create combo to select language.
							On change, the input with selected language is displayed. Others hidden. -->

							<xsl:variable name="mainLanguageRef">
								<xsl:choose>
									<xsl:when test="gco:CharacterString/geonet:element/@ref" >
										<xsl:value-of select="concat('_', gco:CharacterString/geonet:element/@ref)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:variable name="strings" select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale=$mainLangId]/geonet:element/@ref"/>
										<xsl:value-of select="concat('_', $strings[0])"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>

							<xsl:variable name="suggestionDiv" select="concat('suggestion', $mainLanguageRef)"/>

							<!-- Language selector is only displayed when more than one language
								 is set in gmd:locale. -->
							<select class="md lang_selector" name="localization" id="localization_{geonet:element/@ref}"
								onchange="enableLocalInput(this);clearSuggestion('{$suggestionDiv}');"
								selected="true">
								<xsl:attribute name="style">
									<xsl:choose>
										<xsl:when test="count($ptFreeTextTree//lan:LocalisedCharacterString)=0">display:none;</xsl:when>
										<xsl:otherwise>display:block;</xsl:otherwise>
									</xsl:choose>
								</xsl:attribute>
								<xsl:choose>
									<xsl:when test="gco:*">
										<option value="_{gco:*/geonet:element/@ref}" code="{substring-after($mainLangId, '#')}">
											<xsl:value-of
													select="/root/gui/isoLang/record[code=$mainLang]/label/*[name(.)=/root/gui/language]" />
										</option>
										<xsl:for-each select="$ptFreeTextTree//lan:LocalisedCharacterString[@locale!=$mainLangId]">
											<option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
												<xsl:value-of select="@language" />
											</option>
										</xsl:for-each>
									</xsl:when>
									<xsl:otherwise>
										<xsl:for-each select="$ptFreeTextTree//lan:LocalisedCharacterString[@locale=$mainLangId]">
											<option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
												<xsl:value-of select="@language" />
											</option>
										</xsl:for-each>
										<xsl:for-each select="$ptFreeTextTree//lan:LocalisedCharacterString[@locale!=$mainLangId]">
											<option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
												<xsl:value-of select="@language" />
											</option>
										</xsl:for-each>
									</xsl:otherwise>
								</xsl:choose>
							</select>

							<!-- =================================
									Google translation API demo
									See: http://code.google.com/apis/ajaxlanguage/documentation/
								 =================================
								 Simple button to translate one element from one language to another.
								 This is useful to help editor to translate metadata content.
								 
								 To be improved :
									* check that jeeves GUI language is equal to Google language code
									* target parameter of translate function could be set to:
									$('localization_{geonet:element/@ref}').options[$('localization_{geonet:element/@ref}').selectedIndex].value
									but this will copy Google results to a form field. User should review suggested translation.
							-->
							<xsl:if test="/root/gui/config/editor-google-translate = 1">
								<xsl:text> </xsl:text>
								<a href="javascript:googleTranslate('{$mainLanguageRef}',
										'{$suggestionDiv}',
										null,
										'{substring-after($mainLangId, '#')}', 
										$('localization_{geonet:element/@ref}').options[$('localization_{geonet:element/@ref}').selectedIndex].readAttribute('code'));"
										alt="{/root/gui/strings/translateWithGoogle}" title="{/root/gui/strings/translateWithGoogle}">
									<img width="14px" src="../../images/translate.png"/>
								</a>
								<br/>
								<div id="suggestion_{gco:CharacterString/geonet:element/@ref|
									lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale=$mainLangId]/geonet:element/@ref}"
									style="display:none;"
									class="suggestion"
									alt="{/root/gui/strings/translateWithGoogle}" title="{/root/gui/strings/translateWithGoogle}"
								/>
							</xsl:if>
						</xsl:when>
					</xsl:choose>
				</td></tr></table>
			</xsl:if>
		</xsl:variable>
		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema" />
			<xsl:with-param name="edit" select="$edit" />
			<xsl:with-param name="langId" select="$langId" />
			<xsl:with-param name="widget" select="$widget" />
			<xsl:with-param name="rows" select="$rows" />
		</xsl:call-template>
	</xsl:template>

	<!--
		Create a PT_FreeText_Tree_19115-3.2018 for multilingual editing.

		The lang prefix for geonet:element is used by the DataManager 
		to clean multilingual content and add required attribute (xsi:type).
	-->
	<xsl:template name="PT_FreeText_Tree_19115-3.2018">
		<xsl:variable name="mainLang"
		select="string(/root/*/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue)" />
		<xsl:variable name="languages"
			select="/root/*/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue" />

		<xsl:variable name="currentNode" select="node()" />
		<xsl:for-each select="$languages">
			<xsl:variable name="langId"
				select="concat('&#35;',string(../../../@id))" />
			<xsl:variable name="code">
				<xsl:call-template name="getLangCode19115-3.2018">
					<xsl:with-param name="md"
						select="ancestor-or-self::*[name(.)='mdb:MD_Metadata' or contains(@gco:isoType,'MD_Metadata')]" />
					<xsl:with-param name="langId" select="substring($langId,2)" />
				</xsl:call-template>
			</xsl:variable>

			<xsl:variable name="ref" select="$currentNode/../geonet:element/@ref" />
			<xsl:variable name="min" select="$currentNode/../geonet:element/@min" />
			<xsl:variable name="guiLang" select="/root/gui/language" />
			<xsl:variable name="language"
				select="/root/gui/isoLang/record[code=$code]/label/*[name(.)=$guiLang]" />
			<lan:PT_FreeText>
				<lan:textGroup>
					<lan:LocalisedCharacterString locale="{$langId}"
						code="{$code}" language="{$language}">
						<xsl:value-of
							select="$currentNode//lan:LocalisedCharacterString[@locale=$langId]" />
						<xsl:choose>
							<xsl:when
								test="$currentNode//lan:LocalisedCharacterString[@locale=$langId]">
								<geonet:element
									ref="{$currentNode//lan:LocalisedCharacterString[@locale=$langId]/geonet:element/@ref}" />
							</xsl:when>
							<xsl:otherwise>
								<geonet:element ref="lang_{substring($langId,2)}_{$ref}" />
							</xsl:otherwise>
						</xsl:choose>
					</lan:LocalisedCharacterString>
					<geonet:element ref="" />
				</lan:textGroup>
				<geonet:element ref="">
					<!-- Add min attribute from current node to PT_FreeText
					child in order to turn on validation criteria. -->
					<xsl:if test="$min = 1">
						<xsl:attribute name="min">1</xsl:attribute>
					</xsl:if>
				</geonet:element>
			</lan:PT_FreeText>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="iso19115-3.2018-javascript"/>

</xsl:stylesheet>
