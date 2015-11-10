(function() {
  goog.provide('gn_openwis_productmetadata_controller');

  var module = angular.module('gn_openwis_productmetadata_controller', []);
  module.controller('GnOpenwisProductMetadataController', 
      [
       '$scope',
       '$http',
       '$rootScope',
       'DTOptionsBuilder',
       'DTColumnBuilder',
       function($scope, $http, $rootScope, 
           DTOptionsBuilder, DTColumnBuilder) {

         $scope.dtInstance = {};

         $scope.dtOptions = DTOptionsBuilder.newOptions()
           .withOption("sAjaxSource", $scope.url + 'openwis.productmetadata.search')
           .withDataProp('data')
           .withOption('processing', true)
           .withOption('serverSide', true)
           .withOption('iDisplayLength', 25)
           .withOption('scrollX', '100%')
           .withOption('scrollCollapse', true)
           .withOption('autoWidth', false)
           .withPaginationType('full_numbers')
           .withFixedColumns({
             leftColumns: 0,
             rightColumns: 1
           })
           .withColumnFilter({
             aoColumns: [null, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             },{
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, {
               type: 'text'
             }, null
             ]
           });

         $scope.dtColumns = [
           DTColumnBuilder.newColumn('metadataId').notVisible(),
           DTColumnBuilder.newColumn('metadataUrn').withOption('name', '_uuid'),
           DTColumnBuilder.newColumn('metadataTitle').withOption('name', '_title'),
           DTColumnBuilder.newColumn('metadataCategory').withOption('name', '_cat'),
           DTColumnBuilder.newColumn('originator').withOption('name', '_originator'),
           DTColumnBuilder.newColumn('process').withOption('name', '_process'),
           DTColumnBuilder.newColumn('gtsCategory').withOption('name', '_gtsCategory'),
           DTColumnBuilder.newColumn('fncPattern').withOption('name', '_fncPattern'),
           DTColumnBuilder.newColumn('dataPolicy').withOption('name', '_dataPolicy'),
           DTColumnBuilder.newColumn('priority').withOption('name', '_priority'),
           DTColumnBuilder.newColumn('localDataResource').withOption('name', '_localDataResource'),
           DTColumnBuilder.newColumn('ingested').withOption('name', '_isIngested'),
           DTColumnBuilder.newColumn('fed').withOption('name', '_isStopGap'),
           DTColumnBuilder.newColumn('fileExtension').withOption('name', '_fileExtension'),
           DTColumnBuilder.newColumn('actions').renderWith(function(data, type, full) {
             return "<div style=\"width:120px\">" +
               "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.search#/metadata/" + full.metadataUrn + "\" title=\"View\"><i class=\"fa fa-eye\"></i></a>" +
               "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().edit('" + full.metadataUrn + "')\" title=\"Edit product info\"><i class=\"fa fa-edit\"></i></a>" +
               "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.edit#/metadata/" + full.metadataId + "\" title=\"Edit metadata\"><i class=\"fa fa-pencil\"></i></a>" +
               "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.edit#/create?from=" + full.metadataId + "\"  title=\"Duplicate\"><i class=\"fa fa-copy\"></i></a>" +
               "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().delete('" + full.metadataId + "')\" title=\"Remove\"><i class=\"fa fa-times text-danger\"></i></a>" +
               "</div>";
           })
         ];

         $scope.edit = function(metadataUrn) {
           $http({
             url : $scope.url + 'openwis.productmetadata.get',
             method : 'GET',
             params : {urn: metadataUrn}
           }).success(function(data) {
             $scope.product = data;
             $scope.filter = {};

             $scope.filter.regex = '^' + $scope.product.urn + '$';
             $scope.filter.description = 'Description';

             $("#editProductMetadata").modal();
           }).error(function(data) {
             console.log(data);
             $scope.updateData();
             $rootScope.$broadcast('StatusUpdated', {
               title : 'Error',
               msg : 'Error getting product metadata details. Please reload.',
               type : 'danger'
             });
           });
         };

         $scope.delete = function(metadataId) {
           $scope.metadataId = metadataId;
           $("#deleteMetadata").modal();
         };

         $scope.updateData = function() {
           // Refresh the
            // table
           if ($scope.dtInstance.dataTable) {
             $scope.dtInstance.dataTable._fnDraw();
           }
         };


  }

   ]);
  module
      .controller(
          'GnOpenwisProductMetadataModalController',
          [
              '$scope',
              '$http',
              '$rootScope',
              function($scope, $http, $rootScope) {

                $scope.addIngestFilter = function() {
                  $("#addToIngestionFilter").modal();
                };

                $scope.addFeedingFilter = function() {
                  $("#addToFeedingFilter").modal();
                };

                $scope.ok = function() {
                  $http({
                    url : $scope.url + 'openwis.productmetadata.set',
                    method : 'POST',
                    params : $scope.product
                  })
                      .success(function(data) {
                        $scope.updateData();
                        $("#editProductMetadata").modal('hide');
                      })
                      .error(
                          function(data) {
                            console.log(data);
                            $scope.updateData();
                            $rootScope
                                .$broadcast(
                                    'StatusUpdated',
                                    {
                                      title : 'Error',
                                      msg : 'Error saving product metadadata details. Please try again.',
                                      type : 'danger'
                                    });
                          });
                };

                $scope.cancel = function() {
                  $("#editProductMetadata").modal('hide');
                };
              }
          ]);

  module
      .controller(
          'GnOpenwisMetadataDeleteModalController',
          [
              '$scope',
              '$http',
              '$rootScope',
              function($scope, $http, $rootScope) {
                $scope.ok = function() {
                  $http({
                    url : $scope.url + 'xml.metadata.delete',
                    method : 'POST',
                    params : {
                      id : $scope.metadataId
                    }
                  })
                      .success(function(data) {
                        $scope.updateData();
                        $("#deleteMetadata").modal('hide');
                      })
                      .error(
                          function(data) {
                            console.log(data);
                            $scope.updateData();
                            $rootScope
                                .$broadcast(
                                    'StatusUpdated',
                                    {
                                      title : 'Error',
                                      msg : 'Error saving product metadadata details. Please try again.',
                                      type : 'danger'
                                    });
                          });
                };

                $scope.cancel = function() {
                  $("#deleteMetadata").modal('hide');
                };
              }
          ]);

  module.controller('GnOpenwisProductMetadataModalFilterController', [
      '$scope', '$http', '$rootScope', function($scope, $http, $rootScope) {

        $scope.saveToIngestionFilter = function() {
          $http({
            url : $scope.url + 'openwis.controlservice.saveingestionfilter',
            method : 'POST',
            params : {
              description : $scope.filter.description,
              regex : $scope.filter.regex
            }
          }).success(function(data) {
            $scope.updateData();
            $("#addToIngestionFilter").modal('hide');
          }).error(function(data) {
            console.log(data);
            $scope.updateData();
            $rootScope.$broadcast('StatusUpdated', {
              title : 'Error',
              msg : 'Error saving to ingestion filter. Please try again.',
              type : 'danger'
            });
          });
        };

        $scope.saveToFeedingFilter = function() {
          $http({
            url : $scope.url + 'openwis.controlservice.savefeedingfilter',
            method : 'POST',
            params : {
              description : $scope.filter.description,
              regex : $scope.filter.regex
            }
          }).success(function(data) {
            $scope.updateData();
            $("#addToFeedingFilter").modal('hide');
          }).error(function(data) {
            console.log(data);
            $scope.updateData();
            $rootScope.$broadcast('StatusUpdated', {
              title : 'Error',
              msg : 'Error saving to feeding filter. Please try again.',
              type : 'danger'
            });
          });
        };

        $scope.cancelToIngestionFilter = function() {
          $("#addToIngestionFilter").modal('hide');
        };

        $scope.cancelToFeedingFilter = function() {
          $("#addToFeedingFilter").modal('hide');
        };
      }
  ]);

})();
