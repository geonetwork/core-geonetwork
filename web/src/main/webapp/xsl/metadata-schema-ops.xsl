<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sc="scaling">

	<xsl:include href="main.xsl"/>

	<!-- decide what to do based on whether the service is add, update or 
		   delete -->
	<xsl:variable name="formAction">
		<xsl:choose>
			<xsl:when test="contains(/root/gui/reqService,'add')">
				<xsl:text>metadata.schema.add</xsl:text>
			</xsl:when>
			<xsl:when test="contains(/root/gui/reqService,'update')">
				<xsl:text>metadata.schema.update</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>metadata.schema.delete</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:template mode="script" match="/">

		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"/>

	</xsl:template>

	<!-- page content -->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/schemaOps"/>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">

				<xsl:variable name="buttonName">
					<xsl:choose>
						<xsl:when test="contains($formAction,'add')">
							<xsl:value-of select="/root/gui/strings/add"/>
						</xsl:when>
						<xsl:when test="contains($formAction,'update')">
							<xsl:value-of select="/root/gui/strings/update"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="/root/gui/strings/delete"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<button class="content" type="button" id="btn" onclick="checkAndSubmitSchemaOps('{$formAction}')"><xsl:value-of select="$buttonName"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================== -->

	<xsl:template name="form">
		<script type="text/javascript">

			// enable the control attached to the radio button id supplied as arg
			// disable and clear all others
			function schemaOptEnable(radioId) {

				textId = radioId.sub('-radio','');

				var form = $('schema-ops');

				var radios = form.getInputs('radio');
				for (i = 0; i &lt; radios.length; i++) {
					radio = radios[i];
					if (radio.readAttribute('name') == radioId) {
						radio.checked = true;
					} else {
						radio.checked = false;
					}
				}

				var texts = form.getInputs('text');
				for (i = 0; i &lt; texts.length; i++) {
					text = texts[i];
					if (text.readAttribute('name') == textId) {
						text.enable();
					} else if (text.readAttribute('name') != 'schema') {
						text.disable();
						text.clear();
					}
				}
			}

			// check that at least one radio is enabled and that a value has been 
			// entered and that if schema list is present, one schema has been 
			// selected
			function checkAndSubmitSchemaOps(serviceName) {
				var form = $('schema-ops');
				var radios = form.getInputs('radio');
				for (i = 0; i &lt; radios.length; i++) {
					radio = radios[i];
					if (radio.checked) {
						textId = radio.identify().sub('-radio','');
						if (!$(textId).present()) {
							alert('No value entered for '+textId);
							return;
						}
					}
				}

				if ($F('schema')=='') {
					alert('No metadata schema selected/entered');
					return;
				}

				var schemaOpsOverlay = new Element("div", { id: "schemaOpsOverlay" });
				$('formContent').insert({'top':schemaOpsOverlay});
				$('schemaOpsOverlay').setStyle({opacity: "0.65"});

				var http = new Ajax.Request(
					serviceName,
					{
						method: 'put',
						parameters: $('schema-ops').serialize(true),
						onComplete: function(originalRequest){},
            onLoaded: function(originalRequest){},
            onSuccess: function(originalRequest){                                
              // get the XML root item
              var root = originalRequest.responseXML.documentElement;
              var resp = root.getAttribute('status');
              var mess = root.getAttribute('message');
							Element.remove($("schemaOpsOverlay"));

              if (resp == "ok")
                alert (mess);
              else
                alert(translate('error')+": "+mess);
						},
						onFailure: function(originalRequest){
							Element.remove($("schemaOpsOverlay"));
              var root = originalRequest.responseXML.documentElement;
							var errorid = root.getAttribute('id');
							alert(serviceName + ' failed: '+root.childNodes[0].nodeValue+' (Exception was '+errorid+')');
						}
					});
			}

		</script>

			
		<form name="schema-ops" id="schema-ops">
			<div class="table-container" id="formContent">
			<div class="table-row">
				<xsl:choose>
					<xsl:when test="contains($formAction,'update') or contains($formAction,'delete')">
						<div class="table-left">
							<xsl:value-of select="/root/gui/strings/selectSchema"/>
						</div>
						<div class="table-right">
							<select class="content" id="schema" name="schema" size="8">
								<xsl:for-each select="/root/gui/schemalist/name[@plugin='true']">
									<xsl:sort select="."/>
									<option value="{string(.)}">
										<xsl:value-of select="string(.)"/>
									</option>
								</xsl:for-each>
							</select>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<div class="table-left">
							<xsl:value-of select="/root/gui/strings/enterSchema"/>
						</div>
						<div class="table-right">
							<input name="schema" id="schema" class="content" type="text" size="80" value=""/>
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>

			<xsl:if test="not(contains($formAction,'delete'))">

				<div class="table-row">
					<div class="table-left">
						<xsl:value-of select="/root/gui/strings/selectSchemaOperations"/>
					</div>
				</div>
				<div class="table-row">
					<div class="table-left">
						<input class="labelField" type="radio" id="fname-radio" name="fname-radio" checked="true" onclick="schemaOptEnable(this.id)"/>
						<label for="fname-radio"><xsl:value-of select="/root/gui/strings/schemaFileName"/></label>&#160;
					</div>
					<div class="table-right">
						<input name="fname" id="fname" class="content" type="text" size="80" value=""/>
					</div>
				</div>

				<div class="table-row">
					<div class="table-left">
						<input class="content" type="radio" name="url-radio" id="url-radio" onclick="schemaOptEnable(this.id)"/>
						<label for="url-radio"><xsl:value-of select="/root/gui/strings/schemaUrl"/></label>&#160;
					</div>
					<div class="table-right">
						<input name="url" id="url" class="content" type="text" size="80" value=""/>
					</div>
				</div>
	
				<div class="table-row">
					<div class="table-left">
						<input class="content" type="radio" name="uuid-radio" id="uuid-radio"  onclick="schemaOptEnable(this.id)"/>
						<label for="uuid-radio"><xsl:value-of select="/root/gui/strings/schemaUuid"/></label>&#160;
					</div>
					<div class="table-right">
						<input name="uuid" id="uuid" class="content" type="text" size="80" value=""/>
					</div>
				</div>

			</xsl:if>
			</div>

		</form>			
	</xsl:template>

	<!-- ================================================================== -->
	
</xsl:stylesheet>
