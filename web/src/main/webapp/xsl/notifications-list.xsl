<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


	<xsl:include href="main.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript">
			
			function dizable(id) {				
				var nameElement = $(id+"-name");
				nameElement.setAttribute("readonly", "readonly");
				var usernameElement = $(id+"-username");
				usernameElement.setAttribute("readonly", "readonly");
            	var passwordElement = $(id+"-password");
				passwordElement.setAttribute("readonly", "readonly");
				var urlElement = $(id+"-url");
				urlElement.setAttribute("readonly", "readonly");
				var enableElement = $(id+"-enabled");
				enableElement.setAttribute("onchange", "emable("+id+");");				
			}
			
			function emable(id) {
				var nameElement = $(id+"-name");
				nameElement.removeAttribute("readonly");
				var usernameElement = $(id+"-username");
				usernameElement.removeAttribute("readonly");
				var passwordElement = $(id+"-password");
				passwordElement.removeAttribute("readonly");
				var urlElement = $(id+"-url");
				urlElement.removeAttribute("readonly");
				var enableElement = $(id+"-enabled");
				enableElement.setAttribute("onchange", "dizable("+id+");");			
			}
			
			function removeNotification(id){
				var notificationRow = $(id+"-row");
				notificationRow.parentNode.removeChild(notificationRow);
			}
			
			var deleteText = '<xsl:value-of select="/root/gui/strings/delete"/>';
			
			function addNotification() {
				var notifications = $('notifications');
				var tmpID = tempId();
				// insert at third row: first row has column headers, second row has add button
				var newRow = notifications.insertRow(2);
				newRow.setAttribute("id", tmpID+"-row");
				var nameCell = newRow.insertCell(0);
				nameCell.style.margin = "5px";
				nameCell.style.padding = "5px";
				nameCell.innerHTML="&lt;input id='" + tmpID + "-name" + "' class='content' type='text' name='name-" + tmpID+ "' value='' size='30'/>";
				var usernameCell = newRow.insertCell(1);
				usernameCell.style.margin = "5px";
				usernameCell.style.padding = "5px";
				usernameCell.innerHTML="&lt;input id='" + tmpID + "-username" + "' class='content' type='text' name='username-" + tmpID+ "' value='' size='30'/>";
				var passwordCell = newRow.insertCell(2);
				passwordCell.style.margin = "5px";
				passwordCell.style.padding = "5px";
				passwordCell.innerHTML="&lt;input id='" + tmpID + "-password" + "' class='content' type='password' name='password-" + tmpID+ "' value='' size='30'/>";
                var urlCell = newRow.insertCell(3);
				urlCell.style.margin = "5px";
				urlCell.style.padding = "5px";
				urlCell.innerHTML="&lt;input id='" + tmpID + "-url" + "' class='content' type='text' name='url-" + tmpID+ "' value='' size='60'/>";
				var enabledCell = newRow.insertCell(4);
				enabledCell.style.margin = "5px";
				enabledCell.style.padding = "5px";
				enabledCell.innerHTML="&lt;input id='" + tmpID + "-enabled" + "' class='content' type='checkbox' name='enabled-" + tmpID+ "' checked='checked' onchange='dizable("+tmpID+");' />";				
				var removeCell = newRow.insertCell(5);
				removeCell.style.margin = "5px";
				removeCell.style.padding = "5px";
				removeCell.innerHTML = "&lt;img src='../../images/map/delete_layer.png' alt='"+deleteText+"' title='"+deleteText+"' onclick='removeNotification("+tmpID+");' onmouseover='this.style.cursor=\"pointer\";'/>";
			}
			
			function tempId() {
				var newDate = new Date;
				return newDate.getTime(); 
			}
			
			function saveNotifications() {
				// TODO some validation? E.g. if a name or url is not empty, then the corresponding url or name should not be empty ?
				document.notificationsUpdateForm.submit();
			}

			</script>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/notifications"/>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="saveNotifications()"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<form name="notificationsUpdateForm" accept-charset="UTF-8" action="{/root/gui/locService}/notifications.save" method="post">
			<input type="submit" style="display: none;" />
			<div id="this-is-just-to-get-the-content-left-thank-you" style="text-align:left;margin:0 auto 0 auto;">
				<table id="notifications" style="table-layout: fixed;">
					<col style="width:100px;"/>
                    <col style="width:100px;"/>
                    <col style="width:100px;"/>
					<col style="width:200px;"/>
					<col style="width:100px;"/>
					<col style="width:100px;"/>
					<tbody>
					<tr>
						<td>
							<xsl:value-of select="/root/gui/strings/name"/>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/username"/>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/password"/>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/url"/>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/enabled"/>
						</td>
						<td>
                            <xsl:value-of select="/root/gui/strings/addremove"/>
						</td>
					</tr>
					<tr>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>						
						<td style="margin:5px;padding:5px;">
								<img src="../../images/map/add_layer.png" alt="{/root/gui/strings/add}" title="{/root/gui/strings/add}" onclick="addNotification();" onmouseover="this.style.cursor='pointer';"/>
						</td>						
					</tr>					
					<xsl:for-each select="/root/response/response/record">
						<xsl:if test="id">
							<input type="hidden" name="id-{id}" size="-1" value="{id}"/>
						</xsl:if>
						<tr id="{id}-row">
							<xsl:choose>
								<xsl:when test="enabled = 'y'">
									<td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-name" name="name-{id}" value="{name}" size="30"/>
									</td>
							        <td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-username" name="username-{id}" value="{username}" size="30"/>
									</td>
							        <td style="margin:5px;padding:5px;">
										<input class="content" type="password" id="{id}-password" name="password-{id}" value="{password}" size="30"/>
									</td>
									<td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-url" name="url-{id}" value="{url}" size="60"/>
									</td>									
									<td style="margin:5px;padding:5px;">
										<input class="content" type="checkbox" id="{id}-enabled" name="enabled-{id}" checked="checked" onchange="dizable({id});" />
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-name" name="name-{id}" value="{name}" size="30" readonly="readonly"/>
									</td>
									<td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-username" name="username-{id}" value="{username}" size="30" readonly="readonly"/>
									</td>
									<td style="margin:5px;padding:5px;">
										<input class="content" type="password" id="{id}-password" name="password-{id}" value="{password}" size="30" readonly="readonly"/>
									</td>
									<td style="margin:5px;padding:5px;">
										<input class="content" type="text" id="{id}-url" name="url-{id}" value="{url}" size="60" readonly="readonly"/>
									</td>
									<td style="margin:5px;padding:5px;">										
										<input class="content" type="checkbox" id="{id}-enabled" name="enabled-{id}" onchange="emable({id});" />
									</td>
								</xsl:otherwise>
							</xsl:choose>
							<td style="margin:5px;padding:5px;">
								<img src="../../images/map/delete_layer.png" alt="{/root/gui/strings/delete}" title="{/root/gui/strings/delete}" onclick="removeNotification({id});" onmouseover="this.style.cursor='pointer';"/>
							</td>
						</tr>
					</xsl:for-each>
					</tbody>
				</table>
			</div>
		</form>
	</xsl:template>
	
</xsl:stylesheet>