var links = [ "../../apps/shared-objects/app/lib/angular/angular-route.js",
		"../../apps/shared-objects/app/js/factories.js",
		"../../apps/shared-objects/app/js/controllers.js",
		"../../apps/shared-objects/app/js/filters.js",
		"../../apps/shared-objects/app/js/directives.js" ];

for (var link in links) {
	jQuery.ajax(links[link], {
		async : false,
		cache : true,
		dataType : 'script'
	});
}

'use strict';
// Declare app level module which depends on filters, and services
angular.module(
		'SharedObjects',
		[ 'SharedObjects.filters', 'SharedObjects.factories',
				'SharedObjects.directives', 'SharedObjects.controllers',
				'ngRoute' ]).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/:validated/keywords', {
				templateUrl : 'partials/shared.html',
				controller : 'KeywordControl'
			});
			$routeProvider.otherwise({
				templateUrl : 'partials/shared.html',
				controller : 'KeywordControl'
			});
		} ]);

// Check language on url
//changePageLanguage = function(lang) {
//	window.location.search = '?lang=' + lang;
//};

var languageHolder = 'eng';
var init = function() {
	try {
		var matches = window.location.search.match(/lang=(\w\w\w)/);
		if (matches.length == 2) {
			languageHolder = matches[1];
		}

		if (languageHolder === 'deu') {
			languageHolder = 'ger';
		} else if (languageHolder === 'fra') {
			languageHolder = 'fre';
		}

		jQuery.ajax('../../srv/' + languageHolder + '/strings.js', {
			async : false,
			cache : true,
			dataType : 'script'
		});
		Geonet.language = languageHolder;
	} catch (e) {
		if (window.location.href.indexOf('?') === -1) {
			window.location.search = '?lang=eng';
		}
	}
};

init();

// Share input between keyword and group (for UI reasons)
$(function() {

	$("#xpathtype").change(function() {
		$("#xpath").val("");
		$("#keyword").val("");
		$("#group").val("");

		$("#keyword").removeClass("error");
		$("#group").removeClass("error");
		if ($("#xpathtype").val() == "KEYWORD") {
			$("#group").hide();
			$("#keyword").show();
		} else {
			$("#keyword").hide();
			$("#group").show();
		}
	});
	
	$("select[name=schematron]").change(function() {
		$("div.schematron_required").hide();
		var checkbox = $("input", $("div.schematron_required"))[0];
		
		$.ajax({
			url: 'metadata.schema.isMandatory',
			data: {
				schematron : $("select[name=schematron] option:selected")[0].value
			}
		}).success(function(data) {
			angular.forEach(data.children, function(child) {
				var c = child.children[0];
				checkbox.checked = (c.innerText || c.textContent) == "t";
			});
			$("div.schematron_required").show();
		});
	 });
});

// Load javascript translations
$.ajax('../../srv/' + languageHolder + '/strings.js', {
	async : false,
	cache : true,
	dataType : 'script'
});


var t_requiredSchematron = angular.module('required_schematron', []);

t_requiredSchematron.controller('required_schematron',

// Load existing schematron rules
function($scope, $http) {

});

var t_controller = angular.module('table_module', []);

t_controller.controller('table_controller',

// Load existing schematron rules
function($scope, $http) {

    // Remove item from table list
    $scope.removeItem = function(a) {
        var id;
        var url = 'metadata.schema.schematron.criteria';
        var confirmMsg;
        if (a.row) {
            id = a.row.id;
            confirmMsg = confirmDelete;
        } else {
            id = a.group.name;
            url = url + '.group';
            confirmMsg = confirmDeleteGroup;
        }
        if (confirm(confirmMsg) == true) {
            $http({
                method : 'GET',
                url : url,
                params : {
                    action : 'delete',
                    id : id,
                    groupName : id
                }
            }).success(function() {
                if (a.row) {
                    var group = a.row.group;
                    var index = group.criteria.indexOf(a.row);
                    group.criteria.splice(index,1);
                } else {
                    var index = $scope.data.indexOf(a.group);
                    $scope.data.splice(index, 1);
                }
            });
        }
    };
    $scope.calculateRequired = function(criteria) {
        if (criteria.requirement === "REQUIRED") {
            criteria.required = "ok-sign";
        } else if (criteria.requirement === "DISABLED") {
            criteria.required = "minus-sign";
        } else {
            criteria.required = "info-sign";
        }
    }
    // Remove item from table list
    $scope.toggleRequirement = function(a) {
        var requirement;
        if (a.group.requirement === "REQUIRED") {
            requirement = "REPORT";
        }else if (a.group.requirement === "DISABLED") {
            requirement = "REQUIRED";
        }else {
            requirement = "DISABLED";
        }
        $http({
            method : 'GET',
            url : 'metadata.schema.schematron.criteria.group',
            params : {
                action: 'edit',
                requirement : requirement,
                name : a.group.name
            }
        }).success(function() {
            a.group.requirement = requirement;
            $scope.calculateRequired(a.group);
        });
    };

	// update items on table list
	$scope.update = function($scope, $http) {
		$http({
			method : 'GET',
			url : 'metadata.schema.schematron.criteria.group@json',
            params: {
                includeCriteria: true,
                includeSchematron: true
            }
		}).success(function(data) {
			$scope.data = data;

			angular.forEach(data, function(group) {
                $scope.calculateRequired(group);
                angular.forEach(group.criteria, function (criteria) {
                    criteria.group = group;
                });
			});
		});
	};

	$scope.update($scope, $http);
});

function checkErrors() {
	$("#keyword").removeClass("error");
	$("#group").removeClass("error");
	if (criteria.keyword.value == '') {
		$(criteria.keyword).addClass("error");
	}
	if (criteria.group.value == '') {
		$(criteria.group).addClass("error");
	}
}

function TypeaheadCtrl($scope, $http, limitToFilter) {
	$scope.getKeywords = function(val) {
		return $http.get('reusable.list.js', {
			params : {
				validated : true,
				type : 'keywords'
			}
		}).then(function(data) {
			var res = [];
			angular.forEach(data.data, function(item) {
				var str = item.desc;
				
				if(str.toUpperCase().indexOf(val.toUpperCase()) > 0) {
					res.push({
						label : str,
						value : str
					});
				}
			});
			return limitToFilter(res, 8);
		});
	};

	$scope.getCriteriaGroups = function(val) {
		return $http.get('metadata.schema.schematron.criteria.group@json', {
		}).then(function(data) {
			var res = [];
			angular.forEach(data.data, function(item) {
				var str = item.name;

				if(str.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
					res.push({
						label : item.name
					});
				}
			});
			return limitToFilter(res, 8);
		});
    }
	$scope.getGroups = function(val) {
		return $http.get('xml.group.list@json', {
		}).then(function(data) {
			var res = [];
			angular.forEach(data.data, function(item) {
				var str = item.name;

				if(str.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
					res.push({
						label : item.name,
						value : item.id
					});
				}
			});
			return limitToFilter(res, 8);
		});
	};

	$scope.updateVal = function($item, $model, $label) {
		$('#xpath').val($item.value);
	};
}

var app = angular.module('metadataSchemaValidation', [ 'table_module', 'required_schematron',
		'ui.bootstrap' ]);

app.controller('addNewEntry',
// Add new entry on table list
function($scope, $http) {
    $scope.formData = {
        type: 'KEYWORD',
        groupName: '',
        schematron: '',
        xpath: ''
    };
    $scope.submit = function() {
		checkErrors();
		
		//enforce failback: xpath cannot be empty
		if($('#xpath').val() == '') {
			if($("#keyword").is(":visible")) {
				$('#xpath').val($("#keyword").val());
			}
			else if($("#group").is(":visible")) {
				$('#xpath').val($("#group").val());
			}
		}
		
		$http({
			method : 'GET',
			url : 'metadata.schema.schematron.criteria',
			params : {
				action : 'add',
                schematronId : $scope.formData.schematron,
                groupName: $scope.formData.groupName,
				value : $('#xpath').val(),
				type : $scope.formData.type
			}
		}).success(function(data) {
			$("#resultTable").scope().update($("#resultTable").scope(), $http);
			$("#xpath").val("");
			$("#keyword").val("");
			$("#group").val("");
		});
	};
});
