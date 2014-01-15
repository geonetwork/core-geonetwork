(function() {
  goog.provide('gn_map_service');


  var module = angular.module('gn_map_service', [
  ]);

  module.provider('gnMapProjection', function() {
    this.$get = [
      function() {
        return {
          
          /**
           * Reproject a given extent. Extent is an object 
           * defined as
           * {left,bottom,right,top}
           *  
           */
          reprojExtent: function(extent, src, dest) {
            if(src == dest) {
              return extent;
            }
            else {
              return ol.proj.transform(extent, 
                  src, dest);
            }
          },
          
          
          getCoordinatesFromExtent: function(extent) {
            return [
                    [
                     [extent[0], extent[1]],
                     [extent[0], extent[3]],
                     [extent[2], extent[3]],
                     [extent[2], extent[1]]
                   ]
                 ];
          }
        }
      }];
  });
})();
