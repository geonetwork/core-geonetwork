<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="../main.xsl"/>

	<xsl:variable name="modalArg">
		<xsl:choose>
			<xsl:when test="/root/request/modal">
				<xsl:text>&amp;modal</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:template mode="script" match="/" priority="2">
		<script type="text/javascript" src="../../apps/js/ext/adapter/ext/ext-base.js"/>
		<script type="text/javascript" src="../../apps/js/ext/ext-all-debug.js"/>
		<script type="text/javascript" src="../../apps/js/ext-ux/timeago.js"></script>
	</xsl:template>
	
	<xsl:variable name="fullHistory" select="contains(/root/gui/reqService,'full')"/>
	<xsl:variable name="pageHistorySize" select="10"/>

 	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/harvestingManagement"/>

			<xsl:with-param name="content">

			<xsl:variable name="totalCount" select="count(/root/response/response/record)" />
			<script>
				function viewAllHarvestingHistory() {
					$('harvesterHistoryCounter').toggle();
					$('harvesterHistoryTotal').toggle();
					$$('tr.harvestingHistoryRow').invoke('toggle');
				}

				function deleteHarvestingHistory() {
					if(confirm('<xsl:value-of select="/root/gui/harvesting/historyDeleteConfirm"/>')) {
						// collect up ids
						var params = '';
						$$('#harvesterHistoryContainer input[type=checkbox]').each(function(check) {
							if (check.checked) {
								var toks = check.name.split('_');
								if (toks[1] != 'all') {
									params += '&amp;id='+toks[1];	
								}
							}
						});
						var url = '<xsl:value-of select="/root/gui/locService" />/harvesting.history.delete?'+params;
						Modalbox.show(url, {width: 600, 
							title: '<xsl:value-of select="/root/gui/harvesting/historyDeletedTitle"/>',
							afterHide: 
								function() {
									refresh(); 
								}
							}
						);
					}
				}


				function toggleSelection() {
					var toggle = $('sel_all').checked;
					$$('#harvesterHistoryContainer input[type=checkbox]').each(function(check) {
						// only visible elements
						if ($(check).getStyle("display") != 'none') {
							check.checked = toggle;
						} else {
							check.checked = toggle;
						}
					});
				}

				function refresh() {
					<xsl:choose>
						<xsl:when test="$fullHistory">
							var url = '<xsl:value-of select="/root/gui/locService" />/harvesting.history.full?sort=' + $('sort').value + '<xsl:value-of select="$modalArg" />';
						</xsl:when>
						<xsl:otherwise>
							var url = '<xsl:value-of select="/root/gui/locService" />/harvesting.history?id=<xsl:value-of select="/root/response/node/@id" />&amp;uuid=<xsl:value-of select="/root/response/node/site/uuid" />' + '<xsl:value-of select="$modalArg" />';
						</xsl:otherwise>
					</xsl:choose>
					load(url);
				}

				Event.observe(window, 'load',
					function() {
						var sort = $('sort');
						if (sort) {
							sort.value = '<xsl:value-of select="/root/response/sort" />';
						}
						
						Ext.each(Ext.DomQuery.select('abbr'), function (item) {
							var time = new Date(item.innerHTML);
							item.innerHTML = time.toRelativeTime(0);
						});
					});

			</script>

			<h1><xsl:value-of select="/root/gui/harvesting/harvesterHistory"/>
				<xsl:if test="not($fullHistory)">
					<xsl:value-of select="concat(' ',/root/response/node/site/name,' (',/root/response/node/@type,')')"/>
				</xsl:if>
			</h1>

			<div id="harvesterHistoryContainer">

				<xsl:if test="$totalCount=0">
					<xsl:value-of select="/root/gui/harvesting/noHistory"/>
				</xsl:if>


				<xsl:if test="$fullHistory and $totalCount!=0">
					<xsl:value-of select="/root/gui/harvesting/sortBy"/>&#160;<select id="sort" onchange="refresh()">
						<option value="date"><xsl:value-of select="/root/gui/harvesting/dateDesc"/></option>
						<option value="type"><xsl:value-of select="/root/gui/harvesting/type"/></option>
					</select>
				</xsl:if>

				<table id="history_table">
					<xsl:if test="$totalCount=0">
						<xsl:attribute name="style">display:none;</xsl:attribute>
					</xsl:if>
					
					<tr>
						<th class="padded" style="width:32px"><input id="sel_all" name="sel_all" type="checkbox" onclick="toggleSelection()"/></th>
						<xsl:if test="$fullHistory">
							<th class="padded" style="width:64px;text-align:center"><b><xsl:value-of select="/root/gui/harvesting/type"/></b></th>
							<th class="padded" style="width:128px;text-align:center"><b><xsl:value-of select="/root/gui/harvesting/name"/></b></th>
						</xsl:if>
						<th class="padded" style="width:64px;text-align:center"><b><xsl:value-of select="/root/gui/harvesting/lastRun"/></b></th>
						<th class="padded" style="width:64px;text-align:center"><b><xsl:value-of select="/root/gui/harvesting/elapsedTime"/></b></th>
						<th class="padded" style="width:32px"><b><xsl:value-of select="/root/gui/harvesting/ok"/></b></th>
						<th class="padded" style="width:384px;text-align:center"><b><xsl:value-of select="/root/gui/harvesting/detail"/></b></th>
						<xsl:if test="$fullHistory">
							<th class="padded" style="width:32px"><b><xsl:value-of select="/root/gui/harvesting/status"/></b></th>
							<th class="padded" style="width:32px"><b><xsl:value-of select="/root/gui/harvesting/deleted"/></b></th>
						</xsl:if>
					</tr>
					<xsl:for-each select="/root/response/response/record">

						<xsl:variable name="uuid" select="harvesteruuid"/>
						<xsl:variable name="id" select="id"/>
						<xsl:element name="tr">

							<xsl:if test="position() gt $pageHistorySize">
								<xsl:attribute name="class">harvestingHistoryRow</xsl:attribute>
								<xsl:attribute name="style">display:none</xsl:attribute>
							</xsl:if>

							<!-- column: checkbox -->
							<td class="padded"><input name="sel_{id}" type="checkbox" /></td>
								<xsl:if test="$fullHistory">
									<!-- column: harvestertype -->
									<td class="padded" style="text-align:center"><xsl:value-of select="harvestertype" /></td>

									<!-- column: harvestername -->
									<td class="padded" style="text-align:center"><xsl:value-of select="harvestername" /></td>
								</xsl:if>

								<!-- column: harvestdate -->
								<td class="padded" style="text-align:center">
									<abbr title="{harvestdate}"><xsl:value-of select="harvestdate" /></abbr>
								</td>
								<td class="padded" style="text-align:center">
									<xsl:variable name="minutes" select="floor(elapsedtime div 60)" />
									<xsl:variable name="seconds" select="elapsedtime mod 60" />
									<xsl:value-of select="if ($minutes &lt; 1) then 
										concat($seconds, 's') else concat($minutes, 'min ', $seconds, 's')" />
								</td>
							
								<!-- column: ok? -->
								<td class="padded" align="center">
									<xsl:choose>
										<xsl:when test="info/result">
											<img id="error" src="{/root/gui/url}/images/button_ok.png" alt="ok" />
										</xsl:when>
										<xsl:otherwise>
											<img id="error" src="{/root/gui/url}/images/important.png" alt="failed" />
										</xsl:otherwise>
									</xsl:choose>
								</td>

								<!-- column: details -->
								<td class="padded">
									<table>
										<xsl:for-each select="info/result">
											<xsl:for-each select="stats">
												<tr><td>
													<xsl:call-template name="showResult">
														<xsl:with-param name="server" select="@server"/>
													</xsl:call-template>
												</td></tr>
											</xsl:for-each>
											<xsl:if test="count(stats)>1 or count(stats)=0">
												<tr><td>
													<xsl:choose>
														<xsl:when test="count(stats)>1">
															<xsl:call-template name="showResult">
																<xsl:with-param name="server" select="/root/gui/harvesting/grandTotal"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise>
															<xsl:call-template name="showResult"/>
														</xsl:otherwise>
													</xsl:choose>
												</td></tr>
											</xsl:if>
										</xsl:for-each>
										<xsl:for-each select="info/error">
											<tr><td>
												<xsl:call-template name="showError">
													<xsl:with-param name="id" select="$id"/>
												</xsl:call-template>
											</td></tr>
										</xsl:for-each>
									</table>
								</td>

								<xsl:if test="$fullHistory">
									<!-- column: status -->
									<td class="padded" style="text-align:center"><xsl:value-of select="/root/response/nodes/node[site/uuid=$uuid]/options/status" /></td>

									<!-- column: deleted -->
									<td class="padded" style="text-align:center"><xsl:value-of select="deleted" /></td>

								</xsl:if>

							</xsl:element>

							<!-- display either full history count or size -->
							<xsl:element name="tr">

								<xsl:if test="position() gt $pageHistorySize">
									<xsl:attribute name="class">harvestingHistoryRow</xsl:attribute>
									<xsl:attribute name="style">display:none</xsl:attribute>
								</xsl:if>
								<xsl:choose>
									<xsl:when test="$fullHistory">
										<td colspan="9"><hr/></td>
									</xsl:when>
									<xsl:otherwise>
										<td colspan="5"><hr/></td>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:element>
						</xsl:for-each>
					</table>

					<div id="harvestResultsFooter" style="margin-top: 8px">
						<xsl:if test="$totalCount=0">
							<xsl:attribute name="style">display:none;</xsl:attribute>
						</xsl:if>
						<xsl:choose>
							<xsl:when test="$totalCount gt $pageHistorySize">
								<div id="harvesterHistoryCounter"><xsl:value-of select="concat($pageHistorySize,' ',/root/gui/harvesting/counter,' ',$totalCount)" /></div>
								<div id="harvesterHistoryTotal" style="display:none"><xsl:value-of select="concat(/root/gui/harvesting/totalHistory,' ',$totalCount)"/></div>
							</xsl:when>
							<xsl:otherwise>
								<div id="harvesterHistoryTotal"><xsl:value-of select="concat(/root/gui/harvesting/totalHistory,' ',$totalCount)"/></div>
							</xsl:otherwise>
						</xsl:choose>
					</div>
				</div>

			</xsl:with-param>

			<xsl:with-param name="buttons">
			<div id="listButtons">
				<button class="content" onclick="load('{/root/gui/locService}/harvesting?{$modalArg}')">
					<xsl:value-of select="/root/gui/harvesting/backToHarvestManager"/>
				</button>
				&#160;
				<xsl:if test="count(/root/response/response/record) gt $pageHistorySize">
					<button class="content" onclick="viewAllHarvestingHistory()">
						<xsl:value-of select="/root/gui/harvesting/viewAll"/>
					</button>
				</xsl:if>
				&#160;
				<xsl:if test="count(/root/response/response/record) gt 0">
					<button class="content" onclick="deleteHarvestingHistory();">
						<xsl:value-of select="/root/gui/harvesting/deleteHistory"/>
					</button>
				</xsl:if>
			</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="showResult">
		<xsl:param name="server" select="''"/>
		<table style="width:100%">
			<xsl:if test="normalize-space($server)!=''">
				<tr>
					<td colspan="2" style="font-weight:bold"><xsl:value-of select="$server"/></td>
				</tr>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="count(*[name()!='stats' and number()!=0])>0">
					<xsl:for-each select="*[name()!='stats']">
						<xsl:if test="number()!=0">
							<tr>
								<xsl:variable name="statName" select="name(.)"/>
								<td style="width:80%"><span style="margin-left:8px"><xsl:value-of select="/root/gui/harvesting/tipHeader/*[name()=$statName]"/></span></td>
								<td style="width:20%"><span style="margin-left:8px"><xsl:value-of select="."/></span></td>
							</tr>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<tr>
						<td colspan="2" style="text-align:center;font-style:italic;color:#ff0000"><xsl:value-of select="/root/gui/harvesting/harvestedNothing"/></td>
					</tr>
				</xsl:otherwise>
			</xsl:choose>
		</table>
	</xsl:template>

	<xsl:template name="showError">
		<xsl:param name="id" select="generate-id()"/>

		<!-- No localization necessary here -->
		<table style="width:100%">
			<tr>
				<td>
					<span class="padded" style="font-weight:bold;color:#ff0000">Error: </span>
					<span class="padded" style="font-weight:bold"><xsl:value-of select="message"/></span>
					&#160;<a href="#"><img src="{/root/gui/url}/images/arrow_down.gif" onclick="$('stackDetails_{$id}').toggle();"/></a>
				</td>
			</tr>
		</table>
		<div id="stackDetails_{$id}" style="display:none;">
			<table style="width:100%">
				<tr>
					<td><span class="padded" style="font-weight:bold">Class: </span><span><xsl:value-of select="class"/></span></td>
				</tr>
				<tr>
					<td><span class="padded" style="font-weight:bold">Stack: </span></td>
				</tr>
				<xsl:for-each select="stack/at">
					<tr>
						<td>
							<span class="padded" style="margin-left:8px;font-weight:bold">at: </span><span><xsl:value-of select="@class"/></span>
							<span class="padded" style="font-weight:bold"> file: </span><span><xsl:value-of select="@file"/></span>
							<span class="padded" style="font-weight:bold"> line: </span><span><xsl:value-of select="@line"/></span>
							<span class="padded" style="font-weight:bold"> method: </span><span><xsl:value-of select="@method"/></span>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</div>
	</xsl:template>
</xsl:stylesheet>
