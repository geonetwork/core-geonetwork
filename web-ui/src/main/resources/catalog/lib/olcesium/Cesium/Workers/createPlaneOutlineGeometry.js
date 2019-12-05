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
define(["./defined-2a4f2d00","./Check-e5651467","./freezeObject-a51e076f","./defaultValue-29c9b1af","./Math-7782f09e","./Cartesian2-ba70b51f","./defineProperties-c817531e","./Transforms-5119c07b","./RuntimeError-51c34ab4","./WebGLConstants-90dbfe2f","./ComponentDatatype-418b1c61","./GeometryAttribute-8bc1900e","./when-1faa3867","./GeometryAttributes-f8548d3f"],function(r,e,t,n,a,i,o,u,c,f,s,y,p,b){"use strict";function m(){this._workerName="createPlaneOutlineGeometry"}m.packedLength=0,m.pack=function(e,t){return t},m.unpack=function(e,t,n){return r.defined(n)?n:new m};var d=new i.Cartesian3(-.5,-.5,0),w=new i.Cartesian3(.5,.5,0);return m.createGeometry=function(){var e=new b.GeometryAttributes,t=new Uint16Array(8),n=new Float64Array(12);return n[0]=d.x,n[1]=d.y,n[2]=d.z,n[3]=w.x,n[4]=d.y,n[5]=d.z,n[6]=w.x,n[7]=w.y,n[8]=d.z,n[9]=d.x,n[10]=w.y,n[11]=d.z,e.position=new y.GeometryAttribute({componentDatatype:s.ComponentDatatype.DOUBLE,componentsPerAttribute:3,values:n}),t[0]=0,t[1]=1,t[2]=1,t[3]=2,t[4]=2,t[5]=3,t[6]=3,t[7]=0,new y.Geometry({attributes:e,indices:t,primitiveType:y.PrimitiveType.LINES,boundingSphere:new u.BoundingSphere(i.Cartesian3.ZERO,Math.sqrt(2))})},function(e,t){return r.defined(t)&&(e=m.unpack(e,t)),m.createGeometry(e)}});
