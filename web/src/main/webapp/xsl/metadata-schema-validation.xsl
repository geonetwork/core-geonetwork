<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sc="scaling">

	<xsl:include href="banner.xsl" />

	<!-- page content -->
	<xsl:template match="/">
		<html lang="en" ng-app="metadataSchemaValidation">
			<head>
				<meta charset="utf-8" />
				<meta name="viewport" content="width=device-width, initial-scale=1.0" />
				<title>Metadata Validation</title>
				<link rel="stylesheet"
					href="../../apps/shared-objects/app/lib/bootstrap3/css/bootstrap.css" />
        		<link href="../../apps/shared-objects/app/lib/bootstrap3/css/bootstrap-glyphicons.css" 
        			type="text/css" rel="stylesheet"/>
				<link rel="stylesheet" href="../../geocat.css" />
			</head>
			<body>
				<div id="header">
					<xsl:call-template name="banner" />
				</div>

				<script src="../../apps/shared-objects/app/lib/jquery.js"></script>
				<script
					src="../../apps/shared-objects/app/lib/angular/angular.min.js"></script>
				<script
					src="../../apps/shared-objects/app/lib/bootstrap3/js/ui-bootstrap-tpls-0.9.0.js"></script>
				<script
					src="../../apps/shared-objects/app/js/metadata.schema.validation.app.js"></script>


				<div id="content_container" class="angularapp">
					<h3>
						<xsl:value-of select="/root/gui/strings/metadataschemaValidate" />
					</h3>
					<p>
						<xsl:value-of select="/root/gui/strings/metadataschemaValidateDes" />
					</p>

					<div class="metadataValidation">
						<form method="post" name="criteria" ng-submit="submit()"
							ng-controller="addNewEntry">
							<div>
								<label>
									<xsl:value-of select="/root/gui/strings/xpathschematron" />
									:
								</label>

								<select ng-model="formData.schematron" name="schematron"
									autofocus="autofocus" required="true">
									<xsl:for-each select="/root/schematron/schematron">
										<option>
											<xsl:attribute name="value"><xsl:value-of
												select="id" /></xsl:attribute>
											[
											<xsl:value-of select="isoschema" />
											]
											<xsl:value-of select="tokenize(file,'/')[last()]" />
										</option>
									</xsl:for-each>
								</select>
							</div>
							<div>
								<label>
									<xsl:value-of select="/root/gui/strings/xpathlabel" />
									:
								</label>
								<select ng-model="formData.type" name="type" id="xpathtype"
									placeholder="{/root/gui/strings/xpathlabel}" required="true">
									<option value="KEYWORD">
										<xsl:value-of select="/root/gui/strings/capitalKeyword" />
									</option>
									<option value="GROUP">
										<xsl:value-of select="/root/gui/strings/group" />
									</option>
								</select>
							</div>

							<script type="text/ng-template" id="customTemplate.html">
								<a>
									<span bind-html-unsafe="match.label | typeaheadHighlight:query"></span>
								</a>
							</script>

							<div class='container-fluid' ng-controller="TypeaheadCtrl">
								<label>
									<xsl:value-of select="/root/gui/strings/xpathvalue" />
									:
								</label>
								<input type="hidden" required="true" name="value"
									ng-model="formData.value" id="xpath" placeholder="{/root/gui/strings/xpathvalue}" />

								<input type="text" id="keyword" placeholder="{/root/gui/strings/xpathvalue}"
									ng-model="asyncSelected" typeahead-editable="false"
									typeahead-on-select="updateVal($item, $model, $label)"
									typeahead="keyword.value as keyword.label for keyword in getKeywords($viewValue) | filter:$viewValue | limitTo:8" />

								<input style="display:none" type="text" id="group"
									placeholder="{/root/gui/strings/xpathvalue}" ng-model="asyncSelected"
									typeahead-editable="false" typeahead-on-select="updateVal($item, $model, $label)"
									typeahead="group.value as group.label for group in getGroups($viewValue) | filter:$viewValue | limitTo:8" />
							</div>
							<input type="submit" />
						</form>
					</div>
					<br/>
					<div>
						<table id="resultTable" class="table table-condensed"
							ng-controller="table_controller">
							<thead>
								<tr>
									<th>
										<xsl:value-of select="/root/gui/strings/xpathrequired" />
									</th>
									<th>
										<xsl:value-of select="/root/gui/strings/xpathschematron" />
									</th>
									<th>
										<xsl:value-of select="/root/gui/strings/xpathlabel" />
									</th>
									<th>
										<xsl:value-of select="/root/gui/strings/xpathvalue" />
									</th>
									<td>
									</td>
								</tr>
							</thead>
							<tbody>
								<tr data-ng-repeat="row in data">
									<td>
										<span>
											<xsl:attribute name="class">glyphicon glyphicon-{{row.required}}</xsl:attribute>
										</span>
									</td>
									<td>
										{{row.desc}}
									</td>
									<td>
										{{row.type}}
									</td>
									<td>
										{{row.value}}
									</td>
									<td>
										<a class="glyphicon glyphicon-remove-circle removeItem">
											<xsl:attribute name="title"><xsl:value-of select="/root/gui/strings/deleteRow" /></xsl:attribute>
											<xsl:attribute name="ng-click">removeItem(this);</xsl:attribute>
										</a>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<script type="text/javascript">
					confirmDelete = '<xsl:value-of select="/root/gui/strings/xpathconfirm" />';
				</script>

			</body>
		</html>
	</xsl:template>

	<!-- ================================================================== -->

</xsl:stylesheet>
