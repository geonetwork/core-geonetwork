(function() {
  goog.provide('gn_solr_requestmanager');

  goog.require('gn_solr_request');
  goog.require('gn_solr_request_config');

  var module = angular.module('gn_solr_requestmanager', [
    'gn_solr_request', 'gn_solr_request_config'
  ]);

  /**
   * The SolrRequestManager manage a pool of solr request objects. Each object
   * is a state of a current solr search.
   *
   * @param $injector angular injector
   * @constructor
   */
  var GnSolrRequestManager = function($injector) {

    var pool_ = [];

    /**
     * Register a Solr request object with a given type and name.
     * The manager will check in the pool if such an object has already been
     * instanciated, returns it or instanciate it.
     * The solr request object is initialize following its type. For the given
     * type, an config angular Value must exist with the corresponding name.
     *
     * @param type used to init the object
     * @param name identify the object in the pool
     * @returns {*}
     */
    this.register = function(type, name) {

      if(!(type && name)) {
        console.error('You can\'t register a SOLR object without identifiers');
        return;
      }

      var objId = type + '_' + name;
      var configName = 'gnSolr'+ type + 'Config';

      // Retrieve the angular value config object
      if(!$injector.has(configName)) {
        console.error('The Solr config is not defined: ' + configName);
        return;
      }

      // Instanciate the solr request object
      if(!pool_[objId]) {
        var solrObj = new geonet.GnSolrRequest($injector.get(configName), $injector);
        pool_[objId] = solrObj;
      }
      return pool_[objId];
    };

    /**
     * Unregister the solr request object from manager. The object is deleted.
     * @param type
     * @param name
     */
    this.unregister = function(type, name) {
      var objId = type + '_' + name;
      if(pool_[objId]) {
        delete pool_[objId];
      }
    };
  };
  
  module.service('gnSolrRequestManager', ['$injector', GnSolrRequestManager]);


})();
