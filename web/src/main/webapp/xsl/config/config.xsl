<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:threadUtils="java:org.fao.geonet.util.ThreadUtils" exclude-result-prefixes="#all">

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:variable name="users" select="/root/gui/users"/>
	<xsl:variable name="groups" select="/root/gui/groups/record"/>
	<xsl:variable name="configStrings" select="/root/gui/config"/>
	<xsl:variable name="separator" select="'/'"/>
	<xsl:variable name="settingLabel" select="/root/gui/config/setting"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<!-- CSS should move in CSS -->
		<style media="screen" type="text/css">
			form fieldset > div{
			    margin-bottom:2px;
			    text-align:left;
			    display:inline-block;
			    width:100%;
			}
			form fieldset legend{
			    font-weight:bold;
			    text-transform:uppercase;
			}
			form fieldset{
			    border:none;
			    margin-bottom:10px;
			    text-align:left;
			}
			form label{
			    float:left;
			    width:20%;
			    text-align:right;
			}
			form fieldset > div > div{
			    float:left;
			    width:70%;
			}
			form input{
			    clear:right;
			}
			
			form span{
			    font-style:italic;
			    color:gray;
			}</style>
	</xsl:template>


	<!-- Form layout and buttons -->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>

			<xsl:with-param name="content">
				<xsl:call-template name="panel"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="panel">
		<form action="config.set" name="settings" method="post">
			<xsl:for-each select="/root/settings/*">
				<xsl:sort select="@position" data-type="number"/>
				<xsl:apply-templates mode="make-settings-form" select="."/>
			</xsl:for-each>
		</form>
	</xsl:template>


	<xsl:template mode="make-settings-form" match="*[*]" priority="2">
		<xsl:variable name="name" select="name()"/>
		<xsl:variable name="translation" select="$settingLabel[@name = $name]/@label"/>
		<xsl:variable name="legend" select="if ($translation) then $translation else $name"/>
		<fieldset>
			<legend>
				<xsl:value-of select="$legend"/>
				<a href="{$name}"/>
			</legend>
			<xsl:variable name="tagName" select="name()"/>
			<xsl:for-each select="*">
				<xsl:sort select="@position" data-type="number"/>
				<!-- Define here some custom forms -->
				<xsl:choose>
					<xsl:when test="name() = 'indexoptimizer'">
						<xsl:call-template name="make-settings-form-for-indexoptimizer"/>
					</xsl:when>
					<xsl:when test="name() = 'threadedindexing'">
						<xsl:call-template name="make-settings-form-for-threadedindexing"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="make-settings-form" select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</fieldset>
	</xsl:template>


	<!-- Match all setting with no child -->
	<xsl:template mode="make-settings-form" match="*[not(*)]">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="isCheckBox" select="@datatype='2'"/>
		<xsl:variable name="isNumber" select="@datatype='1'"/>
		<xsl:variable name="isPassword" select="ends-with($name, 'password')"/>

		<!-- A settings:
		* matching true or false is displayed as a checkbox
		* with name ending with email is displayed as email field
		* else as text
		-->
		<xsl:variable name="type"
			select="if ($isCheckBox) then 'checkbox' 
			else if ($isNumber) then 'number' 
			else if ($isPassword) then 'password' 
			else if (ends-with($name, 'email')) then 'email' 
			else 'text'"/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<xsl:choose>
					<xsl:when test="$isCheckBox">
						<input type="{$type}" id="{$id}" value="{.}"
							onclick="document.getElementById('{$id}value').value=this.checked;">
							<xsl:if test=". = 'true'">
								<xsl:attribute name="checked"/>
							</xsl:if>
						</input>
						<!-- Add hidden field to submit both checked or unchecked states -->
						<input type="hidden" id="{$id}value" name="{translate(@name, '/', '.')}"
							value="{.}"/>
					</xsl:when>
					<xsl:otherwise>
						<input type="{$type}" id="{$id}" name="{translate(@name, '/', '.')}"
							value="{.}"/>
					</xsl:otherwise>
				</xsl:choose>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>

	<!-- Define custom settings fields here when a settings
	require a custom list or field. Text, boolean and number are handled by default
	mode. -->
	<xsl:template mode="make-settings-form" match="contactId" priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="{translate($name, '/', '.')}">

					<option value="-1"/>
					<xsl:for-each select="$users/record">
						<xsl:sort select="username"/>
						<option>
							<xsl:if test="$value = id">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<xsl:attribute name="value">
								<xsl:value-of select="id"/>
							</xsl:attribute>
							<xsl:value-of select="username"/>
							<xsl:text> ( </xsl:text>
							<xsl:value-of select="surname"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="name"/>
							<xsl:text> ) </xsl:text>
						</option>
					</xsl:for-each>
				</select>
				<span>
					<xsl:copy-of select="$settingLabel[@name = $name]/*"/>
					<!-- TODO add link to CSW configuration page -->
				</span>
			</div>
		</div>
	</xsl:template>


	<xsl:template mode="make-settings-form" match="mdmode" priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="{translate($name, '/', '.')}">
					<xsl:for-each select="$configStrings/datesearchopt/dsopt">
						<option value="{.}">
							<xsl:if test="$value = .">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@label"/>
						</option>
					</xsl:for-each>
				</select>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>

	<xsl:template mode="make-settings-form" match="defaultView" priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<xsl:variable name="views">
			<option value="simple"/>
			<option value="advanced"/>
			<option value="iso"/>
			<option value="inspire"/>
		</xsl:variable>
		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="{translate($name, '/', '.')}">
					<xsl:for-each select="$views/*">
						<option value="{@value}">
							<xsl:if test="$value = @value">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="@value"/>
						</option>
					</xsl:for-each>
				</select>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>


	<xsl:template mode="make-settings-form" match="*[@name='system/requestedLanguage/only']"
		priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="system.requestedLanguage.only">
					<option value="off">
						<xsl:if test="$value = 'off'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_offonly"/>
					</option>
					<option value="prefer_locale">
						<xsl:if test="$value = 'prefer_locale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguageprefer_locale"/>
					</option>
					<option value="prefer_docLocale">
						<xsl:if test="$value = 'prefer_docLocale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguageprefer_docLocale"/>
					</option>
					<option value="only_locale">
						<xsl:if test="$value = 'only_locale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_localeonly"/>
					</option>
					<option value="only_docLocale">
						<xsl:if test="$value = 'only_docLocale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_docLocaleonly"/>
					</option>
					<option value="prefer_ui_locale">
						<xsl:if test="$value = 'prefer_ui_locale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_prefer_ui_locale"/>
					</option>
					<option value="only_ui_locale">
						<xsl:if test="$value = 'only_ui_locale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_only_ui_locale"/>
					</option>
					<option value="prefer_ui_docLocale">
						<xsl:if test="$value = 'prefer_ui_docLocale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_prefer_ui_docLocale"/>
					</option>
					<option value="only_ui_docLocale">
						<xsl:if test="$value = 'only_ui_docLocale'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$configStrings/requestedlanguage_only_ui_docLocale"/>
					</option>
				</select>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>


	<xsl:template mode="make-settings-form" match="defaultGroup" priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="{translate($name, '/', '.')}">
					<xsl:for-each select="$groups">
						<xsl:sort select="name"/>
						<option value="{id}">
							<xsl:if test="$value = .">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="name"/>
						</option>
					</xsl:for-each>
				</select>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>



	<xsl:template mode="make-settings-form" match="protocol" priority="2">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="@name"/>
		<xsl:variable name="value" select="."/>

		<div>
			<label for="{$id}">
				<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
			</label>
			<div>
				<select name="{translate($name, '/', '.')}">
					<option value="http">
						<xsl:if test="$value = 'http'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>http </option>
					<option value="https">
						<xsl:if test="$value = 'https'">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>https </option>
				</select>
				<span>
					<xsl:value-of select="$settingLabel[@name = $name]"/>
				</span>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="make-settings-form-for-indexoptimizer">

		<fieldset>
			<legend>
				<xsl:value-of select="$settingLabel[@name = 'indexoptimizer']/@label"/>
				<a href="#indexoptimizer"/>
			</legend>
			<div>
				<label for="system.indexoptimizer.at.hour">
					<xsl:value-of select="$configStrings/at"/>
				</label>
				<xsl:variable name="hour" select="at/hour"/>
				<!-- Becausue all leaves are in a branch with same name -->
				<xsl:variable name="min" select="../indexoptimizer/at/min"/>

				<div>
					<select name="system.indexoptimizer.at.hour">
						<xsl:for-each select="$configStrings/hours/hour">
							<option value="{.}">
								<xsl:if test="$hour = .">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="@label"/>
							</option>
						</xsl:for-each>
					</select> : <select name="system.indexoptimizer.at.min">
						<xsl:for-each select="$configStrings/minutes/minute">
							<option value="{.}">
								<xsl:if test="$min = .">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="@label"/>
							</option>
						</xsl:for-each>
					</select>
					<span>
						<xsl:value-of select="$configStrings/atSpec"/>
					</span>
				</div>
			</div>

			<div>
				<label for="system.indexoptimizer.interval.hour">
					<xsl:value-of select="$configStrings/interval"/>
				</label>
				<xsl:variable name="interval" select="../indexoptimizer/interval/hour"/>
				<div>
					<select name="system.indexoptimizer.interval.day">
						<xsl:for-each select="$configStrings/hourintervals/hour">
							<option value="{.}">
								<xsl:if test="$interval = .">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="@label"/>
							</option>
						</xsl:for-each>
					</select>
					<span/>
				</div>
			</div>
		</fieldset>

	</xsl:template>


	<xsl:template name="make-settings-form-for-threadedindexing">
		<xsl:variable name="id" select="generate-id()"/>
		<xsl:variable name="name" select="'system/threadedindexing/maxthreads'"/>
		<xsl:variable name="value" select="maxthreads"/>

		<fieldset>
			<legend>
				<xsl:value-of select="$settingLabel[@name = 'threadedindexing']/@label"/>
				<a href="#threadedindexing"/>
			</legend>
			<div>
				<label for="{$id}">
					<xsl:value-of select="$settingLabel[@name = $name]/@label"/>
				</label>

				<div>
					<input type="number" id="{$id}" name="{translate($name, '/', '.')}" value="{.}"/>
					<span>
						<xsl:variable name="nrProcs" select="threadUtils:getNumberOfProcessors()"/>
						<xsl:choose>
							<xsl:when test="$nrProcs='1'">
								<xsl:value-of
									select="concat(':  ', $nrProcs, '  (', $configStrings/reasonForOne, ')')"
								/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$configStrings/recommendedThreads"/>: <span
									style="alert"><xsl:value-of select="$nrProcs"/></span>&#160;
									<xsl:value-of
									select="concat('(',$configStrings/reasonForMore,')')"/>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of select="$settingLabel[@name = $name]"/>
					</span>
				</div>
			</div>
		</fieldset>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button> &#160; <button class="content" onclick="document.forms.settings.submit()">
			<xsl:value-of select="/root/gui/config/save"/>
		</button>
	</xsl:template>
</xsl:stylesheet>
