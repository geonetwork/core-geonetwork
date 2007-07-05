<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>
	
		<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<!-- javascript -->
		<script language="JavaScript1.2" type="text/javascript">
			var locService= '<xsl:value-of select="/root/gui/locService"/>';
			var create = '<xsl:value-of select="/root/gui/strings/create"/>';
			var remove = '<xsl:value-of select="/root/gui/strings/delete"/>';
			var edit =  '<xsl:value-of select="/root/gui/strings/edit"/>';
			var download = '<xsl:value-of select="/root/gui/strings/download"/>';
		</script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/thesaurusadmin.js" language="JavaScript"/>
	</xsl:template>
	<!--
	page content
	-->
	<xsl:template name="content">
			
	<table width="100%">
		<tr>
			<td>			
				<xsl:call-template name="formLayout">
					<xsl:with-param name="title" select="/root/gui/strings/thesaurus/management"/>
					<xsl:with-param name="content">
						<xsl:call-template name="adminform"/>
					</xsl:with-param>
				</xsl:call-template>				
			
				<xsl:call-template name="formLayout">
					<xsl:with-param name="title" select="/root/gui/strings/thesaurus/upload"/>
					<xsl:with-param name="content">
						<xsl:call-template name="uploadform"/>
					</xsl:with-param>
					<xsl:with-param name="buttons">
						<button class="content" type="button" onclick="load('{/root/gui/locService}/admin');"><xsl:value-of select="/root/gui/strings/back"/></button>
						&#160;
						<button class="content" onclick="goSubmit('UploadForm')"><xsl:value-of select="/root/gui/strings/upload"/></button>
						&#160;				
					</xsl:with-param>
				</xsl:call-template>						
		
			</td>
		</tr>
	</table>

	</xsl:template>

	<xsl:template name="adminform">
		<form name="AdminForm" method="get">
			<table id="myTable">
				<tr><td colspan="4"><hr color="#0263b2"/></td></tr>
				
				<tr>
				<td class="blue-contentBold" align="center">	
					<xsl:value-of select="/root/gui/strings/type"/>
  				</td>
				<td class="blue-contentBold">&#160;</td> 	
				<td class="blue-contentBold" align="center">	
					<xsl:value-of select="/root/gui/strings/name"/>
  				</td>
				<td class="blue-contentBold" align="center">	
					<xsl:value-of select="/root/gui/strings/operation"/>
  				</td>
				</tr>
				
				<tr><td colspan="4"><hr color="#0263b2"/></td></tr>
				
				<xsl:for-each select="/root/response/thesaurusList/directory">
				<xsl:sort select="@label" order="ascending"/>

  				<tr id="_tr{@label}">
					 <td class="padded-content" colspan="4"><xsl:value-of select="@label"/>&#160;<div id="_div{@label}" style="display:inline"><a href="javascript:addThesaurusEditRow('{@label}')"><img src="{/root/gui/url}/images/plus.gif"></img></a></div></td>			   
  				</tr>
					
					<xsl:for-each select="thesaurus">
    				<tr>
                     <td class="padded-content">&#160;</td>
	
					<td class="padded-content">	
						<xsl:value-of select="@type"/>
      				</td>
					<td class="padded-content">	
						<xsl:value-of select="fname"/>
      				</td>
      				<td class="padded-content">
    						<button type="button" class="content" onclick="load('{/root/gui/locService}/thesaurus.download?ref={@value}')"><xsl:value-of select="/root/gui/strings/download"/></button>
    						<xsl:text>&#160;</xsl:text>
    						<button  type="button" class="content" onclick="load('{/root/gui/locService}/thesaurus.delete?ref={@value}')"><xsl:value-of select="/root/gui/strings/delete"/></button>
							<xsl:text>&#160;</xsl:text>
    						<xsl:if test="@type='local'">
								<button type="button" class="content" onclick="load('{/root/gui/locService}/thesaurus.edit?selected={@value}&amp;mode=edit')">
    								<xsl:value-of select="/root/gui/strings/edit"/>
    							</button>    							
    							<xsl:text>&#160;</xsl:text>
    						</xsl:if>
    						<xsl:if test="@type='external'">
								<button type="button" class="content" onclick="load('{/root/gui/locService}/thesaurus.edit?selected={@value}&amp;mode=consult')">
    								<xsl:value-of select="/root/gui/strings/view"/>
    							</button>
    							<xsl:text>&#160;</xsl:text>
    						</xsl:if>									
      				</td>
    				</tr>
  				</xsl:for-each>
  			</xsl:for-each>
		</table>
		</form>
	</xsl:template>			



			
	<xsl:template name="uploadform">
		<form name="UploadForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/thesaurus.upload" enctype="multipart/form-data">
		
			<table align="left">
				<tr>
  				<td class="padded-content">
  					<table>
    					<tr>
        				<td class="padded-content"><xsl:value-of select="/root/gui/strings/thesaurus/category"/>
            			</td>
      					<td class="padded-content">
      						<select class="content" name="dir" size="1">
      							<xsl:for-each select="/root/gui/thesaurusCategory/thesaurusList/directory">
      								<option value="{@label}">
      									<xsl:value-of select="@label"/>
      								</option>
      							</xsl:for-each>
      						</select>
						</td>
    					</tr>
    					<tr>
        				<td class="padded-content"><xsl:value-of select="/root/gui/strings/file"/>
            			</td>
      					<td class="padded-content">
									<input type="file" accept="*.rdf" class="content" size="60" name="fname" value=""/>&#160;
								</td>
    					</tr>
  					</table>
  				</td>
				</tr>
			</table>
			
		</form>
	</xsl:template>

	<!-- Upload info -->
	<xsl:template name="result">	
		<xsl:if test="/root/response/record">
			<table align="left">
				<tr>
  				<td class="padded-content">
  					<table>
							<xsl:for-each select="/root/response/record">
	    					<tr>
	        				<td class="padded-content"><xsl:value-of select="/root/gui/strings/uploadResult"/>
	        				</td>
	      					<td class="padded-content"><xsl:value-of select="text()"/></td>
	    					</tr>
							</xsl:for-each>
  					</table>
  				</td>
				</tr>
			</table>
		</xsl:if>			
	</xsl:template>

</xsl:stylesheet>
