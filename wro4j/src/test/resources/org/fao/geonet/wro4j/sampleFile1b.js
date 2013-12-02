// This file depends on 1a (which depends on 2a) and 2a to test that 2a is only included once in transitive dependency list

(function(){
   geonet.require('3c');
   geonet.require('1a');
   geonet.provide('1b');
   geonet.require('2a');
});