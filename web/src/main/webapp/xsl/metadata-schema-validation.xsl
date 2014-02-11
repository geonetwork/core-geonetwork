<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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
                <style type="text/css">
                    .css-form .ng-invalid.ng-dirty {
                        background-color: #FA787E;
                    }
                </style>
			</head>
			<body>
				<div id="header">
					<xsl:call-template name="banner" />
				</div>

				<script src="../../apps/shared-objects/app/lib/jquery.js"></script>
                <xsl:choose>
                    <xsl:when test="/root/request/debug">
                        <script src="../../apps/shared-objects/app/lib/angular/angular.js"></script>
                    </xsl:when>
                    <xsl:otherwise>
                        <script src="../../apps/shared-objects/app/lib/angular/angular.min.js"></script>
                    </xsl:otherwise>
                </xsl:choose>
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
						<form  class="css-form" method="post" name="criteria" ng-submit="submit()" ng-controller="addNewEntry" >
                            <xsl:attribute name="novalidate" />
							<div>
								<label>
									<xsl:value-of select="/root/gui/strings/xpathschematron" /> :
								</label>

								<select ng-model="formData.schematron" name="schematron" id="schematron" autofocus="autofocus" required="true"
                                    ng-options="schematron as schematron.name group by schematron.schemaname for schematron in schematrons">
								</select>
							</div>
                            <div class='container-fluid' ng-controller="TypeaheadCtrl">
                                <label>
                                    <xsl:value-of select="/root/gui/strings/schematroncriteriagroup" />
                                    :
                                </label>
                                <input type="text" id="groupName" placeholder="{/root/gui/strings/schematroncriteriagroup}" required="true"
                                    ng-model="formData.groupName" typeahead-editable="true"
                                    typeahead-wait-ms="200"
                                    typeahead-loading="loadingGroups"
                                    typeahead="criteriagroup.label for criteriagroup in getCriteriaGroups($viewValue) | filter:$viewValue | limitTo:8" />
                                <i ng-show="loadingGroups" class="glyphicon glyphicon-refresh"></i>
                            </div>
                            <div>
								<label>
									<xsl:value-of select="/root/gui/strings/xpathlabel" /> :
								</label>
								<select ng-model="formData.type" name="type" id="xpathtype" required="true"
                                    ng-options="type as type.name for type in schemasToCriteriaTypes[formData.schematron.schemaname]">
								</select>
							</div>

							<!--<script type="text/ng-template" id="customTemplate.html">-->
								<!--<a>-->
									<!--<span bind-html-unsafe="match.label | typeaheadHighlight:query"></span>-->
								<!--</a>-->
							<!--</script>-->

							<div class='container-fluid' ng-controller="TypeaheadCtrl">
								<label>
									<xsl:value-of select="/root/gui/strings/xpathvalue" />
									:
								</label>
								<input type="text" id="xpathValue" placeholder="{/root/gui/strings/xpathvalue}"
									ng-model="formData.value" typeahead-editable="false"
                                    typeahead-wait-ms="200"
                                    typeahead-loading="loadingValues"
									typeahead="value.value as value.label for value in getCriteriaValues($viewValue) | filter:$viewValue | limitTo:8" />
                                    <i ng-show="loadingValues" class="glyphicon glyphicon-refresh"></i>
                                <div>
                                    <input id="addButton" type="submit" disabled="true"></input>
                                    <span class="glyphicon"></span>
                                </div>
							</div>
						</form>
					</div>
					<br/>
					<br/>
                <div class="container-fluid" ng-controller="table_controller" id="resultTable">
                    <div data-ng-repeat="group in data | orderBy:+schematron.schemaname | orderBy:+schematron.ruleName | orderBy:+name ">
                        <div class="row">
                            <div class="col-md-1">
                                <a>
                                    <xsl:attribute name="class">glyphicon glyphicon-{{group.required}}</xsl:attribute>
                                    <xsl:attribute name="ng-click">toggleRequirement(this);</xsl:attribute>
                                    <xsl:attribute name="title"><xsl:value-of select="/root/gui/strings/toggleRequirement" /></xsl:attribute>
                                </a>
                            </div>
                            <div class="col-md-1">
                                <b>{{group.schematron.schemaname}}</b>
                            </div>
                            <div class="col-md-4">
                                {{group.schematron.ruleName}}
                            </div>
                            <div class="col-md-5">
                                {{group.name}}
                            </div>
                            <!--<div class="col-md-3">-->
                                <!--{{group.schematron.ruleName}}-->
                            <!--</div>-->
                            <div class="col-md-1">
                                <a class="glyphicon glyphicon-remove-circle removeItem">
                                    <xsl:attribute name="title"><xsl:value-of select="/root/gui/strings/deleteCriteriaGroup" /></xsl:attribute>
                                    <xsl:attribute name="ng-click">removeItem(this);</xsl:attribute>
                                </a>
                            </div>
                        </div>
                        <div class="row" style="background-color: #f5f5f5;" data-ng-repeat="row in group.criteria">
                            <div class="col-md-2 col-md-offset-2">
                                {{row.type}}
                            </div>
                            <div class="col-md-7">
                                {{row.value}}
                            </div>
                            <div class="col-md-1">
                                <a class="glyphicon glyphicon-remove-circle removeItem">
                                    <xsl:attribute name="title"><xsl:value-of select="/root/gui/strings/deleteCriteria" /></xsl:attribute>
                                    <xsl:attribute name="ng-click">removeItem(this);</xsl:attribute>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

				</div>

				<script type="text/javascript">
confirmDelete = '<xsl:value-of select="/root/gui/strings/xpathconfirmDeleteCriteria" />';
confirmDeleteGroup = '<xsl:value-of select="/root/gui/strings/xpathconfirmDeleteCriteriaGroup" />';
confirmToggle = '<xsl:value-of select="/root/gui/strings/schematronRequiredConfirmToggle" />';

<xsl:variable name="pPat">"</xsl:variable>
<xsl:variable name="rPat">'</xsl:variable>
var schematrons = [<xsl:for-each select="/root/schemas/*/schematron">
    {"name": "<xsl:value-of select="tokenize(file,'/|\\')[last()]" />",
    "schemaname": "<xsl:value-of select="schemaname"/>",
    "id": <xsl:value-of select="id"/>
    }<xsl:if test="position() &lt; last()">,</xsl:if>
</xsl:for-each>];

var schemasToCriteriaTypes = {<xsl:for-each select="/root/schemas/*">
  "<xsl:value-of select="name"/>": [<xsl:for-each select="criteriaTypes/type">
      {"name" : "<xsl:value-of select="name"/>",
       "type" : "<xsl:value-of select="type"/>",
       "value": "<xsl:value-of select="replace(value,$pPat,$rPat)"/>",
       "allowArbitraryValue": <xsl:value-of select="allowArbitraryValue" />,
       "service": {
          "url": "<xsl:value-of select="service/url"/>",
          "cacheable": "<xsl:value-of select="service/cacheable"/>",
          "records": <xsl:value-of select="service/selectRecordArray"/>,
          "label": <xsl:value-of select="service/selectLabelFunction"/>,
          "value": <xsl:value-of select="service/selectValueFunction"/>
       }
      }<xsl:if test="position() &lt; last()">,</xsl:if>
    </xsl:for-each>
  ]<xsl:if test="position() &lt; last()">,</xsl:if>
  </xsl:for-each>
};
var GeoNetworkLang = '<xsl:value-of select="/root/gui/language"/>';
				</script>

			</body>
		</html>
	</xsl:template>

	<!-- ================================================================== -->

</xsl:stylesheet>
