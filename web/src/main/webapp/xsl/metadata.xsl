<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt= "http://exslt.org/common"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:svrl="http://purl.oclc.org/dsdl/svrl" 
	exclude-result-prefixes="exslt xlink gco gmd geonet svrl">

	<xsl:import href="text-utilities.xsl"/>
	<xsl:include href="metadata-utils.xsl"/>
	<xsl:include href="metadata-controls.xsl"/>
	
	<xsl:variable name="flat" select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
	 
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- main schema switch -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template mode="elementEP" match="*|@*">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded" />
		
		<xsl:choose>
		
			<!-- ISO 19115 -->
			<xsl:when test="$schema='iso19115'">
				<xsl:apply-templates mode="iso19115" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- ISO 19139 and profiles -->
			<xsl:when test="starts-with($schema,'iso19139')">
				<xsl:apply-templates mode="iso19139" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- ISO 19110 -->
			<xsl:when test="$schema='iso19110'">
				<xsl:apply-templates mode="iso19110" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- FGDC -->
			<xsl:when test="$schema='fgdc-std'">
				<xsl:apply-templates mode="fgdc-std" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:when>

			<!-- Dublin Core -->
			<xsl:when test="$schema='dublin-core'">
				<xsl:apply-templates mode="dublin-core" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:when>
		
			<!-- default, no schema-specific formatting -->
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>

	<!--
	new children
	-->
	<xsl:template mode="elementEP" match="geonet:child">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded" />

		<!-- draw child element place holder if
			- child is an OR element or
			- there is no other element with the name of this placeholder 
		-->
		<xsl:variable name="name">
			<xsl:choose>
				<xsl:when test="@prefix=''"><xsl:value-of select="@name"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="concat(@prefix,':',@name)"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- build a qualified name with COLON as the separator -->
		<xsl:variable name="qname">
			<xsl:choose>
				<xsl:when test="@prefix=''"><xsl:value-of select="@name"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="concat(@prefix,'COLON',@name)"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="parentName" select="../geonet:element/@ref|@parent"/>
		<xsl:variable name="max" select="../geonet:element/@max|@max"/>
		<xsl:variable name="prevBrother" select="preceding-sibling::*[1]"/>
		
		<!--
			Exception for gmd:graphicOverview because GeoNetwork manage thumbnail
			using specific interface for thumbnail and large_thumbnail but user should be able to add
			thumbnail using a simple URL.
		-->		
		<xsl:variable name="exception" select="../gmd:graphicOverview[gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString='thumbnail' or gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString='large_thumbnail']"/>
		
		<!-- <xsl:variable name="subtemplates" select="/root/gui/subtemplates/record[string(root)=$name]"/> -->
		<xsl:variable name="subtemplates" select="/root/gui/subtemplates/record[string(root)='']"/>
		<xsl:if test="not($flat)">
		  <xsl:if test="(geonet:choose or name($prevBrother)!=$name) or $subtemplates"> 
			<xsl:if test="(geonet:choose 
							or name($prevBrother)!=$name 
							or $exception) 
							or $subtemplates">
				<xsl:variable name="text">
					<xsl:if test="geonet:choose">
						<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
						<select class="md" name="_{$parentName}_{$qname}" size="1">
							<xsl:if test="$isXLinked">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>

							<xsl:variable name="defaultSelection" select="/root/gui/config/editor-default-substitutions/element[@name=$name]/@default" />

							<xsl:for-each select="geonet:choose">
								<!-- FIXME : here we should sort by title ? -->
								<xsl:sort select="@name"/>
								<option value="{@name}">
									<xsl:if test="@name = $defaultSelection">
									<xsl:attribute name="selected">selected</xsl:attribute> 
									</xsl:if>
									
									<xsl:call-template name="getTitle">
										<xsl:with-param name="name"   select="@name"/>
										<xsl:with-param name="schema" select="$schema"/>
									</xsl:call-template>
									<xsl:text> </xsl:text>
									(<xsl:value-of select="@name"/>)
								</option>
							</xsl:for-each>
						</select>
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="id" select="@uuid"/>
				<xsl:variable name="addLink">
					<xsl:choose>
						<xsl:when test="geonet:choose or $subtemplates">
							<xsl:value-of select="concat('doNewORElementAction(',$apos,'/metadata.elem.add',$apos,',',$parentName,',',$apos,$name,$apos,',document.mainForm._',$parentName,'_',$qname,'.value,',$apos,$id,$apos,',',$apos,@action,$apos,',',$max,');')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="concat('doNewElementAction(',$apos,'/metadata.elem.add',$apos,',',$parentName,',',$apos,$name,$apos,',',$apos,$id,$apos,',',$apos,@action,$apos,',',$max,');')"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="addXMLFragment">
					<xsl:variable name="function">
						<xsl:apply-templates mode="addXMLFragment" select="."/>
					</xsl:variable>
					<xsl:if test="normalize-space($function)!=''">
						<xsl:value-of select="concat('javascript:', $function, '(',$parentName,',',$apos,$name,$apos,');')"/>
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="helpLink">
					<xsl:call-template name="getHelpLink">
						<xsl:with-param name="name"   select="$name"/>
						<xsl:with-param name="schema" select="$schema"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="title">
						<xsl:call-template name="getTitle">
							<xsl:with-param name="name"   select="$name"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="text" select="$text"/>
					<xsl:with-param name="addLink"  select="$addLink"/>
					<xsl:with-param name="addXMLFragment"  select="$addXMLFragment"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="id"     	select="$id"/>
				</xsl:call-template>
			</xsl:if>
		  </xsl:if>
		</xsl:if>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- callbacks from schema templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template mode="element" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		<xsl:param name="flat"   select="false()"/>
		<xsl:param name="embedded" />
							
		<xsl:choose>
			<!-- has children or attributes, existing or potential -->
			<xsl:when test="*[namespace-uri(.)!=$geonetUri]|@*|geonet:child|geonet:element/geonet:attribute">
			
				<xsl:choose>
					
					<!-- display as a list -->
					<xsl:when test="$flat=true()">
						
						<!-- if it does not have children show it as a simple element -->
						<xsl:if test="not(*[namespace-uri(.)!=$geonetUri]|geonet:child|geonet:element/geonet:attribute)">
							<xsl:apply-templates mode="simpleElement" select=".">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
						</xsl:if>
						<!-- existing attributes -->
						<xsl:apply-templates mode="simpleElement" select="@*">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
						<!-- new attributes -->
						<!-- FIXME
						<xsl:apply-templates mode="elementEP" select="geonet:attribute">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
						-->
						<!-- existing and new children -->
						<xsl:apply-templates mode="elementEP" select="*[namespace-uri(.)!=$geonetUri]|geonet:child">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:when>
					
					<!-- display boxed -->
					<xsl:otherwise>
						<xsl:apply-templates mode="complexElement" select=".">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>

			<!-- neither children nor attributes, just text -->
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:otherwise>
			
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="simpleElement" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		<xsl:param name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="text">
			<xsl:call-template name="getElementText">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="helpLink">
			<xsl:call-template name="getHelpLink">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:call-template name="editSimpleElement">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="text"     select="$text"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="showSimpleElement">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="text"     select="$text"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="simpleElement" match="@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		<xsl:param name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="text">
			<xsl:call-template name="getAttributeText">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="helpLink">
			<xsl:call-template name="getHelpLink">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:call-template name="editAttribute">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="text"     select="$text"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="showSimpleElement">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="text"     select="$text"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="complexElement" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		<xsl:param name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="content">
			<xsl:call-template name="getContent">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:call-template>
		</xsl:param>
		<xsl:param name="helpLink">
			<xsl:call-template name="getHelpLink">
				<xsl:with-param name="name"   select="name(.)"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:param>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:call-template name="editComplexElement">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="content"  select="$content"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="showComplexElement">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="title"    select="$title"/>
					<xsl:with-param name="content"  select="$content"/>
					<xsl:with-param name="helpLink" select="$helpLink"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!--
	prevent drawing of geonet:* elements
	-->
    <xsl:template mode="element" match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType|@gco:nilReason"/>
    <xsl:template mode="simpleElement" match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType|@gco:nilReason"/>
    <xsl:template mode="complexElement" match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType|@gco:nilReason"/>
	
	<!--
	prevent drawing of attributes starting with "_", used in old GeoNetwork versions
	-->
	<xsl:template mode="simpleElement" match="@*[starts-with(name(.),'_')]"/>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- elements/attributes templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!--
	shows a simple element
	-->
	<xsl:template name="showSimpleElement">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="text"/>
		<xsl:param name="helpLink"/>
	
		<!-- don't show it if there isn't anything in it! -->
		<xsl:if test="normalize-space($text)!=''">
			<xsl:call-template name="simpleElementGui">
				<xsl:with-param name="title" select="$title"/>
				<xsl:with-param name="text" select="$text"/>
				<xsl:with-param name="helpLink" select="$helpLink"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!--
	shows a complex element
	-->
	<xsl:template name="showComplexElement">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="helpLink"/>
	
		<!-- don't show it if there isn't anything in it! -->
		<xsl:if test="normalize-space($content)!=''">
			<xsl:call-template name="complexElementGui">
				<xsl:with-param name="title" select="$title"/>
				<xsl:with-param name="text" select="text()"/>
				<xsl:with-param name="content" select="$content"/>
				<xsl:with-param name="helpLink" select="$helpLink"/>
				<xsl:with-param name="schema" select="$schema"/>
			</xsl:call-template>
		</xsl:if>

	</xsl:template>
	
	<!--
	shows editable fields for a simple element
	-->
	<xsl:template name="editSimpleElement">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="text"/>
		<xsl:param name="helpLink"/>
		
		<!-- if it's the last brother of it's type and there is a new brother make addLink -->

		<xsl:variable name="id" select="geonet:element/@uuid"/>
		<xsl:variable name="addLink">
			<xsl:call-template name="addLink">
				<xsl:with-param name="id" select="$id"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="addXMLFragment">
			<xsl:call-template name="addXMLFragment">
				<xsl:with-param name="id" select="$id"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="removeLink">
			<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',geonet:element/@ref,',',geonet:element/@parent,',',$apos,$id,$apos,',',geonet:element/@min,');')"/>
			<xsl:if test="not(geonet:element/@del='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="upLink">
			<xsl:value-of select="concat('doMoveElementAction(',$apos,'/metadata.elem.up',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
			<xsl:if test="not(geonet:element/@up='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="downLink">
			<xsl:value-of select="concat('doMoveElementAction(',$apos,'/metadata.elem.down',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
			<xsl:if test="not(geonet:element/@down='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
<!-- xsd and schematron validation info -->
		<xsl:variable name="validationLink">
			<xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
			<xsl:call-template name="validationLink">
				<xsl:with-param name="ref" select="$ref"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="$text"/>
			<xsl:with-param name="addLink" select="$addLink"/>
			<xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
			<xsl:with-param name="removeLink" select="$removeLink"/>
			<xsl:with-param name="upLink"     select="$upLink"/>
			<xsl:with-param name="downLink"   select="$downLink"/>
			<xsl:with-param name="helpLink"   select="$helpLink"/>
			<xsl:with-param name="validationLink" select="$validationLink"/>
			<xsl:with-param name="edit"       select="true()"/>
			<xsl:with-param name="id" select="$id"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
		Template to create validation link popup on XSD errors
		or schematron errors.
		-->
	<xsl:template name="validationLink">
		<xsl:param name="ref"/>
		
		<xsl:if test="@geonet:xsderror
			or */@geonet:xsderror
			or //svrl:failed-assert[@ref=$ref]">
			<ul>
				<xsl:choose> 
					<!-- xsd validation -->
					<xsl:when test="@geonet:xsderror">
						<li><xsl:value-of select="concat(/root/gui/strings/xsdError,': ',@geonet:xsderror)"/></li>
					</xsl:when>
					<!-- some simple elements hide lower elements to remove some
						complexity from the display (eg. gco: in iso19139) 
						so check if they have a schematron/xsderror and move it up 
						if they do -->
					<xsl:when test="*/@geonet:xsderror"> 
						<li><xsl:value-of select="concat(/root/gui/strings/xsdError,': ',*/@geonet:xsderror)"/></li>
					</xsl:when>
					<!-- schematrons -->
					<xsl:when test="//svrl:failed-assert[@ref=$ref]"> 
						<xsl:for-each select="//svrl:failed-assert[@ref=$ref]">
							<li><xsl:value-of select="preceding-sibling::svrl:active-pattern[1]/@name"/> : <xsl:copy-of select="svrl:text/*"/></li>
						</xsl:for-each>
					</xsl:when>
				</xsl:choose>
			</ul>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="addLink">
		<xsl:param name="id"/>

		<xsl:variable name="name" select="name(.)"/>
		<xsl:variable name="nextBrother" select="following-sibling::*[1]"/>
		<xsl:variable name="nb">
			<xsl:if test="name($nextBrother)='geonet:child'">
				<xsl:choose>
					<xsl:when test="$nextBrother/@prefix=''">
						<xsl:if test="$nextBrother/@name=$name"><xsl:copy-of select="$nextBrother"/></xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="concat($nextBrother/@prefix,':',$nextBrother/@name)=$name">
							<xsl:copy-of select="$nextBrother"/>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="newBrother" select="exslt:node-set($nb)"/>

		<xsl:choose>
			<!-- place + because schema insists ie. next element is geonet:child -->
			<xsl:when test="$newBrother/* and not($newBrother/*/geonet:choose)">
				<xsl:value-of select="concat('doNewElementAction(',$apos,'/metadata.elem.add',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');')"/>
			</xsl:when>
			<!-- place optional + for use when re-ordering etc -->
			<xsl:when test="geonet:element/@add='true' and name($nextBrother)=name(.)">
				<xsl:value-of select="concat('doNewElementAction(',$apos,'/metadata.elem.add',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');!OPTIONAL')"/>
			</xsl:when>
			<!-- place + because schema insists but no geonet:child nextBrother 
			     this case occurs in the javascript handling of the + -->
			<xsl:when test="geonet:element/@add='true' and not($newBrother/*/geonet:choose)">
				<xsl:value-of select="concat('doNewElementAction(',$apos,'/metadata.elem.add',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');')"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- 
		Add elements : will popup a remote element selector
		and add the XML fragment in the metadata
	-->
	<xsl:template name="addXMLFragment">
		<xsl:param name="id"/>
		
		<xsl:variable name="name" select="name(.)"/>
		
		<xsl:variable name="function">
			<xsl:apply-templates mode="addXMLFragment" select="."/>
		</xsl:variable>
		<xsl:choose>
			<!-- Create link only when a function is available -->
			<xsl:when test="normalize-space($function)!=''">

				<xsl:variable name="nextBrother" select="following-sibling::*[1]"/>
				<xsl:variable name="nb">
					<xsl:if test="name($nextBrother)='geonet:child'">
						<xsl:choose>
							<xsl:when test="$nextBrother/@prefix=''">
								<xsl:if test="$nextBrother/@name=$name"><xsl:copy-of select="$nextBrother"/></xsl:if>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test="concat($nextBrother/@prefix,':',$nextBrother/@name)=$name">
									<xsl:copy-of select="$nextBrother"/>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</xsl:variable>
				
				<xsl:variable name="newBrother" select="exslt:node-set($nb)"/>
				
				<xsl:choose>
					<!-- place +/x because schema insists ie. next element is geonet:child -->
					<xsl:when test="$newBrother/* and not($newBrother/*/geonet:choose) and $nextBrother/@prefix=''">
						<xsl:value-of select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@name,$apos,');')"/>
					</xsl:when>
					<xsl:when test="$newBrother/* and not($newBrother/*/geonet:choose)">
		                <xsl:value-of select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@prefix,':',$nextBrother/@name,$apos,');')"/>
					</xsl:when>
					<!-- place optional +/x for use when re-ordering etc -->
		            <xsl:when test="geonet:element/@add='true' and name($nextBrother)=name(.)">
		            	<xsl:value-of select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@name,$apos,');!OPTIONAL')"/>
		            </xsl:when>
					<!-- place +/x because schema insists but no geonet:child nextBrother 
		                 this case occurs in the javascript handling of the +/+ -->
		            <xsl:when test="geonet:element/@add='true' and not($newBrother/*/geonet:choose)">
		            	<xsl:value-of select="concat('javascript:', $function, '(',geonet:element/@parent,',',$apos,$name,$apos,');')"/>
		            </xsl:when>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
		
	</xsl:template>
	
	<!--
	shows editable fields for an attribute
	-->
	<!-- FIXME: not schema-configurable -->
	<xsl:template name="editAttribute">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="text"/>
		<xsl:param name="helpLink"/>
		
		<xsl:variable name="name" select="name(.)"/>
		<xsl:variable name="value" select="string(.)"/>
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="$text"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
			<xsl:with-param name="edit"     select="true()"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	shows editable fields for a complex element
	-->
	<xsl:template name="editComplexElement">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="helpLink"/>
		
		<xsl:variable name="id" select="geonet:element/@uuid"/>
		<xsl:variable name="addLink">
			<xsl:call-template name="addLink">
				<xsl:with-param name="id" select="$id"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="addXMLFragment">
			<xsl:call-template name="addXMLFragment">
				<xsl:with-param name="id" select="$id"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="removeLink">
			<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',geonet:element/@ref,',',geonet:element/@parent,',',$apos,$id,$apos,',',geonet:element/@min,');')"/>
			<xsl:if test="not(geonet:element/@del='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="upLink">
			<xsl:value-of select="concat('doMoveElementAction(',$apos,'/metadata.elem.up',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
			<xsl:if test="not(geonet:element/@up='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="downLink">
			<xsl:value-of select="concat('doMoveElementAction(',$apos,'/metadata.elem.down',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
			<xsl:if test="not(geonet:element/@down='true')">
				<xsl:text>!OPTIONAL</xsl:text>
			</xsl:if>
		</xsl:variable>
<!-- xsd and schematron validation info -->
		<xsl:variable name="validationLink">
			<xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
			<xsl:call-template name="validationLink">
				<xsl:with-param name="ref" select="$ref"/>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="text()"/>
			<xsl:with-param name="content" select="$content"/>
			<xsl:with-param name="addLink" select="$addLink"/>
			<xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
			<xsl:with-param name="removeLink" select="$removeLink"/>
			<xsl:with-param name="upLink" select="$upLink"/>
			<xsl:with-param name="downLink" select="$downLink"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
			<xsl:with-param name="validationLink" select="$validationLink"/>
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>			
			<xsl:with-param name="id" select="$id"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- gui templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!--
	gui to show a simple element
	-->
	<xsl:template name="simpleElementGui">
		<xsl:param name="title"/>
		<xsl:param name="text"/>
		<xsl:param name="helpLink"/>
		<xsl:param name="addLink"/>
		<xsl:param name="addXMLFragment"/>
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		<xsl:param name="validationLink"/>
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="id" select="generate-id(.)"/>
		<xsl:param name="visible" select="true()"/>

		<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
		<xsl:variable name="geonet" select="starts-with(name(.),'geonet:')"/>

		<tr id="{$id}" type="metadata">
			<xsl:if test="not($visible)">
				<xsl:attribute name="style">
					display:none;
				</xsl:attribute>
			</xsl:if>
			<th class="md" width="20%" valign="top">
				<xsl:choose>
					<xsl:when test="$isXLinked">
						<xsl:attribute name="class">md xlinked</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="class">md</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			
				<xsl:choose>
					<xsl:when test="$helpLink!=''">
						<span id="stip.{$helpLink}|{$id}" onclick="toolTip(this.id);" class="content" style="cursor:help;">
							<xsl:value-of select="$title"/>
						</span>
						<xsl:call-template name="asterisk">
							<xsl:with-param name="link" select="$removeLink"/>
							<xsl:with-param name="edit" select="$edit"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="showTitleWithTag">
							<xsl:with-param name="title" select="$title"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&#160;</xsl:text>
				<xsl:if test="$edit and not($isXLinked)">
					<xsl:call-template name="getButtons">
						<xsl:with-param name="addLink" select="$addLink"/>
						<xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
						<xsl:with-param name="removeLink" select="$removeLink"/>
						<xsl:with-param name="upLink" select="$upLink"/>
						<xsl:with-param name="downLink" select="$downLink"/>
						<xsl:with-param name="validationLink" select="$validationLink"/>
						<xsl:with-param name="id" select="$id"/>
					</xsl:call-template>
				</xsl:if>
			</th>
			<td class="padded" valign="top">
			
				<xsl:variable name="textnode" select="exslt:node-set($text)"/>
				<xsl:choose>
					<xsl:when test="$edit">
						<xsl:copy-of select="$text"/>
					</xsl:when>
					<xsl:when test="count($textnode/*) &gt; 0">
					<!-- In some templates, text already contains HTML (eg. codelist, link for download).
						In that case copy text content and does not resolve
						hyperlinks. -->
						<xsl:copy-of select="$text"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="addLineBreaksAndHyperlinks">
							<xsl:with-param name="txt" select="$text"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>			
			</td>
		</tr>
	</xsl:template>
	<!--
	gui to show a title and do special mapping for container elements
	-->
	<xsl:template name="showTitleWithTag">
		<xsl:param name="title"/>
		<xsl:param name="class"/>
		<xsl:variable name="shortTitle" select="normalize-space($title)"/>
		<xsl:variable name="conthelp" select="concat('This is a container element name - you can give it a title and help by entering some help for ',$shortTitle,' in the help file')"/>
		<xsl:variable name="nohelp" select="concat('This is an element/attribute name - you can give it a title and help by entering some help for ',$shortTitle,' in the help file')"/>

		<xsl:choose>
			<xsl:when test="contains($title,'CHOICE_ELEMENT')">
				<a class="{$class}" title="{$conthelp}">Choice</a>
			</xsl:when>
			<xsl:when test="contains($title,'GROUP_ELEMENT')">
				<a class="{$class}" title="{$conthelp}">Group</a>
			</xsl:when>
			<xsl:when test="contains($title,'SEQUENCE_ELEMENT')">
				<a class="{$class}" title="{$conthelp}">Sequence</a>
			</xsl:when>
			<xsl:otherwise>
				<a class="{$class}" title="{$nohelp}"><xsl:value-of select="$title"/></a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--
	gui to show a complex element
	-->
	<xsl:template name="complexElementGui">
		<xsl:param name="title"/>
		<xsl:param name="text"/>
		<xsl:param name="content"/>
		<xsl:param name="helpLink"/>
		<xsl:param name="addLink"/>
		<xsl:param name="addXMLFragment"/>
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		<xsl:param name="validationLink"/>
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="id" select="generate-id(.)"/>
	
		<xsl:variable name="isXLinked" select="count(ancestor::node()[@xlink:href]) > 0" />

		<tr id="{$id}" type="metadata">
			<td class="padded-content" width="100%" colspan="2">
				<fieldset class="metadata-block">
					<legend class="block-legend">
						<xsl:if test="/root/gui/config/metadata-view-toggleTab">
							<input id="toggle{$id}" type="checkbox" class="toggle" 
								onclick="$('toggled{$id}').style.display=($(this.id).checked?'none':'block');"
							/>
							<!--
								Toggle mechanism could have been achieved without any JS but pure CSS
								input.toggle { display: block; }
								input.toggle:checked+table { display: none; }
								
								Issue is IE does not support pseudo class selection checked.
							 -->
						</xsl:if>
						<xsl:choose>
							<xsl:when test="$helpLink!=''">
								<span id="stip.{$helpLink}|{$id}" onclick="toolTip(this.id);" class="content" style="cursor:help;"><xsl:value-of select="$title"/>
								</span>
								<!-- Only show asterisks on simpleElements - user has to know
									which ones to fill out 
									<xsl:call-template name="asterisk">
									<xsl:with-param name="link" select="$helpLink"/>
									<xsl:with-param name="edit" select="$edit"/>
									</xsl:call-template>
								-->
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="showTitleWithTag">
									<xsl:with-param name="title" select="$title"/>
									<xsl:with-param name="class" select="'no-help'"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
						
						<xsl:if test="$edit and not($isXLinked)">
							<xsl:call-template name="getButtons">
								<xsl:with-param name="addLink" select="$addLink"/>
								<xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
								<xsl:with-param name="removeLink" select="$removeLink"/>
								<xsl:with-param name="upLink" select="$upLink"/>
								<xsl:with-param name="downLink" select="$downLink"/>
								<xsl:with-param name="validationLink" select="$validationLink"/>
								<xsl:with-param name="id" select="$id"/>
							</xsl:call-template>
						</xsl:if>
					</legend>
					<table width="100%" id="toggled{$id}">
						<xsl:copy-of select="$content"/>
					</table>
				</fieldset>
			</td>
		</tr>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- utility templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->


    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- utility templates -->
    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:template name="getXPath">
        <xsl:for-each select="ancestor-or-self::*">
            <xsl:if test="not(position() = 1)">
                <xsl:value-of select="name()" />
            </xsl:if>
            <xsl:if test="not(position() = 1) and not(position() = last())">
                <xsl:text>/</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <!-- Check if is an attribute: http://www.dpawson.co.uk/xsl/sect2/nodetest.html#d7610e91 -->
        <xsl:if test="count(. | ../@*) = count(../@*)">/@<xsl:value-of select="name()" /></xsl:if>
    </xsl:template>


    <xsl:template name="getTitleColor">
        <xsl:param name="name"/>
        <xsl:param name="schema"/>

        <xsl:variable name="fullContext">
            <xsl:call-template name="getXPath" />
        </xsl:variable>

        <xsl:variable name="context" select="name(parent::node())"/>
        <xsl:variable name="contextIsoType" select="parent::node()/@gco:isoType"/>

        <xsl:variable name="color">
            <xsl:choose>
                <xsl:when test="starts-with($schema,'iso19139')">

                    <!-- Name with context in current schema -->
                    <xsl:variable name="colorTitleWithContext"
                                  select="string(/root/gui/*[name(.)=$schema]
                                  /element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]
                                  /label_color)"/>

                    <!-- Name with context in base schema -->
                    <xsl:variable name="colorTitleWithContextIso"
                        select="string(/root/gui/iso19139/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]
                        /label_color)"/>

                    <!-- Name in current schema -->
                    <xsl:variable name="colorTitle" select="string(/root/gui/*[name(.)=$schema]/element[@name=$name and not(@context)]/label_color)"/>

                    <xsl:choose>

                        <xsl:when test="normalize-space($colorTitle)='' and
                                        normalize-space($colorTitleWithContext)='' and
                                        normalize-space($colorTitleWithContextIso)=''">
                            <xsl:value-of select="string(/root/gui/iso19139/element[@name=$name]/label_color)"/>
                        </xsl:when>
                        <xsl:when test="normalize-space($colorTitleWithContext)='' and
                                        normalize-space($colorTitleWithContextIso)=''">
                                <xsl:value-of select="$colorTitle"/>
                        </xsl:when>
                        <xsl:otherwise>
                                <xsl:value-of select="$colorTitleWithContext"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <!-- otherwise just get the title out of the approriate schema help file -->

                <xsl:otherwise>
                    <xsl:value-of select="string(/root/gui/*[name(.)=$schema]/element[@name=$name]/label_color)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:value-of select="$color"/>
    </xsl:template>


    <!--
        Returns the title of an element. If the schema is an ISO profil then search:
        * the ISO profil help first
        * with context (ie. context is the class where the element is defined)
        * with no context
        and if not found search the iso19139 main help.

        If not iso based, search in corresponding schema.

        If not found return the element name between "[]".
    -->
    <xsl:template name="getTitle">
        <xsl:param name="name"/>
        <xsl:param name="schema"/>

        <xsl:variable name="fullContext">
            <xsl:call-template name="getXPath" />
        </xsl:variable>

        <xsl:variable name="context" select="name(parent::node())"/>
        <xsl:variable name="contextIsoType" select="parent::node()/@gco:isoType"/>

        <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="starts-with($schema,'iso19139')">

                    <!-- Name with context in current schema -->
                    <xsl:variable name="schematitleWithContext"
                                  select="string(/root/gui/*[name(.)=$schema]
                                  /element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]
                                  /label)"/>

                    <!-- Name with context in base schema -->
                    <xsl:variable name="schematitleWithContextIso"
                        select="string(/root/gui/iso19139/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]
                        /label)"/>

                    <!-- Name in current schema -->
                    <xsl:variable name="schematitle" select="string(/root/gui/*[name(.)=$schema]/element[@name=$name and not(@context)]/label)"/>

                    <xsl:choose>

                        <xsl:when test="normalize-space($schematitle)='' and
                                        normalize-space($schematitleWithContext)='' and
                                        normalize-space($schematitleWithContextIso)=''">
                            <xsl:value-of select="string(/root/gui/iso19139/element[@name=$name]/label)"/>
                        </xsl:when>
                        <xsl:when test="normalize-space($schematitleWithContext)='' and
                                        normalize-space($schematitleWithContextIso)=''">
                                <xsl:value-of select="$schematitle"/>
                        </xsl:when>
                        <xsl:otherwise>
                                <xsl:value-of select="$schematitleWithContext"/>
                        </xsl:otherwise>

                    </xsl:choose>
                </xsl:when>

                <!-- otherwise just get the title out of the approriate schema help file -->

                <xsl:otherwise>
                    <xsl:value-of select="string(/root/gui/*[name(.)=$schema]/element[@name=$name]/label)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:choose>
            <xsl:when test="normalize-space($title)!=''">
                <xsl:value-of select="$title"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

	<!--
	returns the text of an element
	-->
	<xsl:template name="getElementText">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="rows" select="1"/>
		<xsl:param name="cols" select="40"/>
		<xsl:param name="langId"/>
		<xsl:param name="visible" select="true"/>
		<!-- Add javascript validator function. By default, if element 
		is mandatory a non empty validator is defined. -->
		<xsl:param name="validator"/>
		<!-- Use input_type parameter to create an hidden field. 
		Default is a text input. -->
		<xsl:param name="input_type">text</xsl:param>
		<!-- Set to true no_name parameter in order to create an element 
		which will not be submitted to the form. -->
		<xsl:param name="no_name" select="false()" />

		
		<xsl:variable name="name"  select="name(.)"/>
		<xsl:variable name="value" select="string(.)"/>
		<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />		
							
		<xsl:choose>
			<!-- list of values -->
			<xsl:when test="geonet:element/geonet:text">

				<xsl:variable name="mandatory" select="geonet:element/@min='1' and
								geonet:element/@max='1'"/>
							
				<!-- This code is mainly run under FGDC 
				but also for enumeration like topic category and 
				service parameter direction in ISO. 
				
				Create a temporary list and retrive label in 
				current gui language which is sorted after. -->				
				<xsl:variable name="list">
					<items>
						<xsl:for-each select="geonet:element/geonet:text">
							<xsl:variable name="choiceValue" select="string(@value)"/>							
							<xsl:variable name="label" select="/root/gui/*[name(.)=$schema]/codelist[@name = $name]/entry[code = $choiceValue]/label"/>
							
							<item>
								<value>
									<xsl:value-of select="@value"/>
								</value>
								<label>
									<xsl:choose>
										<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
										<xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
									</xsl:choose>									
								</label>
							</item>
						</xsl:for-each>
					</items>
				</xsl:variable>
				<select class="md" name="_{geonet:element/@ref}" size="1">
					<xsl:if test="$visible = 'false'">
						<xsl:attribute name="style">display:none;</xsl:attribute>
					</xsl:if>
					<xsl:if test="$isXLinked">
						<xsl:attribute name="disabled">disabled</xsl:attribute>
					</xsl:if>
					<xsl:if test="$mandatory and $edit">
						<xsl:attribute name="onchange">
							validateNonEmpty(this);
						</xsl:attribute>
					</xsl:if>
					<option name=""/>
					<xsl:for-each select="exslt:node-set($list)//item">
						<xsl:sort select="label"/>
						<option>
							<xsl:if test="value=$value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:attribute name="value"><xsl:value-of select="value"/></xsl:attribute>
							<xsl:value-of select="label"/>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:when test="$edit=true() and $rows=1">
				<xsl:choose>
					<xsl:when test="($schema='dublin-core' and $name='dc:subject') or
									($schema='fgdc' and $name='themekey') or
									($schema='iso19115' and $name='keyword') or
									(starts-with($schema,'iso19139') and (name(..)='gmd:keyword' 
										or ../@gco:isoType='gmd:keyword'))">
						<input class="md" type="text" id="_{geonet:element/@ref}" name="_{geonet:element/@ref}" value="{text()}" size="{$cols}">
							<xsl:if test="$visible = 'false'">
								<xsl:attribute name="style">display:none;</xsl:attribute>
							</xsl:if>
							<xsl:if test="$isXLinked">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
							<xsl:if test="(
								(name(.)='gmd:LocalisedCharacterString' and ../../geonet:element/@min='1')
								or ../geonet:element/@min='1'
								) and $edit">
								<xsl:attribute name="onkeyup">validateNonEmpty(this);</xsl:attribute>
							</xsl:if>
						</input>

						<!-- Removed autocompletion div when editing metadata -->  
						<!--
						<div id='keywordList' class="keywordList" ></div>
						
						<script type="text/javascript">
						  <xsl:text>var _</xsl:text>
						  <xsl:value-of select="geonet:element/@ref"/>
						  <xsl:text>_acurl = "xml.search.keywords?pNewSearch=true&amp;pTypeSearch=1";</xsl:text>
						  
						  <xsl:text>var _</xsl:text>
						  <xsl:value-of select="geonet:element/@ref"/>
						  <xsl:text>_ac = new Ajax.Autocompleter('_</xsl:text>
						  <xsl:value-of select="geonet:element/@ref"/>
						  <xsl:text>', 'keywordList', 'xml.search.keywords?pNewSearch=true&amp;pTypeSearch=1&amp;pMode=search',{method:'get', paramName: 'pKeyword'});</xsl:text>

						</script>
						-->

					</xsl:when>
					
					<!-- heikki doeleman: for gco:Boolean, use checkbox.
					Default value set to false. -->
					<xsl:when test="name(.)='gco:Boolean'">
					    <input type="hidden" name="_{geonet:element/@ref}" id="_{geonet:element/@ref}" value="{.}">
							<xsl:if test="$isXLinked">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
					        <xsl:choose>
					            <xsl:when test=". = ''">
					                <xsl:attribute name="value">false</xsl:attribute>
					            </xsl:when>
					            <xsl:otherwise>
					                <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
					            </xsl:otherwise>
					        </xsl:choose>
					    </input>
					    
						<xsl:choose>
						    <xsl:when test="text()='true' or text()='1'">
								<input class="md" type="checkbox" id="_{geonet:element/@ref}_checkbox" onclick="handleCheckboxAsBoolean(this, '_{geonet:element/@ref}');" checked="checked">
									<xsl:if test="$isXLinked">
										<xsl:attribute name="disabled">disabled</xsl:attribute>
									</xsl:if>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input class="md" type="checkbox" id="_{geonet:element/@ref}_checkbox" onclick="handleCheckboxAsBoolean(this, '_{geonet:element/@ref}');">
									<xsl:if test="$isXLinked">
										<xsl:attribute name="disabled">disabled</xsl:attribute>
									</xsl:if>								
								</input>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>

					<xsl:otherwise>
						<input class="md" type="{$input_type}" value="{text()}" size="{$cols}">
							<xsl:if test="$isXLinked">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>						
							<xsl:choose>
								<xsl:when test="$no_name=false()">
									<xsl:attribute name="name">_<xsl:value-of select="geonet:element/@ref"/></xsl:attribute>
									<xsl:attribute name="id">_<xsl:value-of select="geonet:element/@ref"/></xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="id"><xsl:value-of select="geonet:element/@ref"/></xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>

							<xsl:if test="$visible = 'false'">
								<xsl:attribute name="style">display:none;</xsl:attribute>
							</xsl:if>
							
							<xsl:variable name="mandatory" select="(name(.)='gmd:LocalisedCharacterString' 
									and ../../geonet:element/@min='1')
									or ../geonet:element/@min='1'"/>
							
							<xsl:choose>
								<!-- Numeric field -->
								<xsl:when test="name(.)='gco:Integer' or 
									name(.)='gco:Decimal' or name(.)='gco:Real'">
									<xsl:choose>
										<xsl:when test="name(.)='gco:Integer'">
											<xsl:attribute name="onkeyup">validateNumber(this, <xsl:value-of select="not($mandatory)"/>, false);</xsl:attribute>
										</xsl:when>
										<xsl:otherwise>
											<xsl:attribute name="onkeyup">validateNumber(this, <xsl:value-of select="not($mandatory)"/>, true);</xsl:attribute>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<!-- Mandatory field (with extra validator) -->
								<xsl:when test="$mandatory
									and $edit">
									<xsl:attribute name="onkeyup">
										validateNonEmpty(this);
									</xsl:attribute>
								</xsl:when>
								<!-- Custom validator -->
								<xsl:when test="$validator">
									<xsl:attribute name="onkeyup"><xsl:value-of select="$validator"/></xsl:attribute>
								</xsl:when>
							</xsl:choose>
						</input>
						<xsl:call-template name="helper">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="attribute" select="false()"/>
						</xsl:call-template>
						
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$edit=true()">
				<textarea class="md" name="_{geonet:element/@ref}" id="_{geonet:element/@ref}" rows="{$rows}" cols="{$cols}">
					<xsl:if test="$isXLinked">
						<xsl:attribute name="disabled">disabled</xsl:attribute>
					</xsl:if>
					<xsl:if test="$visible = 'false'">
						<xsl:attribute name="style">display:none;</xsl:attribute>
					</xsl:if>
					<xsl:if test="(
						(name(.)='gmd:LocalisedCharacterString' and ../../geonet:element/@min='1')
						or ../geonet:element/@min='1'
						) and $edit">
						<xsl:attribute name="onkeyup">validateNonEmpty(this);</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="text()"/>
				</textarea>
			</xsl:when>
			<xsl:when test="$edit=false() and $rows!=1">
				<xsl:choose>
					<xsl:when test="starts-with($schema,'iso19139')">
						<xsl:apply-templates mode="localised" select="..">
							<xsl:with-param name="langId" select="$langId"></xsl:with-param>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="$value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- not editable text/codelists -->
				<xsl:variable name="label" select="/root/gui/*[name(.)=$schema]/codelist[@name = $name]/entry[code=$value]/label"/>
				<xsl:choose>
					<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
					<xsl:when test="starts-with($schema,'iso19139') 
							and name(.)!='gco:ScopedName' and name(.)!='gco:Date' and name(.)!='gco:DateTime'">
						<xsl:apply-templates mode="localised" select="..">
							<xsl:with-param name="langId" select="$langId"></xsl:with-param>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	returns the text of an attribute
	-->
	<xsl:template name="getAttributeText">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="rows" select="1"/>
		<xsl:param name="cols" select="40"/>
		
		<xsl:variable name="name"  select="name(.)"/>
		<xsl:variable name="value" select="string(.)"/>
		<xsl:variable name="parent"  select="name(..)"/>
		<!-- the following variable is used in place of name as a work-around to
         deal with qualified attribute names like gml:id
		     which if not modified will cause JDOM errors on update because of the
				 way in which changes to ref'd elements are parsed as XML -->
		<xsl:variable name="updatename">
		  <xsl:choose>
	        <xsl:when test="contains($name,':')">
	          <xsl:value-of select="concat(substring-before($name,':'),'COLON',substring-after($name,':'))"/>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:value-of select="$name"/>
	        </xsl:otherwise>
	      </xsl:choose>
	    </xsl:variable>
    	<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
		
		<xsl:choose>
			<!-- list of values -->
			<xsl:when test="../geonet:attribute[string(@name)=$name]/geonet:text">
				<select class="md" name="_{../geonet:element/@ref}_{name(.)}" size="1">
					<xsl:if test="$isXLinked">
						<xsl:attribute name="disabled">disabled</xsl:attribute>
					</xsl:if>
					<option name=""/>
					<xsl:for-each select="../geonet:attribute/geonet:text">
						<option>
							<xsl:if test="@value=$value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:variable name="choiceValue" select="string(@value)"/>
							<xsl:attribute name="value"><xsl:value-of select="$choiceValue"/></xsl:attribute>

							<!-- codelist in edit mode -->
							<xsl:variable name="label" select="/root/gui/*[name(.)=$schema]/codelist[@name = $parent]/entry[code=$choiceValue]/label"/>
							<xsl:choose>
								<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
								<xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
							</xsl:choose>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:when test="$edit=true() and $rows=1">
				<input class="md" type="text" id="_{../geonet:element/@ref}_{$updatename}" name="_{../geonet:element/@ref}_{$updatename}" value="{string()}" size="{$cols}" />
				
				<xsl:call-template name="helper">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="attribute" select="true()"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$edit=true()">
				<textarea class="md" name="_{../geonet:element/@ref}_{$updatename}" id="_{../geonet:element/@ref}_{$updatename}" rows="{$rows}" cols="{$cols}">
					<xsl:value-of select="string()"/>
				</textarea>
			</xsl:when>
			<xsl:otherwise>
				<!-- codelist in view mode -->
				<xsl:variable name="label" select="/root/gui/*[name(.)=$schema]/codelist[@name = $parent]/entry[code = $value]/label"/>
				<xsl:choose>
					<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	returns the content of a complex element
	-->
	<xsl:template name="getContent">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="elementEP" select="@*">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				<xsl:apply-templates mode="elementEP" select="*[namespace-uri(.)!=$geonetUri]|geonet:child">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="elementEP" select="@*">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="false()"/>
				</xsl:apply-templates>
				<xsl:apply-templates mode="elementEP" select="*">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="false()"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ================================================================================ -->
	<!-- returns the help url -->
	<!-- ================================================================================ -->
	
	<xsl:template name="getHelpLink">
		<xsl:param name="name"/>
		<xsl:param name="schema"/>

		<xsl:choose>
			<xsl:when test="contains($name,'_ELEMENT')">
				<xsl:value-of select="''"/>
			</xsl:when>
			<xsl:otherwise>

                <xsl:variable name="fullContext">
                     <xsl:call-template name="getXPath" />
                 </xsl:variable>

                 <xsl:value-of select="concat($schema,'|', $name ,'|', name(parent::node()) ,'|', $fullContext ,'|', ../@gco:isoType)"/>                
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ================================================================================ -->
	
	<xsl:template name="asterisk">
		<xsl:param name="link"/>
		<xsl:param name="edit"/>

		<!-- <xsl:if test="$link='' and not(contains(name(.),'geonet:')) and $edit"> -->
		<xsl:if test="geonet:element/@min='1' and $edit">
			<sup><font size="-1" color="#FF0000">&#xA0;*</font></sup>
		</xsl:if>
	</xsl:template>

	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- XML formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!--
	draws an element as xml document
	-->
	<xsl:template mode="xmlDocument" match="*">
		<xsl:param name="edit" select="false()"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<tr><td>
					<textarea class="md" name="data" rows="30" cols="100">
						<xsl:text>&lt;?xml version="1.0" encoding="UTF-8"?&gt;</xsl:text>
						<xsl:text>&#10;</xsl:text>
						<xsl:apply-templates mode="editXMLElement" select="."/>
					</textarea>
				</td></tr>
			</xsl:when>
			<xsl:otherwise>
				<tr><td>
					<b><xsl:text>&lt;?xml version="1.0" encoding="UTF-8"?&gt;</xsl:text></b><br/>
					<xsl:apply-templates mode="showXMLElement" select="."/>
				</td></tr>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!--
	draws an editable element in xml
	-->
	<xsl:template mode="editXMLElement" match="*">
		<xsl:param name="indent"/>
		<xsl:choose>

			<!-- has children -->
			<xsl:when test="*[not(starts-with(name(),'geonet:'))]">
				<xsl:if test="not(contains(name(.),'_ELEMENT'))">
					<xsl:call-template name="editXMLStartTag">
						<xsl:with-param name="indent" select="$indent"/>
					</xsl:call-template>
					<xsl:text>&#10;</xsl:text>
				</xsl:if>
				<xsl:for-each select="*">
					<xsl:apply-templates select="." mode="editXMLElement">
						<xsl:with-param name="indent" select="concat($indent, '&#09;')"/>
					</xsl:apply-templates>
				</xsl:for-each>
				<xsl:if test="not(contains(name(.),'_ELEMENT'))">
					<xsl:call-template name="editEndTag">
						<xsl:with-param name="indent" select="$indent"/>
					</xsl:call-template>
					<xsl:text>&#10;</xsl:text>
				</xsl:if>
			</xsl:when>
			
			<!-- no children but text -->
			<xsl:when test="text()">
				<xsl:if test="not(contains(name(.),'_ELEMENT'))">
					<xsl:call-template name="editXMLStartTag">
						<xsl:with-param name="indent" select="$indent"/>
					</xsl:call-template>
				
					<!-- xml entities should be doubly escaped -->
					<xsl:apply-templates mode="escapeXMLEntities" select="text()"/>
				
					<xsl:call-template name="editEndTag"/>
					<xsl:text>&#10;</xsl:text>
				</xsl:if>
			</xsl:when>
			
			<!-- empty element -->
			<xsl:otherwise>
				<xsl:if test="not(contains(name(.),'_ELEMENT'))">
					<xsl:call-template name="editXMLStartEndTag">
						<xsl:with-param name="indent" select="$indent"/>
					</xsl:call-template>
					<xsl:text>&#10;</xsl:text>
				</xsl:if>
			</xsl:otherwise>
			
		</xsl:choose>
	</xsl:template>
	<!--
	draws the start tag of an editable element
	-->
	<xsl:template name="editXMLStartTag">
		<xsl:param name="indent"/>
		
		<xsl:value-of select="$indent"/>
		<xsl:text>&lt;</xsl:text>
		<xsl:value-of select="name(.)"/>
		<xsl:call-template name="editXMLNamespaces"/>
		<xsl:call-template name="editXMLAttributes"/>
		<xsl:text>&gt;</xsl:text>
	</xsl:template>

	<!--
	draws the end tag of an editable element
	-->
	<xsl:template name="editEndTag">
		<xsl:param name="indent"/>
		
		<xsl:value-of select="$indent"/>
		<xsl:text>&lt;/</xsl:text>
		<xsl:value-of select="name(.)"/>
		<xsl:text>&gt;</xsl:text>
	</xsl:template>
	
	<!--
	draws the empty tag of an editable element
	-->
	<xsl:template name="editXMLStartEndTag">
		<xsl:param name="indent"/>
		
		<xsl:value-of select="$indent"/>
		<xsl:text>&lt;</xsl:text>
		<xsl:value-of select="name(.)"/>
		<xsl:call-template name="editXMLNamespaces"/>
		<xsl:call-template name="editXMLAttributes"/>
		<xsl:text>/&gt;</xsl:text>
	</xsl:template>
	
	<!--
	draws attribute of an editable element
	-->
	<xsl:template name="editXMLAttributes">
		<xsl:for-each select="@*">
			<xsl:if test="not(starts-with(name(.),'geonet:'))">
				<xsl:text> </xsl:text>
				<xsl:value-of select="name(.)"/>
				<xsl:text>=</xsl:text>
					<xsl:text>"</xsl:text>
					<xsl:value-of select="string()"/>
					<xsl:text>"</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<!--
	draws namespaces of an editable element
	-->
	<xsl:template name="editXMLNamespaces">
		<xsl:variable name="parent" select=".."/>
		<xsl:for-each select="namespace::*">
			<xsl:if test="not(.=$parent/namespace::*) and name()!='geonet'">
				<xsl:text> xmlns</xsl:text>
				<xsl:if test="name()">
					<xsl:text>:</xsl:text>
					<xsl:value-of select="name()"/>
				</xsl:if>
				<xsl:text>=</xsl:text>
				<xsl:text>"</xsl:text>
				<xsl:value-of select="string()"/>
				<xsl:text>"</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<!--
	draws an element in xml
	-->
	<xsl:template mode="showXMLElement" match="*">
		<xsl:choose>
			
			<!-- has children -->
			<xsl:when test="*">
				<xsl:call-template name="showXMLStartTag"/>
				<dl>
					<xsl:for-each select="*">
						<dd>
							<xsl:apply-templates select="." mode="showXMLElement"/>
						</dd>
					</xsl:for-each>
				</dl>
				<xsl:call-template name="showEndTag"/>
			</xsl:when>
			
			<!-- no children but text -->
			<xsl:when test="text()">
				<xsl:call-template name="showXMLStartTag"/>
				<xsl:value-of select="text()"/>
				<xsl:call-template name="showEndTag"/>
			</xsl:when>
			
			<!-- empty element -->
			<xsl:otherwise>
				<xsl:call-template name="showXMLStartEndTag"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
	draws the start tag of an element
	-->
	<xsl:template name="showXMLStartTag">
			<font color="4444ff">
			<xsl:text>&lt;</xsl:text>
			<b>
				<xsl:value-of select="name(.)"/>
			</b>
			<xsl:call-template name="showXMLNamespaces"/>
			<xsl:call-template name="showXMLAttributes"/>
			<xsl:text>&gt;</xsl:text>
		</font>
	</xsl:template>

	<!--
	draws the end tag of an element
	-->
	<xsl:template name="showEndTag">
		<font color="4444ff">
			<xsl:text>&lt;/</xsl:text>
			<b>
				<xsl:value-of select="name(.)"/>
			</b>
			<xsl:text>&gt;</xsl:text>
		</font>
	</xsl:template>
	
	<!--
	draws the empty tag of an element
	-->
	<xsl:template name="showXMLStartEndTag">
		<font color="4444ff">
			<xsl:text>&lt;</xsl:text>
			<b>
				<xsl:value-of select="name(.)"/>
			</b>
			<xsl:call-template name="showXMLNamespaces"/>
			<xsl:call-template name="showXMLAttributes"/>
			<xsl:text>/&gt;</xsl:text>
		</font>
	</xsl:template>
	
	<!--
	draws attributes of an element
	-->
	<xsl:template name="showXMLAttributes">
		<xsl:for-each select="@*">
			<xsl:if test="not(starts-with(name(.),'geonet:'))">
				<xsl:text> </xsl:text>
				<xsl:value-of select="name(.)"/>
				<xsl:text>=</xsl:text>
				<font color="ff4444">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="string()"/>
					<xsl:text>"</xsl:text>
				</font>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<!--
	draws namespaces of an element
	-->
	<xsl:template name="showXMLNamespaces">
		<xsl:variable name="parent" select=".."/>
		<xsl:for-each select="namespace::*">
			<xsl:if test="not(.=$parent/namespace::*) and name()!='geonet'">
				<xsl:text> xmlns</xsl:text>
				<xsl:if test="name()">
					<xsl:text>:</xsl:text>
					<xsl:value-of select="name()"/>
				</xsl:if>
				<xsl:text>=</xsl:text>
				<font color="888844">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="string()"/>
					<xsl:text>"</xsl:text>
				</font>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>



	<!-- Create an helper list for the current input element.
		Current input could be an element or an attribute (eg. uom). 
	
	In editing mode, for gco:CharacterString elements (with no codelist 
	or enumeration defined in the schema) an helper list could be defined 
	in loc files using the helper tag. Then a list of values
	is displayed next to the input field. 
	-->
	<xsl:template name="helper">
		<xsl:param name="schema"/>
		<xsl:param name="attribute"/>
		
		<!-- Define the element to look for. -->
		<xsl:variable name="parentName">
			<xsl:choose>
				<!-- In dublin core element contains value.
					In ISO, attribute also but element contains characterString which contains the value -->
				<xsl:when test="$attribute=true() or $schema = 'dublin-core'">
					<xsl:value-of select="name(.)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="name(parent::node())"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!-- Look for the helper -->
		<xsl:variable name="helper">
			<xsl:choose>
				<xsl:when test="starts-with($schema,'iso19139') and not(/root/gui/*[name(.)=$schema]/element[@name = $parentName]/helper)">
					<!-- Fallback to iso19139 helper for ISO profil if not exist ... -->
					<xsl:copy-of select="/root/gui/iso19139/element[@name = $parentName]/helper/*"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="/root/gui/*[name(.)=$schema]/element[@name = $parentName]/helper/*"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		
		<!-- Display the helper list -->
		<xsl:if test="normalize-space($helper)!=''">
			<xsl:variable name="refId">
				<xsl:choose>
					<xsl:when test="$attribute=true()">
						<xsl:value-of select="concat(../geonet:element/@ref, '_', name(.))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="geonet:element/@ref"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<xsl:text> </xsl:text>				
			(<xsl:value-of select="/root/gui/strings/helperList"/>
			<select onchange="$('_{$refId}').value=this.options[this.selectedIndex].value; if ($('_{$refId}').onkeyup) $('_{$refId}').onkeyup();" class="md">
				<option/>
				<!-- This assume that helper list is already sort in alphabetical order in loc file. -->
				<xsl:copy-of select="exslt:node-set($helper)"/>
			</select>)
		</xsl:if>
	</xsl:template>
	

	<!--
	prevent drawing of geonet:* elements
	-->
	<xsl:template mode="showXMLElement" match="geonet:*"/>
	<xsl:template mode="editXMLElement" match="geonet:*"/>
	
</xsl:stylesheet>
