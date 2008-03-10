<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:variable name="apos">&#x27;</xsl:variable>

	<xsl:variable name="maxAbstract" select="200"/>
	
	<!-- default: just copy -->
	<xsl:template match="@*|node()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="copy"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="escapeXMLEntities" match="text()">
	
		<xsl:variable name="expr" select="."/>
		
		<xsl:variable name="e1">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$expr"/>
				<xsl:with-param name="pattern"     select="'&amp;'"/>
				<xsl:with-param name="replacement" select="'&amp;amp;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e2">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e1"/>
				<xsl:with-param name="pattern"     select="'&lt;'"/>
				<xsl:with-param name="replacement" select="'&amp;lt;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e3">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e2"/>
				<xsl:with-param name="pattern"     select="'&gt;'"/>
				<xsl:with-param name="replacement" select="'&amp;gt;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e4">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e3"/>
				<xsl:with-param name="pattern"     select='"&apos;"'/>
				<xsl:with-param name="replacement" select="'&amp;apos;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="replaceString">
			<xsl:with-param name="expr"        select="$e4"/>
			<xsl:with-param name="pattern"     select="'&quot;'"/>
			<xsl:with-param name="replacement" select="'&amp;quot;'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="replaceString">
		<xsl:param name="expr"/>
		<xsl:param name="pattern"/>
		<xsl:param name="replacement"/>
		
		<xsl:variable name="first" select="substring-before($expr,$pattern)"/>
		<xsl:choose>
			<xsl:when test="$first">
				<xsl:value-of select="$first"/>
				<xsl:value-of select="$replacement"/>
				<xsl:call-template name="replaceString">
					<xsl:with-param name="expr"        select="substring-after($expr,$pattern)"/>
					<xsl:with-param name="pattern"     select="$pattern"/>
					<xsl:with-param name="replacement" select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$expr"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="socialBookmarks">
		<xsl:param name="baseURL" />
		<xsl:param name="mdURL" />
		<xsl:param name="title" />
		<xsl:param name="abstract" />
		<xsl:variable name="t" select="normalize-space($title)" />
		<xsl:variable name="a" select="normalize-space($abstract)" />
		
		<xsl:if test="not(contains($mdURL,'localhost')) and not(contains($mdURL,'127.0.0.1'))">
			<a href="mailto:?subject={$t}&amp;body=%0ALink:%0A{$mdURL}%0A%0AAbstract:%0A{$a}">
				<img src="{$baseURL}/images/mail.png" 
					alt="Send to a friend" title="Send to a friend" 
					style="border: 0px solid;padding:2px;padding-right:10px;"/>
			</a>
				
				<!-- Not browser independent, thus commented out -->
<!--			<a href="javascript:window.external.AddFavorite('{$mdURL}', '{$t}');">
				<img src="{$baseURL}/images/bookmark.png" 
					alt="Bookmark" title="Bookmark" 
					style="border: 0px solid;padding:2px;"/>
			</a> -->
			
			<!-- add first sentence of abstract to the delicious notes -->
			<a href="http://del.icio.us/post?url={$mdURL}&amp;title={$t}&amp;notes={substring-before($a,'. ')}. ">
				<img src="{$baseURL}/images/delicious.gif" 
					alt="Bookmark on Delicious" title="Bookmark on Delicious" 
					style="border: 0px solid;padding:2px;"/>
			</a> 
			<a href="http://digg.com/submit?url={$mdURL}&amp;title={substring($t,0,75)}&amp;bodytext={substring(substring-before($a,'. '),0,350)}.&amp;topic=environment">
				<img src="{$baseURL}/images/digg.gif" 
					alt="Bookmark on Digg" title="Bookmark on Digg" 
					style="border: 0px solid;padding:2px;"/>
			</a> 
			<a href="http://www.facebook.com/sharer.php?u={$mdURL}">
				<img src="{$baseURL}/images/facebook.gif" 
					alt="Bookmark on Facebook" title="Bookmark on Facebook" 
					style="border: 0px solid;padding:2px;"/>
			</a> 
			<a href="http://www.stumbleupon.com/submit?url={$mdURL}&amp;title={$t}">
				<img src="{$baseURL}/images/stumbleupon.gif" 
					alt="Bookmark on StumbleUpon" title="Bookmark on StumbleUpon" 
					style="border: 0px solid;padding:2px;"/>
			</a> 
		</xsl:if>
		
	</xsl:template>

</xsl:stylesheet>
