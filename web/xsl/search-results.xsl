<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl   ="http://www.w3.org/1999/XSL/Transform"
										xmlns:geonet="http://www.fao.org/geonetwork"
										xmlns:xalan = "http://xml.apache.org/xalan">
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<xsl:variable name="pageRange"   select="5"/>
	<xsl:variable name="hitsPerPage">
		<xsl:choose>
			<xsl:when test="/root/gui/searchDefaults/hitsPerPage"><xsl:value-of select="string(/root/gui/searchDefaults/hitsPerPage)"/></xsl:when>
			<xsl:otherwise>10</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	
	<!--
	page content
	-->
	<xsl:template name="content">
		<table  width="100%" height="100%">
		
			<!-- title -->
			<xsl:call-template name="formTitle">
				<xsl:with-param name="title">
					<xsl:value-of select="/root/gui/strings/resultsMatching"/>
					&#160;
					<xsl:value-of select="/root/response/summary/@count"/>
				</xsl:with-param>
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>
			
			<!-- list of metadata -->
			<xsl:call-template name="hits"/>
			
			<!-- page list -->
			<xsl:call-template name="formSeparator"/>
			<xsl:call-template name="formContent">
				<xsl:with-param name="content">
					<xsl:call-template name="pageList"/>
				</xsl:with-param>
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>
			<xsl:call-template name="formFiller">
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>

	<!--
	all presented hits
	-->
	<xsl:template name="hits">
		<xsl:variable name="remote" select="/root/response/summary/@type='remote'"/>
		<xsl:for-each select="/root/response/*[name(.)!='summary']">
		
			<xsl:variable name="md">
				<xsl:apply-templates mode="brief" select="."/>
			</xsl:variable>
			
			<xsl:variable name="metadata" select="xalan:nodeset($md)/*[1]"/>
			<xsl:call-template name="formSeparator"/>
			<xsl:call-template name="formContent">
				<xsl:with-param name="content">
					<xsl:call-template name="hit">
						<xsl:with-param name="metadata" select="$metadata"/>
						<xsl:with-param name="remote"   select="$remote"/>
					</xsl:call-template>
				</xsl:with-param>
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>
	
	<!--
	one hit
	-->
	<xsl:template name="hit">
		<xsl:param name="metadata"/>
		<xsl:param name="remote"/>
	
		<!-- info -->
		<table width="100%">
			<tr>
				<td valign="top">
					<table>
					
						<!-- title -->
						<tr>
							<xsl:choose>
								<xsl:when test="$remote=true()">
									<xsl:variable name="host" select="substring-before($metadata/geonet:info/server,':')"/>
									<xsl:variable name="rest" select="substring-after($metadata/geonet:info/server,':')"/>
									<xsl:variable name="port" select="substring-before($rest,'/')"/>
									<xsl:variable name="db" select="substring-after($rest,'/')"/>
									<td class="padded" colspan="2">
										<h1 align="left"><a href="{/root/gui/locService}/remote.show?id={$metadata/geonet:info/id}&amp;currTab=simple"><xsl:value-of select="concat($metadata/geonet:info/id,' - ',$metadata/title)"/></a></h1>
										<xsl:variable name="server" select="$metadata/geonet:info/server"/>
										<xsl:variable name="name" select="/root/gui/repositories/Collection[@collection_dn=$server]/@collection_name"/>
										<font class="green-neg"><xsl:value-of select="$name"/></font>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td align="center" valign="middle">
										<xsl:variable name="source" select="string($metadata/geonet:info/source)"/>
										<xsl:if test="/root/gui/sources/record[string(siteid)=$source]">
											<img src="{/root/gui/url}/images/logos/{$source}.png" width="40"/>
										</xsl:if>
									</td>
									<td class="padded"><h1 align="left"><a href="{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab=simple"><xsl:value-of select="$metadata/title"/></a></h1></td>
								</xsl:otherwise>
							</xsl:choose>
						</tr>
						
						<!-- abstract -->
						<xsl:if test="$metadata/abstract">
							<tr>
								<th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/abstract"/></th>
								<td class="padded" valign="top">
									<xsl:choose>
										<xsl:when test="string-length ($metadata/abstract) &gt; $maxAbstract">
											<xsl:value-of select="substring ($metadata/abstract, 0, $maxAbstract)"/>
											<xsl:choose>
												<xsl:when test="$remote=true()">
													<a href="{/root/gui/locService}/remote.show?id={$metadata/geonet:info/id}&amp;currTab=simple">
														...<xsl:value-of select="/root/gui/strings/more"/>...
													</a>
												</xsl:when>
												<xsl:otherwise>
													<a href="{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab=simple">
														...<xsl:value-of select="/root/gui/strings/more"/>...
													</a>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$metadata/abstract"/>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</xsl:if>
						
						<!-- keywords -->
						<xsl:if test="$metadata/keyword">
							<tr>
								<th class="padded" valign="top"><xsl:value-of select="/root/gui/strings/keywords"/></th>
								<td class="padded" valign="top">
									<xsl:for-each select="$metadata/keyword">
										<xsl:if test="position() &gt; 1">,	</xsl:if>
										<xsl:value-of select="."/>
									</xsl:for-each>
								</td>
							</tr>
						</xsl:if>
					</table>
				</td>
				<td class="padded" align="center" valign="center" width="200">
					<xsl:call-template name="score">
						<xsl:with-param name="score" select="$metadata/geonet:info/score * 100"/>
						<xsl:with-param name="class" select="5"/>
						<xsl:with-param name="currentClass" select="0"/>
					</xsl:call-template>
					
					<br/>
					<xsl:call-template name="thumbnail">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template>
				</td>
			</tr>
		</table>
		
		<!-- buttons -->
		<table width="100%">
			<tr>
				<td>
					
					<!-- view button -->
					<xsl:if test="$metadata/geonet:info/view='true'">
						<xsl:choose>
							<xsl:when test="$remote=true()">
								<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info/id}&amp;currTab=simple')"><xsl:value-of select="/root/gui/strings/show"/></button>
							</xsl:when>
							<xsl:otherwise>
								<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab=simple')"><xsl:value-of select="/root/gui/strings/show"/></button>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
		
					<!-- download button -->
					<xsl:if test="$metadata/geonet:info/download='true'">
						&#160;
						<xsl:choose>
							<xsl:when test="count($metadata/link[@type='download'])>1">
								<xsl:choose>
									<xsl:when test="$remote=true()">
										<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info/id}&amp;currTab=distribution')"><xsl:value-of select="/root/gui/strings/download"/></button>
									</xsl:when>
									<xsl:otherwise>
										<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab=distribution')"><xsl:value-of select="/root/gui/strings/download"/></button>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="count($metadata/link[@type='download'])=1">
								<button class="content" onclick="load('{$metadata/link[@type='download']}')"><xsl:value-of select="/root/gui/strings/download"/></button>
							</xsl:when>
						</xsl:choose>
					</xsl:if>
					
					<!-- dynamic map button -->
					<xsl:if test="$metadata/geonet:info/dynamic='true'">
						&#160;
						<xsl:variable name="count" select="count($metadata/link[@type='arcims']) + count($metadata/link[@type='wms'])"/>
						<xsl:choose>
							<xsl:when test="$count>1">
								<xsl:choose>
									<xsl:when test="$remote=true()">
										<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info/id}&amp;currTab=distribution')"><xsl:value-of select="/root/gui/strings/interactiveMap"/></button>
									</xsl:when>
									<xsl:otherwise>
										<button class="content" onclick="load('{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab=distribution')"><xsl:value-of select="/root/gui/strings/interactiveMap"/></button>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="$count=1">
								<button class="content" onclick="load('{$metadata/link[@type='arcims' or @type='wms']}')"><xsl:value-of select="/root/gui/strings/interactiveMap"/></button>
							</xsl:when>
						</xsl:choose>
					</xsl:if>
				</td>
				<td align="right">
					<xsl:call-template name="buttons">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!--
	list of pages
	-->
	<xsl:template name="pageList">
		<xsl:variable name="count" select="/root/response/summary/@count"/>
		<xsl:variable name="from" select="/root/response/@from"/>
		<xsl:variable name="to" select="/root/response/@to"/>
		
		<xsl:variable name="currPage" select="floor(($from - 1) div $hitsPerPage + 1)"/>
		<xsl:variable name="minPage">
			<xsl:choose>
				<xsl:when test="$currPage > $pageRange">
					<xsl:value-of select="$currPage - $pageRange"/>
				</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="maxPage">
			<xsl:choose>
				<xsl:when test="$currPage &lt; floor(($count - 1) div $hitsPerPage + 1 - $pageRange)">
					<xsl:value-of select="$currPage + $pageRange"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="floor(($count - 1) div $hitsPerPage + 1)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<b><xsl:value-of select="/root/gui/strings/resultPage"/></b>
		&#160;
		<xsl:if test="$currPage > $minPage">
			<xsl:call-template name="pageLink">
				<xsl:with-param name="count" select="$count"/>
				<xsl:with-param name="page"  select="$currPage - 1"/>
				<xsl:with-param name="label" select="/root/gui/strings/previous"/>
			</xsl:call-template>
		</xsl:if>
		&#160;
		<xsl:call-template name="pageLoop">
			<xsl:with-param name="count" select="$count"/>
			<xsl:with-param name="minPage" select="$minPage"/>
			<xsl:with-param name="currPage" select="$currPage"/>
			<xsl:with-param name="maxPage" select="$maxPage"/>
		</xsl:call-template>
		
		<xsl:if test="$currPage &lt; $maxPage">
			<xsl:call-template name="pageLink">
				<xsl:with-param name="count" select="$count"/>
				<xsl:with-param name="page"  select="$currPage + 1"/>
				<xsl:with-param name="label" select="/root/gui/strings/next"/>
			</xsl:call-template>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template name="pageLoop">
		<xsl:param name="count"/>
		<xsl:param name="minPage"/>
		<xsl:param name="currPage"/>
		<xsl:param name="maxPage"/>
		
		<xsl:if test="$minPage &lt;= $maxPage">
			<xsl:choose>
				<xsl:when test="$minPage = $currPage">
					<b><xsl:value-of select="$minPage"/></b>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="pageLink">
						<xsl:with-param name="count" select="$count"/>
						<xsl:with-param name="page"  select="$minPage"/>
						<xsl:with-param name="label" select="$minPage"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			&#160;
			<xsl:call-template name="pageLoop">
				<xsl:with-param name="minPage" select="$minPage + 1"/>
				<xsl:with-param name="currPage" select="$currPage"/>
				<xsl:with-param name="maxPage" select="$maxPage"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="pageLink">
		<xsl:param name="count"/>
		<xsl:param name="page"/>
		<xsl:param name="label"/>
		
		<xsl:variable name="from" select="($page - 1) * $hitsPerPage + 1"/>
		<xsl:variable name="to">
			<xsl:choose>
				<xsl:when test="$count &lt; $from + $hitsPerPage - 1">
					<xsl:value-of select="$count"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$from + $hitsPerPage - 1"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a href="{/root/gui/locService}/main.present?from={$from}&amp;to={$to}"><xsl:value-of select="$label"/></a>
	</xsl:template>


	<!-- Display rating information -->
	<xsl:template name="score">
		<xsl:param name="score"/>
		<xsl:param name="class"/>
		<xsl:param name="currentClass"/>
		<xsl:param name="interval" select="100 div $class"/>
		<xsl:param name="value" select="100 - $interval * $currentClass"/>

		<xsl:choose>
			<xsl:when test="$score &gt;= $value">		
				<img src="{/root/gui/url}/images/score.png" title="{floor($score)}%" alt="{floor($score)}%"/>
			</xsl:when>
			<xsl:otherwise>
				<img src="{/root/gui/url}/images/scoreno.png" title="{floor($score)}%" alt="{floor($score)}%"/>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="$currentClass &lt; $class - 1">			
				<xsl:call-template name="score">
					<xsl:with-param name="score" select="$score"/>
					<xsl:with-param name="class" select="$class"/>
					<xsl:with-param name="currentClass" select="$currentClass + 1"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	
	</xsl:template>
</xsl:stylesheet>
