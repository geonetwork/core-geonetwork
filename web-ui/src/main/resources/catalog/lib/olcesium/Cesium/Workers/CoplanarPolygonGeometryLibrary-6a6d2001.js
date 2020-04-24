/**
 * Cesium - https://github.com/AnalyticalGraphicsInc/cesium
 *
 * Copyright 2011-2017 Cesium Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Columbus View (Pat. Pend.)
 *
 * Portions licensed separately.
 * See https://github.com/AnalyticalGraphicsInc/cesium/blob/master/LICENSE.md for full licensing details.
 */
define(["exports","./Check-e5651467","./Cartesian2-ba70b51f","./Transforms-5119c07b","./OrientedBoundingBox-2b5c2949"],function(n,t,f,x,B){"use strict";var e={},s=new f.Cartesian3,P=new f.Cartesian3,M=new f.Cartesian3,b=new f.Cartesian3,h=new B.OrientedBoundingBox;function o(n,t,e,r,a){var i=f.Cartesian3.subtract(n,t,s),o=f.Cartesian3.dot(e,i),u=f.Cartesian3.dot(r,i);return f.Cartesian2.fromElements(o,u,a)}e.validOutline=function(n){var t=B.OrientedBoundingBox.fromPoints(n,h).halfAxes,e=x.Matrix3.getColumn(t,0,P),r=x.Matrix3.getColumn(t,1,M),a=x.Matrix3.getColumn(t,2,b),i=f.Cartesian3.magnitude(e),o=f.Cartesian3.magnitude(r),u=f.Cartesian3.magnitude(a);return!(0===i&&(0===o||0===u)||0===o&&0===u)},e.computeProjectTo2DArguments=function(n,t,e,r){var a,i,o=B.OrientedBoundingBox.fromPoints(n,h),u=o.halfAxes,s=x.Matrix3.getColumn(u,0,P),C=x.Matrix3.getColumn(u,1,M),c=x.Matrix3.getColumn(u,2,b),m=f.Cartesian3.magnitude(s),g=f.Cartesian3.magnitude(C),d=f.Cartesian3.magnitude(c),l=Math.min(m,g,d);return(0!==m||0!==g&&0!==d)&&(0!==g||0!==d)&&(l!==g&&l!==d||(a=s),l===m?a=C:l===d&&(i=C),l!==m&&l!==g||(i=c),f.Cartesian3.normalize(a,e),f.Cartesian3.normalize(i,r),f.Cartesian3.clone(o.center,t),!0)},e.createProjectPointsTo2DFunction=function(r,a,i){return function(n){for(var t=new Array(n.length),e=0;e<n.length;e++)t[e]=o(n[e],r,a,i);return t}},e.createProjectPointTo2DFunction=function(e,r,a){return function(n,t){return o(n,e,r,a,t)}},n.CoplanarPolygonGeometryLibrary=e});
