<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	exclude-result-prefixes="#all">

	<xsl:variable name="modal" select="count(/root/gui/config/search/use-modal-box-for-banner-functions)"/>

	<!--
	main html banner
	-->
	<xsl:template name="banner">

		<table width="100%">

			<!-- title -->
			<tr class="banner">
				<td class="banner">
					<img src="{/root/gui/url}/images/header-left.jpg" alt="World picture" align="top" />
				</td>
				<td align="right" class="banner">
					<img src="{/root/gui/url}/images/header-right.gif" alt="GeoNetwork opensource logo" align="top" />
				</td>
			</tr>

			<!-- buttons -->
			<tr class="banner">
				<td class="banner-menu" width="380px">
					<a class="banner" href="{/root/gui/locService}/home"><xsl:value-of select="/root/gui/strings/home"/></a>
					|
					<xsl:if test="$modal">
						<xsl:if test="java:isAccessibleService('metadata.add.form')">
							<a class="banner" href="javascript:void(0)" onclick="doBannerButton('{/root/gui/locService}/metadata.create.form','{/root/gui/strings/newMetadata}',{$modal}, 600);"><xsl:value-of select="/root/gui/strings/newMetadata"/></a>
						|
						</xsl:if>
					</xsl:if>
					<xsl:if test="string(/root/gui/session/userId)!=''">
						<xsl:choose>
							<xsl:when test="/root/gui/reqService='admin'">
								<font class="banner-active"><xsl:value-of select="/root/gui/strings/admin"/></font>
							</xsl:when>
							<xsl:otherwise>
								<a class="banner" onclick="doAdminBannerButton('{/root/gui/locService}/admin','{/root/gui/strings/admin}','{$modal}',800, 500)" href="javascript:void(0);"><xsl:value-of select="/root/gui/strings/admin"/></a>
							</xsl:otherwise>
						</xsl:choose>
						|
					</xsl:if>
					<xsl:choose>
						<xsl:when test="/root/gui/reqService='feedback'">
							<font class="banner-active"><xsl:value-of select="/root/gui/strings/contactUs"/></font>
						</xsl:when>
						<xsl:otherwise>
							<a class="banner" onclick="doBannerButton('{/root/gui/locService}/feedback','{/root/gui/strings/contactUs}','{$modal}',600)" href="javascript:void(0);"><xsl:value-of select="/root/gui/strings/contactUs"/></a>
						</xsl:otherwise>
					</xsl:choose>
					|
					<xsl:choose>
						<xsl:when test="/root/gui/reqService='links'">
							<font class="banner-active"><xsl:value-of select="/root/gui/strings/links"/></font>
						</xsl:when>
						<xsl:otherwise>
							<a class="banner" onclick="doBannerButton('{/root/gui/locService}/links','{/root/gui/strings/links}','{$modal}',600)" href="javascript:void(0);"><xsl:value-of select="/root/gui/strings/links"/></a>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:if test="string(/root/gui/session/userId)='' and
											string(/root/gui/env/userSelfRegistration/enable)='true'">

						|
						<a class="banner" onclick="doBannerButton('{/root/gui/locService}/password.forgotten.form','{/root/gui/strings/changePassword}','1',300)" href="javascript:void(0);">
							<xsl:value-of select="/root/gui/strings/forgottenPassword"/>
						</a>
						|
						<a class="banner" onclick="doBannerButton('{/root/gui/locService}/user.register.get','{/root/gui/strings/registerTitle}','{$modal}',600)" href="javascript:void(0);">
							<xsl:value-of select="/root/gui/strings/register"/>
						</a>
					</xsl:if>
					|
					<xsl:choose>
						<xsl:when test="/root/gui/reqService='about'">
							<font class="banner-active"><xsl:value-of select="/root/gui/strings/about"/></font>
						</xsl:when>
						<xsl:otherwise>
							<a class="banner" onclick="doBannerButton('{/root/gui/locService}/about','{/root/gui/strings/about}','{$modal}',800)" href="javascript:void(0);"><xsl:value-of select="/root/gui/strings/about"/></a>
						</xsl:otherwise>
					</xsl:choose>
					|
					<!-- Help section to be displayed according to GUI language -->
					<xsl:choose>
						<xsl:when test="/root/gui/language='fr'">
							<a class="banner" href="{/root/gui/url}/docs/fra/users" target="_blank"><xsl:value-of select="/root/gui/strings/help"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a class="banner" href="{/root/gui/url}/docs/eng/users" target="_blank"><xsl:value-of select="/root/gui/strings/help"/></a>
						</xsl:otherwise>
					</xsl:choose>
					|
				</td>
				<td align="right" class="banner-menu" width="590px">
					<xsl:if test="count(/root/gui/config/languages/*) &gt; 1">
						<!-- Redirect to current page when no error could happen 
						(ie. when having no parameters in GET), if not redirect to the home page. -->
						<xsl:variable name="redirectTo">
						<xsl:choose>
							<xsl:when test="/root/gui/reqService='metadata.show'">main.home</xsl:when>
							<!-- TODO : Add other exception ? -->
							<xsl:otherwise><xsl:value-of select="/root/gui/reqService"/></xsl:otherwise>
						</xsl:choose>
						</xsl:variable>
						
						<select class="banner-content content">
							<xsl:attribute name="onchange">location.replace('../' + this.options[this.selectedIndex].value + '/<xsl:value-of select="$redirectTo"/>');</xsl:attribute>
							<xsl:for-each select="/root/gui/config/languages/*">
								<xsl:variable name="lang" select="name(.)"/>
								<option value="{$lang}">
									<xsl:if test="/root/gui/language=$lang">
										<xsl:attribute name="selected">selected</xsl:attribute>
									</xsl:if>
									<xsl:value-of select="/root/gui/strings/*[name(.)=$lang]"/>
								</option>	
							</xsl:for-each>
						</select>
					</xsl:if>
				</td>
			</tr>

			<!-- FIXME: should also contain links to last results and metadata -->

			<!-- login -->
			<tr class="banner">
				<td class="banner-login" align="right" width="380px">
					<!-- FIXME
					<button class="banner" onclick="goSubmit('{/root/gui/service}/es/main.present')">Last search results (11-20 of 73)</button>
					<a class="banner" href="{/root/gui/service}/es/main.present">Last search results (11-20 of 73)<xsl:value-of select="/root/gui/strings/results"/></a>
					-->
				</td>
				<xsl:choose>
					<xsl:when test="string(/root/gui/session/userId)!=''">
						<td align="right" class="banner-login">
							<form name="logout" action="{/root/gui/url}/j_spring_security_logout" method="post">
								<xsl:value-of select="/root/gui/strings/user"/>
								<xsl:text>: </xsl:text>
								<xsl:value-of select="/root/gui/session/name"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="/root/gui/session/surname"/>
								<xsl:text> </xsl:text>
								<button class="banner" onclick="doLogout()"><xsl:value-of select="/root/gui/strings/logout"/></button>
							</form>
						</td>
					</xsl:when>
					<xsl:when test="string(/root/gui/reqService) = 'login.form'">
					<!-- let login page display fields -->
					<td align="right" class="banner-login"></td>
					</xsl:when>
					<xsl:otherwise>
						<td align="right" class="banner-login">
							<xsl:choose>
								<xsl:when test="java:isCasEnabled()">
									<xsl:variable name="casparams">
										<xsl:apply-templates mode="casParams" select="root/request/*"></xsl:apply-templates>
									</xsl:variable>
									<a class="banner" href="{/root/gui/locService}/{/root/gui/reqService}?casLogin{$casparams}">
										<xsl:value-of select="/root/gui/strings/login"/>
									</a>
								</xsl:when>
								<xsl:otherwise>
									<form name="login" action="{/root/gui/url}/j_spring_security_check" method="post">
										<xsl:if test="string(/root/gui/env/shib/use)='true'">
											<a class="banner" href="{/root/gui/env/shib/path}">
												<xsl:value-of select="/root/gui/strings/shibLogin"/>
											</a>
											|
										</xsl:if>
										<input type="submit" style="display: none;" />
										<xsl:value-of select="/root/gui/strings/username"/>
										<input class="banner" type="text" id="username" name="username" size="10" onkeypress="return entSub('login')"/>
										<xsl:value-of select="/root/gui/strings/password"/>
										<input class="banner" type="password" id="password" name="password" size="10" onkeypress="return entSub('login')"/>
										<button class="banner" onclick="goSubmit('login')"><xsl:value-of select="/root/gui/strings/login"/></button>
									</form>
								</xsl:otherwise>
							</xsl:choose>
						</td>
					</xsl:otherwise>
				</xsl:choose>
			</tr>
		</table>
	</xsl:template>

	<!--
	main html banner in a popup window
	-->
	<xsl:template name="bannerPopup">

		<table width="100%">

			<!-- title -->
			<!-- TODO : Mutualize with main banner template -->
			<tr class="banner">
				<td class="banner">
					<img src="{/root/gui/url}/images/header-left.jpg" alt="GeoNetwork opensource" align="top" />
				</td>
				<td align="right" class="banner">
					<img src="{/root/gui/url}/images/header-right.gif" alt="World picture" align="top" />
				</td>
			</tr>

			<!-- buttons -->
			<tr class="banner">
				<td class="banner-menu" colspan="2">
				</td>
			</tr>

			<tr class="banner">
				<td class="banner-login" colspan="2">
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template mode="casParams" match="casLogin" priority="10"></xsl:template>
	<xsl:template mode="casParams" match="*">&amp;<xsl:value-of select="name(.)"/><xsl:if test="normalize-space(text())!=''">=<xsl:value-of select="text()"/></xsl:if></xsl:template>

</xsl:stylesheet>

