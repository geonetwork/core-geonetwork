<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--
	Add button
	-->
	<xsl:template name="buttonAdd">
		<xsl:param name="mode"/>
		<xsl:param name="thesaurus"/>

		<xsl:if test="$mode='edit'">
			<button onclick="javascript:popWindow('{/root/gui/locService}/thesaurus.editelement?ref={$thesaurus}');" class="content">
				<xsl:value-of select="/root/gui/strings/add"/>
			</button>
		</xsl:if>
	</xsl:template>


	<xsl:template name="legend">
		<xsl:param name="mode"/>
		<xsl:param name="keywords"/>
		<xsl:param name="thesaurus"/>
		<xsl:param name="url"/>

		<div class="legend">

			<xsl:if test="count($keywords/broader/descKeys/keyword) &gt; 0">
				<div class="broader">
					<xsl:value-of select="/root/gui/strings/thesaurus/broader"/>
					<br/>
					<ul>
						<xsl:for-each select="$keywords/broader/descKeys/keyword">
							<xsl:call-template name="keywordRow">
								<xsl:with-param name="keyword" select="."/>
								<xsl:with-param name="mode" select="$mode"/>
								<xsl:with-param name="thesaurus" select="$thesaurus"/>
								<xsl:with-param name="url" select="$url"/>
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>

			<xsl:if test="count($keywords/related/descKeys/keyword) &gt; 0">
				<div class="related">
					<xsl:value-of select="/root/gui/strings/thesaurus/related"/>
					<br/>
					<ul>
						<xsl:for-each select="$keywords/related/descKeys/keyword">
							<xsl:call-template name="keywordRow">
								<xsl:with-param name="keyword" select="."/>
								<xsl:with-param name="mode" select="$mode"/>
								<xsl:with-param name="thesaurus" select="$thesaurus"/>
								<xsl:with-param name="url" select="$url"/>
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>

			<xsl:if test="count($keywords/narrower/descKeys/keyword) &gt; 0">
				<div class="narrower">
					<xsl:value-of select="/root/gui/strings/thesaurus/narrower"/>
					<br/>
					<ul>
						<xsl:for-each select="$keywords/narrower/descKeys/keyword">
							<xsl:call-template name="keywordRow">
								<xsl:with-param name="keyword" select="."/>
								<xsl:with-param name="mode" select="$mode"/>
								<xsl:with-param name="thesaurus" select="$thesaurus"/>
								<xsl:with-param name="url" select="$url"/>
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>
		</div>
	</xsl:template>



	<xsl:template name="keywordRow">
		<xsl:param name="keyword"/>
		<xsl:param name="thesaurus"/>
		<xsl:param name="mode"/>
		<xsl:param name="url"/>
		<li>
			<xsl:if test="$mode='edit'">
				<xsl:variable name="selected" select="selected"/>
				<xsl:if test="selected='true'">
					<input type="checkbox" id="chk{$keyword/id}" value="{$keyword/id}" checked="checked" onclick="javascript:ksearching.select({$keyword/id})"/>
				</xsl:if>
				<xsl:if test="selected='false'">
					<input type="checkbox" id="chk{$keyword/id}" value="{$keyword/id}" onclick="javascript:ksearching.select({$keyword/id})"/>
				</xsl:if>
			</xsl:if>

			<xsl:choose>
				<xsl:when test="$mode='search'">
					<xsl:value-of select="$keyword/value"/>
				</xsl:when>
				<xsl:when test="$mode='legend'">
					<form id="{./uri}" name="{./uri}" action="{/root/gui/locService}/thesaurus.editelement" method="POST">
						<a onclick="$('{./uri}').submit();">
							<xsl:value-of select="$keyword/value"/>
						</a>
						<input type="hidden" name="mode" value="consult"/>
						<input type="hidden" name="ref" value="{$thesaurus}"/>
						<input type="hidden" name="uri" value="{./uri}"/>
					</form>

				</xsl:when>
				<xsl:otherwise>
					<a href="javascript:popWindow('{/root/gui/locService}/thesaurus.editelement?mode={$mode}&amp;ref={$thesaurus}&amp;id={./id}');">
						<xsl:value-of select="$keyword/value"/>
					</a>
				</xsl:otherwise>
			</xsl:choose>

		</li>
	</xsl:template>

</xsl:stylesheet>
