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
define(["exports","./defined-2a4f2d00","./Check-e5651467","./defaultValue-29c9b1af","./Cartesian2-ba70b51f","./defineProperties-c817531e","./Transforms-5119c07b"],function(e,u,a,p,m,i,r){"use strict";function t(e,a){this._ellipsoid=e,this._cameraPosition=new m.Cartesian3,this._cameraPositionInScaledSpace=new m.Cartesian3,this._distanceToLimbInScaledSpaceSquared=0,u.defined(a)&&(this.cameraPosition=a)}i.defineProperties(t.prototype,{ellipsoid:{get:function(){return this._ellipsoid}},cameraPosition:{get:function(){return this._cameraPosition},set:function(e){var a=this._ellipsoid.transformPositionToScaledSpace(e,this._cameraPositionInScaledSpace),i=m.Cartesian3.magnitudeSquared(a)-1;m.Cartesian3.clone(e,this._cameraPosition),this._cameraPositionInScaledSpace=a,this._distanceToLimbInScaledSpaceSquared=i}}});var o=new m.Cartesian3;t.prototype.isPointVisible=function(e){var a=this._ellipsoid.transformPositionToScaledSpace(e,o);return this.isScaledSpacePointVisible(a)},t.prototype.isScaledSpacePointVisible=function(e){var a=this._cameraPositionInScaledSpace,i=this._distanceToLimbInScaledSpaceSquared,t=m.Cartesian3.subtract(e,a,o),n=-m.Cartesian3.dot(t,a);return!(i<0?0<n:i<n&&n*n/m.Cartesian3.magnitudeSquared(t)>i)},t.prototype.computeHorizonCullingPoint=function(e,a,i){u.defined(i)||(i=new m.Cartesian3);for(var t=this._ellipsoid,n=h(t,e),r=0,o=0,s=a.length;o<s;++o){var c=S(t,a[o],n);r=Math.max(r,c)}return C(n,r,i)};var f=new m.Cartesian3;t.prototype.computeHorizonCullingPointFromVertices=function(e,a,i,t,n){u.defined(n)||(n=new m.Cartesian3),t=p.defaultValue(t,m.Cartesian3.ZERO);for(var r=this._ellipsoid,o=h(r,e),s=0,c=0,l=a.length;c<l;c+=i){f.x=a[c]+t.x,f.y=a[c+1]+t.y,f.z=a[c+2]+t.z;var d=S(r,f,o);s=Math.max(s,d)}return C(o,s,n)};var s=[];t.prototype.computeHorizonCullingPointFromRectangle=function(e,a,i){var t=m.Rectangle.subsample(e,a,0,s),n=r.BoundingSphere.fromPoints(t);if(!(m.Cartesian3.magnitude(n.center)<.1*a.minimumRadius))return this.computeHorizonCullingPoint(n.center,t,i)};var c=new m.Cartesian3,l=new m.Cartesian3;function S(e,a,i){var t=e.transformPositionToScaledSpace(a,c),n=m.Cartesian3.magnitudeSquared(t),r=Math.sqrt(n),o=m.Cartesian3.divideByScalar(t,r,l);n=Math.max(1,n);var s=1/(r=Math.max(1,r));return 1/(m.Cartesian3.dot(o,i)*s-m.Cartesian3.magnitude(m.Cartesian3.cross(o,i,o))*(Math.sqrt(n-1)*s))}function C(e,a,i){if(!(a<=0||a===1/0||a!=a))return m.Cartesian3.multiplyByScalar(e,a,i)}var n=new m.Cartesian3;function h(e,a){return m.Cartesian3.equals(a,m.Cartesian3.ZERO)?a:(e.transformPositionToScaledSpace(a,n),m.Cartesian3.normalize(n,n))}e.EllipsoidalOccluder=t});
