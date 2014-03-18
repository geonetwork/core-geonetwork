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
        var id, schematronId;
        var url = 'metadata.schema.schematron.criteria';
        var confirmMsg;
        if (a.row) {
            id = a.row.id;
            confirmMsg = confirmDelete;
        } else {
            id = a.group.name;
            schematronId = a.group.schematron.id;
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
                    groupName : id,
                  schematronId: schematronId
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
            requirement = "REPORT_ONLY";
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
                name : a.group.name,
                schematronid: a.group.schematron.id
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

function TypeaheadCtrl($scope, $http, limitToFilter) {
	$scope.getCriteriaGroups = function(val) {
		return $http({
            method : 'GET',
            url : 'metadata.schema.schematron.criteria.group@json',
            params: {
                schematronId: $scope.formData.schematron.id
            }
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
    };

	$scope.getCriteriaValues = function(val) {
        var service = $scope.formData.type ? $scope.formData.type.service : undefined;
        if (service) {
            var url = service.url.replace(/@@search@@/g, val).replace(/@@lang@@/g, GeoNetworkLang);
            return $http.get(url).then(function(data) {
                var res = [];
                var rawRecords = service.records(data.data);
                angular.forEach(rawRecords, function(item) {
                    var str = service.label(item);

                    if(str.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
                        res.push({
                            label : service.label(item),
                            value : service.value(item)
                        });
                    }
                });
                return limitToFilter(res, 8);
            });
        } else {
            return [];
        }
	};

}

var app = angular.module('metadataSchemaValidation', [ 'table_module', 'required_schematron',
		'ui.bootstrap' ]);

app.controller('addNewEntry',
// Add new entry on table list
function($scope, $http) {
    $scope.schematrons = schematrons;
    $scope.schemasToCriteriaTypes = schemasToCriteriaTypes;

    $scope.formData = {
        showErrors: false,
        schematron: null,
        groupName: null,
        type: null,
        value: null
    };
    var dirty = function (elem) {
        elem.addClass('ng-dirty');
        elem.removeClass('ng-pristine');
    }
    var invalid = function (id) {
        var elem = $(id);
        elem.removeClass('ng-valid');
        elem.addClass('ng-invalid');
        dirty(elem);
    };
    var valid = function (id) {
        var elem = $(id);
        elem.addClass('ng-valid');
        elem.removeClass('ng-invalid');
        dirty(elem);
    };
    $scope.$watch(function () {return $scope.formData.schematron}, function() {
        $scope.formData.groupName = null;
        $scope.formData.type = null;
    });
    $scope.$watch(function () {return $scope.formData.type}, function() {
        $scope.formData.value = null;
    });
    $scope.$watchCollection(function () { return $scope.formData}, function(newValue, oldValue) {
        if (!newValue.showErrors) {
            newValue.showErrors = newValue.schematron !== null || newValue.groupName !== null ||
                newValue.type !== null || newValue.value !== null;
        }
        var enableSubmit = true;
        if (newValue.showErrors) {
            if (newValue.schematron === null) {
                invalid('#schematron');
                enableSubmit = false;
            } else {
                valid('#schematron');
            }
            if (newValue.groupName === null) {
                invalid('#groupName');
                enableSubmit = false;
            } else {
                valid('#groupName');
            }
            if (newValue.type === null) {
                invalid('#xpathtype');
                enableSubmit = false;
            } else {
                valid('#xpathtype');
            }
            if (newValue.value === null) {
                invalid('#xpathValue');
                enableSubmit = false;
            } else {
                valid('#xpathValue');
            }
        } else {
            enableSubmit = false;
        }
        $("#addButton")[0].disabled = !enableSubmit;

    });


    $scope.submit = function() {
        $http({
            method : 'POST',
            url : 'metadata.schema.schematron.criteria',
            params : {
                action : 'add',
                schematronId : $scope.formData.schematron.id,
                groupName: $scope.formData.groupName,
                value : $scope.formData.type.value.replace(/@@value@@/g, $scope.formData.value).replace(/@@lang@@/g, GeoNetworkLang),
                type : $scope.formData.type.type
            }
        }).success(function(data) {
            $("#resultTable").scope().update($("#resultTable").scope(), $http);
            $("#xpathValue").val("");
        });
	};
});
