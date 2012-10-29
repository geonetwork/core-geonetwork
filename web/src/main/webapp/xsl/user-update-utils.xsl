<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--
		additional scripts
	-->
	<xsl:template mode="script" match="/" name="user-admin-js">
		<script type="text/javascript" language="JavaScript">
            function profileChanged() {
                // If user is administrator, do not manage user groups
                var isadmin = $F('isadmin');
                if (isadmin !== null) {
                    Element.hide('group.list');
                    $('user.profile').value = 'Administrator';
                    return;
                } else
                    Element.show('group.list');
                
                var profiles = ['RegisteredUser', 'Editor', 'Reviewer', 'UserAdmin'];
                var profile = $F('user.profile');
                
                // Define the highest profile for user
                var newprofile = 'RegisteredUser';
                for (var i = 0; i &lt; profiles.length; i++) {
                    var groups = $('groups_' + profiles[i]);
                    // If one of the group is selected, main user profile is updated
                    if (groups.selectedIndex &gt; 0 &amp;&amp; groups.options[groups.selectedIndex].value != '') {
                       newprofile = profiles[i];
                    }
                }
                //console.log(profile + "->" + newprofile);
                $('user.profile').value = newprofile;
                
                // If user is reviewer in one group, he is also editor for that group
                var editorGroups = $('groups_Editor');
                var reviewerGroups = $('groups_Reviewer');
                if(reviewerGroups.selectedIndex &gt; 0) {
                    for (var j = 0; j &lt; reviewerGroups.options.length; j++){
                        if (reviewerGroups.options[j].selected) {
                            editorGroups.options[j].selected = true;
                        }
                        // We can't turn off editor groups according to reviewer groups
                        // A user may be editor for a group and not a reviewer
                    }
                }
            }
            
            function init() {
                profileChanged();
            }
</script>
	</xsl:template>
	
	<xsl:template name="userinfofields">
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/surName"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="surname" value="{/root/response/record/surname}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/firstName"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="name" value="{/root/response/record/name}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/address"/></th>
					<td class="padded"><input class="content" type="text" name="address" value="{/root/response/record/address}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/city"/></th>
					<td class="padded"><input class="content" type="text" name="city" value="{/root/response/record/city}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/state"/></th>
					<td class="padded"><input class="content" type="text" name="state" value="{/root/response/record/state}" size="8"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/zip"/></th>
					<td class="padded"><input class="content" type="text" name="zip" value="{/root/response/record/zip}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/country"/></th>
					<td class="padded">
						<select class="content" size="1" name="country">
							<xsl:if test="string(/root/response/record/country)=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/countries/country">
								<xsl:sort select="."/>
								<option value="{@iso2}">
									<xsl:if test="string(/root/response/record/country)=@iso2">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/email"/> (*)</th>
					<td class="padded"><input class="content" type="text" name="email" value="{/root/response/record/email}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/organisation"/></th>
					<td class="padded"><input class="content" type="text" name="org" value="{/root/response/record/organisation}"/></td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/kind"/></th>
					<td class="padded">
						<select class="content" size="1" name="kind">
							<xsl:for-each select="/root/gui/strings/kindChoice">
								<xsl:sort select="."/>
								<option value="{@value}">
									<xsl:if test="string(/root/response/record/kind)=@value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="."/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				<tr>
					<th class="padded"><xsl:value-of select="/root/gui/strings/profile"/></th>
					<td class="padded">
						<input type="checkbox" id="isadmin" onclick="profileChanged()">
							<xsl:if test="/root/response/record/profile='Administrator'">
								<xsl:attribute name="checked"/>
							</xsl:if>
							<xsl:if test="/root/gui/session/profile!='Administrator'">
								<xsl:attribute name="disabled"/>
							</xsl:if>
						</input><label for="isadmin"><xsl:value-of select="/root/gui/strings/isAdmin"/></label>
						<input type="hidden" id="user.profile" name="profile" value="{/root/response/record/profile}"/>
						
						<br/>
						<div id="group.list">
							<xsl:variable name="lang" select="/root/gui/language"/>
							
							<xsl:for-each select="/root/gui/profiles/*[not(name(.)=('Monitor', 'Administrator'))]">
								<div style="width:100%; margin: 5px">
									<xsl:variable name="profileName" select="name(.)"/>
									<xsl:value-of select="/root/gui/strings/*[name()=$profileName]"/><br/>
									<select class="content" style="background-color: #FFF !important;" size="7"
										id="groups_{$profileName}" name="groups_{$profileName}" 
										multiple="" onChange="profileChanged()">
										
										<xsl:choose>
											<xsl:when test="not(/root/gui/session/profile=('UserAdmin', 'Administrator'))">
												<xsl:attribute name="disabled">disabled</xsl:attribute>
											</xsl:when>
										</xsl:choose>
										<!--[id=/root/gui/groupsAndProfiles/record[profile=$name]/id]
										-->
										<option value=""/>
										<xsl:for-each select="/root/gui/groups/record">
											<xsl:sort select="name"/>
											<option value="{id}">
												<xsl:variable name="aGroup" select="id"/>
												<xsl:for-each select="/root/response/groups/id[.=$aGroup and @profile=$profileName]">
													<xsl:attribute name="selected"/>
												</xsl:for-each>
												<xsl:value-of select="label/child::*[name() = $lang]"/>
											</option>
										</xsl:for-each>
									</select>
								</div>
							</xsl:for-each>
						</div>
					</td>
				</tr>
				
				<!-- Add groups 
				
				<xsl:variable name="lang" select="/root/gui/language"/>

				<tr id="group.list">
					<th class="padded"><xsl:value-of select="/root/gui/strings/usergroups"/></th>
					<td class="padded">
						<select class="content" size="7" name="groups" multiple="" id="groups">
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort select="name"/>
								<option value="{id}">
									<xsl:variable name="aGroup" select="id"/>
									<xsl:for-each select="/root/response/groups/id">
										<xsl:if test="$aGroup=(.)">
											<xsl:attribute name="selected"/>
										</xsl:if>
									</xsl:for-each>
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
					</tr>-->
	</xsl:template>
	
</xsl:stylesheet>

