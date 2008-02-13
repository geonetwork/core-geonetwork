<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan" xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl xalan geonet">

	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	<xsl:include href="searchform_simple_template.xsl"/>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/slider.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/effects.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/controls.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/dragdrop.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/builder.js"/>

		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar-setup.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/lang/calendar-{/root/gui/language}.js"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/calendar/calendar-blue2.css" />
		
		
		<script type="text/javascript" src="{/root/gui/url}/scripts/gn_search.js?"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/gn_intermap.js?"/>

		<script type="text/javascript" src="/intermap/scripts/util.js?" />
		<script type="text/javascript" src="/intermap/scripts/im_extras.js?"/>
		<script type="text/javascript" src="/intermap/scripts/im_ajax.js?" />
		
		<script type="text/javascript" src="/intermap/scripts/im_class.js?"/>
		<script type="text/javascript" src="/intermap/scripts/im_minimap.js?"/>
		<script type="text/javascript" src="/intermap/scripts/im_bigmap.js?" />
		<script type="text/javascript" src="/intermap/scripts/im_integration.js?"/>
		
		<script type="text/javascript" src="/intermap/scripts/im_layers.js?" />

		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js?"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip-manager.js?"></script>
		
		<!--  FIXME move this line elsewhere.  -->
		<link rel="stylesheet" type="text/css" href="/intermap/intermap-embedded.css?" />
		
		<xsl:variable name="wmc"><xsl:copy-of select="/root/request/wmc"/></xsl:variable>
		
		<script type="text/javascript" language="JavaScript1.2">

			function init()
			{
				var currentSearch = get_cookie('search');
				if (currentSearch=='advanced') {
					showAdvancedSearch();
				} else {
					initSimpleSearch("<xsl:value-of select="$wmc"/>");
				}
			}
			
			var getGNServiceURL = function(service)
			{				
				return Env.locService+"/"+service;
			};
			
			var getIMServiceURL = function(service)
			{
				// FIXME: the "/intermap/" context should be parametrized
				return "/intermap/srv/"+Env.lang+"/"+service;
			};
					
			
<!--			function getWmsLayerInfo(name, url) 
			{
				window.open('/intermap/srv/en/map.service.wmsLayerInfo?url='+url+'&amp;name=' + name, 'dialog', 'HEIGHT=300,WIDTH=400,scrollbars=yes,toolbar=no,status=no,menubar=no,location=no,resizable=yes');
			}
			
			
			function goExtended(onoff, link)
			{
				document.search.intermap.value='off';			
				document.search.extended.value=onoff;
				document.search.action=link;
				document.search.submit();
			}
	
			function goRemote(onoff, link)
			{
				document.search.remote.value=onoff;
				document.search.action=link;
				document.search.submit();
			}

			function goIntermap(onoff, link)
			{
				document.search.intermap.value=onoff;
				document.search.action=link;
				document.search.submit();
			}

			function deselect(select)
			{
				for (var i=0; i &lt; select.length; i++)
				{
					select.options[i].selected = false;
				}
			}
	
			function profileSelected()
			{
				var serverList = document.search.profile.options[document.search.profile.selectedIndex].value;
				var serverArray = serverList.split(' ');
				deselectAllServers();
				for (var i=0; i &lt; serverArray.length; i++)
					selectServer(serverArray[i]);
			}
	
			function serverSelected()
			{
				document.search.profile.options[0].selected = true;
			}
	
			function deselectAllServers()
			{
				for (var i=0; i &lt; document.search.servers.length; i++)
					document.search.servers.options[i].selected = false;
			}
	
			function selectServer(server)
			{
				for (var i=0; i &lt; document.search.servers.length; i++)
					if (document.search.servers.options[i].value == server)
						document.search.servers.options[i].selected = true;
			}
-->		
			function checkSubmit()
			{
				if (document.search.remote.value == 'on')
				{
					if (isWhitespace(document.search.any.value) &amp;&amp;
						!(document.search.title    &amp;&amp; !isWhitespace(document.search.title.value)) &amp;&amp;
						!(document.search['abstract'] &amp;&amp; !isWhitespace(document.search['abstract'].value)) &amp;&amp;
						!(document.search.themekey &amp;&amp; !isWhitespace(document.search.themekey.value)))
					{
						alert("Please type some search criteria");
						return false;
					}
					servers = 0;
					for (var i=0; i &lt; document.search.servers.length; i++)
						if (document.search.servers.options[i].selected) servers++;
					if (servers == 0)
					{
						alert("Please select a server");
						return false;
					}
				}
				return true;
			}
			
			function doSubmit()
			{
				if (checkSubmit())
					document.search.submit();
			}
			
		</script>
	</xsl:template>


	<xsl:variable name="lang" select="/root/gui/language"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:comment xml:space="preserve">
			page content
		</xsl:comment>
		<xsl:choose>
			<xsl:when test="/root/gui/searchDefaults/intermap='on'">
				<xsl:call-template name="mapcontent"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="normalcontent"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="mapcontent">

		<h1 class="padded-content" style="margin-bottom: 0px;">
			<xsl:value-of select="/root/gui/strings/mainpageTitle"/>
		</h1>

		<xsl:call-template name="localization"/>
		
		<xsl:comment>FORM FIELDS</xsl:comment>
		<form name="search" action="{/root/gui/locService}/main.search" method="post"
			onsubmit="return checkSubmit()">
			<input name="extended" type="hidden" value="{/root/gui/searchDefaults/extended}"/>
			<input name="intermap" type="hidden" value="{/root/gui/searchDefaults/intermap}"/>
			<input name="remote" type="hidden" value="{/root/gui/searchDefaults/remote}"/>
			<input name="attrset" type="hidden" value="geo"/>
			<!-- FIXME: possibly replace with menu -->
			<input type="submit" style="display: none;"/>
		</form>

		<xsl:comment>MAIN CONTENT TABLE</xsl:comment>
		<table class="geosearchmain" width="100%">  <!--height="100%">-->

			<tr>
				<td colspan="2" height="0" style="height: 0px;" >
					<div id="advancedsearch" style="background-color: rgb(220, 225, 234);"/>															
				</td>
			</tr>
			
			<tr>

				<!-- search -->
				<xsl:comment>LEFT: search, minimap and news</xsl:comment>

				<td class="padded-content" width="224px" style="background-color:#dce1ea;">
					<table class="geosearch" width="224px">
						<!--  TODO: set a fixed width  -->
						<xsl:comment>SEARCH</xsl:comment>
						<tr>
							<td valign="top">
								<div id="simplesearch">
									<xsl:call-template name="geofields"/>
								</div>
							</td>
						</tr>
						<tr>
							<td valign="top">
								<table class="geosearchfields" width="100%">
									<tr>
										<td>
											<xsl:call-template name="categories"/>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr>
							<td valign="top">
								<table class="geosearchfields" width="100%">
									<tr>
										<td>
											<xsl:call-template name="latestUpdates"/>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</td>

				<xsl:comment>RIGHT: 1st time message, collapsable extended map, search results</xsl:comment>
				
				<td class="padded-content" width="100%">
					<table class="padded-content"  width="100%" height="100%" >
						
						<tr id="intermaprow"  width="100%" height="0">
							<xsl:comment>COLLAPSABLE MAP</xsl:comment>
							<td>
								<div id="fillMeWithIntermap" style="display: none;">
									<!--  This DIV will be filled dynamically with intermap contents -->
								</div>
							</td>
						</tr>
						
						<tr width="100%">
							<td width="100%">
								<h1 id="loadingMD" align="center" style="display: none;" width="100%"><xsl:value-of select="/root/gui/strings/searching"/></h1>
								
								<!-- This DIV contains a first-time message that will be removed when the first search will be run -->
								<div id="resultList">

									<table>
										<tr>
											<td>
												<xsl:comment>MAINPAGE 1</xsl:comment>
												<xsl:copy-of select="/root/gui/strings/mainpage1"/>
												<xsl:comment>MAINPAGE 2</xsl:comment>
												<xsl:copy-of select="/root/gui/strings/mainpage2"/> <a href="mailto:{/root/gui/env/feedback/email}">
													<xsl:value-of select="/root/gui/env/feedback/email"/>
												</a>
											</td>
										</tr>
											<xsl:if test="/root/gui/featured/*">
												<tr style="padding: 10px;">
													<td>
														<xsl:comment>Featured Map</xsl:comment>
														<xsl:call-template name="featured"/>
													</td>
												</tr>
											</xsl:if>
										<tr>
											<td>
												<xsl:comment>END MAINPAGE</xsl:comment>
												
											</td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
						
					</table>
				</td>

			</tr>
		</table>

	</xsl:template>


	<xsl:template name="normalcontent">
		<table width="100%" height="100%">
			<tr height="100%">

				<!-- search and purpose -->
				<td class="padded-content" width="70%" height="100%">
					<table width="100%" height="100%">
						<tr>

							<!-- search -->
							<td>
								<h1>
									<xsl:value-of select="/root/gui/strings/mainpageTitle"/>
								</h1>
								<form name="search" action="{/root/gui/locService}/main.search" method="post" onsubmit="return checkSubmit()">
									<input name="extended" type="hidden" value="{/root/gui/searchDefaults/extended}"/>
									<input name="intermap" type="hidden" value="{/root/gui/searchDefaults/intermap}"/>
									<input name="remote" type="hidden" value="{/root/gui/searchDefaults/remote}"/>
									<input name="attrset" type="hidden" value="geo"/>
									<!-- FIXME: possibly replace with menu -->
									<input type="submit" style="display: none;"/>
									<table width="100%" height="100%">
										<tr>
											<td valign="top">
												<xsl:call-template name="fields"/>
											</td>
											<td width="200">
												<table height="100%">
													<tr>
														<td class="padded" align="right">
															<button class="content-small" type="button" onclick="goIntermap('on','{/root/gui/locService}/main.home')">
																<xsl:value-of select="/root/gui/strings/intermapSearch"/>
															</button>
														</td>
													</tr>
													<tr>
														<td class="padded" align="right">
															<xsl:choose>
																<xsl:when test="/root/gui/searchDefaults/extended='off'">
																	<button class="content-small" type="button" onclick="goExtended('on','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/extended"/>
																	</button>
																</xsl:when>
																<xsl:otherwise>
																	<button class="content-small" type="button" onclick="goExtended('off','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/simple"/>
																	</button>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>
													<tr>
														<td class="padded" align="right">
															<xsl:choose>
																<xsl:when test="/root/gui/searchDefaults/remote='off'">
																	<button class="content-small" type="button" onclick="goRemote('on','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/remote"/>
																	</button>
																</xsl:when>
																<xsl:otherwise>
																	<button class="content-small" type="button" onclick="goRemote('off','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/local"/>
																	</button>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>
													<tr height="100%">
														<td align="right" valign="baseline">
															<a onclick="doSubmit()">
																<img onmouseover="this.src='{/root/gui/locUrl}/images/search-white.gif'" onmouseout="this.src='{/root/gui/locUrl}/images/search-blue.gif'" style="cursor:hand;cursor:pointer" src="{/root/gui/locUrl}/images/search-blue.gif" alt="Search" title="{/root/gui/strings/search}" align="top"/>
															</a>
															
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</form>
							</td>
						</tr>
						<tr>
							<td class="dots"/>
						</tr>

						<tr height="100%">
							<!-- Info -->
							<td valign="top">
								<xsl:copy-of select="/root/gui/strings/mainpage1"/>
								<xsl:copy-of select="/root/gui/strings/mainpage2"/>
								<a href="mailto:{/root/gui/env/feedback/email}">
									<xsl:value-of select="/root/gui/env/feedback/email"/>
								</a>
							</td>
						</tr>
					</table>
				</td>

				<td class="separator"/>

				<!-- right -->
				<td class="padded-content" valign="top">
					<center>
						<img src="{/root/gui/url}/images/intermap.gif" alt="InterMap" align="top"/>
					</center>
					<xsl:copy-of select="/root/gui/strings/interMapInfo"/>
				</td>
			</tr>

			<tr>
				<td class="separator"/>
			</tr>

			<!-- types -->
			<tr>
				<td colspan="3">
					<table width="100%">
						<tr>

							<xsl:choose>
								<xsl:when test="/root/gui/featured/*">

									<!-- featured map -->
									<td class="footer" align="center" valign="top" width="33%">
										<xsl:call-template name="featured"/>
									</td>

									<td class="separator"/>

									<!-- latest updates -->
									<td class="footer" align="left" valign="top" width="33%">
										<xsl:call-template name="latestUpdates"/>
									</td>

								</xsl:when>
								<xsl:otherwise>

									<!-- latest updates -->
									<td class="footer" align="left" valign="top" width="50%">
										<xsl:call-template name="latestUpdates"/>
									</td>

								</xsl:otherwise>
							</xsl:choose>

							<td class="separator"/>

							<!-- categories -->
							<td class="footer" align="left" valign="top">
								<xsl:call-template name="categories"/>
							</td>

						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!--
	featured map
	-->
	<xsl:template name="featured">
		<fieldset style="margin: 20px; background-color: #dce1ea;">
			<legend><xsl:value-of select="/root/gui/strings/featuredMap"/></legend>
			<table>
				<xsl:for-each select="/root/gui/featured/*">
					<xsl:variable name="md">
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:variable>
					<xsl:variable name="metadata" select="xalan:nodeset($md)/*[1]"/>
					<tr>
						<td>
							<h2>
								<div class="arrow" onClick="gn_showSingleMetadata('{geonet:info/id}');" 
				style="cursor:hand;cursor:pointer">
									<xsl:value-of select="$metadata/title"/>
								</div>
							</h2>
							<p/>
							<xsl:variable name="abstract" select="$metadata/abstract"/>
							<xsl:choose>
								<xsl:when test="string-length($abstract) &gt; $maxAbstract">
									<xsl:value-of select="substring($abstract, 0, $maxAbstract)"/>
										<div class="arrow" onClick="gn_showSingleMetadata('{geonet:info/id}');" 
				style="cursor:hand;cursor:pointer">...<xsl:value-of select="/root/gui/strings/more"/>...</div>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$abstract"/>
								</xsl:otherwise>
							</xsl:choose>
						</td>
						<td width="40%">
							<xsl:call-template name="thumbnail">
								<xsl:with-param name="metadata" select="$metadata"/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</fieldset>
	</xsl:template>

	<!--
	latest updates
	-->
	<xsl:template name="latestUpdates">
		<h1 align="left">
			<xsl:value-of select="/root/gui/strings/recentAdditions"/> &#160;&#160;&#160; 
			<a href="{/root/gui/locService}/rss.latest?georss=gml">
				<img style="cursor:hand;cursor:pointer" src="{/root/gui/url}/images/georss.png"
					alt="GeoRSS-GML" title="{/root/gui/strings/georss}" align="top"/>
			</a>
		</h1>
		<xsl:for-each select="/root/gui/latestUpdated/*">
			<xsl:variable name="md">
				<xsl:apply-templates mode="brief" select="."/>
			</xsl:variable>
			<xsl:variable name="metadata" select="xalan:nodeset($md)/*[1]"/>
			<div class="arrow" onClick="gn_showSingleMetadata('{geonet:info/id}');" 
				style="cursor:hand;cursor:pointer">
				<xsl:value-of select="$metadata/title"/>
				<br/>
			</div>
		</xsl:for-each>
	</xsl:template>

	<!--
	categories
	-->
	<xsl:template name="categories">
		<h1 align="left">
			<xsl:value-of select="/root/gui/strings/categories"/>
		</h1>
		<xsl:for-each select="/root/gui/categories/*">
			<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
			<xsl:variable name="categoryName" select="name"/>
			<xsl:variable name="categoryLabel" select="label/child::*[name() = $lang]"/>
			<div class="arrow" onClick="runCategorySearch('{$categoryName}');" style="cursor:hand;cursor:pointer">
				<xsl:value-of select="$categoryLabel"/>
				<br/>
			</div>
		</xsl:for-each>
	</xsl:template>

	<!--
	search fields
	-->
	<xsl:template name="fields">
		<table>
			<!-- Title -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/rtitle"/>
					</th>
					<td class="padded">
						<input class="content" name="title" size="30"
							value="{/root/gui/searchDefaults/title}"/>
					</td>
				</tr>
			</xsl:if>

			<!-- Abstract -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/abstract"/>
					</th>
					<td class="padded">
						<input class="content" name="abstract" size="30"
							value="{/root/gui/searchDefaults/abstract}"/>
					</td>
				</tr>
			</xsl:if>

			<!-- Any (free text) -->
			<tr>
				<th class="padded">
					<xsl:value-of select="/root/gui/strings/searchText"/>
				</th>
				<td class="padded">
					<input class="content" name="any" size="30"
						value="{/root/gui/searchDefaults/any}"/>
					<br/>
				</td>
			</tr>

			<!-- Keywords -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/keywords"/>
					</th>
					<td class="padded">
						<input class="content" id="themekey" name="themekey" size="30"
							value="{/root/gui/searchDefaults/themekey}"/>

						<a href="#">
							<img src="{/root/gui/url}/images/gdict.png" align="absmiddle"
								onclick="keywordSelector();"/>
						</a>

						<div id="keywordSelectorFrame" class="keywordSelectorFrame"
							style="display:none;">
							<div id="keywordSelector" class="keywordSelector"/>
						</div>

						<div id="keywordList" class="keywordList"/>
						<script type="text/javascript">
					  var keyordsSelected = false;

					  function addQuote (li){
					  $("themekey").value = '"'+li.innerHTML+'"';
					  }

					  function keywordSelector(){
						if ($("keywordSelectorFrame").style.display == 'none'){
							if (!keyordsSelected){
								new Ajax.Updater("keywordSelector","portal.search.keywords?mode=selector&amp;keyword="+$("themekey").value);
								keyordsSelected = true;
							}
							$("keywordSelectorFrame").style.display = 'block';
						}else{
							$("keywordSelectorFrame").style.display = 'none';
						}
					  }

					  function keywordCheck(k, check){
						k = '"'+ k + '"';
						//alert (k+"-"+check);
						if (check){	// add the keyword to the list
							if ($("themekey").value != '') // add the "or" keyword
								$("themekey").value += ' or '+ k;
							else
								$("themekey").value = k;
						}else{ // Remove that keyword
							$("themekey").value = $("themekey").value.replace(' or '+ k, '');
							$("themekey").value = $("themekey").value.replace(k, '');
							pos = $("themekey").value.indexOf(" or ");
							if (pos == 0){
								$("themekey").value = $("themekey").value.substring (4, $("themekey").value.length);
							}
						}
					  }

					  new Ajax.Autocompleter('themekey', 'keywordList', 'portal.search.keywords?',{paramName: 'keyword', updateElement : addQuote});
					</script>
					</td>
				</tr>
			</xsl:if>

			<!-- Fuzzy search similarity for text field only (ie. Keywords, Any, Abstract, Title) set to 80% by default -->
			<input class="content" id="similarity" name="similarity" type="hidden" value=".8"/>
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th>
						<xsl:value-of select="/root/gui/strings/fuzzy"/>
					</th>
					<td>
						<table>
							<tr>
								<td width="20px" align="center">-</td>
								<td>
									<div class="track" id="similarityTrack"
										style="width:100px;height:5px;">
										<xsl:attribute name="alt">
											<xsl:value-of select="/root/gui/strings/fuzzySearch"/>
										</xsl:attribute>
										<xsl:attribute name="title">
											<xsl:value-of select="/root/gui/strings/fuzzySearch"/>
										</xsl:attribute>
										<div class="handle" id="similarityHandle"
											style="width:5px;height:10px;"> </div>
									</div>
								</td>
								<td width="20px" align="center">+</td>
								<td>
									<div id="similarityDebug" style="display:none;"/>
								</td>
							</tr>
						</table>
						<script type="text/javascript" language="JavaScript1.2">
						var similaritySlider = new Control.Slider(
													'similarityHandle',
													'similarityTrack'
													,{range:$R(0,10),
														values:[0,1,2,3,4,5,6,7,8,9,10]}
													);
						similaritySlider.options.onSlide = function(v){
							$('similarity').value = (v/10);
							$('similarityDebug').innerHTML = '('+(v/10)+')';
						};
						similaritySlider.options.onChange = function(v){
					        $('similarity').value = (v/10);
							$('similarityDebug').innerHTML = '('+(v/10)+')';
					    };
						similaritySlider.setValue($('similarity').value*10);
					</script>
					</td>
				</tr>
			</xsl:if>


			<!-- Area -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/location"/>
					</th>
					<td class="padded">
						<table>
							<tr>
								<td>
									<select class="content" name="relation">
										<xsl:for-each select="/root/gui/strings/boundingRelation">
											<option>
												<xsl:if test="@value=/root/gui/searchDefaults/relation">
													<xsl:attribute name="selected"/>
												</xsl:if>
												<xsl:attribute name="value">
													<xsl:value-of select="@value"/>
												</xsl:attribute>
												<xsl:value-of select="."/>
											</option>
										</xsl:for-each>
									</select>
								</td>
								<td>
									<xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text>
								</td>
								<td>

									<!-- regions combobox -->

									<select class="content" name="region" id="region">
										<option value="">
											<xsl:if test="/root/gui/searchDefaults/theme='_any_'">
												<xsl:attribute name="selected"/>
											</xsl:if>
											<xsl:value-of select="/root/gui/strings/any"/>
										</option>

										<xsl:for-each select="/root/gui/regions/record">
											<xsl:sort select="label/child::*[name() = $lang]"
												order="ascending"/>
											<option>
												<xsl:if test="id=/root/gui/searchDefaults/region">
												<xsl:attribute name="selected"/>
												</xsl:if>
												<xsl:attribute name="value">
												<xsl:value-of select="id"/>
												</xsl:attribute>
												<xsl:value-of
												select="label/child::*[name() = $lang]"/>
											</option>
										</xsl:for-each>
									</select>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</xsl:if>

			<!-- Group -->
			<xsl:if
				test="string(/root/gui/session/userId)!='' and string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/group"/>
					</th>
					<td class="padded">
						<select class="content" name="group">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/group=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort order="ascending" select="name"/>
								<option>
									<xsl:if test="id=/root/gui/searchDefaults/group">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="id"/>
									</xsl:attribute>
									<xsl:value-of select="name"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Category -->
			<xsl:if
				test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/category"/>
					</th>
					<td class="padded">
						<select class="content" name="category">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/category=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>

							<xsl:for-each select="/root/gui/categories/record">
								<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>

								<option>
									<xsl:if test="name = /root/gui/searchDefaults/category">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="name"/>
									</xsl:attribute>
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Source -->
			<xsl:if
				test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/site"/>
					</th>
					<td class="padded">
						<select class="content" name="siteId">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/siteId=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>
							<xsl:for-each select="/root/gui/sources/record">
								<!--
								<xsl:sort order="ascending" select="name"/>
								-->
								<xsl:variable name="source" select="siteid/text()"/>
								<xsl:variable name="sourceName" select="name/text()"/>
								<option>
									<xsl:if test="$source=/root/gui/searchDefaults/siteId">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="$source"/>
									</xsl:attribute>
									<xsl:value-of select="$sourceName"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Map type -->
			<xsl:if test="string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/mapType"/>
					</th>
					<td>
						<input name="digital" type="checkbox" value="on">
							<xsl:if test="/root/gui/searchDefaults/digital='on'">
								<xsl:attribute name="checked"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/digital"/>
						</input>
						<!--
						FIXME: disabled
						<xsl:if test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
							&#xA0;&#xA0;
							<input class="content" name="download" type="checkbox">
								<xsl:if test="/root/gui/searchDefaults/download='on'">
									<xsl:attribute name="checked"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/downloadData"/>
							</input>
							&#xA0;&#xA0;
							<input class="content" name="online" type="checkbox">
								<xsl:if test="/root/gui/searchDefaults/online='on'">
									<xsl:attribute name="checked"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/interactiveMap"/>
							</input>
							&#xA0;&#xA0;
							&#xA0;&#xA0;
						</xsl:if>
						-->
						&#xA0;&#xA0; <input name="paper" type="checkbox" value="on">
							<xsl:if test="/root/gui/searchDefaults/paper='on'">
								<xsl:attribute name="checked"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/paper"/>
						</input>
					</td>
				</tr>
			</xsl:if>

			<!-- Template -->
			<xsl:if
				test="string(/root/gui/session/userId)!='' and /root/gui/services/service[@name='metadata.edit'] and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/kind"/>
					</th>
					<td>
						<select class="content" name="template" size="1">
							<option value="n">
								<xsl:if test="/root/gui/searchDefaults/template='n'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/metadata"/>
							</option>
							<option value="y">
								<xsl:if test="/root/gui/searchDefaults/template='y'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/template"/>
							</option>
							<option value="s">
								<xsl:if test="/root/gui/searchDefaults/template='s'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/subtemplate"/>
							</option>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- remote search fields -->
			<xsl:if test="string(/root/gui/searchDefaults/remote)='on'">

				<tr>
					<td class="dots" colspan="2"/>
				</tr>

				<!-- Profiles and servers -->
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/profile"/>
					</th>
					<td class="padded">
						<select class="content" name="profile" onchange="profileSelected()">
							<xsl:for-each select="/root/gui/searchProfiles/profile">
								<option>
									<xsl:if
										test="string(@value)=string(/root/gui/searchDefaults/profile)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="@value"/>
									</xsl:attribute>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>

				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/server"/>
					</th>
					<td class="padded">
						<select class="content" name="servers" size="6" multiple="true"
							onchange="serverSelected()">
							<xsl:for-each select="/root/gui/repositories/Instance">
								<xsl:variable name="name" select="@instance_dn"/>
								<xsl:variable name="collection" select="@collection_dn"/>
								<xsl:variable name="description"
									select="/root/gui/repositories/Collection[@collection_dn=$collection]/@collection_name"/>
								<option>
									<xsl:if
										test="/root/gui/searchDefaults/servers/server[string(.)=$name]">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="$name"/>
									</xsl:attribute>
									<xsl:value-of select="$description"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>

				<!-- timeout -->
				<tr>
					<th class="padded">
						<xsl:apply-templates select="/root/gui/strings/timeout" mode="caption"/>
					</th>
					<td class="padded">
						<select class="content" name="timeout">
							<xsl:for-each select="/root/gui/strings/timeoutChoice">
								<option>
									<xsl:if
										test="string(@value)=string(/root/gui/searchDefaults/timeout)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="@value"/>
									</xsl:attribute>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- other search options -->

			<tr>
				<td class="dots" colspan="2"/>
			</tr>

			<!-- hits per page -->
			<tr>
				<th class="padded">
					<xsl:value-of select="/root/gui/strings/hitsPerPage"/>
				</th>
				<td class="padded">
					<select class="content" name="hitsPerPage" onchange="profileSelected()">
						<xsl:for-each select="/root/gui/strings/hitsPerPageChoice">
							<option>
								<xsl:if
									test="string(@value)=string(/root/gui/searchDefaults/hitsPerPage)">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:attribute name="value">
									<xsl:value-of select="@value"/>
								</xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
			</tr>

		</table>
	</xsl:template>

	<xsl:template name="localization">
		<xsl:comment>These fields are needed for js on-the-fly translations</xsl:comment>	
		<xsl:for-each select="/root/gui/strings/*[@js='true']">			
			<input type="hidden" id="i18n_{name(.)}" value="{.}" />
		</xsl:for-each>				
		<xsl:comment>End of i18n fields</xsl:comment>	
	</xsl:template>	
	

</xsl:stylesheet>
