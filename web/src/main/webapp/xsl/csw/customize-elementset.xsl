<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="../main.xsl"/>

	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript1.2">

            function save() {
                document.customElementsetForm.submit();
            }

			function addElement(thisnode) {
				var newNode = document.createElement('div');
				newNode.style.cssText = "margin-top:5px;";

				var newRemove = document.createElement('img');
				newRemove.setAttribute("src", "../../images/map/delete_layer.png");
				newRemove.setAttribute("alt", "{/root/gui/strings/delete}");
				newRemove.setAttribute("title", "{/root/gui/strings/delete}");
				newRemove.setAttribute("onclick", "removeElement(this.parentNode);");
				newRemove.setAttribute("onmouseover", "this.style.cursor='pointer';");

				var newInput = document.createElement('input');
				newInput.style.cssText = style="width:80%;";
				newInput.setAttribute("type","text");
				newInput.setAttribute("value","");
				newInput.setAttribute("name","xpath");
				newInput.setAttribute("maxlength","1000");

				newNode.appendChild(newRemove);
				newNode.appendChild(newInput);
				insertAfter(thisnode, newNode);
			}

			function removeElement(node) {
				node.parentNode.removeChild(node);
			}

			function insertAfter( refNode, newNode ) {
				refNode.parentNode.insertBefore(newNode, refNode.nextSibling);
			}
		</script>
	</xsl:template>

	<!-- ================================================================================== -->
	<!-- page content -->
	<!-- ================================================================================== -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/customize-elementset"/>
			<xsl:with-param name="content">
				<xsl:choose>
					<xsl:when test="not(/root/gui/customelementsets/cswEnabled)">
						<table>
							<tr>
								<td>
									<xsl:value-of select="/root/gui/strings/csw-server-disabled"/>
								</td>
							</tr>
						</table>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="custom-elementset-list-form"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<xsl:if test="/root/gui/customelementsets/cswEnabled">
					<button class="content" onclick="save();return false;">
						<xsl:value-of select="/root/gui/strings/save"/>
					</button>
					&#160;
				</xsl:if>
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="custom-elementset-list-form">
		<div id="search-results-content">
			<form id="customElementsetForm" name="customElementsetForm" accept-charset="UTF-8" action="{/root/gui/locService}/csw.customelementset.set" method="post">
				<xsl:comment>list of elementsets</xsl:comment>
				<xsl:call-template name="display-elementsets"/>
			</form>
		</div>

	</xsl:template>

	<!-- ================================================================================== -->
	<!-- display element xpaths -->
	<!-- ================================================================================== -->

	<xsl:template name="display-elementsets">
		<xsl:comment>custom elementset</xsl:comment>
		<div id="elementset-list" class="select-metadata-hits" style="margin:30px 0px 30px 0px;text-align:left;">
			<div style="text-align:left;line-height:1.5em;">
                <xsl:value-of select="/root/gui/strings/custom-elementset-intro"/>
			</div>
			<div style="line-height:1.5em;">
				<xsl:value-of select="/root/gui/strings/custom-elementset-revert"/>
			</div>
			<div style="margin:10px 0px;" onclick="addElement(this);" onmouseover="this.style.cursor='pointer';">
				<img src="../../images/map/add_layer.png" alt="{/root/gui/strings/add}" title="{/root/gui/strings/add}"/>
				<xsl:value-of select="/root/gui/strings/custom-elementset-add"/>
			</div>
			<xsl:for-each select="/root/gui/customelementsets/xpath">
				<xsl:variable name="i" select="position()"/>
				<div style="margin-top:5px;" id="{$i}">
					<img src="../../images/map/delete_layer.png" alt="{/root/gui/strings/delete}" title="{/root/gui/strings/delete}" onclick="removeElement(this.parentNode);" onmouseover="this.style.cursor='pointer';"/>
					<input name="xpath" type="text" value="{.}" maxlength="1000" style="width:80%;"/>
				</div>
			</xsl:for-each>
		</div>
	</xsl:template>

</xsl:stylesheet>