<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:import href="translate-widget.xsl"/>
    <xsl:import  href="utils.xsl"/>
    
	<!--
	Add button
	-->
	<xsl:template name="buttonAdd">
        <xsl:param name="typename"/>
        <xsl:param name="wfs"/>
        <xsl:param name="id"/>
		
			<button onclick="javascript:popWindow('{/root/gui/locService}/extent.edit?crs=EPSG:21781&amp;typename={$typename}&amp;id={$id}&amp;wfs={$wfs}&amp;modal');" class="content">
				<xsl:value-of select="/root/gui/strings/add"/>
			</button>
	</xsl:template>


	<xsl:template name="legend">
		<xsl:param name="mode" />
		<xsl:param name="features" />

		<div class="legend">

			<xsl:if test="count($features/broader/descKeys/feature) &gt; 0">
				<div class="broader">
					<xsl:value-of select="/root/gui/strings/thesaurus/broader" />
					<br />
					<ul>
						<xsl:for-each select="$features/broader/descKeys/feature">
							<xsl:call-template name="featureRow">
								<xsl:with-param name="feature" select="." />
								<xsl:with-param name="mode" select="$mode" />
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>

			<xsl:if test="count($features/related/descKeys/feature) &gt; 0">
				<div class="related">
					<xsl:value-of select="/root/gui/strings/thesaurus/related" />
					<br />
					<ul>
						<xsl:for-each select="$features/related/descKeys/feature">
							<xsl:call-template name="featureRow">
								<xsl:with-param name="feature" select="." />
								<xsl:with-param name="mode" select="$mode" />
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>

			<xsl:if test="count($features/narrower/descKeys/feature) &gt; 0">
				<div class="narrower">
					<xsl:value-of select="/root/gui/strings/thesaurus/narrower" />
					<br />
					<ul>
						<xsl:for-each select="$features/narrower/descKeys/feature">
							<xsl:call-template name="featureRow">
								<xsl:with-param name="feature" select="." />
								<xsl:with-param name="mode" select="$mode" />
							</xsl:call-template>
						</xsl:for-each>
					</ul>
				</div>
			</xsl:if>
		</div>
	</xsl:template>



	<xsl:template name="featureRow">
        <xsl:param name="feature" />
        <xsl:param name="mode" />
		
        <xsl:variable name="wfs" select="$feature/../../@id"/>
        <xsl:variable name="typename" select="$feature/../@typename"/>
        <xsl:variable name="id" select="$feature/@id"/>
        <xsl:variable name="lang" select="/root/gui/language"/>

		<li>
			<!-- Edit mode: add check box -->
			<xsl:if test="$mode='edit'">
				<xsl:variable name="selected" select="selected" />
			   <xsl:choose>
				<xsl:when test="$feature/@selected='true'">
					<input type="checkbox" id="chk{$id}" value="{$id}"
						checked="checked" onclick="javascript:esearching.select('{$wfs}','{$typename}','{$id}')" />
				</xsl:when>
				<xsl:otherwise>
					<input type="checkbox" id="chk{$id}" value="{$id}"
						onclick="javascript:esearching.select('{$wfs}','{$typename}','{$id}')" />
				</xsl:otherwise>
		      </xsl:choose>
			</xsl:if>

            <xsl:variable name="desc">
                <xsl:variable name="tmp">
                     <xsl:call-template name="translateElem">
                       <xsl:with-param name="lang" select="$lang"/>
                       <xsl:with-param name="elem" select="$feature/desc" />
                    </xsl:call-template>
                 </xsl:variable>
                 <xsl:value-of select="normalize-space($tmp)"/>
            </xsl:variable>

            <xsl:variable name="code">
                <xsl:variable name="tmp">
                     <xsl:call-template name="translateElem">
                       <xsl:with-param name="lang" select="$lang"/>
                       <xsl:with-param name="elem" select="$feature/geoId" />
                    </xsl:call-template>
                 </xsl:variable>
                 <xsl:value-of select="normalize-space($tmp)"/>
            </xsl:variable>

            <xsl:variable name="completeDesc">
                <xsl:choose>
                    <xsl:when test="$desc!='' and $code!=''">
                        <xsl:value-of select="$desc"/><xsl:text> </xsl:text>&lt;<xsl:value-of select="$code"/>&gt;
                    </xsl:when>
                    <xsl:when test="$desc!='' and $code=''">
                        <xsl:value-of select="$desc"/>
                    </xsl:when>
                    <xsl:when test="$desc='' and $code!=''">
                        <xsl:value-of select="$code"/>
                    </xsl:when>
                    <xsl:otherwise><!-- nothing --></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="valid">
                <xsl:call-template name="validIndicator">
                    <xsl:with-param name="indicator" select="contains(string(@href),'typename=gn:non_validated')" />
                </xsl:call-template>
            </xsl:variable>
                               
			<xsl:choose>
				<xsl:when test="$mode='search'">
					<!-- Add XLink attribute to be used for reusable elements -->
					<xsl:attribute name="xlink:href">
						<!-- Escape # sign in URL -->
			                <xsl:choose>
			                   <xsl:when test="contains ($feature/@href, '#')">
			                       <xsl:value-of select="concat(substring-before($feature/@href, '#'), '%23', substring-after($feature/@href, '#'))" />
			                   </xsl:when>
			                   <xsl:otherwise>
			                       <xsl:value-of select="$feature/@href" />
			                   </xsl:otherwise>
			               </xsl:choose>
					</xsl:attribute>
					<xsl:copy-of select="$valid"/><xsl:text> </xsl:text><xsl:value-of select="$completeDesc"/>
				</xsl:when>
				<xsl:otherwise>
					<a href="javascript:popWindow('{/root/gui/locService}/extent.edit?crs=EPSG:21781&amp;wfs={$wfs}&amp;typename={$typename}&amp;id={$id}&amp;modal');">
	                    <xsl:copy-of select="$valid"/><xsl:text> </xsl:text><xsl:value-of select="$completeDesc"/>
					</a>
				</xsl:otherwise>
			</xsl:choose>

		</li>
	</xsl:template>
	
    <xsl:template name="translateElem">
        <xsl:param name="lang" />
        <xsl:param name="elem" />
        
        <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
        <xsl:variable name="upperLang" select="translate(substring($lang,1,2), $LOWER, $UPPER)"></xsl:variable>
        <xsl:choose>
            <xsl:when test="$elem/node()[translate(name(), $LOWER, $UPPER)=$upperLang]">
                <xsl:value-of select="$elem/node()[translate(name(), $LOWER, $UPPER)=$upperLang]" />
            </xsl:when>
            <xsl:when test="$elem/text()">
                <xsl:value-of select="$elem/text()" />
            </xsl:when>
            <xsl:when test="$elem/node()[translate(name(), $LOWER, $UPPER)='EN']">
                <xsl:value-of select="$elem/node()[translate(name(), $LOWER, $UPPER)='EN']" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$elem/node()[1]" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template
        match="feature/desc/node()[name()=/root/gui/strings/languageIso3]">
        <xsl:value-of select="." />
    </xsl:template>

</xsl:stylesheet>
