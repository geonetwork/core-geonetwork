<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	

	<xsl:include href="../main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
			    <xsl:choose>
			     <xsl:when test="/root/gui/reqService = 'validated.shared.user.admin'">
    			     <xsl:value-of select="/root/gui/strings/userManagement"/> - <xsl:value-of select="/root/gui/strings/reusable/validated"/>
			     </xsl:when>
			     <xsl:otherwise>
                     <xsl:value-of select="/root/gui/strings/userManagement"/> - <xsl:value-of select="/root/gui/strings/reusable/nonValidated"/>
			     </xsl:otherwise>
			    </xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="load('{/root/gui/locService}/admin')"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
                <xsl:choose>
                 <xsl:when test="/root/gui/reqService = 'validated.shared.user.admin'">
                    <button class="content" onclick="load('{/root/gui/locService}/shared.user.edit?validated=y&amp;operation=newuser')"><xsl:value-of select="/root/gui/strings/newUser"/></button>
                 </xsl:when>
                 <xsl:otherwise>
                 </xsl:otherwise>
                </xsl:choose>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<script type="text/javascript" language="JavaScript1.2">
			function deleteUser(service, message, id){
				var cswContactId = '<xsl:value-of select="/root/gui/env/csw/contactId"/>';
			
				if (id == cswContactId) {
					if (!confirm("<xsl:value-of select="/root/gui/strings/delUserCsw"/>"))
						return null;
				}
				doConfirm(service, message);
				
			}
		</script>
		
		<table border="0">
            <xsl:if test="/root/gui/reqService = 'validated.shared.user.admin'">
	            <tr>
	                <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/username"/></b></th>
	                <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/surName"/></b></th>
	                <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/firstName"/></b></th>
	                <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/operation"/></b></th>
	            </tr>
                <tr class="h-line"></tr>
                <xsl:for-each select="/root/response/record[profile='Shared' and validated!='n']">
                    <tr>
                        <td class="padded"><xsl:value-of select="username"/></td>
                        <td class="padded"><xsl:value-of select="surname"/></td>
                        <td class="padded"><xsl:value-of select="name"/></td>
                        <td class="padded">
		                <xsl:choose>
		                 <xsl:when test="/root/gui/reqService = 'validated.shared.user.admin'">
                            <button class="content" onclick="load('{/root/gui/locService}/shared.user.edit?validated=y&amp;operation=fullupdate&amp;id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
                            &#160;
                            <xsl:if test="/root/gui/session/userId != id">
                                <button class="content" onclick="deleteUser('{/root/gui/locService}/validated.shared.user.remove?id={id}','{/root/gui/strings/delUserConf}', {id})"><xsl:value-of select="/root/gui/strings/delete"/></button>
                            </xsl:if>
		                 </xsl:when>
		                 <xsl:otherwise>
                            <button class="content" onclick="load('{/root/gui/locService}/shared.user.edit?validated=y&amp;operation=fullupdate&amp;id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
                            &#160;
                            <xsl:if test="/root/gui/session/userId != id">
                                <button class="content" onclick="deleteUser('{/root/gui/locService}/nonvalidated.shared.user.remove?id={id}','{/root/gui/strings/delUserConf}', {id})"><xsl:value-of select="/root/gui/strings/delete"/></button>
                            </xsl:if>
		                 </xsl:otherwise>
		                </xsl:choose>
                        </td>
                    </tr>
    			</xsl:for-each>
		    </xsl:if>
    			
            <xsl:if test="/root/gui/reqService = 'nonvalidated.shared.user.admin'">
                <tr>
                    <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/username"/></b></th>
                    <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/surName"/></b></th>
                    <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/firstName"/></b></th>
                    <th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/strings/operation"/></b></th>
                </tr>
                <tr class="h-line"></tr>
                <xsl:for-each select="/root/response/record[profile='Shared' and validated='n']">
                    <tr>
                        <td class="padded"><xsl:value-of select="username"/></td>
                        <td class="padded"><xsl:value-of select="surname"/></td>
                        <td class="padded"><xsl:value-of select="name"/></td>
                        <td class="padded">
                            <button class="content" onclick="load('{/root/gui/locService}/shared.user.edit?validated=n&amp;operation=fullupdate&amp;id={id}')"><xsl:value-of select="/root/gui/strings/edit"/></button>
                            &#160;
                            <xsl:if test="/root/gui/session/userId != id">
                                <button class="content" onclick="deleteUser('{/root/gui/locService}/nonvalidated.shared.user.remove?id={id}','{/root/gui/strings/delUserConf}', {id})"><xsl:value-of select="/root/gui/strings/delete"/></button>
                            </xsl:if>
                        </td>
                    </tr>
                </xsl:for-each>
            </xsl:if>			
		</table>
	</xsl:template>
</xsl:stylesheet>
