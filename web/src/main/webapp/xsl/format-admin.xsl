<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<xsl:template mode="script" match="/">
		<script type="text/javascript">
		function edit(id, action){
			var url = null;
		
			if (id == null)
				url = 'format?action=PUT&amp;name=' + $('addFName').value + '&amp;version=' + $('addFVersion').value;		
			else if (action == 'PUT')
				url = 'format?action=PUT&amp;id=' + id + 
					'&amp;name=' + $(id + 'FName').value +
					'&amp;version='+$(id + 'FVersion').value;
			else if (action == 'DELETE')
				url = 'format?action=DELETE&amp;id=' + id;
			
			if (url != null)
				new Ajax.Request(url, {
                    method: 'get',
					onSuccess: function(result){
						if (id == null || action == 'DELETE')
							location.reload();
						else if (action == 'PUT')
							$(id + 'bts').style.display='none';
					}
				});
		}
		
        function finishEditing(){
           if(window.opener) {  
             window.opener.location.href="javascript:refresh();";
           }
         }
		</script>
	</xsl:template>

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/format/manDes"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
			 <xsl:choose>
	                <xsl:when test="/root/request/dialog = true()">
	                    <button class="content" onclick="finishEditing();">
	                        <xsl:value-of select="/root/gui/strings/close"/>
	                    </button>
	                </xsl:when>
	                <xsl:otherwise>
						<button class="content" onclick="load('{/root/gui/locService}/admin')">
							<xsl:value-of select="/root/gui/strings/back"/>
						</button>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="form">

		<table>
            <tr>
                <th><b><xsl:value-of select="/root/gui/strings/reusable/validated"/></b></th>
            </tr>
            <tr>
                <th class="padded, bottom_border">
                    <xsl:value-of select="/root/gui/strings/name"/>
                </th>
                <th class="padded, bottom_border">
                    <xsl:value-of select="/root/gui/strings/version"/>
                </th>
            </tr>           
            <xsl:for-each select="/root/gui/formats/record[validated != 'n']">
                <xsl:sort select="name"/>
                <xsl:if test="not(/root/request/id) or /root/request/id=id">
                    <tr>
                        <td class="padded, bottom_border">
                            <input type="hidden" id="{id}" value="{id}"/>
                            <input type="text" id="{id}FName" value="{name}"
                                onkeyup="if((this.value+$('{id}FVersion').value)!='{name}{version}') $('{id}bts').style.display='block'; else $('{id}bts').style.display='none';"
                            />
                        </td>
                        <td class="padded, bottom_border">
                            <input type="text" id="{id}FVersion" value="{version}"
                                onkeyup="if(($('{id}FName').value+this.value)!='{name}{version}') $('{id}bts').style.display='block'; else $('{id}bts').style.display='none';"
                            />
                        </td>
                        <td>
                            <button class="content"
                                onclick="edit('{id}', 'DELETE');">
                                <xsl:value-of select="/root/gui/strings/delete"/>
                            </button>&#160; </td>
                        <td>
                            <button style="display:none;" id="{id}bts" class="content"
                                onclick="edit('{id}', 'PUT');">
                                <xsl:value-of select="/root/gui/strings/save"/>
                            </button>
                        </td>
                    </tr>
                </xsl:if>
            </xsl:for-each>
            <xsl:if test="/root/gui/formats/record[validated = 'n']">
                <tr>
                    <th class="padded, bottom_border">
                        <xsl:value-of select="/root/gui/strings/reusable/nonValidated"/>
                    </th>
                </tr>
                <tr>
                    <th class="padded, bottom_border">
                        <xsl:value-of select="/root/gui/strings/name"/>
                    </th>
                    <th class="padded, bottom_border">
                        <xsl:value-of select="/root/gui/strings/version"/>
                    </th>
                </tr>           
                <xsl:for-each select="/root/gui/formats/record[validated = 'n']">
                    <xsl:sort select="name"/>
                    <xsl:if test="not(/root/request/id) or /root/request/id=id">
                        <tr>
                            <td class="padded, bottom_border">
                                <input type="hidden" id="{id}" value="{id}"/>
                                <input type="text" id="{id}FName" value="{name}"
                                    onkeyup="if((this.value+$('{id}FVersion').value)!='{name}{version}') $('{id}bts').style.display='block'; else $('{id}bts').style.display='none';"
                                />
                            </td>
                            <td class="padded, bottom_border">
                                <input type="text" id="{id}FVersion" value="{version}"
                                    onkeyup="if(($('{id}FName').value+this.value)!='{name}{version}') $('{id}bts').style.display='block'; else $('{id}bts').style.display='none';"
                                />
                            </td>
                            <td>
                                <button class="content"
                                    onclick="edit('{id}', 'DELETE');">
                                    <xsl:value-of select="/root/gui/strings/delete"/>
                                </button>&#160; </td>
                            <td>
                                <button style="display:none;" id="{id}bts" class="content"
                                    onclick="edit('{id}', 'PUT');">
                                    <xsl:value-of select="/root/gui/strings/save"/>
                                </button>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
			<xsl:if test="not(/root/request/id)">
				<tr>
					<td class="padded, bottom_border">
						<input type="text" id="addFName" value=""/>
					</td>
					<td class="padded, bottom_border">
						<input type="text" id="addFVersion" value=""/>
					</td>
					<td colspan="2">
						<button class="content"
							onclick="edit(null);">
							<xsl:value-of select="/root/gui/strings/add"/>
						</button>
					</td>
				</tr>
			</xsl:if>
		</table>

	</xsl:template>

</xsl:stylesheet>
