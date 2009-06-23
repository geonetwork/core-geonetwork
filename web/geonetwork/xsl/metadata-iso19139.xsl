<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml"
    xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common">

	<!-- =================================================================== -->
	<!-- default: in simple mode just a flat list -->
	<!-- =================================================================== -->

	<xsl:template mode="iso19139" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<!-- do not show empty elements in view mode -->
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="flat"   select="$currTab='simple'"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:variable name="empty">
					<xsl:apply-templates mode="iso19139IsEmpty" select="."/>
				</xsl:variable>
				
				<xsl:if test="$empty!=''">
					<xsl:apply-templates mode="element" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="false()"/>
						<xsl:with-param name="flat"   select="$currTab='simple'"/>
					</xsl:apply-templates>
				</xsl:if>
				
			</xsl:otherwise>
		</xsl:choose>
			
	</xsl:template>
	
	<!-- ===================================================================== -->
	<!-- these elements should be boxed -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19139" match="gmd:graphicOverview"/>
	<xsl:template mode="iso19139" match="gmd:identificationInfo|gmd:distributionInfo|gmd:descriptiveKeywords|gmd:spatialRepresentationInfo|gmd:pointOfContact|gmd:dataQualityInfo|gmd:referenceSystemInfo|gmd:equivalentScale|gmd:projection|gmd:ellipsoid|gmd:extent[name(..)!='gmd:EX_TemporalExtent']|gmd:geographicBox|gmd:EX_TemporalExtent|gmd:MD_Distributor|srv:containsOperations">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ===================================================================== -->
	<!-- some gco: elements -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19139" match="gmd:*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType]|srv:*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType]|gco:aName[gco:CharacterString]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ==================================================================== -->

	<!--
		OperatesOn element display or edit attribute uuidref.
		TODO : Add selection popup ?
		TODO : Replace uuid by metadata title ?
		-->
	<xsl:template mode="iso19139" match="srv:operatesOn">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="text">
			
			<xsl:choose>
				<xsl:when test="$edit=true()">
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<input type="text" name="_{$ref}_uuidref" id="_{$ref}_uuidref" value="{./@uuidref}" size="20"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="@xlink:href">
							<a href="{@xlink:href}">
								<xsl:value-of select="@xlink:title"/>
							</a>
						</xsl:when>
						<xsl:otherwise>
							<a href="metadata.show?uuid={@uuidref}">
								<xsl:value-of select="@uuidref"/>
							</a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text"   select="$text"/>
		</xsl:apply-templates>
	</xsl:template>



	<!--
		Create widget to handle editing of xsd:duration elements.
		
		Format: PnYnMnDTnHnMnS
		
		*  P indicates the period (required)
		* nY indicates the number of years
		* nM indicates the number of months
		* nD indicates the number of days
		* T indicates the start of a time section (required if you are going to specify hours, minutes, or seconds)
		* nH indicates the number of hours
		* nM indicates the number of minutes
		* nS indicates the number of seconds
		
		TODO : onload, we should run validateNumber handler in order to change 
		input class when needed.
		
	-->
	<xsl:template mode="iso19139" match="gts:TM_PeriodDuration" priority="100">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		
		<!--Set default value -->
		<xsl:variable name="p">
			<xsl:choose>
				<xsl:when test=".=''">P0Y0M0DT0H0M0S</xsl:when>
				<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!-- Extract fragment -->
		<xsl:variable name="NEG">
			<xsl:choose>
				<xsl:when test="starts-with($p, '-')">true</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="Y" select="substring-before(substring-after($p, 'P'), 'Y')"/>
		<xsl:variable name="M" select="substring-before(substring-after($p, 'Y'), 'M')"/>
		<xsl:variable name="D" select="substring-before(substring-after($p, 'M'), 'DT')"/>
		<xsl:variable name="H" select="substring-before(substring-after($p, 'DT'), 'H')"/>
		<xsl:variable name="MI" select="substring-before(substring-after($p, 'H'), 'M')"/>
		<xsl:variable name="S" select="substring-before(substring-after(substring-after($p,'M' ),'M' ), 'S')"/>
		
		<xsl:variable name="text">
			<xsl:choose>
				<xsl:when test="$edit=true()">
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					
					<input type="checkbox" id="N{$ref}" onchange="buildDuration('{$ref}');">
						<xsl:if test="$NEG!=''"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
					</input>
					<label for="N{$ref}"><xsl:value-of select="/root/gui/strings/durationSign"/></label><br/>
					<xsl:value-of select="/root/gui/strings/durationNbYears"/><input type="text" id="Y{$ref}" value="{substring-before(substring-after($p, 'P'), 'Y')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
					<xsl:value-of select="/root/gui/strings/durationNbMonths"/><input type="text" id="M{$ref}" value="{substring-before(substring-after($p, 'Y'), 'M')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
					<xsl:value-of select="/root/gui/strings/durationNbDays"/><input type="text" id="D{$ref}" value="{substring-before(substring-after($p, 'M'), 'DT')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/><br/>
					<xsl:value-of select="/root/gui/strings/durationNbHours"/><input type="text" id="H{$ref}" value="{substring-before(substring-after($p, 'DT'), 'H')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
					<xsl:value-of select="/root/gui/strings/durationNbMinutes"/><input type="text" id="MI{$ref}" value="{substring-before(substring-after($p, 'H'), 'M')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
					<xsl:value-of select="/root/gui/strings/durationNbSeconds"/><input type="text" id="S{$ref}" value="{substring-before(substring-after(substring-after($p,'M' ),'M' ), 'S')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,true);"/><br/>
					<input type="hidden" name="_{$ref}" id="_{$ref}" value="{$p}" size="20"/><br/>
					
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$NEG!=''">-</xsl:if><xsl:text> </xsl:text>
					<xsl:value-of select="$Y"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationYears"/><xsl:text>  </xsl:text>
					<xsl:value-of select="$M"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationMonths"/><xsl:text>  </xsl:text>
					<xsl:value-of select="$D"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationDays"/><xsl:text> / </xsl:text>
					<xsl:value-of select="$H"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationHours"/><xsl:text>  </xsl:text>
					<xsl:value-of select="$MI"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationMinutes"/><xsl:text>  </xsl:text>
					<xsl:value-of select="$S"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationSeconds"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text"   select="$text"/>
		</xsl:apply-templates>
	</xsl:template>

    <!-- ==================================================================== -->

	<xsl:template name="iso19139String">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="rows" select="1"/>
		<xsl:param name="cols" select="50"/>

		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="helpLink">
			<xsl:call-template name="getHelpLink">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="text">
			<xsl:for-each select="gco:*">
				<xsl:call-template name="getElementText">
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="rows"   select="$rows"/>
					<xsl:with-param name="cols"   select="$cols"/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="attrs">
			<xsl:for-each select="gco:*/@*">
				<xsl:value-of select="name(.)"/>
			</xsl:for-each>
		</xsl:variable>


		<xsl:choose>
		<xsl:when test="normalize-space($attrs)!=''">
			<xsl:apply-templates mode="complexElement" select=".">
		  	<xsl:with-param name="schema"   select="$schema"/>
				<xsl:with-param name="edit"     select="$edit"/>
				<xsl:with-param name="title"    select="$title"/>
				<xsl:with-param name="helpLink" select="$helpLink"/>
				<xsl:with-param name="content">

				<!-- existing attributes -->
				<xsl:for-each select="gco:*/@*">
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:for-each>

				<!-- existing content -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
					<xsl:with-param name="text"     select="$text"/>
				</xsl:apply-templates>
				</xsl:with-param>
			</xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates mode="simpleElement" select=".">
				<xsl:with-param name="schema"   select="$schema"/>
				<xsl:with-param name="edit"     select="$edit"/>
				<xsl:with-param name="title"    select="$title"/>
				<xsl:with-param name="helpLink" select="$helpLink"/>
				<xsl:with-param name="text"     select="$text"/>
			</xsl:apply-templates>
		</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template mode="iso19139" match="gco:ScopedName|gco:LocalName">
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

	<!-- ================================================================= -->
	<!-- some elements that have both attributes and content               -->
	<!-- ================================================================= -->

	<xsl:template mode="iso19139" match="gml:coordinates|gml:identifier|gml:axisDirection|gml:descriptionReference">
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

	<xsl:template mode="iso19139" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19139Codelist">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================= -->

	<xsl:template name="iso19139Codelist">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	
	<!-- LanguageCode is a codelist, but retrieving
	the list of language as defined in the language database table
	allows to create the list for selection.
	
	This table is also used for gmd:language element.
	-->
	<xsl:template mode="iso19139" match="gmd:LanguageCode" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="value" select="@codeListValue" />
		<xsl:variable name="lang" select="/root/gui/language" />
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<select class="md" name="_{geonet:element/@ref}_codeListValue"
					size="1">
					<option name="" />
					
					<xsl:for-each select="/root/gui/isoLang/record">
						<xsl:sort select="label/child::*[name() = $lang]"/>
						<option value="{code}">
							<xsl:if test="code = $value">
								<xsl:attribute name="selected" />
							</xsl:if>
							<xsl:value-of select="label/child::*[name() = $lang]" />
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of
					select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--  Do not allow editing of id to end user. Id is based on language selection
	and iso code.-->
	<xsl:template mode="iso19139" match="gmd:PT_Locale/@id"
		priority="2">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema" />
			<xsl:with-param name="edit" select="false()" />
		</xsl:apply-templates>
	</xsl:template>
	
	
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139GetAttributeText" match="@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="name"     select="local-name(..)"/>
		<xsl:variable name="qname"    select="name(..)"/>
		<xsl:variable name="value"    select="../@codeListValue"/>
		<xsl:variable name="codelist" select="/root/gui/*[name(.)=$schema]/codelist[@name = $qname]"/>

		<xsl:choose>
			<xsl:when test="$qname='gmd:LanguageCode'">
				<xsl:apply-templates mode="iso19139" select="..">
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$edit=true()">
						<!-- codelist in edit mode -->
						<select class="md" name="_{../geonet:element/@ref}_{name(.)}" size="1">
							<option name=""/>
							<xsl:for-each select="$codelist/entry">
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
		<!--
		<xsl:call-template name="getAttributeText">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
		-->
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!--
	make the following fields always not editable:
	dateStamp
	metadataStandardName
	metadataStandardVersion
	fileIdentifier
	characterSet
	-->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:dateStamp|gmd:metadataStandardName|gmd:metadataStandardVersion|gmd:fileIdentifier" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="false()"/>
			<xsl:with-param name="text">
				<xsl:choose>
					<xsl:when test="normalize-space(gco:*)=''">
						<span class="info">
							- <xsl:value-of select="/root/gui/strings/setOnSave"/> - 
						</span>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="gco:*"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="iso19139" match="//gmd:MD_Metadata/gmd:characterSet|//*[@gco:isoType='gmd:MD_Metadata']/gmd:characterSet" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19139Codelist">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="false()"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- electronicMailAddress -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:electronicMailAddress" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:call-template name="iso19139String">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<a href="mailto:{string(.)}"><xsl:value-of select="string(.)"/></a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- descriptiveKeywords -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:descriptiveKeywords">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="text">
						<xsl:for-each select="gmd:MD_Keywords/gmd:keyword">
							<xsl:if test="position() &gt; 1">, </xsl:if>
							<xsl:value-of select="."/>
						</xsl:for-each>
						<xsl:if test="gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue!=''">
							<xsl:text> (</xsl:text>
							<xsl:value-of select="gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue"/>
							<xsl:text>)</xsl:text>
						</xsl:if>
						<xsl:text>.</xsl:text>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- place keyword; only called in edit mode (see descriptiveKeywords template) -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place']">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="text">
			<xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
			<xsl:variable name="keyword" select="gco:CharacterString/text()"/>
			
			<input class="md" type="text" name="_{$ref}" value="{gco:CharacterString/text()}" size="50" />

			<!-- regions combobox -->

			<xsl:variable name="lang" select="/root/gui/language"/>
			<xsl:text> </xsl:text>
			<select name="place" size="1" onChange="document.mainForm._{$ref}.value=this.options[this.selectedIndex].text" class="md">
				<option value=""/>
				<xsl:for-each select="/root/gui/regions/record">
					<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
					<option value="{id}">
						<xsl:if test="string(label/child::*[name() = $lang])=$keyword">
							<xsl:attribute name="selected"/>
						</xsl:if>
						<xsl:value-of select="label/child::*[name() = $lang]"/>
					</option>
				</xsl:for-each>
			</select>
		</xsl:variable>
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>
			<xsl:with-param name="text"   select="$text"/>
		</xsl:apply-templates>
	</xsl:template>
		
	<!-- ============================================================================= -->
	<!-- EX_GeographicBoundingBox -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="geoBox">
			<xsl:apply-templates mode="iso19139GeoBox" select=".">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="places">
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<xsl:variable name="keyword" select="string(.)"/>

					<xsl:variable name="selection" select="concat(gmd:westBoundLongitude/gco:Decimal,';',gmd:eastBoundLongitude/gco:Decimal,';',gmd:southBoundLatitude/gco:Decimal,';',gmd:northBoundLatitude/gco:Decimal)"/>

					<!-- regions combobox -->

					<xsl:variable name="lang" select="/root/gui/language"/>

					<select name="place" size="1" onChange="javascript:setRegion(document.mainForm._{gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref}, document.mainForm._{gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref}, document.mainForm._{gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref}, document.mainForm._{gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref}, this.options[this.selectedIndex].value)" class="md">
						<option value=""/>
						<xsl:for-each select="/root/gui/regions/record">
							<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
		
							<xsl:variable name="value" select="concat(west,';',east,';',south,';',north)"/>
							<option value="{$value}">
								<xsl:if test="$value=$selection">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="label/child::*[name() = $lang]"/>
							</option>
						</xsl:for-each>
					</select>
				</xsl:variable>
				<xsl:apply-templates mode="complexElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="content">
						<tr>
							<td align="center">
								<xsl:copy-of select="$geoBox"/>
							</td>
							<td>
								<xsl:copy-of select="$places"/>
							</td>
						</tr>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="complexElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="content">
						<tr>
							<td align="center">
								<xsl:copy-of select="$geoBox"/>
							</td>
						</tr>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19139GeoBox" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<table>
			<tr>
				<td/>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19139VertElement" select="gmd:northBoundLatitude/gco:Decimal">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="name"   select="'gmd:northBoundLatitude'"/>
					</xsl:apply-templates>
				</td>
				<td/>
			</tr>
			<tr>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19139VertElement" select="gmd:westBoundLongitude/gco:Decimal">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="name"   select="'gmd:westBoundLongitude'"/>
					</xsl:apply-templates>
				</td>
				
				<!--
				<td class="box" width="100" height="100" align="center">
				-->
				<xsl:variable name="md">
					<xsl:apply-templates mode="brief" select="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']"/>
				</xsl:variable>
				<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
				<!--td width="100" height="100" align="center">
					<xsl:call-template name="thumbnail">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template>
				</td-->
				<td/>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19139VertElement" select="gmd:eastBoundLongitude/gco:Decimal">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="name"   select="'gmd:eastBoundLongitude'"/>
					</xsl:apply-templates>
				</td>
			</tr>
			<tr>
				<td/>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19139VertElement" select="gmd:southBoundLatitude/gco:Decimal">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="name"   select="'gmd:southBoundLatitude'"/>
					</xsl:apply-templates>
				</td>
				<td/>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139VertElement" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="name"/>
		
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="name"   select="$name"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="helpLink">
			<xsl:call-template name="getHelpLink">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="name"   select="$name"/>
			</xsl:call-template>
		</xsl:variable>
		<b>
			<xsl:choose>
				<xsl:when test="$helpLink!=''">
					<span id="stip.{$helpLink}|{generate-id(.)}" onclick="toolTip(this.id);" style="cursor:help;"><xsl:value-of select="$title"/>
						<!--
						<xsl:call-template name="asterisk">
							<xsl:with-param name="link" select="$helpLink"/>
							<xsl:with-param name="edit" select="$edit"/>
						</xsl:call-template>
					-->
					</span>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$title"/>
				</xsl:otherwise>
			</xsl:choose>
		</b>
		<br/>
		<xsl:call-template name="getElementText">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="cols"  select="10"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- abstract -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:abstract" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="rows"   select="10"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- supplementalInformation | purpose -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:supplementalInformation|gmd:purpose|gmd:statement" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19139String">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="rows"   select="5"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!--
	dateTime (format = %Y-%m-%dT%H:%M:00)
	usageDateTime
	plannedAvailableDateTime
	-->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:dateTime|gmd:usageDateTime|gmd:plannedAvailableDateTime" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="text">
						<xsl:variable name="ref" select="gco:Date/geonet:element/@ref|gco:DateTime/geonet:element/@ref"/>
						
						<table width="100%"><tr>
							<td>
								<xsl:choose>
									<xsl:when test="gco:DateTime">
										<input class="md" type="text" name="_{$ref}" id="_{$ref}_cal" value="{gco:DateTime/text()}" size="30" readonly="1"/>
									</xsl:when>
									<xsl:otherwise>
										<input class="md" type="text" name="_{$ref}" id="_{$ref}_cal" value="{gco:Date/text()}" size="30" readonly="1"/>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td align="center" width="30" valign="middle">
								<img src="{/root/gui/url}/scripts/calendar/img.gif"
									 id="_{$ref}_trigger"
									 style="cursor: pointer; border: 1px solid;"
									 title="Date selector"
									 onmouseover="this.style.background='red';"
									 onmouseout="this.style.background=''" />
								<script type="text/javascript">
									Calendar.setup(
										{
											inputField  : &quot;_<xsl:value-of select="$ref"/>_cal&quot;,         // ID of the input field
											<xsl:choose>
												<xsl:when test="gco:Date">
											ifFormat    : "%Y-%m-%d",
											showsTime   : false,
												</xsl:when>
												<xsl:otherwise>
											ifFormat    : "%Y-%m-%dT%H:%M:00",   // the date format
											showsTime   : true,                  // show the time
												</xsl:otherwise>
											</xsl:choose>
											button      : &quot;_<xsl:value-of select="$ref"/>_trigger&quot;  // ID of the button
										}
									);
									Calendar.setup(
										{
											inputField  : &quot;_<xsl:value-of select="$ref"/>_cal&quot;,         // ID of the input field
											<xsl:choose>
												<xsl:when test="gco:Date">
											ifFormat    : "%Y-%m-%d",
											showsTime   : false,
												</xsl:when>
												<xsl:otherwise>
											ifFormat    : "%Y-%m-%dT%H:%M:00",  // the date format
											showsTime   : true,                 // show the time
												</xsl:otherwise>
											</xsl:choose>
											button      : &quot;_<xsl:value-of select="$ref"/>_cal&quot;  // ID of the button
										}
									);
								</script>
							</td>
							<td align="left" width="100%">
								<xsl:text>  </xsl:text><a onclick="javascript:setBunload(false);" href="javascript:clearRef('{$ref}');"> <xsl:value-of select="/root/gui/strings/clear"/></a>
							</td>
						</tr></table>
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

	<!-- ============================================================================= -->
	<!--
	date (format = %Y-%m-%d)
	editionDate
	dateOfNextUpdate
	mdDateSt is not editable (!we use DateTime instead of only Date!)
	-->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:date[gco:DateTime|gco:Date]|gmd:editionDate|gmd:dateOfNextUpdate" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="text">
						<xsl:variable name="ref" select="gco:DateTime/geonet:element/@ref|gco:Date/geonet:element/@ref"/>
						
						<table width="100%"><tr>
							<td>
								<xsl:choose>
                  <xsl:when test="gco:DateTime">
                <input class="md" type="text" name="_{$ref}" id="_{$ref}_cal" value="{gco:DateTime/text()}" size="30" readonly="1"/>
                  </xsl:when>
                  <xsl:otherwise>
                <input class="md" type="text" name="_{$ref}" id="_{$ref}_cal" value="{gco:Date/text()}" size="30" readonly="1"/>
                  </xsl:otherwise>
                </xsl:choose>
							</td>
							<td align="center" width="30" valign="middle">
								<img src="{/root/gui/url}/scripts/calendar/img.gif"
									 id="_{$ref}_trigger"
									 style="cursor: pointer; border: 1px solid;"
									 title="Date selector"
									 onmouseover="this.style.background='red';"
									 onmouseout="this.style.background=''" />
								<script type="text/javascript">
									Calendar.setup(
										{
											inputField  : &quot;_<xsl:value-of select="$ref"/>_cal&quot;,         // ID of the input field
								<xsl:choose>
                  <xsl:when test="gco:DateTime">
                      ifFormat    : "%Y-%m-%dT%H:%M:00", // the date format
                      showsTime : true, // Show the time
                  </xsl:when>
                  <xsl:otherwise>
                      ifFormat    : "%Y-%m-%d", // the date format
                      showsTime : false, // Do not show the time
                  </xsl:otherwise>
                </xsl:choose>
											button      : &quot;_<xsl:value-of select="$ref"/>_trigger&quot;  // ID of the button
										}
									);
									Calendar.setup(
										{
											inputField  : &quot;_<xsl:value-of select="$ref"/>_cal&quot;,         // ID of the input field
								<xsl:choose>
                  <xsl:when test="gco:DateTime">
                      ifFormat    : "%Y-%m-%dT%H:%M:00", // the date format
                      showsTime : true, // Show the time
                  </xsl:when>
                  <xsl:otherwise>
                      ifFormat    : "%Y-%m-%d",  // the date format
                      showsTime : false, // Do not show the time
                  </xsl:otherwise>
                </xsl:choose>
											button      : &quot;_<xsl:value-of select="$ref"/>_cal&quot;  // ID of the button
										}
									);
								</script>
							</td>
							<td align="left" width="100%">
								<xsl:text>  </xsl:text><a onclick="javascript:setBunload(false);" href="javascript:clearRef('{$ref}');"> <xsl:value-of select="/root/gui/strings/clear"/></a>
							</td>
						</tr></table>
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

	<xsl:template mode="iso19139" match="gml:*[gml:beginPosition|gml:endPosition]|gml:TimeInstant[gml:timePosition]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:for-each select="gml:beginPosition|gml:endPosition|gml:timePosition">
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="text">
						<xsl:variable name="ref" select="geonet:element/@ref"/>
						
						<table width="100%"><tr>
							<td>
	                					<input class="md" type="text" name="_{$ref}" id="_{$ref}_cal" value="{text()}" size="30" readonly="1"/>
							</td>
							<td align="center" width="30" valign="middle">
								<img src="{/root/gui/url}/scripts/calendar/img.gif"
									 id="_{$ref}_trigger"
									 style="cursor: pointer; border: 1px solid;"
									 title="Date selector"
									 onmouseover="this.style.background='red';"
									 onmouseout="this.style.background=''" />
								<script type="text/javascript">
									Calendar.setup(
										{
											inputField  : &quot;_<xsl:value-of select="$ref"/>_cal&quot;,         // ID of the input field
						                    ifFormat    : "%Y-%m-%dT%H:%M:00", // the date format
                    						showsTime : true, // Do not show the time
											button      : &quot;_<xsl:value-of select="$ref"/>_trigger&quot;  // ID of the button
										}
									);
								</script>
							</td>
							<td align="left" width="100%">
								<xsl:text>  </xsl:text><a onclick="javascript:setBunload(false);" href="javascript:clearRef('{$ref}');"> <xsl:value-of select="/root/gui/strings/clear"/></a>
							</td>
						</tr></table>

						<xsl:if test="@indeterminatePosition">
							<xsl:apply-templates mode="simpleElement" select="@indeterminatePosition">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
						</xsl:if>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<xsl:choose>
							<xsl:when test="normalize-space(.)=''">
								<xsl:value-of select="@indeterminatePosition"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="text()"/>
								<xsl:if test="@indeterminatePosition">
									&#160;
									<xsl:value-of select="concat('Qualified by indeterminatePosition',': ',@indeterminatePosition)"/>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<!-- =================================================================== -->
	<!-- subtemplates -->
	<!-- =================================================================== -->

	<xsl:template mode="iso19139" match="*[geonet:info/isTemplate='s']" priority="3">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- =================================================================== -->
	<!--
	placeholder
	<xsl:template mode="iso19139" match="TAG">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		BODY
	</xsl:template>
	-->
	<!-- ==================================================================== -->

	<xsl:template mode="iso19139" match="@gco:isoType"/>

	<!-- ==================================================================== -->
	<!-- Metadata -->
	<!-- ==================================================================== -->

	<xsl:template mode="iso19139" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded"/>

		<xsl:variable name="dataset" select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset' or normalize-space(gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)=''"/>
		
		<xsl:choose>
		
			<!-- metadata tab -->
			<xsl:when test="$currTab='metadata'">
			
				<!-- thumbnail -->
				<tr>
					<td class="padded" align="center" valign="middle" colspan="2">
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:call-template name="thumbnail">
							<xsl:with-param name="metadata" select="$metadata"/>
						</xsl:call-template>
					</td>
				</tr>

				<xsl:call-template name="iso19139Metadata">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- identification tab -->
			<xsl:when test="$currTab='identification'">
			
				<!-- thumbnail -->
				<tr>
					<td class="padded" align="center" valign="middle" colspan="2">
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:call-template name="thumbnail">
							<xsl:with-param name="metadata" select="$metadata"/>
						</xsl:call-template>
					</td>
				</tr>
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- maintenance tab -->
			<xsl:when test="$currTab='maintenance'">
				<xsl:apply-templates mode="elementEP" select="gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- constraints tab -->
			<xsl:when test="$currTab='constraints'">
				<xsl:apply-templates mode="elementEP" select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- spatial tab -->
			<xsl:when test="$currTab='spatial'">
				<xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- refSys tab -->
			<xsl:when test="$currTab='refSys'">
				<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- distribution tab -->
			<xsl:when test="$currTab='distribution'">
				<xsl:apply-templates mode="elementEP" select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- embedded distribution tab -->
			<xsl:when test="$currTab='distribution2'">
				<xsl:apply-templates mode="elementEP" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- dataQuality tab -->
			<xsl:when test="$currTab='dataQuality'">
				<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- appSchInfo tab -->
			<xsl:when test="$currTab='appSchInfo'">
				<xsl:apply-templates mode="elementEP" select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- porCatInfo tab -->
			<xsl:when test="$currTab='porCatInfo'">
				<xsl:apply-templates mode="elementEP" select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- contentInfo tab -->
			<xsl:when test="$currTab='contentInfo'">
			<xsl:apply-templates mode="elementEP" select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			</xsl:when>
			
			<!-- extensionInfo tab -->
			<xsl:when test="$currTab='extensionInfo'">
			<xsl:apply-templates mode="elementEP" select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			</xsl:when>

			<!-- ISOMinimum tab -->
			<xsl:when test="$currTab='ISOMinimum'">
				<xsl:call-template name="isotabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="false()"/>
				</xsl:call-template>
			</xsl:when>

			<!-- ISOCore tab -->
			<xsl:when test="$currTab='ISOCore'">
				<xsl:call-template name="isotabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="true()"/>
				</xsl:call-template>
			</xsl:when>
			
			<!-- ISOAll tab -->
			<xsl:when test="$currTab='ISOAll'">
				<xsl:call-template name="iso19139Complete">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>
			
			<!-- default -->
			<xsl:otherwise>
			
				<!-- thumbnail -->
				<tr>
					<td class="padded" align="center" valign="middle" colspan="2">
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:if test="$embedded = false()">
							<xsl:call-template name="thumbnail">
								<xsl:with-param name="metadata" select="$metadata"/>
							</xsl:call-template>
						</xsl:if>
					</td>
				</tr>
				
				<xsl:call-template name="iso19139Simple">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$currTab='simple'"/>
				</xsl:call-template>
				
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="isotabs">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="dataset"/>
		<xsl:param name="core"/>

		<!-- dataset or resource info in its own box -->
	
		<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification|gmd:identificationInfo/srv:SV_ServiceIdentification">
			<xsl:call-template name="complexElementGuiWrapper">
				<xsl:with-param name="title">
				<xsl:choose>
					<xsl:when test="$dataset=true()">
						<xsl:value-of select="/root/gui/iso19139/element[@name='gmd:MD_DataIdentification']/label"/>
					</xsl:when>
					<xsl:when test="local-name(.)='SV_ServiceIdentification'">
						<xsl:value-of select="/root/gui/iso19139/element[@name='srv:SV_ServiceIdentification']/label"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'Resource Identification'"/><!-- FIXME i18n-->
					</xsl:otherwise>
				</xsl:choose>
				</xsl:with-param>
				<xsl:with-param name="content">
		
				<xsl:apply-templates mode="elementEP" select="gmd:citation/gmd:CI_Citation/gmd:title|gmd:citation/gmd:CI_Citation/geonet:child[string(@name)='title']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:citation/gmd:CI_Citation/gmd:date|gmd:citation/gmd:CI_Citation/geonet:child[string(@name)='date']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:abstract|geonet:child[string(@name)='abstract']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:pointOfContact|geonet:child[string(@name)='pointOfContact']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:descriptiveKeywords|geonet:child[string(@name)='descriptiveKeywords']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:if test="$core and $dataset">
					<xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationType|geonet:child[string(@name)='spatialRepresentationType']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="gmd:spatialResolution|geonet:child[string(@name)='spatialResolution']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:if>

				<xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:characterSet|geonet:child[string(@name)='characterSet']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:apply-templates mode="elementEP" select="gmd:topicCategory|geonet:child[string(@name)='topicCategory']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>

				<xsl:if test="$dataset">
					<xsl:for-each select="gmd:extent/gmd:EX_Extent">
						<xsl:call-template name="complexElementGuiWrapper">
							<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:EX_Extent']/label"/>
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
				<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:LI_Lineage']/label"/>
				<xsl:with-param name="content">

					<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality">
						<xsl:apply-templates mode="elementEP" select="gmd:scope|geonet:child[string(@name)='scope']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="gmd:lineage|geonet:child[string(@name)='lineage']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:for-each>

				</xsl:with-param>
				<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="group" select="/root/gui/strings/dataQualityTab"/>
      	<xsl:with-param name="edit" select="$edit"/>
				<xsl:with-param name="realname"   select="'gmd:DataQualityInfo'"/>
			</xsl:call-template>

		<!-- referenceSystemInfo in its own box -->
		
			<xsl:call-template name="complexElementGuiWrapper">
				<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:referenceSystemInfo']/label"/>
				<xsl:with-param name="content">

				<xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
					<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code|gmd:referenceSystemIdentifier/gmd:RS_Identifier/geonet:child[string(@name)='code']">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>

					<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace|gmd:referenceSystemIdentifier/gmd:RS_Identifier/geonet:child[string(@name)='codeSpace']">
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
    		<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:distributionInfo']/label"/>
      	<xsl:with-param name="content">

				<xsl:for-each select="gmd:distributionInfo">
        	<xsl:apply-templates mode="elementEP" select="*/gmd:distributionFormat|*/geonet:child[string(@name)='distributionFormat']">
          	<xsl:with-param name="schema" select="$schema"/>
          	<xsl:with-param name="edit"   select="$edit"/>
        	</xsl:apply-templates>

        	<xsl:apply-templates mode="elementEP" select="*/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine|*/gmd:transferOptions/gmd:MD_DigitalTransferOptions/geonet:child[string(@name)='onLine']">
          	<xsl:with-param name="schema" select="$schema"/>
          	<xsl:with-param name="edit"   select="$edit"/>
        	</xsl:apply-templates>
				</xsl:for-each>

      	</xsl:with-param>
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="group" select="/root/gui/strings/distributionTab"/>
      	<xsl:with-param name="edit" select="$edit"/>
      	<xsl:with-param name="realname" select="gmd:distributionInfo"/>
    	</xsl:call-template>
			
		</xsl:if>

		<!-- metadata info in its own box -->

		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:MD_Metadata']/label"/>
			<xsl:with-param name="content">

			<xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:characterSet|geonet:child[string(@name)='characterSet']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>

			<xsl:apply-templates mode="elementEP" select="gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
	
			<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>

			<!-- metadata contact info in its own box -->

			<xsl:for-each select="gmd:contact">

				<xsl:call-template name="complexElementGuiWrapper">
					<xsl:with-param name="title" select="/root/gui/iso19139/element[@name='gmd:contact']/label"/>
					<xsl:with-param name="content">

						<xsl:apply-templates mode="elementEP" select="*/gmd:individualName|*/geonet:child[string(@name)='individualName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="*/gmd:organisationName|*/geonet:child[string(@name)='organisationName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:apply-templates mode="elementEP" select="*/gmd:positionName|*/geonet:child[string(@name)='positionName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>

						<xsl:if test="$core and $dataset">
							<xsl:apply-templates mode="elementEP" select="*/gmd:contactInfo|*/geonet:child[string(@name)='contactInfo']">
            		<xsl:with-param name="schema" select="$schema"/>
            		<xsl:with-param name="edit"   select="$edit"/>
        			</xsl:apply-templates>
						</xsl:if>

						<xsl:apply-templates mode="elementEP" select="*/gmd:role|*/geonet:child[string(@name)='role']">
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

			<xsl:apply-templates mode="elementEP" select="gmd:dateStamp|geonet:child[string(@name)='dateStamp']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:if test="$core and $dataset">
				<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
	
				<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']">
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

	<!-- ============================================================================= -->

	<xsl:template name="complexElementGuiWrapper">
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="schema"/>
		<xsl:param name="group"/>
		<xsl:param name="edit"/>
		<xsl:param name="realname" select="name(.)"/>
		
		<!-- do not show empty elements when editing -->

		<xsl:choose>
		<xsl:when test="normalize-space($content)!=''">
			<xsl:call-template name="complexElementGui">
				<xsl:with-param name="title" select="$title"/>
				<xsl:with-param name="content" select="$content"/>
				<xsl:with-param name="helpLink">
					<xsl:call-template name="getHelpLink">
						<xsl:with-param name="name"   select="$realname"/>
						<xsl:with-param name="schema" select="$schema"/>
					</xsl:call-template>
				</xsl:with-param>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:choose>
				<xsl:when test="$edit">
					<xsl:call-template name="complexElementGui">
						<xsl:with-param name="title" select="$title"/>
						<xsl:with-param name="content">
							<span class="missing"> - <xsl:value-of select="/root/gui/strings/missingSeeTab"/> "<xsl:value-of select="$group"/>" - </span>
						</xsl:with-param>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name"   select="$realname"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="schema" select="$schema"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="complexElementGui">
						<xsl:with-param name="title" select="$title"/>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name"   select="$realname"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="content">
							<span class="missing"> - <xsl:value-of select="/root/gui/strings/missing"/> - </span>
						</xsl:with-param>
						<xsl:with-param name="schema" select="$schema"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:otherwise>
		</xsl:choose>
				
	</xsl:template>

	<!-- ================================================================== -->
	<!-- complete mode we just display everything - tab = complete          -->
	<!-- ================================================================== -->

	<xsl:template name="iso19139Complete">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
		  <xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	
		<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
		  <xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates> 

		<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:apply-templates mode="elementEP" select="gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="'Metadata Info'"/>
			<xsl:with-param name="content">

			<xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:characterSet|geonet:child[string(@name)='characterSet']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>

			<xsl:apply-templates mode="elementEP" select="gmd:contact|geonet:child[string(@name)='contact']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:dateStamp|geonet:child[string(@name)='dateStamp']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:dataSetURI|geonet:child[string(@name)='dataSetURI']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:locale|geonet:child[string(@name)='locale']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>

			<xsl:apply-templates mode="elementEP" select="gmd:series|geonet:child[string(@name)='series']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:describes|geonet:child[string(@name)='describes']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:propertyType|geonet:child[string(@name)='propertyType']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:featureType|geonet:child[string(@name)='featureType']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		
			<xsl:apply-templates mode="elementEP" select="gmd:featureAttribute|geonet:child[string(@name)='featureAttribute']">
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

		<xsl:apply-templates mode="elementEP" select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

	</xsl:template>
	
	
	<!-- ============================================================================= -->

  <xsl:template name="iso19139Metadata">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

<!-- if the parent is root then display fields not in tabs -->

		<xsl:choose>
    <xsl:when test="name(..)='root'">
	    <xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:characterSet|geonet:child[string(@name)='characterSet']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:dateStamp|geonet:child[string(@name)='dateStamp']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

			<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:contact|geonet:child[string(@name)='contact']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:dataSetURI|geonet:child[string(@name)='dataSetURI']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:locale|geonet:child[string(@name)='locale']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:series|geonet:child[string(@name)='series']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:describes|geonet:child[string(@name)='describes']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:propertyType|geonet:child[string(@name)='propertyType']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

			<xsl:apply-templates mode="elementEP" select="gmd:featureType|geonet:child[string(@name)='featureType']">
      	<xsl:with-param name="schema" select="$schema"/>
      	<xsl:with-param name="edit"   select="$edit"/>
    	</xsl:apply-templates>

    	<xsl:apply-templates mode="elementEP" select="gmd:featureAttribute|geonet:child[string(@name)='featureAttribute']">
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

  </xsl:template>
	
	<!-- ============================================================================= -->
	<!--
	simple mode; ISO order is:
	- gmd:fileIdentifier
	- gmd:language
	- gmd:characterSet
	- gmd:parentIdentifier
	- gmd:hierarchyLevel
	- gmd:hierarchyLevelName
	- gmd:contact
	- gmd:dateStamp
	- gmd:metadataStandardName
	- gmd:metadataStandardVersion
	+ gmd:dataSetURI
	+ gmd:locale
	- gmd:spatialRepresentationInfo
	- gmd:referenceSystemInfo
	- gmd:metadataExtensionInfo
	- gmd:identificationInfo
	- gmd:contentInfo
	- gmd:distributionInfo
	- gmd:dataQualityInfo
	- gmd:portrayalCatalogueInfo
	- gmd:metadataConstraints
	- gmd:applicationSchemaInfo
	- gmd:metadataMaintenance
	+ gmd:series
	+ gmd:describes
	+ gmd:propertyType
	+ gmd:featureType
	+ gmd:featureAttribute
	-->
	<!-- ============================================================================= -->

	<xsl:template name="iso19139Simple">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="flat"/>

		<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/metadata"/>
			<xsl:with-param name="content">
				<xsl:call-template name="iso19139Simple2">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:call-template>
			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
		
		<xsl:apply-templates mode="elementEP" select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
	</xsl:template>
	
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="//gmd:language">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:apply-templates mode="iso19139GetIsoLanguage" select="gco:CharacterString">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139GetIsoLanguage" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="lang"  select="/root/gui/language"/>
		<xsl:variable name="value" select="string(.)"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<select class="md" name="_{geonet:element/@ref}" size="1">
					<option name=""/>

					<xsl:for-each select="/root/gui/isoLang/record">
						<option value="{code}">
							<xsl:if test="code = $value">
								<xsl:attribute name="selected"/>
							</xsl:if>							
							<xsl:value-of select="label/child::*[name() = $lang]"/>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>

			<xsl:otherwise>
				<xsl:value-of select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]"/>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template name="iso19139Simple2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="flat"/>
		
		<xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:characterSet|geonet:child[string(@name)='characterSet']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:dateStamp|geonet:child[string(@name)='dateStamp']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:contact|geonet:child[string(@name)='contact']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:dataSetURI|geonet:child[string(@name)='dataSetURI']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:locale|geonet:child[string(@name)='locale']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:series|geonet:child[string(@name)='series']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:describes|geonet:child[string(@name)='describes']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:propertyType|geonet:child[string(@name)='propertyType']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:featureType|geonet:child[string(@name)='featureType']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="gmd:featureAttribute|geonet:child[string(@name)='featureAttribute']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:transferOptions">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		
		<xsl:if test="$edit=false()">
			<xsl:if test="count(gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString[contains(string(.),'download')])>1 and
									//geonet:info/download='true'">
				<xsl:call-template name="complexElementGui">
					<xsl:with-param name="title" select="/root/gui/strings/downloadSummary"/>
					<xsl:with-param name="content">
						<tr>
							<td  align="center">
								<button class="content" onclick="javascript:runFileDownloadSummary('{//geonet:info/uuid}','{/root/gui/strings/downloadSummary}')" type="button">
									<xsl:value-of select="/root/gui/strings/showFileDownloadSummary"/>	
								</button>
							</td>
						</tr>
					</xsl:with-param>
					<xsl:with-param name="helpLink">
						<xsl:call-template name="getHelpLink">
							<xsl:with-param name="name"   select="name(.)"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:contact|gmd:pointOfContact">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:variable name="content">
			<xsl:for-each select="gmd:CI_ResponsibleParty">
				<td class="padded-content" width="100%" colspan="2">
					<table width="100%">
						<tr>
							<td width="50%" valign="top">
								<table width="100%">

									<xsl:apply-templates mode="elementEP" select="gmd:individualName|geonet:child[string(@name)='individualName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="gmd:organisationName|geonet:child[string(@name)='organisationName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="gmd:positionName|geonet:child[string(@name)='positionName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="gmd:role|geonet:child[string(@name)='role']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
								</table>
							</td>
							<td valign="top">
								<table width="100%">
									<xsl:apply-templates mode="elementEP" select="gmd:contactInfo|geonet:child[string(@name)='contactInfo']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
								</table>
							</td>
						</tr>
					</table>
				</td>
			</xsl:for-each>
		</xsl:variable>

		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="$edit"/>
			<xsl:with-param name="content" select="$content"/>
		</xsl:apply-templates>

	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- online resources -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:CI_OnlineResource" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
		<xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString)" />
		<xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string($linkage)!=''">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<a href="{$linkage}" target="_new">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:when test="string($name)!=''">
									<xsl:value-of select="$name"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$linkage"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19139EditOnlineRes" match="*">
		<xsl:param name="schema"/>
	
		<xsl:variable name="id" select="generate-id(.)"/>
		<div id="{$id}"/>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:linkage|geonet:child[string(@name)='linkage']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:protocol|geonet:child[string(@name)='protocol']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:applicationProfile|geonet:child[string(@name)='applicationProfile']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:choose>
					<xsl:when test="string(gmd:protocol/gco:CharacterString)='WWW:DOWNLOAD-1.0-http--download' and string(gmd:name/gco:CharacterString)!=''">
						<xsl:apply-templates mode="iso19139FileRemove" select="gmd:name/gco:CharacterString">
							<xsl:with-param name="access" select="'private'"/>
							<xsl:with-param name="id" select="$id"/>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="gmd:name|geonet:child[string(@name)='name']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="true()"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:apply-templates mode="elementEP" select="gmd:description|geonet:child[string(@name)='description']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:function|geonet:child[string(@name)='function']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- online resources: WMS get map -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(gmd:protocol/gco:CharacterString,'-get-map') and gmd:name]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
		<xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString)" />
		<xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(//geonet:info/dynamic)='true' and string($name)!='' and string($linkage)!=''">
			<!-- Create a link for a WMS service that will open in InterMap opensource -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
					<xsl:with-param name="text">
						<!-- ETj
						<a href="javascript:popInterMap('{/root/gui/url}/intermap/srv/{/root/gui/language}/map.addServicesExt?url={gmd:linkage/gmd:URL}&amp;service={gmd:name/gco:CharacterString}&amp;type=2')" title="{/root/strings/interactiveMap}">
						-->
						<a href="javascript:runIM_addService('{$linkage}','{$name}',2)" title="{/root/strings/interactiveMap}">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
						</a><br/>(OGC-WMS Server: <xsl:value-of select="$linkage"/> )
					</xsl:with-param>
				</xsl:apply-templates>
				<!-- Create a link for a WMS service that will open in Google Earth through the reflector -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/viewInGE"/>
					<xsl:with-param name="text">
						<a href="{/root/gui/locService}/google.kml?uuid={//geonet:info/uuid}&amp;layers={$name}" title="{/root/strings/interactiveMap}">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
							&#160;
							<img src="{/root/gui/url}/images/google_earth_link.gif" height="20px" width="20px" alt="{/root/gui/strings/viewInGE}" title="{/root/gui/strings/viewInGE}" style="border: 0px solid;"/>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- online resources: WMS get capabilities -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(gmd:protocol/gco:CharacterString,'-get-capabilities') and gmd:name]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
		<xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString)" />
		<xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(//geonet:info/dynamic)='true' and string($linkage)!=''">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
					<xsl:with-param name="text">
						<a href="javascript:runIM_selectService('{$linkage}',2,{//geonet:info/id})" title="{/root/strings/interactiveMap}">							
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- online resources: ARCIMS -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'ESRI:AIMS-') and contains(gmd:protocol/gco:CharacterString,'-get-image') and gmd:name]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
		<xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString)" />
		<xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(//geonet:info/dynamic)='true' and string($linkage)!='' and string($name)!=''">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
					<xsl:with-param name="text">
<!--	ETj					<a href="javascript:popInterMap('{/root/gui/url}/intermap/srv/{/root/gui/language}/map.addServicesExt?url={gmd:linkage/gmd:URL}&amp;service={gmd:name/gco:CharacterString}&amp;type=1')" title="{/root/strings/interactiveMap}">
-->						<a href="javascript:runIM_addService('{$linkage}','{$name}',1)" title="{/root/strings/interactiveMap}">
								<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- online resources: download -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(gmd:protocol/gco:CharacterString,'http--download') and gmd:name]" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
		<xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString)" />
		<xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(//geonet:info/download)='true' and string($linkage)!='' and not(contains($linkage,$download_check))">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/downloadData"/>
					<xsl:with-param name="text">
						<xsl:variable name="title">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<a href="{$linkage}" title="{$title}" onclick="runFileDownload(this.href, this.title); return false;"><xsl:value-of select="$title"/></a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- protocol -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="gmd:protocol" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="title">
						<xsl:call-template name="getTitle">
							<xsl:with-param name="name"   select="name(.)"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:variable name="value" select="string(gco:CharacterString)"/>
						<xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
						<xsl:variable name="fref" select="../gmd:name/gco:CharacterString/geonet:element/@ref"/>
						<input type="hidden" id="_{$ref}" name="_{$ref}" value="{$value}"/>
						<select id="s_{$ref}" name="s_{$ref}" size="1" onchange="checkForFileUpload('{$fref}', '{$ref}');" class="md">
							<xsl:if test="$value=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/strings/protocolChoice[@value]">
								<option>
									<xsl:if test="string(@value)=$value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value"><xsl:value-of select="string(@value)"/></xsl:attribute>
									<xsl:value-of select="string(.)"/>
								</option>
							</xsl:for-each>
						</select>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="false()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- FIXME graphOver -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="graphOver">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:if test="$edit=true() and $currTab!='simple'">
			<xsl:apply-templates mode="iso19139EditGraphOver" select=".">
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- FIXME 	-->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139EditGraphOver" match="*">
		<xsl:param name="schema"/>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>
			<xsl:with-param name="content">
				
				<xsl:choose>
					<xsl:when test="(string(bgFileDesc)='thumbnail' or string(bgFileDesc)='large_thumbnail') and string(bgFileName)!=''">
						<xsl:apply-templates mode="iso19139FileRemove" select="bgFileName"/>
					</xsl:when>
					<xsl:when test="string(bgFileDesc)='thumbnail' or string(bgFileDesc)='large_thumbnail'">
						<xsl:apply-templates mode="iso19139FileUpload" select="bgFileName"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="bgFileName|geonet:child[string(@name)='bgFileName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="true()"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:apply-templates mode="elementEP" select="bgFileDesc|geonet:child[string(@name)='bgFileDesc']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="bgFileType|geonet:child[string(@name)='bgFileType']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- FIXME bgFileDesc -->
	<!-- ============================================================================= -->

	<xsl:template mode="iso19139" match="bgFileDesc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="text">
						<xsl:variable name="value" select="string(.)"/>
						<select name="_{geonet:element/@ref}" size="1">
							<xsl:if test="string(.)=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/strings/bgFileDescChoice[@value]">
								<option value="{string(@value)}">
									<xsl:if test="string(@value)=$value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="string(.)"/>
								</option>
							</xsl:for-each>
						</select>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="false()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ===================================================================== -->
	<!-- name for onlineresource only -->
	<!-- ===================================================================== -->

	<xsl:template mode="iso19139" match="gmd:name[name(..)='gmd:CI_OnlineResource']" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="protocol" select="../gmd:protocol/gco:CharacterString"/>
				<xsl:variable name="pref" select="../gmd:protocol/gco:CharacterString/geonet:element/@ref"/>
				<xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
				<xsl:variable name="value" select="gco:CharacterString"/>
				<xsl:variable name="button" select="starts-with($protocol,'WWW:DOWNLOAD') and contains($protocol,'http') and normalize-space($value)=''"/>

				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="title" select="/root/gui/strings/file"/>
					<xsl:with-param name="text">
						<button class="content" onclick="startFileUpload({//geonet:info/id}, '{$ref}');" type="button">
							<xsl:value-of select="'Upload File'"/>
						</button>
					</xsl:with-param>
					<xsl:with-param name="id" select="concat('db_',$ref)"/>
					<xsl:with-param name="visible" select="$button"/>
				</xsl:call-template>

				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="title">
						<xsl:call-template name="getTitle">
							<xsl:with-param name="name"   select="name(.)"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="text">
						<input id="_{gco:CharacterString/geonet:element/@ref}" class="md" type="text" name="_{gco:CharacterString/geonet:element/@ref}" value="{gco:CharacterString/text()}" size="50" />
						</xsl:with-param>
					<xsl:with-param name="id" select="concat('di_',$ref)"/>
					<xsl:with-param name="visible" select="not($button)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="false()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template mode="iso19139FileRemove" match="*">
		<xsl:param name="access" select="'public'"/>
		<xsl:param name="id"/>
	
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/file"/>
			<xsl:with-param name="text">
				<table width="100%"><tr>
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<td width="70%"><xsl:value-of select="string(.)"/></td>
					<td align="right"><button class="content" onclick="javascript:doFileRemoveAction('{/root/gui/locService}/resources.del','{$ref}','{$access}',{$id})"><xsl:value-of select="/root/gui/strings/remove"/></button></td>
				</tr></table>
			</xsl:with-param>
			<xsl:with-param name="schema"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ===================================================================== -->
	<!-- === iso19139 brief formatting === -->
	<!-- ===================================================================== -->
	
	<xsl:template name="iso19139Brief">
		<metadata>
			<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
			<xsl:variable name="id" select="geonet:info/id"/>
			<xsl:variable name="uuid" select="geonet:info/uuid"/>
			<xsl:apply-templates mode="briefster" select="gmd:identificationInfo/gmd:MD_DataIdentification|gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|gmd:identificationInfo/srv:SV_ServiceIdentification">
				<xsl:with-param name="id" select="$id"/>
			</xsl:apply-templates>

			<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
				<xsl:variable name="protocol" select="gmd:protocol/gco:CharacterString"/>
				<xsl:variable name="linkage"  select="normalize-space(gmd:linkage/gmd:URL)"/>
				<xsl:variable name="name"     select="normalize-space(gmd:name/gco:CharacterString)"/>
				<xsl:variable name="desc"     select="normalize-space(gmd:description/gco:CharacterString)"/>
				
				<xsl:if test="string($linkage)!=''">
				
						<xsl:element name="link">
							<xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
							<xsl:attribute name="href"><xsl:value-of select="$linkage"/></xsl:attribute>
							<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
							<xsl:choose>
								<xsl:when test="starts-with($protocol,'WWW:LINK-')">
									<xsl:attribute name="type">text/html</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.jpg')">
									<xsl:attribute name="type">image/jpeg</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.png')">
									<xsl:attribute name="type">image/png</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.gif')">
									<xsl:attribute name="type">image/gif</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.doc')">
									<xsl:attribute name="type">application/word</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.zip')">
									<xsl:attribute name="type">application/zip</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.pdf')">
									<xsl:attribute name="type">application/pdf</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kml')">
									<xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kmz')">
									<xsl:attribute name="type">application/vnd.google-earth.kmz</xsl:attribute>
								</xsl:when>
								<xsl:when test="starts-with($protocol,'OGC:WMS-')">
									<xsl:attribute name="type">application/vnd.ogc.wms_xml</xsl:attribute>
								</xsl:when>
								<xsl:when test="$protocol='ESRI:AIMS-'">
									<xsl:attribute name="type">application/vnd.esri.arcims_axl</xsl:attribute>
								</xsl:when>
								<xsl:when test="$protocol!=''">
									<xsl:attribute name="type"><xsl:value-of select="$protocol"/></xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<!-- fall back to the default content type -->
									<xsl:attribute name="type">text/plain</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>

				</xsl:if>

				<!-- Generate a KML output link for a WMS service -->
				<xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
					
					<xsl:element name="link">
						<xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
						<xsl:attribute name="href">
							<xsl:value-of select="concat('http://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
						</xsl:attribute>
						<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
						<xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
					</xsl:element>
				</xsl:if>

				<!-- The old links still in use by some systems. Deprecated -->
				<xsl:choose>
					<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($protocol,'http--download') and not(contains($linkage,$download_check))">
						<link type="download"><xsl:value-of select="$linkage"/></link>
					</xsl:when>
					<xsl:when test="starts-with($protocol,'ESRI:AIMS-') and contains($protocol,'-get-image') and string($linkage)!='' and string($name)!=''">
						<link type="arcims">
<!--							<xsl:value-of select="concat('javascript:popInterMap(&#34;',/root/gui/url,'/intermap/srv/',/root/gui/language,'/map.addServicesExt?url=',$linkage,'&amp;service=',$name,'&amp;type=1&#34;)')"/>-->
							<xsl:value-of select="concat('javascript:runIM_addService(&#34;'  ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;, 1)' )"/>
						</link>
					</xsl:when>
					<xsl:when test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
						<link type="wms">
<!--							<xsl:value-of select="concat('javascript:popInterMap(&#34;',/root/gui/url,'/intermap/srv/',/root/gui/language,'/map.addServicesExt?url=',$linkage,'&amp;service=',$name,'&amp;type=2&#34;)')"/>-->
							<xsl:value-of select="concat('javascript:runIM_addService(&#34;'  ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;, 2)' )"/>
						</link>
						<link type="googleearth">
							<xsl:value-of select="concat(/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
						</link>
					</xsl:when>
					<xsl:when test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-capabilities') and string($linkage)!=''">
						<link type="wms">
							<xsl:value-of select="concat('javascript:runIM_selectService(&#34;'  ,  $linkage  ,  '&#34;, 2,',$id,')' )"/>
						</link>
					</xsl:when>
					<xsl:when test="string($linkage)!=''">
						<link type="url"><xsl:value-of select="$linkage"/></link>
					</xsl:when>
					
				</xsl:choose>
			</xsl:for-each>

			<xsl:copy-of select="geonet:info"/>
		</metadata>
	</xsl:template>

	<xsl:template mode="briefster" match="*">
		<xsl:param name="id"/>
	
			<xsl:if test="gmd:citation/gmd:CI_Citation/gmd:title">
				<title><xsl:value-of select="gmd:citation/gmd:CI_Citation/gmd:title"/></title>
			</xsl:if>
			
			<xsl:if test="gmd:abstract">
				<abstract><xsl:value-of select="gmd:abstract"/></abstract>
			</xsl:if>

			<xsl:for-each select=".//gmd:keyword/gco:CharacterString[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>

			<xsl:if test="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
				<geoBox>
					<westBL><xsl:value-of select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude"/></westBL>
					<eastBL><xsl:value-of select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude"/></eastBL>
					<southBL><xsl:value-of select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude"/></southBL>
					<northBL><xsl:value-of select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude"/></northBL>
				</geoBox>
			</xsl:if>

			<xsl:for-each select="*/gmd:MD_Constraints/*">
				<Constraints>
					<xsl:apply-templates mode="iso19139" select=".">
						<xsl:with-param name="schema" select="//geonet:info/schema"/>
						<xsl:with-param name="edit" select="false()"/>
					</xsl:apply-templates>
				</Constraints>
			</xsl:for-each>

			<xsl:for-each select="*/gmd:MD_SecurityConstraints/*">
				<SecurityConstraints>
					<xsl:apply-templates mode="iso19139" select=".">
						<xsl:with-param name="schema" select="//geonet:info/schema"/>
						<xsl:with-param name="edit" select="false()"/>
					</xsl:apply-templates>
				</SecurityConstraints>
			</xsl:for-each>

			<xsl:for-each select="*/gmd:MD_LegalConstraints/*">
				<LegalConstraints>
					<xsl:apply-templates mode="iso19139" select=".">
						<xsl:with-param name="schema" select="//geonet:info/schema"/>
						<xsl:with-param name="edit" select="false()"/>
					</xsl:apply-templates>
				</LegalConstraints>
			</xsl:for-each>

			<xsl:if test="not(geonet:info/server)">
				<xsl:variable name="info" select="geonet:info"/>

				<xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
					<xsl:variable name="fileName"  select="gmd:fileName/gco:CharacterString"/>
					<xsl:if test="$fileName != ''">
						<xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
						<xsl:choose>

							<!-- the thumbnail is an url -->

							<xsl:when test="contains($fileName ,'://')">
								<image type="unknown"><xsl:value-of select="$fileName"/></image>								
							</xsl:when>

							<!-- small thumbnail -->

							<xsl:when test="string($fileDescr)='thumbnail'">
								<xsl:choose>
									<xsl:when test="$info/isHarvested = 'y'">
										<xsl:if test="$info/harvestInfo/smallThumbnail">
											<image type="thumbnail">
												<xsl:value-of select="concat($info/harvestInfo/smallThumbnail, $fileName)"/>
											</image>
										</xsl:if>
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
									<xsl:when test="$info/isHarvested = 'y'">
										<xsl:if test="$info/harvestInfo/largeThumbnail">
											<image type="overview">
												<xsl:value-of select="concat($info/harvestInfo/largeThumbnail, $fileName)"/>
											</image>
										</xsl:if>
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

	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- iso19139 complete tab template	-->
	<!-- ============================================================================= -->

	<xsl:template name="iso19139CompleteTab">
		<xsl:param name="tabLink"/>
		
		<xsl:call-template name="displayTab">
			<xsl:with-param name="tab"     select="'groups'"/> <!-- just a non-existing tab -->
			<xsl:with-param name="text"    select="/root/gui/strings/byGroup"/>
			<xsl:with-param name="tabLink" select="$tabLink"/>
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

		<xsl:call-template name="displayTab">
			<xsl:with-param name="tab"     select="'packages'"/> <!-- just a non-existing tab -->
			<xsl:with-param name="text"    select="/root/gui/strings/byPackage"/>
			<xsl:with-param name="tabLink" select="$tabLink"/>
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
		
	</xsl:template>
	
	<!-- ============================================================================= -->
	<!-- utilities -->
	<!-- ============================================================================= -->
	
	<xsl:template mode="iso19139IsEmpty" match="*|@*|text()">
		<xsl:choose>
			<!-- normal element -->
			<xsl:when test="*">
				<xsl:apply-templates mode="iso19139IsEmpty"/>
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

	<!-- List of regions to define country.
	gmd:country is not a codelist (only country in PT_Local is).
	A list of existing countries in Regions table is suggested to the editor.
	The input text could also be used to type another value.
	-->
	<xsl:template mode="iso19139" match="gmd:country[gco:CharacterString]" priority="1">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		
		<xsl:variable name="qname" select="name(.)"/>
		<xsl:variable name="value" select="gco:CharacterString"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema" />
			<xsl:with-param name="edit" select="$edit" />
			<xsl:with-param name="text">
				<xsl:choose>                    
					<xsl:when test="$edit=true()">
						
						<xsl:variable name="lang" select="/root/gui/language"/>
						<input class="md" name="_{gco:CharacterString/geonet:element/@ref}"
						id="_{gco:CharacterString/geonet:element/@ref}" value="{gco:CharacterString}"/>
						<xsl:text> </xsl:text>
						<select class="md"
							onchange="$('_{gco:CharacterString/geonet:element/@ref}').value = this.options[this.selectedIndex].value;"
							size="1">
							<option name="" />
							<xsl:for-each select="/root/gui/regions/record">
								<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
								
								<option value="{label/child::*[name() = $lang]}">
									<xsl:if test="$value = label/child::*[name() = $lang]">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="$value" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

</xsl:stylesheet>
