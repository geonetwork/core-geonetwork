<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:import href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/updateCategories"/>
			<xsl:with-param name="content">

				<xsl:variable name="disabled" select="(/root/response/owner='false')"/>

				<div id="categories" align="center">
					<xsl:choose>
						<xsl:when test="/root/response/metadatacategory/*">

							<input name="metadataid" id="metadataid" type="hidden" value="{/root/response/id}"/>
							<table>
								<tr>
									<th class="padded"><xsl:value-of select="/root/gui/strings/category"/></th>
									<th class="padded"><xsl:value-of select="/root/gui/strings/assigned"/></th>
								</tr>
		
								<xsl:variable name="lang" select="/root/gui/language"/>
					
								<!-- loop on all categories -->
		
								<xsl:for-each select="/root/response/metadatacategory/category">
								  <xsl:sort select="label/child::*[name() = $lang]"/>
									<xsl:variable name="categId" select="id"/>
									<tr>
										<td class="padded">
											<label for="_{$categId}">
											  <img class="category" src="../../images/category/{name}.png"/>
												<xsl:value-of select="label/child::*[name() = $lang]"/>
											</label>
										</td>
										<td class="padded" align="center">
											<input type="checkbox" id="_{$categId}" name="_{$categId}">
												<xsl:if test="on">
													<xsl:attribute name="checked"/>
												</xsl:if>
												<xsl:if test="$disabled">
													<xsl:attribute name="disabled"/>
												</xsl:if>
											</input>
										</td>
									</tr>
								</xsl:for-each>				
								<xsl:if test="not($disabled)">
									<tr width="100%">
										<td align="center" colspan="2">
											<xsl:choose>
												<xsl:when test="contains(/root/gui/reqService,'metadata.batch')">
													<button class="content" onclick="checkBoxModalUpdate('categories','metadata.batch.update.categories','true','{concat(/root/gui/strings/results,' ',/root/gui/strings/batchUpdateCategoriesTitle)}')"><xsl:value-of select="/root/gui/strings/submit"/></button>
												</xsl:when>
												<xsl:otherwise>
													<button class="content" onclick="checkBoxModalUpdate('categories','metadata.category');"><xsl:value-of select="/root/gui/strings/submit"/></button>
												</xsl:otherwise>
											</xsl:choose>
										</td>
									</tr>
								</xsl:if>
							</table>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="/root/gui/strings/noCategory"/>
							<!-- TODO : here we should not suggest category management
							from search results if no category available in the catalogue 
							@see search-results.xhtml -->
						</xsl:otherwise>
					</xsl:choose>
				</div>          
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
