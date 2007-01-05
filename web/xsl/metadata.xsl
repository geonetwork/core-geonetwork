<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan= "http://xml.apache.org/xalan" exclude-result-prefixes="xalan"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:include href="metadata-utils.xsl"/>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- main schema switch -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template mode="elementEP" match="*|@*">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>
		<xsl:param name="edit" select="false()"/>
		
		<xsl:choose>
		
			<!-- ISO 19115 -->
			<xsl:when test="$schema='iso19115'">
				<xsl:apply-templates mode="iso19115" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- ISO 19139 -->
			<xsl:when test="$schema='iso19139'">
				<xsl:apply-templates mode="iso19139" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- FGDC -->
			<xsl:when test="$schema='fgdc-std'">
				<xsl:apply-templates mode="fgdc-std" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- Dublin Core -->
			<xsl:when test="$schema='dublin-core'">
				<xsl:apply-templates mode="dublin-core" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- default, no schema-specific formatting -->
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
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
		
		<!-- draw new children if
		- it is not simple mode and
			- it is an OR element or
			- it does not exists a preceding brother
		-->
		<xsl:variable name="name">
			<xsl:choose>
				<xsl:when test="@prefix=''"><xsl:value-of select="@name"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="concat(@prefix,':',@name)"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="prevBrother" select="preceding-sibling::*[1]"/>
		<xsl:if test="$currTab!='simple' and (geonet:choose or name($prevBrother)!=$name)">
			<xsl:variable name="text">
				<xsl:if test="geonet:choose">
					<select class="md" name="_{../geonet:element/@ref}_{@name}" size="1">
						<xsl:for-each select="geonet:choose">
							<option value="{@name}">
								<xsl:call-template name="getTitle">
									<xsl:with-param name="name"   select="@name"/>
									<xsl:with-param name="schema" select="$schema"/>
								</xsl:call-template>
							</option>
						</xsl:for-each>
					</select>
				</xsl:if>
			</xsl:variable>
			<xsl:variable name="addLink">
				<xsl:choose>
					<xsl:when test="geonet:choose">
						<xsl:choose>
							<xsl:when test="@prefix=''">
								<xsl:value-of select="concat('javascript:doNewORElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,@name,$apos,',document.mainForm._',../geonet:element/@ref,'_',@name,'.value);')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat('javascript:doNewORElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,@name,$apos,',document.mainForm._',../geonet:element/@ref,'_',@prefix,':',@name,'.value);')"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="@prefix=''">
								<xsl:value-of select="concat('javascript:doNewElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,@name,$apos,');')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat('javascript:doNewElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,@prefix,':',@name,$apos,');')"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
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
				<xsl:with-param name="helpLink" select="$helpLink"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- callbacks from schema templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template mode="element" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"   select="false()"/>
		<xsl:param name="flat"   select="false()"/>
		
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
	<xsl:template mode="element" match="geonet:element|geonet:info"/>
	<xsl:template mode="simpleElement" match="geonet:element|geonet:info"/>
	<xsl:template mode="complexElement" match="geonet:element|geonet:info"/>
	
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
		
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="$text"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	shows a complex element
	-->
	<xsl:template name="showComplexElement">
		<xsl:param name="schema"/>
		<xsl:param name="title"/>
		<xsl:param name="content"/>
		<xsl:param name="helpLink"/>
		
		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="text()"/>
			<xsl:with-param name="content" select="$content"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
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
		<xsl:variable name="addLink">
			<xsl:call-template name="addLink"/>
		</xsl:variable>
		<xsl:variable name="removeLink">
			<xsl:if test="geonet:element/@del='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.delete',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="upLink">
			<xsl:if test="geonet:element/@up='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.up',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="downLink">
			<xsl:if test="geonet:element/@down='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.down',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="$text"/>
			<xsl:with-param name="addLink" select="$addLink"/>
			<xsl:with-param name="removeLink" select="$removeLink"/>
			<xsl:with-param name="upLink" select="$upLink"/>
			<xsl:with-param name="downLink" select="$downLink"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="addLink">
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
		<xsl:variable name="newBrother" select="xalan:nodeset($nb)"/>
		
		<xsl:if test="$newBrother/* and not($newBrother/*/geonet:choose)">
			<xsl:choose>
				<xsl:when test="$nextBrother/@prefix=''">
					<xsl:value-of select="concat('javascript:doNewElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,$nextBrother/@name,$apos,');')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat('javascript:doNewElementAction(',$apos,/root/gui/locService,'/metadata.elem.add',$apos,',',../geonet:element/@ref,',',$apos,$nextBrother/@prefix,':',$nextBrother/@name,$apos,');')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
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
		
		<!-- if it's the last brother of it's type and there is a new brother make addLink -->
		<xsl:variable name="addLink">
			<xsl:call-template name="addLink"/>
		</xsl:variable>
		<xsl:variable name="removeLink">
			<xsl:if test="geonet:element/@del='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.delete',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="upLink">
			<xsl:if test="geonet:element/@up='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.up',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="downLink">
			<xsl:if test="geonet:element/@down='true'">
				<xsl:value-of select="concat('javascript:doElementAction(',$apos,/root/gui/locService,'/metadata.elem.down',$apos,',',geonet:element/@ref,');')"/>
			</xsl:if>
		</xsl:variable>
		
		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="$title"/>
			<xsl:with-param name="text" select="text()"/>
			<xsl:with-param name="content" select="$content"/>
			<xsl:with-param name="addLink" select="$addLink"/>
			<xsl:with-param name="removeLink" select="$removeLink"/>
			<xsl:with-param name="upLink" select="$upLink"/>
			<xsl:with-param name="downLink" select="$downLink"/>
			<xsl:with-param name="helpLink" select="$helpLink"/>
			<xsl:with-param name="schema" select="$schema"/>
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
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		<xsl:param name="schema"/>
		<!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
		<xsl:param name="anchor">
			<xsl:choose>
				
				<!-- current node is an element -->
				<xsl:when test="geonet:element/@ref">_<xsl:value-of select="geonet:element/@ref"/></xsl:when>
				
				<!-- current node is an attribute or a new child: create anchor to parent -->
				<xsl:when test="../geonet:element/@ref">_<xsl:value-of select="../geonet:element/@ref"/></xsl:when>
				
			</xsl:choose>
		</xsl:param>
		
		<tr>
			<th class="md" width="20%" valign="top">
				<a name="{$anchor}"/>
				<xsl:choose>
					<xsl:when test="$helpLink!=''">
						<a href="javascript:popNew('{$helpLink}')"><xsl:value-of select="$title"/></a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$title"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&#160;</xsl:text>
				<xsl:call-template name="getButtons">
					<xsl:with-param name="addLink" select="$addLink"/>
					<xsl:with-param name="removeLink" select="$removeLink"/>
					<xsl:with-param name="upLink" select="$upLink"/>
					<xsl:with-param name="downLink" select="$downLink"/>
				</xsl:call-template>
			</th>
			<td class="padded" valign="top"><xsl:copy-of select="$text"/></td>
		</tr>
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
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		<xsl:param name="schema"/>
		<!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
		<xsl:param name="anchor">
			<xsl:choose>
				
				<!-- current node is an element -->
				<xsl:when test="geonet:element/@ref">_<xsl:value-of select="geonet:element/@ref"/></xsl:when>
				
				<!-- current node is a new child: create anchor to parent -->
				<xsl:when test="../geonet:element/@ref">_<xsl:value-of select="../geonet:element/@ref"/></xsl:when>
				
			</xsl:choose>
		</xsl:param>
		
		<tr>
			<td class="padded-content" width="100%" colspan="2">
				<table width="100%">
					<tr>
						<td class="content" valign="bottom" colspan="3">
							<table width="100%"><tr>
								<td class="green-content" width="20%">
									<xsl:text>::</xsl:text>
									<a name="{$anchor}"/>
									<xsl:choose>
										<xsl:when test="$helpLink!=''">
											<a class="green-content" href="javascript:popNew('{$helpLink}')"><xsl:value-of select="$title"/></a>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$title"/>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:call-template name="getButtons">
										<xsl:with-param name="addLink" select="$addLink"/>
										<xsl:with-param name="removeLink" select="$removeLink"/>
										<xsl:with-param name="upLink" select="$upLink"/>
										<xsl:with-param name="downLink" select="$downLink"/>
									</xsl:call-template>
								</td>
								<td class="content" width="100%" valign="bottom">
									<table width="100%"><tr><td class="dots"/></tr></table>
								</td>
							</tr></table>
						</td>
					</tr>
					<tr>
						<td class="dots">&#xA0;</td>
						<td class="padded-content" align="center">
							<table width="100%">
								<xsl:copy-of select="$content"/>
							</table>
						</td>
						<td class="dots">&#xA0;</td>
					</tr>
					<tr><td class="dots" colspan="3"/></tr>
				</table>
			</td>
		</tr>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- utility templates -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!--
	returns the title of an element
	-->
	<xsl:template name="getTitle">
		<xsl:param name="name"/>
		<xsl:param name="schema"/>
		
		<xsl:variable name="title" select="string(/root/gui/*[name(.)=$schema]/*[name(.)=$name])"/>
		<xsl:choose>
			<xsl:when test="$title">
				<xsl:value-of select="$title"/>
			</xsl:when>
			<xsl:otherwise>
				[<xsl:value-of select="$name"/>]
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
		<xsl:param name="cols" select="50"/>
		
		<xsl:variable name="name"  select="name(.)"/>
		<xsl:variable name="value" select="string(.)"/>
		<xsl:variable name="label" select="string(/root/gui/*[name(.)=$schema]/codelist[string(@name)=$name]/*[@value=$value])"/>
							
		<xsl:choose>
			<!-- list of values -->
			<xsl:when test="geonet:element/geonet:text">
				<select class="md" name="_{geonet:element/@ref}" size="1">
					<option name=""/>
					<xsl:for-each select="geonet:element/geonet:text">
						<option>
							<xsl:if test="@value=$value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:variable name="choiceValue" select="string(@value)"/>
							<xsl:attribute name="value"><xsl:value-of select="$choiceValue"/></xsl:attribute>

							<xsl:variable name="label" select="string(/root/gui/*[name(.)=$schema]/codelist[string(@name)=$name]/*[@value=$choiceValue])"/>
							<xsl:choose>
								<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
								<xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
							</xsl:choose>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:when test="$edit=true() and $rows=1">
				<input class="md" type="text" name="_{geonet:element/@ref}" value="{text()}" size="{$cols}" />
			</xsl:when>
			<xsl:when test="$edit=true()">
				<textarea class="md" name="_{geonet:element/@ref}" rows="{$rows}" cols="{$cols}">
					<xsl:value-of select="text()"/>
				</textarea>
			</xsl:when>
			<xsl:when test="$edit=false() and $rows!=1">
				<xsl:call-template name="preformatted">
					<xsl:with-param name="text" select="$value"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
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
		<xsl:param name="cols" select="50"/>
		
		<xsl:variable name="name"  select="name(.)"/>
		<xsl:variable name="value" select="string(.)"/>
		<xsl:variable name="parent"  select="name(..)"/>
		<xsl:choose>
			<!-- list of values -->
			<xsl:when test="../geonet:attribute[string(@name)=$name]/geonet:text">
				<select class="md" name="_{../geonet:element/@ref}_{name(.)}" size="1">
					<option name=""/>
					<xsl:for-each select="../geonet:attribute/geonet:text">
						<option>
							<xsl:if test="@value=$value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:variable name="choiceValue" select="string(@value)"/>
							<xsl:attribute name="value"><xsl:value-of select="$choiceValue"/></xsl:attribute>

							<xsl:variable name="label" select="string(/root/gui/*[name(.)=$schema]/codelist[string(@name)=$parent]/*[name()=$name and @value=$choiceValue])"/>
							<xsl:choose>
								<xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
								<xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
							</xsl:choose>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:when test="$edit=true() and $rows=1">
				<input class="md" type="text" name="_{../geonet:element/@ref}_{name(.)}" value="{string()}" size="{$cols}" />
			</xsl:when>
			<xsl:when test="$edit=true()">
				<textarea class="md" name="_{../geonet:element/@ref}_{name(.)}" rows="{$rows}" cols="{$cols}">
					<xsl:value-of select="string()"/>
				</textarea>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="label" select="string(/root/gui/*[name(.)=$schema]/codelist[string(@name)=$parent]/*[name()=$name and @value=$value])"/>
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
	
	<!--
	returns the help url
	-->
	<xsl:template name="getHelpLink">
		<xsl:param name="name"/>
		<xsl:param name="schema"/>
		
		<xsl:variable name="help" select="string(/root/gui/*[name(.)=$schema]/*[name(.)=$name]/@help)"/>
		
		<xsl:if test="$help">
			<xsl:value-of select="concat(/root/gui/locService,'/help.metadata?schema=',$schema,'&amp;name=',$name,'#',$name)"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="getButtons">
		<xsl:param name="addLink"/>
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		
		<!-- add button -->
		<xsl:if test="normalize-space($addLink)">
			<xsl:text> </xsl:text>
			<a href="{$addLink}"><img src="{/root/gui/url}/images/plus.gif" alt="{/root/gui/strings/add}"/></a>
		</xsl:if>
		<!-- remove button -->
		<xsl:if test="normalize-space($removeLink)">
			<xsl:text> </xsl:text>
			<a href="{$removeLink}"><img src="{/root/gui/url}/images/del.gif" alt="{/root/gui/strings/del}"/></a>
		</xsl:if>
		<!-- up button -->
		<xsl:if test="normalize-space($upLink)">
			<xsl:text> </xsl:text>
			<a href="{$upLink}"><img src="{/root/gui/url}/images/up.gif" alt="{/root/gui/strings/up}"/></a>
		</xsl:if>
		<!-- down button -->
		<xsl:if test="normalize-space($downLink)">
			<xsl:text> </xsl:text>
			<a href="{$downLink}"><img src="{/root/gui/url}/images/down.gif" alt="{/root/gui/strings/down}"/></a>
		</xsl:if>
	</xsl:template>

	<!--
	translates CR-LF sequences into HTML newlines <p/>
	-->
	<xsl:template name="preformatted">
		<xsl:param name="text"/>
	
		<xsl:choose>
			<xsl:when test="contains($text,'&#13;&#10;')">
				<xsl:value-of select="substring-before($text,'&#13;&#10;')"/>
				<br/>
				<xsl:call-template name="preformatted">
					<xsl:with-param name="text"  select="substring-after($text,'&#13;&#10;')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
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
				<xsl:call-template name="editXMLStartTag">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<xsl:text>&#10;</xsl:text>
				<xsl:for-each select="*">
					<xsl:apply-templates select="." mode="editXMLElement">
						<xsl:with-param name="indent" select="concat($indent, '&#09;')"/>
					</xsl:apply-templates>
				</xsl:for-each>
				<xsl:call-template name="editEndTag">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<xsl:text>&#10;</xsl:text>
			</xsl:when>
			
			<!-- no children but text -->
			<xsl:when test="text()">
				<xsl:call-template name="editXMLStartTag">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				
				<!-- xml entities should be doubly escaped -->
				<xsl:apply-templates mode="escapeXMLEntities" select="text()"/>
				
				<!--
				<xsl:value-of select="text()"/>
				-->
				
				<xsl:call-template name="editEndTag"/>
				<xsl:text>&#10;</xsl:text>
			</xsl:when>
			
			<!-- empty element -->
			<xsl:otherwise>
				<xsl:call-template name="editXMLStartEndTag">
					<xsl:with-param name="indent" select="$indent"/>
				</xsl:call-template>
				<xsl:text>&#10;</xsl:text>
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
			<xsl:text> </xsl:text>
			<xsl:value-of select="name(.)"/>
			<xsl:text>=</xsl:text>
				<xsl:text>"</xsl:text>
				<xsl:value-of select="string()"/>
				<xsl:text>"</xsl:text>
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
			<xsl:text> </xsl:text>
			<xsl:value-of select="name(.)"/>
			<xsl:text>=</xsl:text>
			<font color="ff4444">
				<xsl:text>"</xsl:text>
				<xsl:value-of select="string()"/>
				<xsl:text>"</xsl:text>
			</font>
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
	
	<!--
	prevent drawing of geonet:* elements
	-->
	<xsl:template mode="showXMLElement" match="geonet:*"/>
	<xsl:template mode="editXMLElement" match="geonet:*"/>
	
</xsl:stylesheet>
