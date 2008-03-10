<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
	<xsl:template match="/">
		<ul id = "im_layerList">
			<xsl:apply-templates select="//layer" />
		</ul>
	</xsl:template>


	<!-- Layers  -->
	<xsl:template match="//layer">
		
		<li id="layerList_{@id}"
			onmousedown="activateMapLayer({@id})"
			ondblclick="layerDblClickListener({@id})">
			
			<xsl:variable name="currid"><xsl:value-of select="@id" /></xsl:variable>
			<xsl:if test="//newLayer/@id=$currid">
				<xsl:attribute name="class" >im_newLayer</xsl:attribute>				
			</xsl:if>
			
<!--
	<xsl:variable name="action">im_mapServerSelected(<xsl:value-of select="@id" />,"<xsl:value-of select="@name" />");</xsl:variable>
	<li id="im_mapserver_{@id}" onclick="{$action}"><a><xsl:value-of select="@name" /></a></li>
-->		
			<xsl:variable name="toggleVisibilityIcon">
				<xsl:choose>
					<xsl:when test="@visible='true'">/intermap/images/showLayer.png</xsl:when>
					<xsl:otherwise>/intermap/images/hideLayer.png</xsl:otherwise>
				</xsl:choose>				
			</xsl:variable>
			
			
			<table>
				<tbody>
					<tr height="35px">
						<td height="35px" class="im_layerControl">
							<img id="visibility_{@id}"  class="im_layerControl" 
								onclick="toggleVisibility({@id})" 
								src="{$toggleVisibilityIcon}" title="{/root/gui/strings/layerShowHide}"></img>
							<xsl:if test="position()>1">
								<img id="im_layerUp_{@id}" class="im_layerControl" 
									onclick="im_layerMoveUp({@id})"
									src="/intermap/images/im_moveup.gif" title="{/root/gui/strings/up}"/>								
							</xsl:if>
							<xsl:if test="position() &lt; last()">
								<img id="im_layerDown_{@id}" class="im_layerControl" 
									onclick="im_layerMoveDown({@id})"
									src="/intermap/images/im_movedown.gif" title="{/root/gui/strings/down}"/>								
							</xsl:if>						
						</td>
						
						<td>
							<p><xsl:value-of select="@title" /></p>
						</td>
					</tr>
					
					<tr id="layerControl_{@id}" style="display:none;">
						<td style="white-space: nowrap;" colspan="2">
							<xsl:if test="last()>1"> <!-- we need at least one layer, so last one is not removeable  -->
								<img id="deleteLayer_{@id}" class="im_layerButton" 
									onclick="im_deleteLayer({@id})" 
									src="/intermap/images/deleteLayer.png" title="{/root/gui/strings/removeLayer}"/>
							</xsl:if>
							
							<xsl:if test="@legend">
								<img id="legend_{@id}"  class="im_layerButton" 
									onclick="showLegend('{@legend}', legend_{@id});"
									src="/intermap/images/legend.png" title="{/root/gui/strings/legend}"/>
							</xsl:if>
							
							<xsl:if test="@style">
								<img id="styles_{@id}"  class="im_layerButton" 
									onclick="im_showStyles({@id});"
									src="/intermap/images/im_btt-style.gif" title="{/root/gui/strings/btnstyles}"/>
							</xsl:if>
							
							<img id="showLayerMD_{@id}" class="im_layerButton"
								onclick="im_showLayerMD({@id})" 								
								src="/intermap/images/metadata.png" title="{/root/gui/strings/metadata}"/>
							<select id="im_transp_{@id}" class="layerSelectTransp"
								onchange="im_layerTransparencyChanged({@id})" title="{/root/gui/strings/transparency}">
								<xsl:call-template name="fillTransparencyOptions">
									<xsl:with-param name="transparency"><xsl:value-of select="@transparency" /></xsl:with-param>
								</xsl:call-template>
							</select>
						</td>
						
					</tr>
				</tbody>
				
			</table>
		
		</li>

	</xsl:template>


	<xsl:template name="fillTransparencyOptions">
		<xsl:param name="transparency"/>
		
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">100</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">90</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">80</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">70</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">60</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">50</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">40</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">30</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">20</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">10</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>
		<xsl:call-template name="transpOption">
			<xsl:with-param name="value">0</xsl:with-param>
			<xsl:with-param name="transp"><xsl:value-of select="$transparency" /></xsl:with-param>																		
		</xsl:call-template>									
	</xsl:template>


	<xsl:template name="transpOption">
		<xsl:param name="value"/>
		<xsl:param name="transp"/>
		
		<option value="{$value}">
			<xsl:if test="$transp = $value">
				<xsl:attribute name="selected" >true</xsl:attribute>				
			</xsl:if>
			
			<xsl:choose>
				<xsl:when test="$value=100">Opaque</xsl:when>
<!--				<xsl:when test="$value=0">Transparent</xsl:when>-->
				<xsl:otherwise><xsl:value-of select="$value"/>%</xsl:otherwise>
			</xsl:choose>

		</option>		
	</xsl:template>
	
</xsl:stylesheet>
