goog.provide('gn_solr_result');

angular.module('gn_solr_result', [])
.directive('gnSolrResult', function() { return {
  scope            : {},
  bindToController : {
    record         : '=',
    collection     : '='
  },
  controller       : ResultController,
  controllerAs     : 'ctrl',
  templateUrl      : '../../catalog/views/solr/components/result/result.html'
}});

function ResultController($scope, $element, $attrs) {

  this.overviews  = this.parseOverviews();
  this.geometries = this.parseGeometries();
}

ResultController.prototype.add = function() {
  if (!this.geometries) { return; }
  if (!this.feature) {
    let options = {
      geometry: this.geometries[0].transform('EPSG:4326', 'EPSG:3857')
    };
    if (this.record.docType == 'feature') {
      options = angular.extend(options, this.record);
    }
    this.feature = new ol.Feature(options);
  }
  this.collection.clear();
  this.collection.push(this.feature);
};

ResultController.prototype.remove = function() {
  if (!this.feature) { return; }
  // this.collection.remove(this.feature);
};

ResultController.prototype.parseOverviews = function() {
  let overviews = [];
  if (!this.record.overviewUrl) {
    return;
  }
  this.record.overviewUrl.forEach(function(overview) {
    let o = overview.split('|');
    overviews.push({
      name : o[0],
      url  : o[1]
    });
  });
  return overviews;
};

ResultController.prototype.parseGeometries = function() {
  let geoms = [];
  let format = new ol.format.WKT();
  if (!this.record.geom) {
    return;
  }
  this.record.geom.forEach(function(geom) {
    geoms.push(format.readGeometry(geom));
  });
  return geoms;
};
