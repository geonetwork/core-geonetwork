<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xalan= "http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

	<xsl:include href="metadata-iso19115.xsl"/>
	<xsl:include href="metadata-iso19139.xsl"/>
	<xsl:include href="metadata-fgdc-std.xsl"/>
	<xsl:include href="metadata-dublin-core.xsl"/>
	
	<!--
	hack to extract geonet URI; I know, I could have used a string constant like
	<xsl:variable name="geonetUri" select="'http://www.fao.org/geonetwork'"/>
	but this is more interesting
	-->
	<xsl:variable name="geonetNodeSet"><geonet:dummy/></xsl:variable>

	<xsl:variable name="geonetUri">
		<xsl:value-of select="namespace-uri(xalan:nodeset($geonetNodeSet)/*)"/>
	</xsl:variable>

	<xsl:variable name="currTab">
		<xsl:choose>
			<xsl:when test="/root/gui/currTab"><xsl:value-of select="/root/gui/currTab"/></xsl:when>
			<xsl:otherwise>simple</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:template mode="schema" match="*">
		<xsl:choose>
			<xsl:when test="string(geonet:info/schema)!=''"><xsl:value-of select="geonet:info/schema"/></xsl:when>
			<xsl:when test="name(.)='Metadata'">iso19115</xsl:when>
			<xsl:when test="local-name(.)='MD_Metadata'">iso19139</xsl:when>
			<xsl:when test="name(.)='metadata'">fgdc-std</xsl:when>
			<xsl:otherwise>UNKNOWN</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- summary: copy it -->
	<xsl:template match="summary" mode="brief">
		<xsl:copy-of select="."/>
	</xsl:template>

	<!-- brief -->
	<xsl:template match="*" mode="brief">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>

		<!--
		[schema:<xsl:value-of select="$schema"/>]
		-->
		<xsl:choose>
			<!-- subtemplate -->
			<xsl:when test="geonet:info/isTemplate='s'">
				<metadata>
					<title><xsl:value-of select="geonet:info/title"/></title>
					<xsl:copy-of select="geonet:info"/>
				</metadata>
			</xsl:when>

			<!-- ISO 19115 -->
			<xsl:when test="$schema='iso19115'">
				<xsl:call-template name="iso19115Brief"/>
			</xsl:when>

			<!-- ISO 19139 -->
			<xsl:when test="$schema='iso19139'">
				<xsl:call-template name="iso19139Brief"/>
			</xsl:when>

			<!-- FGDC -->
			<xsl:when test="$schema='fgdc-std'">
				<xsl:call-template name="fgdc-stdBrief"/>
			</xsl:when>

			<!-- Dublin core -->
			<xsl:when test="$schema='dublin-core'">
				<xsl:call-template name="dublin-coreBrief"/>
			</xsl:when>

			<!-- default, no schema-specific formatting -->
			<xsl:otherwise>
				<metadata>
					<xsl:apply-templates mode="copy" select="*"/>
				</metadata>
			</xsl:otherwise>

		</xsl:choose>
	</xsl:template>

	<!--
	creates a thumbnail image, possibly with a link to larger image
	-->
	<xsl:template name="thumbnail">
		<xsl:param name="metadata"/>

		<xsl:choose>

			<!-- small thumbnail -->
			<xsl:when test="$metadata/image[@type='thumbnail']">

				<xsl:choose>

					<!-- large thumbnail link -->
					<xsl:when test="$metadata/image[@type='overview']">
						<a href="javascript:popWindow('{$metadata/image[@type='overview']}')">
							<img src="{$metadata/image[@type='thumbnail']}" alt="{/root/gui/strings/thumbnail}"/>
						</a>
					</xsl:when>

					<!-- no large thumbnail -->
					<xsl:otherwise>
						<img src="{$metadata/image[@type='thumbnail']}" alt="{/root/gui/strings/thumbnail}"/>
					</xsl:otherwise>
				</xsl:choose>

			</xsl:when>

			<!-- papermaps thumbnail -->
			<!-- FIXME
			<xsl:when test="/root/gui/paperMap and string(dataIdInfo/idCitation/presForm/PresFormCd/@value)='mapHardcopy'">
				<a href="PAPERMAPS-URL">
					<img src="{/root/gui/paperMap}" alt="{/root/gui/strings/paper}" title="{/root/gui/strings/paper}"/>
				</a>
			</xsl:when>
			-->

			<!-- no thumbnail -->
			<xsl:otherwise>
				<img src="{/root/gui/locUrl}/images/nopreview.gif" alt="{/root/gui/strings/thumbnail}"/>
			</xsl:otherwise>
		</xsl:choose>
		<br/>
	</xsl:template>

	<!--
	standard metadata buttons (edit/delete/privileges/categories)
	-->
	<xsl:template name="buttons" match="*">
		<xsl:param name="metadata" select="."/>

		<!-- create button -->
		<!-- When a user with access to the metadata.duplicate.form can see a template, he can use it.
			  Also when not allowed to edit the template himself -->

		<xsl:if test="string(geonet:info/isTemplate)!='s' and (geonet:info/isTemplate='y' or geonet:info/source=/root/gui/env/site/siteId) and /root/gui/services/service/@name='metadata.duplicate.form'">
			<button class="content" onclick="load('{/root/gui/locService}/metadata.duplicate.form?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/create"/></button>
		</xsl:if>
		
		<!-- it is the server that decides if a user can edit/delete/set privileges/set categories to a metadata -->
		<xsl:if test="geonet:info/edit='true'">
			<!-- edit button -->
			&#160;
			<button class="content" onclick="load('{/root/gui/locService}/metadata.edit?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>

			<!-- delete button -->
			&#160;
			<button class="content" onclick="return doConfirm('{/root/gui/locService}/metadata.delete?id={$metadata/geonet:info/id}', '{/root/gui/strings/confirmDelete}')"><xsl:value-of select="/root/gui/strings/delete"/></button>
			
			<!-- privileges button -->
			&#160;
			<button class="content" onclick="load('{/root/gui/locService}/metadata.admin.form?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/privileges"/></button>
			
			<!-- categories button -->
			&#160;
			<button class="content" onclick="load('{/root/gui/locService}/metadata.category.form?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/categories"/></button>
		</xsl:if>

	</xsl:template>

	<!--
	editor left tab
	-->
	<xsl:template name="tab">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>
		<xsl:param name="tabLink"/>
		
		<table width="100%">
		
			<!-- simple tab -->
			<xsl:call-template name="displayTab">
				<xsl:with-param name="tab"     select="'simple'"/>
				<xsl:with-param name="text"    select="/root/gui/strings/simpleTab"/>
				<xsl:with-param name="tabLink" select="$tabLink"/>
			</xsl:call-template>
			
			<!--  complete tab(s) -->
			<xsl:choose>
			
				<!-- hide complete tab for subtemplates -->
				<xsl:when test="geonet:info[isTemplate='s']"/>
			
				<xsl:when test="$currTab='xml' or $currTab='simple'">
					<xsl:call-template name="displayTab">
						<xsl:with-param name="tab"     select="'metadata'"/>
						<xsl:with-param name="text"    select="/root/gui/strings/completeTab"/>
						<xsl:with-param name="tabLink" select="$tabLink"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
				
					<!-- metadata type-specific complete tab -->
					<xsl:choose>
						
						<!-- ISO 19115 -->
						<xsl:when test="$schema='iso19115'">
							<xsl:call-template name="iso19115CompleteTab">
								<xsl:with-param name="tabLink" select="$tabLink"/>
							</xsl:call-template>
						</xsl:when>
						
						<!-- ISO 19139 -->
						<xsl:when test="$schema='iso19139'">
							<xsl:call-template name="iso19139CompleteTab">
								<xsl:with-param name="tabLink" select="$tabLink"/>
							</xsl:call-template>
						</xsl:when>
						
						<!-- default, no schema-specific formatting -->
						<xsl:otherwise>
							<xsl:call-template name="completeTab">
								<xsl:with-param name="tabLink" select="$tabLink"/>
							</xsl:call-template>
						</xsl:otherwise>
						
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			
			<!-- xml tab -->
			<xsl:choose>
				<xsl:when test="contains($tabLink,'metadata.show')">
					<xsl:call-template name="displayTab">
						<xsl:with-param name="tab"     select="'xml'"/>
						<xsl:with-param name="text"    select="/root/gui/strings/xmlTab"/>
						<xsl:with-param name="tabLink" select="$tabLink"/>
				</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="displayTab">
						<xsl:with-param name="tab"     select="'xml'"/>
						<xsl:with-param name="text"    select="/root/gui/strings/xmlTab"/>
						<xsl:with-param name="tabLink" select="$tabLink"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</table>
	</xsl:template>
	
	<!--
	default complete tab template
	-->
	<xsl:template name="completeTab">
		<xsl:param name="tabLink"/>
		
		<xsl:call-template name="displayTab">
			<xsl:with-param name="tab"     select="'metadata'"/>
			<xsl:with-param name="text"    select="/root/gui/strings/completeTab"/>
			<xsl:with-param name="tabLink" select="$tabLink"/>
		</xsl:call-template>
		<!--
		<xsl:call-template name="displayTab">
			<xsl:with-param name="tab"     select="'metadata'"/>
			<xsl:with-param name="text"    select="/root/gui/strings/metadata"/>
			<xsl:with-param name="indent"  select="'&#xA0;&#xA0;'"/>
			<xsl:with-param name="tabLink" select="$tabLink"/>
		</xsl:call-template>
		-->
	</xsl:template>
	
	<!--
	shows a tab
	-->
	<xsl:template name="displayTab">
		<xsl:param name="tab"/>
		<xsl:param name="text"/>
		<xsl:param name="indent"/>
		<xsl:param name="tabLink"/>
		
		<xsl:variable name="currTab" select="/root/gui/currTab"/>
	
		<tr><td class="banner-login">
			<xsl:value-of select="$indent"/>
			
			<xsl:choose>
				<!-- not active -->
				<xsl:when test="$tabLink=''"><font class="banner-passive"><xsl:value-of select="$text"/></font></xsl:when>
				
				<!-- selected -->
				<xsl:when test="$currTab=$tab"><font class="banner-active"><xsl:value-of select="$text"/></font></xsl:when>
				
				<!-- not selected -->
				<xsl:otherwise><a class="palette" href="javascript:doTabAction('{$tabLink}','{$tab}')"><xsl:value-of select="$text"/></a></xsl:otherwise>
			</xsl:choose>
		</td></tr>
	</xsl:template>
	
</xsl:stylesheet>
