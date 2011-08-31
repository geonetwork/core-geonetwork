<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt geonet">

	<xsl:include href="blanks/metadata-schema01.xsl"/>
	<xsl:include href="blanks/metadata-schema02.xsl"/>
	<xsl:include href="blanks/metadata-schema03.xsl"/>
	<xsl:include href="blanks/metadata-schema04.xsl"/>
	<xsl:include href="blanks/metadata-schema05.xsl"/>
	<xsl:include href="blanks/metadata-schema06.xsl"/>
	<xsl:include href="blanks/metadata-schema07.xsl"/>
	<xsl:include href="blanks/metadata-schema08.xsl"/>
	<xsl:include href="blanks/metadata-schema09.xsl"/>
	<xsl:include href="blanks/metadata-schema10.xsl"/>
	<xsl:include href="blanks/metadata-schema11.xsl"/>
	<xsl:include href="blanks/metadata-schema12.xsl"/>
	<xsl:include href="blanks/metadata-schema13.xsl"/>
	<xsl:include href="blanks/metadata-schema14.xsl"/>
	<xsl:include href="blanks/metadata-schema15.xsl"/>
	<xsl:include href="blanks/metadata-schema16.xsl"/>
	<xsl:include href="blanks/metadata-schema17.xsl"/>
	<xsl:include href="blanks/metadata-schema18.xsl"/>
	<xsl:include href="blanks/metadata-schema19.xsl"/>
	<xsl:include href="blanks/metadata-schema20.xsl"/>

	<xsl:template mode="schema" match="*">
		<xsl:choose>
			<xsl:when test="string(geonet:info/schema)!=''"><xsl:value-of select="geonet:info/schema"/></xsl:when>
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
		
			<!-- create XML fragment with name of schema Brief template to 
					 select plus all info in /root and the metadata we are
					 handling - 

					 eg. 
					 /iso19139Brief 
					 /root
					 /metadata

					 The idea is that we get to dynamically call the template
					 we want but all templates can still find gui info on /root
					 Also no need to do a choose on $schema - makes it easier for
					 plugin schemas to just work

					 All schema definitions need to define the Brief template eg.
					 iso19139Brief and unpack the metadata from /metadata -->
			
			<xsl:variable name="briefSchemaCallBack">
				<xsl:element name="{concat($schema,'Brief')}"/>
				<xsl:copy-of select="/root"/>
				<xsl:element name="metadata">
					<xsl:copy-of select="."/>
				</xsl:element>
			</xsl:variable>

			<xsl:apply-templates select="exslt:node-set($briefSchemaCallBack/*[1])"/>
	</xsl:template>

	<!--
	standard metadata buttons (edit/delete/privileges/categories)
	-->
	<xsl:template name="buttons" match="*">
		<xsl:param name="metadata" select="."/>
		<xsl:param name="ownerbuttonsonly" select="false()"/>

		<!-- Title is truncated if longer than maxLength.  -->
		<xsl:variable name="maxLength" select="'40'"/>

		<xsl:variable name="ltitle">
			<xsl:call-template name="escapeString">
				<xsl:with-param name="expr">
					<xsl:choose>
						<xsl:when test="string-length($metadata/title) &gt; $maxLength">
							<xsl:value-of select="concat(substring(normalize-space($metadata/title), 1, $maxLength), ' ...')"/>
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="normalize-space($metadata/title)"/></xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
				
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="not($ownerbuttonsonly) and 
	 /root/gui/schemalist/name[.=$metadata/geonet:info/schema]/@edit='true'">
			&#160;
			<!-- create button -->
			<xsl:variable name="duplicate" select="concat(/root/gui/strings/duplicate,': ',$ltitle)"/>
			<xsl:if test="string(geonet:info/isTemplate)!='s' and (geonet:info/isTemplate='y' or geonet:info/source=/root/gui/env/site/siteId) and /root/gui/services/service/@name='metadata.duplicate.form'">
				<button class="content" onclick="load('{/root/gui/locService}/metadata.duplicate.form?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/create"/></button>
			</xsl:if>

            <!-- edit button -->
			<xsl:if test="
		(/root/gui/config/harvester/enableEditing = 'true' and geonet:info/isHarvested = 'y' and geonet:info/edit='true')
		or (geonet:info/isHarvested = 'n' and geonet:info/edit='true')">
			&#160;
			<button class="content" onclick="load('{/root/gui/locService}/metadata.edit?id={$metadata/geonet:info/id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
			</xsl:if>			
		</xsl:if>
		
		<!-- delete button -->
		<xsl:if test="geonet:info/owner='true'">
			&#160;
			<button class="content" onclick="return doConfirmDelete('{/root/gui/locService}/metadata.delete?id={$metadata/geonet:info/id}', '{/root/gui/strings/confirmDelete}','{$ltitle}','{$metadata/geonet:info/id}', '{/root/gui/strings/deleteConfirmationTitle}')"><xsl:value-of select="/root/gui/strings/delete"/></button>
		</xsl:if>
						
		<xsl:if test="geonet:info/edit='true'">
			&#160;
			<!-- =========================  -->
			<!-- Add other actions list     -->
			<button id="oAc{$metadata/geonet:info/id}" name="oAc{$metadata/geonet:info/id}" class="content" onclick="oActions('oAc',{$metadata/geonet:info/id});" style="width:150px;" title="{/root/gui/strings/otherActions}">
				<img id="oAcImg{$metadata/geonet:info/id}" name="oAcImg{$metadata/geonet:info/id}" src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/>
				<xsl:value-of select="/root/gui/strings/otherActions"/>
			</button>
			<div id="oAcEle{$metadata/geonet:info/id}" class="oAcEle" style="display:none;width:250px" onClick="oActions('oAc',{$metadata/geonet:info/id});">
				
				<!-- privileges button -->
				<xsl:if test="/root/gui/services/service/@name='metadata.admin.form'">
					<xsl:variable name="privileges" select="concat(/root/gui/strings/setshowprivileges,' ',$ltitle)"/>
					<button onclick="doOtherButton('{/root/gui/locService}/metadata.admin.form?id={$metadata/geonet:info/id}','{$privileges}',600)"><xsl:value-of select="/root/gui/strings/privileges"/></button>
				</xsl:if>
				
				<!-- categories button -->
				<xsl:if test="/root/gui/services/service/@name='metadata.category.form' and /root/gui/config/category/admin">
					<xsl:variable name="categories" select="concat(/root/gui/strings/setshowcategories,' ',$ltitle)"/>
					<button onclick="doOtherButton('{/root/gui/locService}/metadata.category.form?id={$metadata/geonet:info/id}','{$categories}',300)"><xsl:value-of select="/root/gui/strings/categories"/></button>
				</xsl:if>
				
				<!-- Create child option only for iso19139 schema based metadata -->
				<xsl:variable name="duplicateChild" select="concat(/root/gui/strings/createChild,': ',$ltitle)"/>
				<xsl:if test="contains(geonet:info/schema, 'iso19139')">
				  <button onclick="load('{/root/gui/locService}/metadata.duplicate.form?uuid={$metadata/geonet:info/uuid}&amp;child=y')"><xsl:value-of select="/root/gui/strings/createChild"/></button>
				</xsl:if>	
			</div>
		</xsl:if>
	</xsl:template>


    <!-- Create a div with class name set to extentViewer in 
        order to generate a new map.  -->

    <xsl:template name="showMap">
        <xsl:param name="edit" />
        <xsl:param name="coords"/>
        <!-- Indicate which drawing mode is used (ie. bbox or polygon) -->
        <xsl:param name="mode"/>
        <xsl:param name="targetPolygon"/>
        <xsl:param name="watchedBbox"/>
        <xsl:param name="eltRef"/>
        <div class="extentViewer" style="width:{/root/gui/config/map/metadata/width}; height:{/root/gui/config/map/metadata/height};" 
            edit="{$edit}" 
            target_polygon="{$targetPolygon}" 
            watched_bbox="{$watchedBbox}" 
            elt_ref="{$eltRef}"
            mode="{$mode}">
            <div style="display:none;" id="coords_{$eltRef}"><xsl:value-of select="$coords"/></div>
        </div>
    </xsl:template>

</xsl:stylesheet>
