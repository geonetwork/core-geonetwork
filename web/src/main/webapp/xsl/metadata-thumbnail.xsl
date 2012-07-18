<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sc="scaling">
	
	<xsl:include href="main.xsl"/>

	<xsl:param name="IMG_SIZE" select="100"/>

	<xsl:template mode="script" match="/">
		<script type="text/javascript">
			function addThumbnail(myElement) {
				if (document.forms[myElement.form.name].fname.value.length &lt; 1) {
					alert("Browse for a file first please!");
					return false;
				}
				document.forms[myElement.form.name].submit();
			}
		</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/thumbnail/title"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<xsl:text> </xsl:text>
				<form name="backToEdit" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.edit" method="post">
					<xsl:text> </xsl:text>
					<input type="hidden" name="id" value="{/root/thumbnail/id}"/>
					<input class="content" type="submit" value="{/root/gui/thumbnail/button}" name="btn"/>
				</form>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================================================ -->
	
	<xsl:template name="form">
		<table>
			<xsl:choose>
				<xsl:when test="/root/thumbnail/small">
					<xsl:call-template name="present">
						<xsl:with-param name="type" select=" 'small' "/>
						<xsl:with-param name="file" select="/root/thumbnail/small"/>
					</xsl:call-template>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:call-template name="absent">
						<xsl:with-param name="type" select=" 'small' "/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
			
			<tr><td class="dots" colspan="5"/></tr>
			
			<xsl:choose>
				<xsl:when test="/root/thumbnail/large">
					<xsl:call-template name="present">
						<xsl:with-param name="type" select=" 'large' "/>
						<xsl:with-param name="file" select="/root/thumbnail/large"/>
					</xsl:call-template>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:call-template name="absent">
						<xsl:with-param name="type" select=" 'large' "/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>					
		</table>
	</xsl:template>

	<!-- ================================================================================================ -->
	
	<xsl:template name="present">
		<xsl:param name="type" />
		<xsl:param name="file" />
		
		<xsl:variable name="urlImg" >
			<xsl:choose>
				<xsl:when test="contains($file, 'http://')">
					<xsl:value-of select="$file" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=', /root/thumbnail/id, '&amp;fname=', $file, '&amp;access=public')"/>
				</xsl:otherwise>
			</xsl:choose> 
		</xsl:variable>
		<xsl:variable name="urlAnc" select="concat(/root/gui/locService,'/graphover.show?id=', /root/thumbnail/id, '&amp;fname=', $file, '&amp;access=public')"/>
		
		<form name="present_{$type}" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.thumbnail.unset" method="post">
			<input type="hidden" name="id"      value="{/root/thumbnail/id}"/>
			<input type="hidden" name="version" value="{/root/thumbnail/version}"/>
			<input type="hidden" name="type"    value="{$type}"/>

			<tr>
				<td>
					<a href="javascript:popWindow('{$urlAnc}')">
						<img width="{$IMG_SIZE}" src="{$urlImg}" alt="{/root/gui/strings/thumbnail}"/>
					</a>
				</td>
				
				<th class="padded">
					<xsl:value-of select="/root/gui/thumbnail/text[@type = $type]"/>
				</th>
				
				<td align="left">
					<input class="content" type="text" value="{$file}" name="btnT" readonly="true"/>
				</td>
				
				<td class="dots"/>
				
				<td align="center">
					<input class="content" type="submit" value="{/root/gui/thumbnail/delete}" name="delete"/>
				</td>
			</tr>
		</form>			
	</xsl:template>
	
	<!-- ================================================================================================ -->
	
	<xsl:template name="absent">
		<xsl:param name="type" />
		
		<form name="absent_{$type}" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.thumbnail.set" 
				method="post" enctype="multipart/form-data">
			<input type="hidden" name="id"      value="{/root/thumbnail/id}"/>
			<input type="hidden" name="version" value="{/root/thumbnail/version}"/>
			<input type="hidden" name="type"    value="{$type}"/>

			<tr>			
				<td>
					<img width="{$IMG_SIZE}" src="{/root/gui/locUrl}/images/nopreview.gif" alt="{/root/gui/strings/thumbnail}"/>
				</td>
				
				<th class="padded">
					<xsl:value-of select="/root/gui/thumbnail/text[@type = $type]"/>
				</th>
							
				<td>
					<input class="content" type="file" value="" name="fname"/>
				</td>
							
				<td class="dots"/>
				
				<td align="center">
					<input class="content" type="button" onclick="addThumbnail(this)" value="{/root/gui/thumbnail/add}" name="add"/>
				</td>
			</tr>
			<tr>
				<td/>
				<td/>
				
				<td>
					<input class="content" type="checkbox" value="on" name="scaling" checked="true">
						<xsl:value-of select="/root/gui/thumbnail/scale"/>
					</input>	
					&#160;
					<xsl:call-template name="scale">
						<xsl:with-param name="name" select=" 'scalingFactor' "/>
						<xsl:with-param name="data" select="document('')//sc:data[@type = $type]/item"/>
					</xsl:call-template>
					
					<br/>					
					<input class="content" type="radio" value="width" name="scalingDir" checked="true">
						<xsl:value-of select="/root/gui/thumbnail/scaleWidth"/>
					</input>	
								
					<br/>					
					<input class="content" type="radio" value="height" name="scalingDir">
						<xsl:value-of select="/root/gui/thumbnail/scaleHeight"/>
					</input>	
					
					<xsl:if test="$type = 'large'">
						<br/>
						<br/>
						<input class="content" type="checkbox" value="on" name="createSmall" checked="true">
							<xsl:value-of select="/root/gui/thumbnail/create"/>
						</input>	
						&#160;
						<xsl:call-template name="scale">
							<xsl:with-param name="name" select=" 'smallScalingFactor' "/>
							<xsl:with-param name="data" select="document('')//sc:data[@type = 'small']/item"/>
						</xsl:call-template>								
					
						<br/>					
						<input class="content" type="radio" value="width" name="smallScalingDir" checked="true">
							<xsl:value-of select="/root/gui/thumbnail/scaleWidth"/>
						</input>	
									
						<br/>					
						<input class="content" type="radio" value="height" name="smallScalingDir">
							<xsl:value-of select="/root/gui/thumbnail/scaleHeight"/>
						</input>	
					</xsl:if>
				</td>
				
				<td class="dots"/>
				<td/>
			</tr>
		</form>			
	</xsl:template>

	<!-- ================================================================================================ -->
	
	<sc:data type="small">
		<item scale="100" label="100 pixel"/>
		<item scale="120" label="120 pixel"/>
		<item scale="140" label="140 pixel"/>
		<item scale="160" label="160 pixel"/>
		<item scale="180" label="180 pixel" selected="true"/>
		<item scale="200" label="200 pixel"/>
		<item scale="220" label="220 pixel"/>
	</sc:data>
	
	<sc:data type="large">
		<item scale="400"  label="400 pixel"/>
		<item scale="500"  label="500 pixel"/>
		<item scale="600"  label="600 pixel"/>
		<item scale="700"  label="700 pixel"/>
		<item scale="800"  label="800 pixel" selected="true"/>
		<item scale="900"  label="900 pixel"/>
		<item scale="1000" label="1000 pixel"/>
		<item scale="1100" label="1100 pixel"/>
		<item scale="1200" label="1200 pixel"/>
	</sc:data>
	
	<!-- ================================================================================================ -->
	
	<xsl:template name="scale">
		<xsl:param name="name"/>
		<xsl:param name="data"/>
		
		<select class="content" name="{$name}" size="1">
			<xsl:for-each select="$data">
				<option value="{@scale}">
					<xsl:if test="@selected = 'true'">
						<xsl:attribute name="selected"/>
					</xsl:if>
					<xsl:value-of select="@label"/>					
				</option>
			</xsl:for-each>
		</select>
	</xsl:template>
	
</xsl:stylesheet>
