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
define(["exports","./defined-2a4f2d00","./Check-e5651467","./Math-7782f09e","./Cartesian2-ba70b51f","./Transforms-5119c07b","./GeometryAttribute-8bc1900e"],function(t,m,n,y,O,a,G){"use strict";var p=Math.cos,v=Math.sin,x=Math.sqrt,r={computePosition:function(t,n,a,r,e,o,s){var i=n.radiiSquared,g=t.nwCorner,h=t.boundingRectangle,u=g.latitude-t.granYCos*r+e*t.granXSin,c=p(u),C=v(u),l=i.z*C,S=g.longitude+r*t.granYSin+e*t.granXCos,d=c*p(S),w=c*v(S),M=i.x*d,X=i.y*w,Y=x(M*d+X*w+l*C);if(o.x=M/Y,o.y=X/Y,o.z=l/Y,a){var f=t.stNwCorner;m.defined(f)?(u=f.latitude-t.stGranYCos*r+e*t.stGranXSin,S=f.longitude+r*t.stGranYSin+e*t.stGranXCos,s.x=(S-t.stWest)*t.lonScalar,s.y=(u-t.stSouth)*t.latScalar):(s.x=(S-h.west)*t.lonScalar,s.y=(u-h.south)*t.latScalar)}}},R=new G.Matrix2,b=new O.Cartesian3,P=new O.Cartographic,W=new O.Cartesian3,_=new a.GeographicProjection;function T(t,n,a,r,e,o,s){var i=Math.cos(n),g=r*i,h=a*i,u=Math.sin(n),c=r*u,C=a*u;b=_.project(t,b),b=O.Cartesian3.subtract(b,W,b);var l=G.Matrix2.fromRotation(n,R);b=G.Matrix2.multiplyByVector(l,b,b),b=O.Cartesian3.add(b,W,b),o-=1,s-=1;var S=(t=_.unproject(b,t)).latitude,d=S+o*C,w=S-g*s,M=S-g*s+o*C,X=Math.max(S,d,w,M),Y=Math.min(S,d,w,M),f=t.longitude,m=f+o*h,p=f+s*c,v=f+s*c+o*h;return{north:X,south:Y,east:Math.max(f,m,p,v),west:Math.min(f,m,p,v),granYCos:g,granYSin:c,granXCos:h,granXSin:C,nwCorner:t}}r.computeOptions=function(t,n,a,r,e,o,s){var i,g,h,u,c,C=t.east,l=t.west,S=t.north,d=t.south,w=!1,M=!1;S===y.CesiumMath.PI_OVER_TWO&&(w=!0),d===-y.CesiumMath.PI_OVER_TWO&&(M=!0);var X=S-d;h=(c=C<l?y.CesiumMath.TWO_PI-l+C:C-l)/((i=Math.ceil(c/n)+1)-1),u=X/((g=Math.ceil(X/n)+1)-1);var Y=O.Rectangle.northwest(t,o),f=O.Rectangle.center(t,P);0===a&&0===r||(f.longitude<Y.longitude&&(f.longitude+=y.CesiumMath.TWO_PI),W=_.project(f,W));var m=u,p=h,v=O.Rectangle.clone(t,e),G={granYCos:m,granYSin:0,granXCos:p,granXSin:0,nwCorner:Y,boundingRectangle:v,width:i,height:g,northCap:w,southCap:M};if(0!==a){var x=T(Y,a,h,u,0,i,g);S=x.north,d=x.south,C=x.east,l=x.west,G.granYCos=x.granYCos,G.granYSin=x.granYSin,G.granXCos=x.granXCos,G.granXSin=x.granXSin,v.north=S,v.south=d,v.east=C,v.west=l}if(0!==r){a-=r;var R=O.Rectangle.northwest(v,s),b=T(R,a,h,u,0,i,g);G.stGranYCos=b.granYCos,G.stGranXCos=b.granXCos,G.stGranYSin=b.granYSin,G.stGranXSin=b.granXSin,G.stNwCorner=R,G.stWest=b.west,G.stSouth=b.south}return G},t.RectangleGeometryLibrary=r});
