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
define(["./defined-2a4f2d00","./Check-e5651467","./freezeObject-a51e076f","./defaultValue-29c9b1af","./Math-7782f09e","./Cartesian2-ba70b51f","./defineProperties-c817531e","./Transforms-5119c07b","./RuntimeError-51c34ab4","./WebGLConstants-90dbfe2f","./ComponentDatatype-418b1c61","./GeometryAttribute-8bc1900e","./when-1faa3867","./GeometryAttributes-f8548d3f","./IndexDatatype-2bcfc06b","./GeometryOffsetAttribute-fa4e7a11","./EllipsoidOutlineGeometry-ec4c7190"],function(n,e,i,r,t,a,s,o,d,l,c,u,f,p,m,y,b){"use strict";function G(e){var i=r.defaultValue(e.radius,1),t={radii:new a.Cartesian3(i,i,i),stackPartitions:e.stackPartitions,slicePartitions:e.slicePartitions,subdivisions:e.subdivisions};this._ellipsoidGeometry=new b.EllipsoidOutlineGeometry(t),this._workerName="createSphereOutlineGeometry"}G.packedLength=b.EllipsoidOutlineGeometry.packedLength,G.pack=function(e,i,t){return b.EllipsoidOutlineGeometry.pack(e._ellipsoidGeometry,i,t)};var k=new b.EllipsoidOutlineGeometry,v={radius:void 0,radii:new a.Cartesian3,stackPartitions:void 0,slicePartitions:void 0,subdivisions:void 0};return G.unpack=function(e,i,t){var r=b.EllipsoidOutlineGeometry.unpack(e,i,k);return v.stackPartitions=r._stackPartitions,v.slicePartitions=r._slicePartitions,v.subdivisions=r._subdivisions,n.defined(t)?(a.Cartesian3.clone(r._radii,v.radii),t._ellipsoidGeometry=new b.EllipsoidOutlineGeometry(v),t):(v.radius=r._radii.x,new G(v))},G.createGeometry=function(e){return b.EllipsoidOutlineGeometry.createGeometry(e._ellipsoidGeometry)},function(e,i){return n.defined(i)&&(e=G.unpack(e,i)),G.createGeometry(e)}});
