<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common"
	xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="exslt geonet">

  <xsl:include href="metadata-fop.xsl"/>
  
	<!-- main template - the way into processing fgdc-std -->
	<xsl:template name="metadata-fgdc-std">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>
	
    <xsl:apply-templates mode="fgdc-std" select="." >
    	<xsl:with-param name="schema" select="$schema"/>
     	<xsl:with-param name="edit"   select="$edit"/>
     	<xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

	<!-- CompleteTab template - fgdc just calls completeTab from 
	     metadata-utils.xsl -->
	<xsl:template name="fgdc-stdCompleteTab">
		<xsl:param name="tabLink"/>

		<xsl:call-template name="completeTab">
			<xsl:with-param name="tabLink" select="$tabLink"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="fgdc-std" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$currTab='simple'"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	these elements should be boxed
	-->
	<xsl:template mode="fgdc-std" match="idinfo|citeinfo|timeperd|status|bounding|keywords|metainfo|metc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	metadata 
	-->
	<xsl:template mode="fgdc-std" match="metadata">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded"/>

		<!-- thumbnail -->
    <tr>
			<td class="padded" align="center" valign="middle" colspan="2">
				<xsl:variable name="md">
					<xsl:apply-templates mode="brief" select="."/>
				</xsl:variable>
				<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
				<xsl:if test="$embedded=false()">
					<xsl:call-template name="thumbnail">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template>
				</xsl:if>
			</td>
		</tr>

		<xsl:apply-templates mode="elementEP" select="*">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	keywords
	-->
	<xsl:template mode="fgdc-std" match="theme|place|stratum|temporal">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
	
		<xsl:choose>
		<xsl:when test="$edit=false()">
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:for-each select="themekey|placekey|stratkey|tempkey">
					<xsl:if test="position() &gt; 1">,	</xsl:if>
					<xsl:value-of select="."/>
				</xsl:for-each>
				<xsl:if test="themekt|placekt|stratkt|tempkt">
					<xsl:text> (</xsl:text>
					<xsl:value-of select="themekt|placekt|stratkt|tempkt"/>
					<xsl:text>)</xsl:text>
				</xsl:if>
			</xsl:with-param>
		</xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		</xsl:otherwise>
		</xsl:choose>
	
	</xsl:template>

	<!--
	online link
	-->
	<xsl:template mode="fgdc-std" match="onlink">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
	
		<xsl:choose>
		<xsl:when test="$edit=false()">
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<a href="{.}"><xsl:value-of select="."/></a>
			</xsl:with-param>
		</xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		</xsl:otherwise>
		</xsl:choose>
	

	</xsl:template>
	
	<!--
	abstract
	-->
	<xsl:template mode="fgdc-std" match="abstract">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:call-template name="getElementText">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="rows"   select="10"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	placeholder
	<xsl:template mode="fgdc-std" match="TAG">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		BODY
	</xsl:template>
	-->
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- fgdc-std brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="fgdc-stdBrief">
		<metadata>
			<xsl:copy-of select="idinfo/citation/citeinfo/title"/>
			<xsl:copy-of select="idinfo/descript/abstract"/>


			<xsl:for-each select="idinfo/keywords/theme/themekey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/place/placekey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/stratum/stratkey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/temporal/tempkey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/citation/citeinfo/onlink[text()]">
				<link type="url"><xsl:value-of select="."/></link>
			</xsl:for-each>
			
			<xsl:if test="idinfo/spdom/bounding">
				<geoBox>
					<westBL><xsl:value-of select="idinfo/spdom/bounding/westbc"/></westBL>
					<eastBL><xsl:value-of select="idinfo/spdom/bounding/eastbc"/></eastBL>
					<southBL><xsl:value-of select="idinfo/spdom/bounding/southbc"/></southBL>
					<northBL><xsl:value-of select="idinfo/spdom/bounding/northbc"/></northBL>
				</geoBox>
			</xsl:if>

			<xsl:if test="not(geonet:info/server)">
				<xsl:variable name="info" select="geonet:info"/>
				<xsl:variable name="id" select="geonet:info/id"/>

				<xsl:for-each select="idinfo/browse">
					<xsl:variable name="fileName"  select="browsen"/>
					<xsl:if test="$fileName != ''">
						<xsl:variable name="fileDescr" select="browset"/>
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
			<xsl:copy-of select="geonet:info"/>
		</metadata>
	</xsl:template>

	<xsl:template name="fgdc-std-javascript"/>

</xsl:stylesheet>
