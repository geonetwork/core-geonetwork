(function() {

  goog.provide('gn_search_controller');

  goog.require('gn_searchsuggestion_service');


  var module = angular.module('gn_search_controller',
      [
        'bootstrap-tagsinput',
          'gn_searchsuggestion_service'
      ]);

  module.controller('GnSearchController', [
    '$scope',
    function($scope) {

      /** Define in the controller scope a reference to the map */
      $scope.map = null;

      /** Facets configuration */
      $scope.facetsConfig = {
        keyword: 'keywords',
        orgName: 'orgNames',
        denominator: 'denominator',
        format: 'formats',
        createDateYear: 'createDateYears'
      };

      /* Pagination configuration */
      $scope.paginationInfo = {
        hitsPerPage: 3
      };

      $scope.resultTemplate = '../../catalog/components/search/resultsview/partials/viewtemplates/thumb.html';
    }]);

  module.directive('gnSearchSuggest',
      ['suggestService',
        function(suggestService) {
          return {
            restrict: 'A',
            scope: {
              field: '@gnSearchSuggest'
            },
            link: function(scope, element, attrs) {
              element.tagsinput({
              });
              element.tagsinput('input').typeahead({
                remote:{
                  url :suggestService.getUrl('QUERY', scope.field, 'ALPHA'),
                  filter: suggestService.filterResponse,
                  wildcard: 'QUERY'
                }
              }).bind('typeahead:selected', $.proxy(function (obj, datum) {
                this.tagsinput('add', datum.value);
                this.tagsinput('input').typeahead('setQuery', '');
              }, element));
            }
          }
  }]);

  module.directive('gnRegionMultiselect',
      ['gnRegionService',
        function(gnRegionService) {
          return {
            restrict: 'A',
            scope: {
              field: '@gnRegionMultiselect',
              callback: '=gnCallback'
            },
            link: function(scope, element, attrs) {
              var type = {
                id: 'http://geonetwork-opensource.org/regions#'+scope.field
              };
              gnRegionService.loadRegion(type, 'fre').then(
                  function(data) {

                    $(element).tagsinput({
                      itemValue:'id',
                      itemText: 'name'
                    });
                    var field = $(element).tagsinput('input');
                    field.typeahead({
                      valueKey: 'name',
                      local: data,
                      minLength: 0,
                      limit: 5
                    }).on('typeahead:selected', function(event, datum) {
                      $(element).tagsinput('add', datum);
                      field.typeahead('setQuery', '');
                    });

                    $('input.tt-query')
                        .on('click', function() {
                          var $input = $(this);

                          // these are all expected to be objects
                          // so falsey check is fine
                          if (!$input.data() || !$input.data().ttView ||
                              !$input.data().ttView.datasets ||
                              !$input.data().ttView.dropdownView ||
                              !$input.data().ttView.inputView) {
                            return;
                          }

                          var ttView = $input.data().ttView;

                          var toggleAttribute = $input.attr('data-toggled');

                          if (!toggleAttribute || toggleAttribute === 'off') {
                            $input.attr('data-toggled', 'on');

                            $input.typeahead('setQuery', '');

                            if ($.isArray(ttView.datasets) &&
                                ttView.datasets.length > 0) {
                              // only pulling the first dataset for this hack
                              var fullSuggestionList = [];
                              // renderSuggestions expects a
                              // suggestions array not an object
                              $.each(ttView.datasets[0].itemHash, function(i, item) {
                                fullSuggestionList.push(item);
                              });

                              ttView.dropdownView.renderSuggestions(
                                  ttView.datasets[0], fullSuggestionList);
                              ttView.inputView.setHintValue('');
                              ttView.dropdownView.open();
                            }
                          }
                          else if (toggleAttribute === 'on') {
                            $input.attr('data-toggled', 'off');
                            ttView.dropdownView.close();
                          }
                        });

                  });
            }
          }
        }]);

  module.constant('gnOlStyles',{
    bbox: new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: 'rgba(255,0,0,1)',
        width: 2
      }),
      fill: new ol.style.Fill({
        color: 'rgba(255,0,0,0.3)'
      }),
      image: new ol.style.Circle({
        radius: 7,
        fill: new ol.style.Fill({
          color: 'rgba(255,0,0,1)'
        })
      })
    })
  })

  module.controller('gocatSearchFormCtrl', [
    '$scope',
      'gnHttp',
      'gnHttpServices',
      'gnRegionService',
      '$timeout',
      'suggestService',
      '$http',
      'gnOlStyles',
    function($scope, gnHttp, gnHttpServices, gnRegionService, $timeout, suggestService,$http, gnOlStyles) {



      $scope.types = ['any',
        'dataset',
        'basicgeodata',
        'basicgeodata-federal',
        'basicgeodata-cantonal',
        'basicgeodata-communal',
        'service',
        'service-OGC:WMS',
        'service-OGC:WFS'
      ];

      /** Manage cantons selection (add feature to the map) */
      var cantonSource = new ol.source.Vector();
      var nbCantons = 0;
      var cantonVector = new ol.layer.Vector({
        source: cantonSource,
        style: gnOlStyles.bbox
      });
      var addCantonFeature = function(id) {
        var url = 'http://www.geocat.ch/geonetwork/srv/eng/region.geom.wkt?id=kantone:'+id+'&srs=EPSG:3857';
        var proxyUrl = '../../proxy?url=' + encodeURIComponent(url);
        nbCantons++;

        return $http.get(proxyUrl).success(function(wkt) {
          var parser = new ol.format.WKT();
          var feature = parser.readFeature(wkt);
          cantonSource.addFeature(feature);
        });
      };

      // Add canton vector layer to the map when map is created
      var unregisterMap = $scope.$watch('map', function() {
        if($scope.map) {
          $scope.map.addLayer(cantonVector);
          unregisterMap();
          delete unregisterMap;
        }
      });

      // Request cantons geometry and zoom to extent when
      // all requests respond.
      $scope.$watch('params.cantons', function(v){
        cantonSource.clear();
        if(angular.isDefined(v) && v != '') {
          var cs = v.split(',');
          for(var i=0; i<cs.length;i++) {
            var id = cs[i].split('#')[1];
            addCantonFeature(Math.floor((Math.random() * 10) + 1)).then(function(){
              if(--nbCantons == 0) {
                $scope.map.getView().fitExtent(cantonSource.getExtent(), $scope.map.getSize());
              }
            });
          }
        }
      });


      $('#categoriesF').tagsinput({
        itemValue: 'id',
        itemText: 'label'
      });
      $('#categoriesF').tagsinput('input').typeahead({
        valueKey: 'label',
        prefetch: {
          url :suggestService.getInfoUrl('categories'),
          filter: function(data) {
            var res = [];
            for(var i=0; i<data.metadatacategory.length;i++) {
              res.push({
                id: data.metadatacategory[i]['@id'],
                label : data.metadatacategory[i].label.eng
              })
            }
            return res;
          }
        }
      }).bind('typeahead:selected', $.proxy(function (obj, datum) {
        this.tagsinput('add', datum);
        this.tagsinput('input').typeahead('setQuery', '');
      }, $('#categoriesF')));


      // Keywords input list
/*
      gnHttpServices.geocatKeywords = 'geocat.keywords.list';
      gnHttp.callService('geocatKeywords').success(function(data) {
        var xmlDoc = $.parseXML(data);
        var $xml = $(xmlDoc);
        var k = $xml.find('keyword');
        var n = $xml.find('name');
      });
*/

    }]);
})();
