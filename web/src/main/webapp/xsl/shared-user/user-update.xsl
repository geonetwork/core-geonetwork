<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:util="xalan://org.fao.geonet.util.XslUtil">

	<xsl:include href="../main.xsl"/>
    <xsl:include href="../translate-widget.xsl" />

	<!--
	additional scripts
	-->
    
	<xsl:template mode="script" match="/">
        <script src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js" type="text/javascript"/>
        <script src="{/root/gui/url}/scripts/ext/ext-all.js" type="text/javascript"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/translation_edit.js"/>
		<script type="text/javascript" language="JavaScript">
			// TODO : translate error message !
			function update1()
			{
				var invalid = " "; // Invalid character is a space
				var minLength = 6; // Minimum length

				if (document.userupdateform.username.value.length == 0) {
					alert('The username field is mandatory.');
					return;
				}
				// all ok, proceed
				document.userupdateform.submit();
				if(window.opener) {  
                    window.opener.location.href="javascript:refresh();";
                }
			}//update

			function init()
			{
			    var lang = '<xsl:value-of select="/root/gui/language"/>';
                editI18n.init('org', lang);
                editI18n.init('orgacronym', lang);
                editI18n.init('positionname', lang);
                editI18n.init('onlinename', lang);
                editI18n.init('onlinedescription', lang);
                editI18n.init('onlineresource', lang);
                editI18n.init('contactinstructions', lang);
			}
		</script>
	</xsl:template>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:choose>
					<xsl:when test="/root/response/record/id">
						<xsl:value-of select="/root/gui/strings/update"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="/root/gui/strings/insert"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()">
					<xsl:value-of select="/root/gui/strings/back"/>
				</button> &#160; <button class="content" onclick="update1()">
					<xsl:value-of select="/root/gui/strings/save"/>
				</button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!--
	form
	-->
	<xsl:template name="form">
	   <xsl:variable name="validatedPrefix">
	       <xsl:choose>
	           <xsl:when test="/root/request/validated = 'y'">validated</xsl:when>
	           <xsl:otherwise>nonvalidated</xsl:otherwise>
	       </xsl:choose>
	   </xsl:variable>
		<form name="userupdateform" class="users" accept-charset="UTF-8"
			 method="post">
			<xsl:attribute name="action"><xsl:value-of select="$validatedPrefix"/>.shared.user.update?operation=<xsl:value-of select="/root/request/operation"/>&amp;validated=<xsl:value-of select="/root/request/validated"/></xsl:attribute>

			<xsl:if test="/root/response/record/id">
				<input type="hidden" name="id" size="-1" value="{/root/response/record/id}"/>
			</xsl:if>
			<label for="username"><xsl:value-of select="/root/gui/strings/username"/> (*) </label><input class="content"
				type="text" id="username" name="username" value="{/root/response/record/username}"/><br/>
			<input class="content" type="password" id="password" name="password" value="shareduser" style="display:none"/>
			<input class="content" type="password" id="password2" name="password2" value="shareduser" style="display:none"/>
			<label for="surname"><xsl:value-of select="/root/gui/strings/surName"/> (*) </label>
			<input class="content" type="text" id="surname" name="surname"
				value="{/root/response/record/surname}"/><br/>
			<label for="name"><xsl:value-of select="/root/gui/strings/firstName"/> (*) </label>
			<input class="content" type="text" id="name" name="name" value="{/root/response/record/name}"/><br/>

<!-- #14078
			<label for="publicaccess" accesskey="A">
				<xsl:value-of select="/root/gui/strings/publicaccess"/>
			</label>
			<input type="checkbox" id="publicaccess" name="publicaccess">
				<xsl:if test="/root/response/record/publicaccess='y'">
					<xsl:attribute name="checked">checked</xsl:attribute>
				</xsl:if>
			</input><br/>
-->
            <br/>

			<fieldset class="users">
				<legend>
					<xsl:value-of select="/root/gui/strings/address"/>
				</legend>
				<label for="streetnumber" accesskey="N">
					<xsl:value-of select="/root/gui/strings/streetnumber"/>
				</label>
                <input class="content" type="text" id="streetnumber" name="streetnumber" size="4"
                    value="{/root/response/record/streetnumber}"/>
				<br/>
				<label for="streetname">
					<xsl:value-of select="/root/gui/strings/streetname"/>
				</label>
				<input class="content" type="text" id="streetname" name="streetname"
					value="{/root/response/record/streetname}"/>
				<br/>
				<label for="address" accesskey="A">
					<xsl:value-of select="/root/gui/strings/address"/>
				</label>
				<input class="content" type="text" id="address" name="address"
					value="{/root/response/record/address}"/>
				<br/>
				<label for="city" accesskey="C">
					<xsl:value-of select="/root/gui/strings/city"/>
				</label>
				<input class="content" type="text" id="city" name="city"
					value="{/root/response/record/city}" size="8"/>
				<br/>
				<label for="state" accesskey="A">
					<xsl:value-of select="/root/gui/strings/state"/>
				</label>
				<input class="content" type="text" id="state" name="state"
					value="{/root/response/record/state}" size="8"/>
				<br/>
				<label for="zip" accesskey="Z">
					<xsl:value-of select="/root/gui/strings/zip"/>
				</label>
				<input class="content" type="text" id="zip" name="zip"
					value="{/root/response/record/zip}" size="8"/>
				<br/>
				<label for="postbox" accesskey="B">
					<xsl:value-of select="/root/gui/strings/postbox"/>
				</label>
				<input class="content" type="text" id="postbox" name="postbox"
					value="{/root/response/record/postbox}" size="8"/>
				<br/>

				<label for="country" accesskey="C">
					<xsl:value-of select="/root/gui/strings/country"/>
               				</label>
                <select class="content" size="1" id="country" name="country">
                 <xsl:if test="string(/root/response/record/country)=''">
                  <option value="" />
                 </xsl:if>
                 <xsl:for-each select="/root/gui/countries/country">
                  <option value="{upper-case(@iso2)}">
                   <xsl:if test="upper-case(string(/root/response/record/country))=upper-case(string(@iso2))">
                    <xsl:attribute name="selected" />
                   </xsl:if>
                   <xsl:value-of select="." />
                  </option>
                 </xsl:for-each>
                </select>
			</fieldset>
			<fieldset class="users">
				<legend>
					<xsl:value-of select="/root/gui/strings/userdetails"/>
				</legend>
                    <label for="email" accesskey="E">
                        <xsl:value-of select="/root/gui/strings/email"/>
                    </label>
                    <input class="content" type="text" id="email" name="email"
                        value="{/root/response/record/email}"/>
                    <br/>
                    <label for="phone" accesskey="O">
                        <xsl:value-of select="/root/gui/strings/phone"/>
                    </label>
                    <input class="content" type="text" id="phone" name="phone"
                        value="{/root/response/record/phone}"/>
                    <br/>
                    <label for="facsimile" accesskey="F">
                        <xsl:value-of select="/root/gui/strings/facsimile"/>
                    </label>
                    <input class="content" type="text" id="facsimile" name="facsimile"
                        value="{/root/response/record/facsimile}"/>
                    <br/>
                    <br/>
                    <label for="email" accesskey="E">
                        <xsl:value-of select="/root/gui/strings/email"/>
                    </label>
                    <input class="content" type="text" id="email1" name="email1"
                        value="{/root/response/record/email1}"/>
                    <br/>
                    <label for="phone" accesskey="O">
                        <xsl:value-of select="/root/gui/strings/phone"/>
                    </label>
                    <input class="content" type="text" id="phone1" name="phone1"
                        value="{/root/response/record/phone1}"/>
                    <br/>
                    <label for="facsimile" accesskey="F">
                        <xsl:value-of select="/root/gui/strings/facsimile"/>
                    </label>
                    <input class="content" type="text" id="facsimile1" name="facsimile1"
                        value="{/root/response/record/facsimile1}"/>
                    <br/>
                    <br/>
                    <label for="email" accesskey="E">
                        <xsl:value-of select="/root/gui/strings/email"/>
                    </label>
                    <input class="content" type="text" id="email2" name="email2"
                        value="{/root/response/record/email2}"/>
                    <br/>
                    <label for="phone" accesskey="O">
                        <xsl:value-of select="/root/gui/strings/phone"/>
                    </label>
                    <input class="content" type="text" id="phone2" name="phone2"
                        value="{/root/response/record/phone2}"/>
                    <br/>
                    <label for="facsimile" accesskey="F">
                        <xsl:value-of select="/root/gui/strings/facsimile"/>
                    </label>
                    <input class="content" type="text" id="facsimile2" name="facsimile2"
                        value="{/root/response/record/facsimile2}"/>
                    <br/>
                    <br/>
				<label for="directnumber" accesskey="F">
					<xsl:value-of select="/root/gui/strings/directnumber"/>
				</label>
				<input class="content" type="text" id="directnumber" name="directnumber"
					value="{/root/response/record/directnumber}"/>
				<br/>
				<label for="facsimile" accesskey="F">
					<xsl:value-of select="/root/gui/strings/mobile"/>
				</label>
				<input class="content" type="text" id="mobile" name="mobile"
					value="{/root/response/record/mobile}"/>
				<br/>
			</fieldset>
			<fieldset class="users">
				<legend>
					<xsl:value-of select="/root/gui/strings/organisation"/>
				</legend>
				<label for="org" accesskey="G">
					<xsl:value-of select="/root/gui/strings/organisation"/>
				</label>
                   <xsl:call-template name="translationWidgetInputs">
                        <xsl:with-param name="key" select="'org'"/>
                        <xsl:with-param name="root" select="/root/response/record/organisation"/>
                   </xsl:call-template>
                   
                   <xsl:call-template name="translationWidgetSelect">
                        <xsl:with-param name="key" select="'org'"/>
                        <xsl:with-param name="class" select="'left-indent'"/>
                   </xsl:call-template>
				<br/>
				<label for="orgacronym">
					<xsl:value-of select="/root/gui/strings/orgacronym"/>
				</label>
                <xsl:call-template name="translationWidgetInputs">
                     <xsl:with-param name="key" select="'orgacronym'"/>
                     <xsl:with-param name="root" select="/root/response/record/orgacronym"/>
                </xsl:call-template>
                
                <xsl:call-template name="translationWidgetSelect">
                     <xsl:with-param name="key" select="'orgacronym'"/>
                     <xsl:with-param name="class" select="'left-indent'"/>
                </xsl:call-template>
				<br/>
				<label id="positionname" for="positionname">
					<xsl:value-of select="/root/gui/strings/positionname"/>
				</label>
                <xsl:call-template name="translationWidgetInputs">
                     <xsl:with-param name="key" select="'positionname'"/>
                     <xsl:with-param name="root" select="/root/response/record/positionname"/>
                </xsl:call-template>
                
                <xsl:call-template name="translationWidgetSelect">
                     <xsl:with-param name="key" select="'positionname'"/>
                     <xsl:with-param name="class" select="'left-indent'"/>
                </xsl:call-template>
				<br/>
                <fieldset class="users">
                    <legend>
                        <xsl:value-of select="/root/gui/strings/onlineresource"/>
                    </legend>
                    <label for="onlineresource" accesskey="W">
                        <xsl:value-of select="/root/gui/strings/url"/>
                    </label>

                    <xsl:call-template name="translationWidgetInputs">
                         <xsl:with-param name="key" select="'onlineresource'"/>
                         <xsl:with-param name="root" select="/root/response/record/onlineresource"/>
                    </xsl:call-template>
                    
                    <xsl:call-template name="translationWidgetSelect">
                         <xsl:with-param name="key" select="'onlineresource'"/>
                         <xsl:with-param name="class" select="'left-indent'"/>
                    </xsl:call-template>
                    <br/>
                    <label for="onlinename">
                        <xsl:value-of select="/root/gui/strings/name"/>
                    </label>
                    <xsl:call-template name="translationWidgetInputs">
                         <xsl:with-param name="key" select="'onlinename'"/>
                         <xsl:with-param name="root" select="/root/response/record/onlinename"/>
                    </xsl:call-template>
                    
                    <xsl:call-template name="translationWidgetSelect">
                         <xsl:with-param name="key" select="'onlinename'"/>
                         <xsl:with-param name="class" select="'left-indent'"/>
                    </xsl:call-template>
                    <br/>
                    <label for="onlinedescription">
                        <xsl:value-of select="/root/gui/strings/desc"/>
                    </label>
                    <xsl:call-template name="translationWidgetInputs">
                         <xsl:with-param name="key" select="'onlinedescription'"/>
                         <xsl:with-param name="root" select="/root/response/record/onlinedescription"/>
                    </xsl:call-template>
                    
                    <xsl:call-template name="translationWidgetSelect">
                         <xsl:with-param name="key" select="'onlinedescription'"/>
                         <xsl:with-param name="class" select="'left-indent'"/>
                    </xsl:call-template>
                    <br/>
                </fieldset>
				<br/>
				<label for="hoursofservice" accesskey="H">
					<xsl:value-of select="/root/gui/strings/hoursofservice"/>
				</label>
				<input class="content" type="text" id="hoursofservice" name="hoursofservice"
					value="{/root/response/record/hoursofservice}"/>
				<br/>
				<label for="contactinstructions" accesskey="I">
					<xsl:value-of select="/root/gui/strings/contactinstructions"/>
				</label>
				   <xsl:call-template name="translationWidgetInputs">
                        <xsl:with-param name="key" select="'contactinstructions'"/>
                        <xsl:with-param name="root" select="/root/response/record/contactinstructions"/>
                   </xsl:call-template>
                   
                   <xsl:call-template name="translationWidgetSelect">
                        <xsl:with-param name="key" select="'contactinstructions'"/>
                        <xsl:with-param name="class" select="'left-indent'"/>
                   </xsl:call-template>
				<br/>
				<label for="kind" accesskey="I">
					<xsl:value-of select="/root/gui/strings/kind"/>
				</label>
				<select class="content" size="1" id="kind" name="kind">
					<xsl:for-each select="/root/gui/strings/kindChoice">
						<xsl:sort select="."/>
						<option value="{@value}">
							<xsl:if test="string(/root/response/record/kind)=@value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="."/>
						</option>
					</xsl:for-each>
				</select><br/>
			</fieldset>
			<input class="content" size="1" name="profile" value="Shared" id="user.profile" style="display:none"/>
		</form>
	</xsl:template>

</xsl:stylesheet>
