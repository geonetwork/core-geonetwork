<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
	<xsl:template match="/">

		<xsl:if test="count(//marker)=0">			
			<xsl:call-template name="empty"/>	
		</xsl:if>		
		<xsl:if test="count(//marker)=1">			
			<xsl:apply-templates select="//marker" mode="single"/>	
		</xsl:if>
		<xsl:if test="count(//marker)>1">
			<xsl:apply-templates select="//marker" />	
		</xsl:if>
		
		
	</xsl:template>


	<!-- Markers  -->
	<xsl:template match="//marker">		
		<div id="marker_{@id}" class="im_markerEntry"
			onmouseover= "className = 'im_markerEntrySel' "
			onmouseout= "className = 'im_markerEntry' "
			onclick="im_selectMarkerFromList({@id})">
			
			<xsl:variable name="currid"><xsl:value-of select="@id" /></xsl:variable>
			<xsl:if test="//newLayer/@id=$currid">
				<xsl:attribute name="class" >im_newLayer</xsl:attribute>				
			</xsl:if>
			
			<b><xsl:value-of select="title"/></b>
			<br/>
			<i><xsl:value-of select="description"/></i>
			<br/>
			Lat: <xsl:value-of select="@lat"/> Lon: <xsl:value-of select="@lon"/>
		</div>						
	</xsl:template>

	<xsl:template match="//marker" mode="single">		
		<!-- =========================================== -->
		<!--   USER MESSAGES --> 
		<!--  TODO i18n all messages -->
		<!-- =========================================== -->
		<div id="im_marker_msg">
			<div id="im_marker_msg_update_start" style="display:none;">
				Updating marker... 
			</div>
			<div id="im_marker_msg_update_ok" style="display:none;">
				The marker has been updated 
			</div>
			<div id="im_marker_msg_update_error" style="display:none;">
				An unexpected error has been encountered while performing the operation:<br/>				
			</div>
			
			<div id="im_marker_msg_delete_start" style="display:none;">
				Deleting marker... 
			</div>
			<div id="im_marker_msg_delete_ok" style="display:none;">
				The marker has been deleted 
			</div>
			<div id="im_marker_msg_delete_error" style="display:none;">
				An unexpected error has been encountered while performing the operation:<br/>				
			</div>			
		</div>
		
		
		<table id="im_marker_form" width="100%">
			<tr>
				<td>Title</td>
				<td colspan="3"><input type="text" id="marker_title_{@id}" name="marker_title_{@id}" value="{title}" size="80"/></td>
			</tr>
			<tr>
				<td>Desc</td>
				<td colspan="3"><input type="text" id="marker_desc_{@id}" name="marker_desc_{@id}" value="{description}" size="80"/></td>
			</tr>
			<tr>
				<td>Lat</td>
				<td><input type="text" id="marker_lat_{@id}" name="marker_lat_{@id}" value="{@lat}" readonly="true"/></td>
				<td>Lon</td>
				<td><input type="text" id="marker_lon_{@id}" name="marker_lon_{@id}" value="{@lon}" readonly="true"/></td>
			</tr>
			<tr height="15px">
				<td colspan="4"> </td>
			</tr>
			<tr>
				<td colspan="3">
					<button onClick="javascript:im_updateMarker({@id});" title="{/root/gui/strings/updateMarker}"><xsl:value-of select="/root/gui/strings/update"/></button>
				</td>
				
				<td>	
					<img id="deleteMarker_{@id}" class="im_layerButton" 
						onclick="im_deleteMarker({@id})" 
						src="/intermap/images/deleteLayer.png" title="{/root/gui/strings/removeMarker}"/>
				</td>
			</tr>
		</table>		
	</xsl:template>
	
	<xsl:template name="empty">
		<div>
			<b>There are no markers defined.</b>
		</div>
		
	</xsl:template>

</xsl:stylesheet>
