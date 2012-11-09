<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:exslt="http://exslt.org/common" 
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	exclude-result-prefixes="geonet exslt java">
	
	<xsl:include href="utils.xsl"/>
	<xsl:include href="text-utilities.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<xsl:variable name="pageRange"   select="5"/>
	<xsl:variable name="hitsPerPage">
		<xsl:choose>
			<xsl:when test="/root/gui/searchDefaults/hitsPerPage"><xsl:value-of select="string(/root/gui/searchDefaults/hitsPerPage)"/></xsl:when>
			<xsl:otherwise>10</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="remote" select="/root/response/summary/@type='remote'"/>
	
	<!-- ================================================================================== -->
	<!-- page content -->
	<!-- ================================================================================== -->

	<xsl:template match="/">
		<xsl:comment>CONTENT</xsl:comment>
		<div id="search-results-content">		
		<!--table  width="100%" height="100%" id="search-results-content"-->

			<xsl:variable name="error" select="/root/response/summary/@status='Failure'"/>
			<xsl:variable name="count" 	select="/root/response/summary/@count"/>
			<xsl:variable name="from" 		select="/root/response/@from"/>
			<xsl:variable name="to" 		select="/root/response/@to"/>
			<xsl:variable name="currPage" select="floor(($from - 1) div $hitsPerPage + 1)"/>
			<xsl:variable name="pages" 	select="floor(($count - 1) div $hitsPerPage + 1)"/>

			<!-- title -->
			<xsl:call-template name="formTitle">
				<xsl:with-param name="title">
					<xsl:choose>
            <xsl:when test="$error and $remote" >
              <font class="error">
                <xsl:text>Remote Search Failed!</xsl:text>
              </font>
            </xsl:when>
            <xsl:otherwise>
							<xsl:value-of select="/root/gui/strings/resultsMatching"/>
							&#160;
							<xsl:value-of select="$from"/>-<xsl:value-of select="$to"/>/<xsl:value-of select="$count"/>
							&#160;
							(page <xsl:value-of select="$currPage"/>/<xsl:value-of select="$pages"/>)
							<xsl:if test="$remote=false()">
								,&#160;
								<span id="nbselected">
								<xsl:choose>
									<xsl:when test="/root/response/@selected">
										<xsl:value-of select="/root/response/@selected"/> 
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="count(//geonet:info[selected='true'])"/>
									</xsl:otherwise>                            
								</xsl:choose>
								</span> <xsl:value-of select="/root/gui/strings/selected"/> 
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>

			<!-- list of metadata -->
			<xsl:call-template name="hits"/>

			<!-- page list -->
			<!--			<xsl:call-template name="formSeparator"/> -->
			<xsl:call-template name="formContent">
				<xsl:with-param name="content">
					<xsl:call-template name="pageList"/>
				</xsl:with-param>
				<xsl:with-param name="indent" select="50"/>
			</xsl:call-template>

			<!--tr><td class="blue-content" colspan="3"/></tr>
		</table-->
		</div>
	</xsl:template>

	<!-- ================================================================================== -->

	<xsl:template name="formTitle">
		<xsl:param name="title"/>
		<xsl:param name="indent" select="100"/>
		
		<!--tr-->
<!--			<td class="padded-content" width="{$indent}"/>-->
			<!--td/>
			<td class="padded-content"-->
			<div class="results_header">
				<div class="results_title" style="float:left;">
					<xsl:copy-of select="$title"/>
					
				</div>		
				<xsl:if test="/root/response/summary/@count > 0">
					<!-- filtered search, batch actions and print pdf - - -->
					<div style="float:right;">	
					 <xsl:if test="$remote=false()">
						<xsl:value-of select="/root/gui/strings/select" />
						<xsl:choose>
							<xsl:when test="number(/root/response/summary/@count) > number(/root/gui/config/selectionmanager/maxrecords)">
								<xsl:variable name="errorMess" select="concat(/root/gui/strings/selectAllNotPossible,/root/gui/config/selectionmanager/maxrecords,'.')"/>
								<a href="javascript:alert('{$errorMess}')" title="{$errorMess}" alt="{$errorMess}">
									<xsl:value-of select="/root/gui/strings/all"/>
								</a>,
							</xsl:when>
							<xsl:otherwise>
								<a href="javascript:metadataselect(0, 'add-all')" title="{/root/gui/strings/selectAll}" alt="{/root/gui/strings/selectAll}">
									<xsl:value-of select="/root/gui/strings/all"/>
								</a>,
							</xsl:otherwise>
						</xsl:choose>
						<a href="javascript:metadataselect(0, 'remove-all')" title="{/root/gui/strings/selectNone}" alt="{/root/gui/strings/selectNone}">
							<xsl:value-of select="/root/gui/strings/none"/>
						</a>
						&#160;
						<!-- Add other actions list on selected metadata     -->
						<button id="oAcOs" name="oAcOs" class="content" onclick="actionOnSelect('{/root/gui/strings/noSelectedMd}')" style="width:220px;" title="{/root/gui/strings/otherActions}">
							<img id="oAcOsImg" name="oAcOsImg" src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/>
							<xsl:value-of select="/root/gui/strings/actionOnSelect"/>
						</button>
		
						<div id="oAcOsEle" name="oAcOsEle" class="oAcEle" style="display:none;" onClick="oActions('oAcOs');">
						<xsl:if test="java:isAccessibleService('metadata.batch.delete')">
							<button onclick="batchOperation('metadata.batch.delete','{/root/gui/strings/batchDeleteTitle}',600,
							    replaceStringParams('{/root/gui/strings/confirmBatchDelete}',[$('nbselected').innerHTML]))">
								<xsl:value-of select="/root/gui/strings/delete"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.newowner')">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.newowner.form','{/root/gui/strings/batchNewOwnerTitle}',800)">
								<xsl:value-of select="/root/gui/strings/newOwner"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.update.categories') and /root/gui/config/category">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.category.form','{/root/gui/strings/batchUpdateCategoriesTitle}',300, null, 400)">
								<xsl:value-of select="/root/gui/strings/updateCategories"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.update.privileges')">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.admin.form','{/root/gui/strings/batchUpdatePrivilegesTitle}',800, null, 400)">
								<xsl:value-of select="/root/gui/strings/updatePrivileges"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.update.status')">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.status.form','{/root/gui/strings/batchUpdateStatusTitle}',800, null)">
								<xsl:value-of select="/root/gui/strings/updateStatus"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.version') and /root/gui/svnmanager/enabled='true'">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.version','{/root/gui/strings/batchStartVersionTitle}',600, null)">
								<xsl:value-of select="/root/gui/strings/startVersion"/>
							</button>
						</xsl:if>
						<xsl:if test="java:isAccessibleService('metadata.batch.extract.subtemplates')">
						<!--xsl:text>&#160;</xsl:text-->
							<button onclick="batchOperation('metadata.batch.extract.subtemplates.form','{/root/gui/strings/batchExtractSubtemplatesTitle}',800, null)">
								<xsl:value-of select="/root/gui/strings/extractSubtemplates"/>
							</button>
						</xsl:if>
							<button onclick="gn_filteredSearch()"><xsl:value-of select="/root/gui/strings/selectedOnly"/></button>
							<button onclick="runPdfSearch(true);" alt="{/root/gui/strings/savepdf}" title="{/root/gui/strings/savepdf}"><xsl:value-of select="/root/gui/strings/printSelection"/></button>
							<button onclick="load('{/root/gui/locService}/mef.export?uuid=&amp;format=full&amp;version=2')"><xsl:value-of select="/root/gui/strings/export"/></button>
                            <button onclick="runCsvSearch()"><xsl:value-of select="/root/gui/strings/exportText"/></button>
						</div>
					 </xsl:if>
					 <xsl:if test="$remote=true()">
						<a href="#" onclick="runRemoteSearch('pdf');"><img align="absmiddle" src="{/root/gui/url}/images/pdf.gif" alt="{/root/gui/strings/savepdf}" title="{/root/gui/strings/savepdf}"/></a>
					 </xsl:if>
						
						<xsl:if test="/root/response/summary/@count > 1 and $remote=false()">
							<div style="margin-top:10px;" align="right">
							&#xA0;<xsl:value-of select="/root/gui/strings/sortBy"/>&#xA0;
							
							<!-- sort by - - - - - - - - - - - - - - - - - - - - -->
										
							<select id="sortBy.live" size="1" class="content" onChange="setSortAndSearch()">
								<xsl:for-each select="/root/gui/strings/sortByType">
									<option value="{@id}">
										<xsl:if test="@id = /root/gui/searchDefaults/sortBy">
											<xsl:attribute name="selected"/>
										</xsl:if>
										<xsl:value-of select="."/>
									</option>
								</xsl:for-each>
							</select>
							</div>
						</xsl:if>			
					</div>
				</xsl:if>

			</div>		
			<!--/td>
		</tr-->
	</xsl:template>
	
	<!-- ================================================================================== -->

	<xsl:template name="formSeparator">
		<xsl:comment>SEPARATOR</xsl:comment>
		<tr><td colspan="3"/></tr>
		<xsl:comment>SEPARATOR END</xsl:comment>
	</xsl:template>
	
	<!-- ================================================================================== -->

	<xsl:template name="formFiller">
		<xsl:param name="indent" select="100"/>
		<xsl:comment>FILLER</xsl:comment>
		
		<tr height="100%">
<!--			<td class="padded-content" width="{$indent}"/>
-->			<td/>
			<td class="padded-content">
			</td>
		</tr>
		<xsl:comment>FILLER END</xsl:comment>
	</xsl:template>	
	
	<!-- ================================================================================== -->

	<xsl:template name="formContent">
		<xsl:param name="content"/>
		<xsl:param name="indent" select="100"/>
		
		<xsl:comment>formContent BEGIN</xsl:comment>
		<!--tr-->
<!--			<td class="padded-content" width="{$indent}"/>
-->			<!--td/>
			<td class="padded-content" align="center"-->
			<div style=" width: 100%; margin-top: 5px;">
				<xsl:copy-of select="$content"/>
			</div>
			<!--/td>
		</tr-->
		<xsl:comment>formContent END</xsl:comment>
	</xsl:template>
	
	<!-- ================================================================================== -->
	<!-- all presented hits -->
	<!-- ================================================================================== -->

	<xsl:template name="hits">
		<xsl:comment>HITS</xsl:comment>		
		<xsl:for-each select="/root/response/*[name(.)!='summary']">

<!-- cope with errors - usually from Z servers with badly formed XML -->

		<xsl:choose>
			<xsl:when test="name(.)='error'">
				<xsl:call-template name="formSeparator"/>
				<xsl:call-template name="formContent">
					<xsl:with-param name="content">
						<xsl:choose>
							<xsl:when test="@server">
								<xsl:variable name="collection" select="@collection"/>
								<xsl:variable name="repocode" select="substring-before(@server,':')"/>
								<xsl:variable name="name" select="/root/gui/repositories/z3950repositories/repository[id/@code=$collection and id/serverCode=$repocode]/label"/>
									<font class="error"><xsl:value-of select="@id"/><xsl:value-of select="/root/gui/strings/metadataBadResponse1"/><xsl:text> </xsl:text><xsl:value-of select="@server"/><xsl:text> </xsl:text>(<i><xsl:value-of select="$name"/></i>).<xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/metadataBadResponse2"/></font>
							</xsl:when>
							<xsl:otherwise>
								<div align="left">
									<font class="error"><xsl:value-of select="@message"/></font>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>

<!-- normal records -->

			<xsl:otherwise>
				<xsl:variable name="md">
					<xsl:apply-templates mode="brief" select="."/>
				</xsl:variable>
			
				<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
				<xsl:call-template name="formContent">
					<xsl:with-param name="content">
						<xsl:call-template name="hit">
							<xsl:with-param name="metadata" select="$metadata"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="indent" select="50"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

		</xsl:for-each>
	</xsl:template>
	
	<!-- ================================================================================== -->
	<!-- one hit -->
	<!-- ================================================================================== -->

	<xsl:template name="hit">
		<xsl:param name="metadata"/>

		<xsl:comment>Single metadata in results</xsl:comment>
		<div class="hit">
				
			<!-- info -->
			<div class="hitdescription">			
				<div class="hittext">
					<xsl:if test="/root/gui/searchDefaults/output = 'full'">
						<div class="thumbnail_results">
						
							<!-- Category icon -->

							<xsl:if test="/root/gui/config/category/display-in-search"> 
									<xsl:choose> 
										<xsl:when test="/root/gui/config/category/display-in-search/@mode = 'all' and count($metadata/geonet:info/category)>0">
											<div style="float: right;;width:18px;">
												<xsl:for-each select="$metadata/geonet:info/category">
													<img class="category" src="../../images/category/{.}.png"/><br/>
												</xsl:for-each>
											</div>
										</xsl:when>
										<xsl:when test="/root/gui/config/category/display-in-search/@mode = 'internal' and count($metadata/geonet:info/category[@internal])>0">
											<div style="float: right;;width:18px;">
												<xsl:for-each select="$metadata/geonet:info/category[@internal]">
													<img class="category" src="../../images/category/{.}.png"/><br/>
												</xsl:for-each>
											</div>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="count($metadata/geonet:info/category[not(@internal)])>0">
												<div style="float: right;;width:18px;">
													<xsl:for-each select="$metadata/geonet:info/category[not(@internal)]">
														<img class="category" src="../../images/category/{.}.png"/><br/>
													</xsl:for-each> 
												</div>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
							</xsl:if>

							<!-- metadata rating -->
							
							<xsl:call-template name="rating">
								<xsl:with-param name="info" select="$metadata/geonet:info"/>
							</xsl:call-template>
						
							<br/> <!-- metadata thumbnail -->
						
							<xsl:call-template name="thumbnail">
								<xsl:with-param name="metadata" select="$metadata"/>
							</xsl:call-template>
						</div>
					</xsl:if>
					<div class="hittext_top">
						<xsl:choose>
							<xsl:when test="$remote=true()">
								<xsl:variable name="host" select="substring-before($metadata/geonet:info/server,':')"/>
								<xsl:variable name="rest" select="substring-after($metadata/geonet:info/server,':')"/>
								<xsl:variable name="port" select="substring-before($rest,'/')"/>
								<xsl:variable name="db" select="substring-after($rest,'/')"/>
								<!-- Title -->
								<div class="arrow">
									<xsl:choose>
										<xsl:when test="/root/gui/config/search/use-separate-window-for-editor-viewer">
                      <span onclick="popEditorViewer('{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=simple','{$metadata/geonet:info/id}')" style="cursor:hand;cursor:pointer;text-decoration:underline;"><xsl:value-of select="concat($metadata/geonet:info/id,' - ',$metadata/title)"/></span>
										</xsl:when>
										<xsl:otherwise>
											<a href="{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=simple">
												<xsl:value-of select="concat($metadata/geonet:info/id,' - ',$metadata/title)"/>
											</a>
										</xsl:otherwise>
									</xsl:choose>
								</div>
								<div class="hittext_middle">
									<xsl:variable name="repocode" select="substring-before($metadata/geonet:info/server,':')"/>
                  <xsl:variable name="collection" select="$metadata/geonet:info/collection"/>
									<xsl:variable name="name" select="/root/gui/repositories/z3950repositories/repository[id/@code=$collection and id/@serverCode=$repocode]/label"/>
									<font class="error"><xsl:value-of select="$name"/></font>
								</div>
							</xsl:when>
							<xsl:otherwise>
								<!-- Attribution logo -->
								<div class="attributionlogo">
									<xsl:variable name="source" select="string($metadata/geonet:info/source)"/>
									<xsl:choose>
										<!-- //FIXME does not point to baseURL yet -->
										<xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
											<a href="{/root/gui/sources/record[string(siteid)=$source]/baseURL}" target="_blank">
												<img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
											</a>
										</xsl:when>
										<xsl:otherwise>
											<img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
										</xsl:otherwise>
									</xsl:choose>
								</div>

								<!-- Save and/or convert XML for use in applications -->
								<div class="hitexport">
									<xsl:call-template name="showMetadataExportIcons"/>
								</div>

								<!-- Title -->
								<div class="hittitle">
									<xsl:variable name="isSelected" select="$metadata/geonet:info/selected" />
									<xsl:choose>
										<xsl:when test="$isSelected='true'">	
											<input class="checkbox_results"  type="checkbox" id="chk{geonet:info/id}" name="chk{geonet:info/id}" onclick="javascript:metadataselect('{geonet:info/uuid}', this.checked)"  checked="true"/>
										</xsl:when>
										<xsl:otherwise>
											<input class="checkbox_results" type="checkbox" onclick="javascript:metadataselect('{geonet:info/uuid}', this.checked)"/>				
										</xsl:otherwise>
									</xsl:choose>
									<!-- <input id="selId" name="{$metadata/geonet:info/id}" type="checkbox" /> -->
									<xsl:choose>
										<xsl:when test="/root/gui/config/search/use-separate-window-for-editor-viewer">
											<span onclick="popEditorViewer('{/root/gui/locService}/metadata.show?id={$metadata/geonet:info/id}&amp;currTab={/root/gui/env/metadata/defaultView}','{$metadata/geonet:info/id}')" style="cursor:hand;cursor:pointer;text-decoration:underline;"><xsl:value-of select="$metadata/title"/></span>
										</xsl:when>
										<xsl:otherwise>
											<a href="metadata.show?id={$metadata/geonet:info/id}&amp;currTab={/root/gui/env/metadata/defaultView}">
												<xsl:value-of select="$metadata/title"/>
											</a>
										</xsl:otherwise>
									</xsl:choose>
								</div>

							</xsl:otherwise>
						</xsl:choose>
					</div>
					
				  <xsl:if test="/root/gui/searchDefaults/output = 'full'">
					<!-- abstract -->
					<xsl:if test="$metadata/abstract">
						<div class="hittext_middle">
							<div class="caption"><xsl:value-of select="/root/gui/strings/abstract"/></div>
							<div class="abstract1">
								<xsl:choose>
									<!-- show a maximum of $maxAbstract characters in the abstract -->
									<xsl:when test="string-length ($metadata/abstract) &gt; $maxAbstract">
										<xsl:value-of select="substring ($metadata/abstract, 0, $maxAbstract)"/>...
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$metadata/abstract"/>
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</xsl:if>
				  </xsl:if>
				  
					<!-- keywords -->
					<xsl:if test="$metadata/keyword">
						<xsl:variable name="keywords">
						  <xsl:for-each select="$metadata/keyword">
								<xsl:if test="position() &gt; 1">,  </xsl:if>
								<xsl:value-of select="."/>
							</xsl:for-each>
						</xsl:variable>

						<div class="hittext_keywords">
							<div class="caption"><xsl:value-of select="/root/gui/strings/keywords"/></div>
							<div class="keywords">
								<xsl:choose>
									<!-- show a maximum of $maxKeywords characters in the keywords -->
									<xsl:when test="string-length ($keywords) &gt; $maxKeywords">
										<xsl:value-of select="substring ($keywords, 0, $maxKeywords)"/>...
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$keywords"/>
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</xsl:if>

				  <xsl:if test="/root/gui/searchDefaults/output = 'full'">
					<!-- schema -->
					<xsl:if test="$metadata/geonet:info/schema">
						<div class="hittext_middle">
							<div class="caption"><xsl:value-of select="/root/gui/strings/schema"/></div>
							<div class="abstract1">
								<xsl:value-of select="$metadata/geonet:info/schema"/>
							</div>
						</div>
					</xsl:if>
				  
					<xsl:if test="normalize-space($metadata/geoBox) or $metadata/temporalExtent">
						<div class="hittext_middle">
							<div class="caption"><xsl:value-of select="/root/gui/strings/extent"/></div>
							<div class="abstract1">
								<xsl:if test="normalize-space($metadata/geoBox)">
									<xsl:call-template name="boundingBox">
										<xsl:with-param name="geoBox" select="$metadata/geoBox"/>
									</xsl:call-template>
								</xsl:if>
								<xsl:if test="$metadata/geoBox and $metadata/temporalExtent">
									&#160;&#160;
								</xsl:if>
								<xsl:if test="$metadata/temporalExtent">
									<xsl:call-template name="timeSpan">
										<xsl:with-param name="temporalExtent" select="$metadata/temporalExtent"/>
									</xsl:call-template>
								</xsl:if>
							</div>
						</div>
					</xsl:if>
				  </xsl:if>
				  
				</div>
			</div>
			
			<!-- some ownership info -->
			<xsl:if test="$remote=false() and $metadata/geonet:info/isHarvested = 'n' and /root/gui/session/userId!=''">
				<div class="ownership">
					<span class="owner"><xsl:value-of select="concat(/root/gui/strings/owner,': ',$metadata/geonet:info/ownername)"/></span>
					&#160;
					<xsl:choose>
						<xsl:when test="$metadata/geonet:info/owner='true'">
								<img src="{/root/gui/url}/images/owner.png" title="{/root/gui/strings/ownerRights}"/>
						</xsl:when>
						<xsl:otherwise>
								<img src="{/root/gui/url}/images/notowner.png" title="{/root/gui/strings/noOwnerRights}"/>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</xsl:if>
			
			<!-- buttons -->
			<!-- Here's where the buttons are created that are shown in the search results. The buttons are:
			View metadata
			Download resources
			View interactive map in InterMap
			View interactive map in Google Earth
			-->
			
			<div class="buttons">
				<div class="buttonsleft">
					<xsl:if test="not(/root/gui/config/search/use-separate-window-for-editor-viewer)">
						<!-- view metadata button -->
						<xsl:if test="$metadata/geonet:info/view='true'">
							<xsl:choose>
								<xsl:when test="$remote=true()">
									<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=simple')" title="{/root/gui/strings/show}">
										<xsl:value-of select="/root/gui/strings/show"/>
									</button>
								</xsl:when>
								<xsl:otherwise>
									<button id="gn_showmd_{$metadata/geonet:info/id}"  class="content" onclick="gn_showMetadata({$metadata/geonet:info/id})" title="{/root/gui/strings/show}">
										<img src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/><xsl:value-of select="/root/gui/strings/show"/>
									</button>
									<button id="gn_hidemd_{$metadata/geonet:info/id}"  class="content" onclick="gn_hideMetadata({$metadata/geonet:info/id})" style="display:none;" title="{/root/gui/strings/show}">
										<img src="{/root/gui/url}/images/minus.png" style="padding-right:3px;"/><xsl:value-of select="/root/gui/strings/show"/>
									</button>
									<button id="gn_loadmd_{$metadata/geonet:info/id}"  class="content" style="display:none;" title="{/root/gui/strings/show}">
										<xsl:value-of select="/root/gui/strings/loading"/>
									</button>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:if>
		
					<!-- download data button -->
					<xsl:choose>
						<!-- add download button if have download privilege and downloads are available -->
						<xsl:when test="$metadata/geonet:info/download='true' and count($metadata/link[@type='download'])>0">
							<xsl:call-template name="download-button">
								<xsl:with-param name="metadata" select="$metadata"/>
								<xsl:with-param name="remote" select="$remote"/>
							</xsl:call-template>
						</xsl:when>
						<!-- or when the metadata has associated data url's -->
						<xsl:when test="count($metadata/link[@type='dataurl'])>0">
							<xsl:call-template name="download-button">
								<xsl:with-param name="metadata" select="$metadata"/>
								<xsl:with-param name="remote" select="$remote"/>
							</xsl:call-template>
							<!-- notify whether additional downloads would be available if logged in -->
							<xsl:if test="$metadata/geonet:info/guestdownload='true' and 
													/root/gui/session/userId='' and
													count($metadata/link[@type='download'])>0">
								&#160;
								<xsl:copy-of select="/root/gui/strings/guestDownloadExtra/node()"/>
							</xsl:if>
						</xsl:when>
						
						<!-- or notify that downloads would be available if logged in when downloads available to GUEST -->
						<xsl:when test="$metadata/geonet:info/guestdownload='true' and 
													/root/gui/session/userId='' and
													count($metadata/link[@type='download'])>0">
							&#160;
							<xsl:copy-of select="/root/gui/strings/guestDownload/node()"/>
						</xsl:when>
					</xsl:choose>

					<!-- dynamic map button -->
					<xsl:if test="$metadata/geonet:info/dynamic='true'">
						&#160;
						<xsl:variable name="count" select="count($metadata/link[@type='arcims']) + count($metadata/link[@type='wms'])"/>
						<xsl:choose>
							<xsl:when test="$count>1">
								<xsl:choose>
									<xsl:when test="$remote=true()">
										<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=distribution')" title="{/root/gui/strings/interactiveMap}"><xsl:value-of select="/root/gui/strings/interactiveMap"/></button>
									</xsl:when>
									<xsl:otherwise>
										<button id="gn_showinterlist_{$metadata/geonet:info/id}"  class="content" onclick="gn_showInterList({$metadata/geonet:info/id})" title="{/root/gui/strings/interactiveMap}">
											<img src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/><xsl:value-of select="/root/gui/strings/interactiveMap"/>
										</button>
										<button id="gn_hideinterlist_{$metadata/geonet:info/id}"  class="content" onclick="gn_hideInterList({$metadata/geonet:info/id})" style="display:none;" title="{/root/gui/strings/interactiveMap}">
											<img src="{/root/gui/url}/images/minus.png" style="padding-right:3px;"/><xsl:value-of select="/root/gui/strings/interactiveMap"/>
										</button>
										<button id="gn_loadinterlist_{$metadata/geonet:info/id}"  class="content" style="display:none;" title="{/root/gui/strings/interactiveMap}">
											<xsl:value-of select="/root/gui/strings/loading"/>
										</button>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="$count=1">
								<button class="content" onclick="{$metadata/link[@type='arcims' or @type='wms']}" title="{/root/gui/strings/interactiveMap}">
									<xsl:value-of select="/root/gui/strings/interactiveMap"/>
								</button>

								<!-- View WMS in Google Earth map button -->
								<xsl:if test="$metadata/link[@type='googleearth']">
									&#160;
									<a onclick="load('{$metadata/link[@type='googleearth']}')" style="vertical-align: middle;cursor: pointer;">
										<img src="{/root/gui/url}/images/google_earth_link.gif" height="20px" width="20px" style="padding-left:3px;" alt="{/root/gui/strings/viewInGE}" title="{/root/gui/strings/viewInGE}"/>
									</a>
								</xsl:if>
							</xsl:when>
						</xsl:choose>
					</xsl:if>

				</div>
			
				<div class="buttonsright">
					<xsl:choose>
						<xsl:when test="/root/gui/config/search/use-separate-window-for-editor-viewer">
							<xsl:call-template name="buttons">
								<xsl:with-param name="metadata" select="$metadata"/>
								<xsl:with-param name="ownerbuttonsonly" select="true()"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="buttons">
								<xsl:with-param name="metadata" select="$metadata"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</div>
			</div>


			</div>
		
					<div class="whiteboard">
				<!-- spare room for metdata display via AJAX -->
				<div id="ilwhiteboard_{$metadata/geonet:info/id}" width="100%" style="display:none;">dummy string, or FF will nest next DIV into this one</div>
				<div id="mdwhiteboard_{$metadata/geonet:info/id}" width="100%">&#160;</div>
<!--				<div id="briefmd"><xsl:copy-of select="$metadata"/></div> -->
			</div>
	</xsl:template>
	
	<xsl:template name="download-button">
		<xsl:param name="metadata"/>
		<xsl:param name="remote"/>
		
		&#160;
		<xsl:choose>
			<xsl:when test="$remote=false()">
			    <xsl:variable name="dataDownloads" select="count($metadata/link[@type='download' and not(ends-with(@protocol,'downloadother'))]|$metadata/link[@type='dataurl'])"/>
			    <xsl:choose>
			    	<xsl:when test="$dataDownloads>0">
						<button class="content" onclick="javascript:runFileDownloadSummary('{$metadata/geonet:info/uuid}','{/root/gui/strings/downloadSummary}')" type="button">
							<xsl:value-of select="/root/gui/strings/dataDownload"/>
						</button>
					</xsl:when>
			    	<xsl:otherwise>
						<button class="content" onclick="javascript:runFileDownloadSummary('{$metadata/geonet:info/uuid}','{/root/gui/strings/downloadSummary}')" type="button">
							<xsl:value-of select="/root/gui/strings/download"/>
						</button>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="/root/gui/config/search/use-separate-window-for-editor-viewer">
						<button class="content" onclick="popEditorViewer('{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=distribution','{$metadata/geonet:info/id}')" title="{/root/gui/strings/download}">
							<xsl:value-of select="/root/gui/strings/download"/>
						</button>
					</xsl:when>
					<xsl:otherwise>
						<button class="content" onclick="load('{/root/gui/locService}/remote.show?id={$metadata/geonet:info[server]/id}&amp;currTab=distribution','{$metadata/geonet:info/id}')" title="{/root/gui/strings/download}">
							<xsl:value-of select="/root/gui/strings/download"/>
						</button>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="boundingBox">
		<xsl:param name="geoBox"/>
		
		<xsl:value-of select="/root/gui/strings/geographicExtent"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="/root/gui/strings/westBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="$geoBox/westBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="/root/gui/strings/southBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="$geoBox/southBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="/root/gui/strings/eastBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="$geoBox/eastBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="/root/gui/strings/northBL"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="$geoBox/northBL"/>
	</xsl:template>
	
	<xsl:template name="timeSpan">
		<xsl:param name="temporalExtent"/>
	
		<xsl:variable name="label" select="/root/gui/strings/temporalExtent"/>
		<xsl:variable name="separator" select="/root/gui/strings/rangeSeparator"/>
		
		<xsl:for-each select="$temporalExtent">
			<xsl:value-of select="$label"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="begin"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="$separator"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="end"/>
			&#160;&#160;
		</xsl:for-each>
	</xsl:template>
	
	<!-- ================================================================================== -->
	<!-- list of pages -->
	<!-- ================================================================================== -->

	<xsl:template name="pageList">
		<xsl:comment>PAGELIST</xsl:comment>
		
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
		
		<div class="pageList"><b><xsl:value-of select="/root/gui/strings/resultPage"/></b>
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
		</div>
	</xsl:template>
	
	<!-- ================================================================================== -->

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

	<!-- ================================================================================== -->

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
<!--		<a href="{/root/gui/locService}/main.present?from={$from}&amp;to={$to}"><xsl:value-of select="$label"/></a>-->
		<a href="javascript:gn_present({$from}, {$to});"><xsl:value-of select="$label"/></a>
	</xsl:template>

	<!-- ================================================================================== -->
	<!-- Display rating information -->

	<xsl:template name="rating">
		<xsl:param name="info"/>

		<xsl:if test="/root/gui/config/rating">
								
			<xsl:variable name="id"     select="$info/id"/>
			<xsl:variable name="rating" select="$info/rating"/>
			
			<xsl:if test="$info/isHarvested = 'n' or $info/harvestInfo/type = 'geonetwork'">
				<a id="rating.link.{$id}" style="cursor:pointer; padding-left:10px;" onClick="showRatingPopup({$id})"
					alt="{/root/gui/strings/rateIt}" title="{/root/gui/strings/rateIt}">
					<xsl:call-template name="showRating">
						<xsl:with-param name="rating" select="$rating"/>
					</xsl:call-template>
				</a>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	
	<!-- ================================================================================== -->
	
	<xsl:template name="showRating">
		<xsl:param name="rating"/>
		<xsl:param name="currRating" select="$rating"/>
		
		<xsl:choose>
			<xsl:when test="$currRating &gt; 0">		
				<img src="{/root/gui/url}/images/score.png" />
				
				<xsl:call-template name="showRating">
					<xsl:with-param name="rating"     select="$rating"/>
					<xsl:with-param name="currRating" select="$currRating -1"/>
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:call-template name="showRatingDiff">
					<xsl:with-param name="diff" select="5 - $rating"/>
				</xsl:call-template>				
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>
	
	<!-- ================================================================================== -->
	
	<xsl:template name="showRatingDiff">
		<xsl:param name="diff"/>
							
		<xsl:if test="$diff &gt; 0">
			<img src="{/root/gui/url}/images/scoreno.png" />
			
			<xsl:call-template name="showRatingDiff">
				<xsl:with-param name="diff" select="$diff -1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- ================================================================================== -->

</xsl:stylesheet>
