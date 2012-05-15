<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:import href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/categories"/>
			<xsl:with-param name="content">

				<div id="extractSubtemplates" align="center">
					<form id="extractSubtemplatesForm"  name="extractSubtemplatesForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.batch.extract.subtemplates">

					<table>

						<!-- xpath -->
						<tr> 
							<td class="padded">
								<xsl:value-of select="/root/gui/strings/xpathToExtract"/>
							</td>
							<td class="padded">
								<input id="xpath" name="xpath" type="text" size="60"/>
							</td>
						</tr>

						<!-- xpath to extract title from subtemplate -->
						<tr> 
							<td class="padded">
								<xsl:value-of select="/root/gui/strings/xpathToExtractTitle"/>
							</td>
							<td class="padded">
								<input id="xpathTitle" name="xpathTitle" type="text" size="60"/>
							</td>
						</tr>

						<!-- category to assign subtemplates to -->
						<tr> 
							<xsl:choose>
								<xsl:when test="/root/response/categories/*">
									<td class="padded">
										<xsl:value-of select="/root/gui/strings/categoryForSubtemplate"/>
									</td>
									<td>
										<xsl:variable name="lang" select="/root/gui/language"/>
										<select id="category" name="category">
											<option value="_none_"/>

											<!-- loop on all categories -->
											<xsl:for-each select="/root/response/categories/category">
								  			<xsl:sort select="label/child::*[name() = $lang]"/>
												<xsl:variable name="categId" select="id"/>
												<option value="_{$categId}">
													<xsl:value-of select="label/child::*[name() = $lang]"/>
												</option>
											</xsl:for-each>
										</select>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td colspan="2">
										<xsl:value-of select="/root/gui/strings/noCategory"/>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</tr>

						<!-- do we want changes to be made? -->
						<tr> 
							<td class="padded">
								<xsl:value-of select="/root/gui/strings/makeChanges"/>&#160;
								<img src="{/root/gui/url}/images/important.png" alt="{/root/gui/strings/dontMakeChanges}"/>
							</td>
							<td class="padded">
								<input name="doChanges" type="checkbox"/>
							</td>
						</tr>

						<!-- buttons -->
						<tr>
							<td align="center" class="content" colspan="2">
      					<button align="center" class="content" onclick="checkBatchExtractSubtemplates('metadata.batch.extract.subtemplates','{concat(/root/gui/strings/results,' ',/root/gui/strings/batchExtractSubtemplatesTitle)}')" type="button">
        					<xsl:value-of select="/root/gui/strings/extractSubtemplates"/>
      					</button>
							</td>
						</tr>
					</table>

					</form>
				</div>          
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
