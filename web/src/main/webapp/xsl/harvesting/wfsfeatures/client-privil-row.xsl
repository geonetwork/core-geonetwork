<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table row for the harvesting's privilege list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/group">

		<tr id="wfsfeatures.group.{@id}">
			<td class="padded" align="center"><xsl:value-of select="@name"/></td>
			
			<!-- view - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded" align="center">
				<input name="view" type="checkbox">
					<xsl:if test="operation/@name = 'view'">
						<xsl:attribute name="checked">on</xsl:attribute>
					</xsl:if>
				</input>
			</td>
		
			<!-- dynamic - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded" align="center">
				<input name="dynamic" type="checkbox">
					<xsl:if test="operation/@name = 'dynamic'">
						<xsl:attribute name="checked">on</xsl:attribute>
					</xsl:if>
				</input>
			</td>
			
			<!-- featured - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded" align="center">
				<input name="featured" type="checkbox">
					<xsl:if test="operation/@name = 'featured'">
						<xsl:attribute name="checked">on</xsl:attribute>
					</xsl:if>
				</input>
			</td>
			
			<!-- actions - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded" align="center">
				<a href="javascript:harvesting.wfsfeatures.removeGroupRow('wfsfeatures.group.{@id}')">
					<xsl:value-of select="/root/strings/remove"/>
				</a>		
			</td>
		</tr>

	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
