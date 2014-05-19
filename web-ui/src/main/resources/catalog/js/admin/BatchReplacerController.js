(function() {
  goog.provide('gn_admintools_batchreplacer_controller');


  var module = angular.module('gn_admintools_batchreplacer_controller',
      []);

  module.controller('GnAdminToolsBatchreplacerController', [
    '$scope', '$http', '$rootScope', '$translate', '$log',
    function($scope, $rootScope, $translate, $log) {
    	$log.debug("GnAdminToolsBatchreplacerController called");

    }]);
})();

