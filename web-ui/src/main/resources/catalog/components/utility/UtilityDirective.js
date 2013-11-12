(function() {
  goog.provide('gn_utility_directive');

  angular.module('gn_utility_directive', [
  ])
  .directive('groupsCombo', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/templates/utils/groupsCombo.html',
            controller: ['$scope', '$translate', function($scope, $translate) {
              $http.get('admin.group.list@json').success(function(data) {
                $scope.groups = data !== 'null' ? data : null;
              });
            }]
          };
        }])
  .directive('protocolsCombo', ['$http',
        function($http) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/templates/utils/protocolsCombo.html',
            controller: ['$scope', '$translate', function($scope, $translate) {

              var getPostRequestBody = function() {
                var helpId = '|gmd:protocol|gmd:CI_OnlineResource|' +
                    'gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution' +
                    '/gmd:transferOptions/gmd:MD_DigitalTransferOptions/' +
                    'gmd:onLine/gmd:CI_OnlineResource/gmd:protocol|';

                var schema = 'iso19139';
                var info = helpId.split('|'),
                    requestBody = '<request><element schema="' + schema +
                                    '" name="' + info[1] +
                                    '" context="' + info[2] +
                                    '" fullContext="' + info[3] +
                                    '" isoType="' + info[4] + '" /></request>';
                schema = schema || info[0].split('.')[1] || 'iso19139';
                return requestBody;
              };


              $http.post('xml.schema.info', getPostRequestBody(), {
                headers: {'Content-type': 'application/xml'}
              }).
                  success(function(data) {
                    $scope.groups = data !== 'null' ? data : null;
                  });
            }]
          };
        }]);
})();
