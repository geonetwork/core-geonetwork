<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>
	
		<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<!-- javascript -->
	  <script type="text/javascript" src="../../scripts/ext/adapter/ext/ext-base.js"></script>
	  <script type="text/javascript" src="../../scripts/ext/ext-all.js"></script>
    <script type="text/javascript" src="../../scripts/lib/gn.geo.libs.js"></script>
	  
		<script language="JavaScript1.2" type="text/javascript">
    OpenLayers.ProxyHost = '<xsl:value-of select="/root/gui/config/proxy-url"/>url=';
    
    var locService= '<xsl:value-of select="/root/gui/locService"/>';
    var create = '<xsl:value-of select="/root/gui/strings/create"/>';
    var remove = '<xsl:value-of select="/root/gui/strings/delete"/>';
    var edit =  '<xsl:value-of select="/root/gui/strings/edit"/>';
    var download = '<xsl:value-of select="/root/gui/strings/download"/>';

    function submit() {
    	var f = document.UploadForm;
    	var value = "";
    	
    	for (var i=0; i &lt; f.mode.length; i++){
          if (f.mode[i].checked){
              value = f.mode[i].value;
          }
      }
    	
      if (value == 'url') {
        f.enctype="application/x-www-form-urlencoded";
    		f.encoding="application/x-www-form-urlencoded";
      } else {
        f.enctype="multipart/form-data";
    		f.encoding="multipart/form-data";
      }
      goSubmit('UploadForm');
    }

    function readRepository () {
      var feed = $('thesaurusFeed').value;
      OpenLayers.Request.GET({
          url : feed,
          success : function(response) {
              if (response.responseText.indexOf('error')!=-1) {  // GeoNetwork proxy return "Some unexpected error occurred"
                $('thesaurusList').innerHTML = '<li>Error loading GeoNetwork thesaurus feed.</li>';
                return;
              }

              var xml = response.responseXML;
              var entries = xml.getElementsByTagName('entry');
              var html = "";
              for (i=0; i&lt;entries.length; i++) {
                var title = entries[i].getElementsByTagName('title')[0].firstChild.nodeValue;
                var link = entries[i].getElementsByTagName('link')[0].getAttribute('href');
                var category = entries[i].getElementsByTagName('category')[0].firstChild.nodeValue;
                var url = "thesaurus.upload?type=external&amp;dir=" + category + "&amp;url=" + link;
                html += '<li>' + title + ' : <a href="' + url + '">Add to the catalogue</a></li>';
              }
              $('thesaurusList').innerHTML = html;
          },
          failure : function(response) {
              $('thesaurusList').innerHTML = '<li>Error loading GeoNetwork thesaurus feed.</li>';
          }
      });
    }

    function init() {
      readRepository();
    }
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
						<button class="content" onclick="submit();"><xsl:value-of select="/root/gui/strings/upload"/></button>
						&#160;				
					</xsl:with-param>
				</xsl:call-template>						
		
		    <xsl:if test="/root/gui/config/repository/thesaurus">
  				<xsl:call-template name="formLayout">
  					<xsl:with-param name="title" select="/root/gui/strings/thesaurus/load"/>
  					<xsl:with-param name="content">
  					  <input type="hidden" value="{/root/gui/config/repository/thesaurus}" id="thesaurusFeed"/>
  						<ul id="thesaurusList" style="text-align: left;">
  						</ul>
  					</xsl:with-param>
  					<xsl:with-param name="buttons"/>
  				</xsl:call-template>						
		    </xsl:if>
			</td>
		</tr>
	</table>

	</xsl:template>

	<xsl:template name="adminform">
		<form name="AdminForm" method="get">
			<table id="myTable" class="text-aligned-left">
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
		<form id="UploadForm" name="UploadForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/thesaurus.upload" enctype="multipart/form-data">
		
			<table align="left"  class="text-aligned-left">
				<tr>
  				<td class="padded-content">
  					<table>
    			      <tr>
        				<td class="padded-content"><xsl:value-of select="/root/gui/strings/thesaurus/category"/></td>
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
    				  	<td class="padded-content"><input class="labelField" type="radio" id="mode" name="mode" 
    				  		checked="true" onclick="url.setValue('')" value="file"/>
    				  		<label for="mode"><xsl:value-of select="/root/gui/strings/file"/></label></td>
      					<td class="padded-content">
      						<input type="file" accept="*.rdf" class="content" size="60" name="fname" value="" onchange="$('UploadForm').mode[0].checked=true;"/>
						</td>
    				  </tr>
	  			      <tr>
	  			      	<td class="padded-content"><input class="labelField" type="radio" id="mode" name="mode" 
	  			      		onclick="fname.setValue('')" value="url"/>
	  						<label for="mode"><xsl:value-of select="/root/gui/strings/url"/></label></td>
	  					<td class="padded-content">
	  						<input class="content" size="60" name="url" value="" onchange="fname.setValue('');$('UploadForm').mode[1].checked=true;"/>
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
