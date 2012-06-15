<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' encoding='UTF-8' indent='yes'/>

<xsl:include href="extent-util.xsl"/>

<xsl:template match="/">
	<xsl:variable name="mode" select="/root/request/mode"/>
	<xsl:variable name="hits" select="count(/root/response/wfs/featureType/child::*)"/>
	
    <xsl:variable name="typename" select="/root/response/wfs/featureType/@typename" />
    <xsl:variable name="wfs" select="/root/response/wfs/@id" />
    <xsl:variable name="numResults" select="/root/request/numResults"/>
    <xsl:variable name="page" select="/root/request/page"/>
    <xsl:variable name="multiplePages" select="/root/response/hasMore='true' or $page>1"/>
    <table width="100%"><tr><td>
		<xsl:choose>
			<xsl:when test="$multiplePages">
				<xsl:value-of select="/root/gui/strings/page"/>&#160;<xsl:value-of select="$page"/> 
			</xsl:when>
			<xsl:otherwise>
                <xsl:value-of select="/root/gui/strings/thesaurus/foundKeyWordsLimit"/>&#160;<xsl:value-of select="$hits"/>
			</xsl:otherwise>
		</xsl:choose>
	</td><td>
	</td></tr></table>


	<xsl:if test="$hits != 0">

		<table align="center" border="0" width="80%">
			<colgroup>
				<col width="5%" />
				<col width="30%" />
				<col width="40%" />
				<col width="20%" />
			</colgroup>
			<tr>
				<td colspan="4">
					<xsl:value-of select="/root/gui/strings/label" />
				</td>
			</tr>
			<tr>
				<td colspan="4">
					<div class="extentResults">
						<ul id="extentResults" name="extentResults">
							<xsl:for-each select="/root/response/wfs/featureType/feature">
								<xsl:call-template name="featureRow">
                                    <xsl:with-param name="feature" select="." />
									<xsl:with-param name="mode" select="$mode" />
								</xsl:call-template>
							</xsl:for-each>
						</ul>
					</div>
				</td>
			</tr>
			<xsl:if test="$multiplePages">
            <tr><td colspan="2">
                <xsl:variable name="nextpage" select="$page + 1"/>
			    <xsl:variable name="prevpage" select="$page - 1"/>
			
		        <xsl:choose>
		        <xsl:when test="$page &gt; 1">
		            <button id="previous" name="Previous" onclick="javascript:doSearchSubmit({$prevpage});" 
		                  class="content">
                        <xsl:value-of select="/root/gui/strings/previous" />
                     </button>
		        </xsl:when>
		        <xsl:otherwise>
		            <button id="previous" name="Previous" onclick="javascript:doSearchSubmit({$prevpage});" 
		                  disabled="disabled" class="content">                        
		                  <xsl:value-of select="/root/gui/strings/previous" />
	                  </button>
		        </xsl:otherwise>
		        </xsl:choose>   
		        &#160;
		        <xsl:choose>
		        <xsl:when test="/root/response/hasMore='true'">
		            <button id="next" name="Next" onclick="javascript:doSearchSubmit({$nextpage});"
		                  class="content">
	                      <xsl:value-of select="/root/gui/strings/next" />
                     </button>
		        </xsl:when>
		        <xsl:otherwise>
		            <button id="next" name="Next" onclick="javascript:doSearchSubmit({$nextpage});" 
		                  disabled="disabled" class="content">
                          <xsl:value-of select="/root/gui/strings/next" />
                     </button>
		        </xsl:otherwise>
		        </xsl:choose>
            </td>
            <td></td><td></td></tr>
            <br/>
            </xsl:if>
	    	<xsl:if test="$mode='edit'">
            <tr>
                <td colspan="2">
                    <button id="selectAll" name="selectAll" onclick="javascript:esearching.selectAll(true,'{$wfs}','{$typename}');"
                        class="content">
                        <xsl:value-of select="/root/gui/strings/extents/selectAll" />
                    </button>
                    &#160;
                    <button id="deselectAll" name="deselectAll" onclick="javascript:esearching.selectAll(false,'{$wfs}','{$typename}');"
                        class="content">
                        <xsl:value-of select="/root/gui/strings/extents/deselectAll" />
                    </button>
                    &#160;
                    <button id="del" name="del" onclick="javascript:removeExtent();"
                        class="content" disabled="disabled">
                        <xsl:value-of select="/root/gui/strings/delete" />
                    </button>
                    &#160;
                        <xsl:call-template name="buttonAdd">
                            <xsl:with-param name="typename" select="$typename" />
                            <xsl:with-param name="wfs" select="$wfs" />
                        </xsl:call-template>
                    </td>
                    <td></td><td></td>
                </tr>
			</xsl:if>
		</table>
	</xsl:if>
</xsl:template>

	
</xsl:stylesheet>