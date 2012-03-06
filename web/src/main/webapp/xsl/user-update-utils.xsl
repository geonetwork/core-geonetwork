<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
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
						<select class="content" size="1" name="profile" onchange="profileChanged()" id="user.profile">
							<xsl:for-each select="/root/gui/profiles/Administrator">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/Administrator"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/UserAdmin">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/UserAdmin"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/Reviewer">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/Reviewer"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/Editor">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.) or (count(/root/response/record)=0 and name(.)='Editor')">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/Editor"/>
								</option>
							</xsl:for-each>
							<xsl:for-each select="/root/gui/profiles/RegisteredUser">
								<option value="{name(.)}">
									<xsl:if test="/root/response/record/profile=name(.)">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/RegisteredUser"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
				
				<!-- Add groups -->
				
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
				</tr>
	</xsl:template>
	
</xsl:stylesheet>

