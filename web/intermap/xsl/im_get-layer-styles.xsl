<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"/>

	<xsl:template match="/">
		<div>
			<b>Layer Info:</b>
			<xsl:apply-templates select="/root/response/layer"/>
			<br/>
			<b>Associated styles:</b>
			<xsl:apply-templates select="/root/response/style"/>		
			
			<xsl:if test="count(/root/response/style)>1">				
				<button onClick="im_setStyle({/root/response/layer/id});" style="margin-bottom:5px;margin-top:5px;">Set style</button>
			</xsl:if>
			
		</div>
	</xsl:template>
		
	<xsl:template match="layer">
		<table>
			<tr>
				<td style="padding-right:5px"><b>Title</b></td>
				<td><xsl:value-of select="./title"/></td>				
			</tr>
		</table>			
	</xsl:template>
	
	
	<xsl:template match="style">
		
		<table class="style">
			<tr>
				<td >
					<input type="radio" name="styleradio" value="{name}">
						<xsl:if test="@selected"><xsl:attribute name="checked" ></xsl:attribute></xsl:if>
					</input>
				</td>
				<td><b>Title</b></td>
				<td width="100%"><xsl:value-of select="title"/></td>				
			</tr>
			<xsl:if test="./abstract">
				<tr>
					<td></td>
					<td><b>Abstract</b></td>
					<td><xsl:value-of select="./abstract"/></td>
				</tr>				
			</xsl:if>
			
			<xsl:apply-templates select="legend"/>
		</table>	
		
	</xsl:template>
	

	<xsl:template match="legend">
		<tr>
			<td></td>
			<td style="padding-right:5px"><b>Legend</b></td>
			<td>	
				<img src="{./href}"  title="{/root/gui/strings/legend}"/>
			</td>
		</tr>
		
	</xsl:template>
	

</xsl:stylesheet>
