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
define(["exports","./Math-7782f09e","./Cartesian2-ba70b51f","./Transforms-5119c07b"],function(a,R,W,h){"use strict";var r={},x=new W.Cartesian3,M=new W.Cartesian3,f=new h.Quaternion,z=new h.Matrix3;function S(a,r,e,t,i,n,s,o,l,C){var y=a+r;W.Cartesian3.multiplyByScalar(t,Math.cos(y),x),W.Cartesian3.multiplyByScalar(e,Math.sin(y),M),W.Cartesian3.add(x,M,x);var u=Math.cos(a);u*=u;var m=Math.sin(a);m*=m;var c=n/Math.sqrt(s*u+i*m)/o;return h.Quaternion.fromAxisAngle(x,c,f),h.Matrix3.fromQuaternion(f,z),h.Matrix3.multiplyByVector(z,l,C),W.Cartesian3.normalize(C,C),W.Cartesian3.multiplyByScalar(C,o,C),C}var B=new W.Cartesian3,b=new W.Cartesian3,Q=new W.Cartesian3,_=new W.Cartesian3;r.raisePositionsToHeight=function(a,r,e){for(var t=r.ellipsoid,i=r.height,n=r.extrudedHeight,s=e?a.length/3*2:a.length/3,o=new Float64Array(3*s),l=a.length,C=e?l:0,y=0;y<l;y+=3){var u=y+1,m=y+2,c=W.Cartesian3.fromArray(a,y,B);t.scaleToGeodeticSurface(c,c);var h=W.Cartesian3.clone(c,b),x=t.geodeticSurfaceNormal(c,_),M=W.Cartesian3.multiplyByScalar(x,i,Q);W.Cartesian3.add(c,M,c),e&&(W.Cartesian3.multiplyByScalar(x,n,M),W.Cartesian3.add(h,M,h),o[y+C]=h.x,o[u+C]=h.y,o[m+C]=h.z),o[y]=c.x,o[u]=c.y,o[m]=c.z}return o};var G=new W.Cartesian3,H=new W.Cartesian3,N=new W.Cartesian3;r.computeEllipsePositions=function(a,r,e){var t=a.semiMinorAxis,i=a.semiMajorAxis,n=a.rotation,s=a.center,o=8*a.granularity,l=t*t,C=i*i,y=i*t,u=W.Cartesian3.magnitude(s),m=W.Cartesian3.normalize(s,G),c=W.Cartesian3.cross(W.Cartesian3.UNIT_Z,s,H);c=W.Cartesian3.normalize(c,c);var h=W.Cartesian3.cross(m,c,N),x=1+Math.ceil(R.CesiumMath.PI_OVER_TWO/o),M=R.CesiumMath.PI_OVER_TWO/(x-1),f=R.CesiumMath.PI_OVER_TWO-x*M;f<0&&(x-=Math.ceil(Math.abs(f)/M));var z,_,v,O,d,p=r?new Array(3*(x*(x+2)*2)):void 0,w=0,P=B,T=b,I=4*x*3,g=I-1,E=0,V=e?new Array(I):void 0;for(P=S(f=R.CesiumMath.PI_OVER_TWO,n,h,c,l,y,C,u,m,P),r&&(p[w++]=P.x,p[w++]=P.y,p[w++]=P.z),e&&(V[g--]=P.z,V[g--]=P.y,V[g--]=P.x),f=R.CesiumMath.PI_OVER_TWO-M,z=1;z<x+1;++z){if(P=S(f,n,h,c,l,y,C,u,m,P),T=S(Math.PI-f,n,h,c,l,y,C,u,m,T),r){for(p[w++]=P.x,p[w++]=P.y,p[w++]=P.z,v=2*z+2,_=1;_<v-1;++_)O=_/(v-1),d=W.Cartesian3.lerp(P,T,O,Q),p[w++]=d.x,p[w++]=d.y,p[w++]=d.z;p[w++]=T.x,p[w++]=T.y,p[w++]=T.z}e&&(V[g--]=P.z,V[g--]=P.y,V[g--]=P.x,V[E++]=T.x,V[E++]=T.y,V[E++]=T.z),f=R.CesiumMath.PI_OVER_TWO-(z+1)*M}for(z=x;1<z;--z){if(P=S(-(f=R.CesiumMath.PI_OVER_TWO-(z-1)*M),n,h,c,l,y,C,u,m,P),T=S(f+Math.PI,n,h,c,l,y,C,u,m,T),r){for(p[w++]=P.x,p[w++]=P.y,p[w++]=P.z,v=2*(z-1)+2,_=1;_<v-1;++_)O=_/(v-1),d=W.Cartesian3.lerp(P,T,O,Q),p[w++]=d.x,p[w++]=d.y,p[w++]=d.z;p[w++]=T.x,p[w++]=T.y,p[w++]=T.z}e&&(V[g--]=P.z,V[g--]=P.y,V[g--]=P.x,V[E++]=T.x,V[E++]=T.y,V[E++]=T.z)}P=S(-(f=R.CesiumMath.PI_OVER_TWO),n,h,c,l,y,C,u,m,P);var A={};return r&&(p[w++]=P.x,p[w++]=P.y,p[w++]=P.z,A.positions=p,A.numPts=x),e&&(V[g--]=P.z,V[g--]=P.y,V[g--]=P.x,A.outerPositions=V),A},a.EllipseGeometryLibrary=r});
