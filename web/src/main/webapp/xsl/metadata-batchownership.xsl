<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<!-- 
     - uses xml/metadata-batchNewOwner.xml on /root/gui/info
     - is the output sheet for the service metadata.PrepareBatchNewOwner
		 which supplies xml on /root/response/ids (the ids of the records to
		 be assigned the new owner) and /root/response/editor - a list of users
		 one of which will be the new owner
-->

	<xsl:include href="modal.xsl"/>

	<!-- ================================================================ -->
	<!-- page content -->
	<!-- ================================================================ -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="content">
			
				<xsl:variable name="lang" select="/root/gui/language"/>

				<form id="batchnewowner"  name="batchnewownerform" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.batch.newowner">
					<xsl:for-each select="/root/response/ids/*">
						<input name="_{.}" type="hidden" value="{.}"/>
					</xsl:for-each>

					<div style="width:100%">
					<div id="users" style="width:65%;float:left;overflow:auto;">
						<div id="users.title">
							<xsl:value-of select="/root/gui/info/users"/>
						</div>
						<div id="users.select">
							<select id="user" name="user" size="10" onchange="doGroups(this.value);">
								<xsl:for-each select="/root/response/editor">
									<xsl:sort select="username"/>
									<option>
										<xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
										<xsl:choose>
											<xsl:when test="normalize-space(name)='' and normalize-space(surname)=''">
												<xsl:value-of select="username"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="concat(profile,': ',username,' - ',name,' ',surname)"/>
											</xsl:otherwise>
										</xsl:choose>
									</option>
								</xsl:for-each>
							</select>
						</div>
					</div>
					<div id="groups" style="width:35%;float:right;">
						<div id="groups.title">
							<xsl:value-of select="/root/gui/info/groups"/>
						</div>
						<div id="groups.select">
							<select id="group" name="group" size="10">
							</select>
						</div>
					</div>
					</div>
			</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<div id="batchNewOwnerButtons" style="float:left;width:100%">
					<center>
      			<button class="content" onclick="checkBatchNewOwner('metadata.batch.newowner','{concat(/root/gui/strings/results,' ',/root/gui/strings/batchNewOwnerTitle)}')" type="button">
        			<xsl:value-of select="/root/gui/strings/transferOwnership"/>
      			</button>
					</center>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================ -->

</xsl:stylesheet>
