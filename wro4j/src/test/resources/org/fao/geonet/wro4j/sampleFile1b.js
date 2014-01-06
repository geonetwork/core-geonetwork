// This file depends on 1a (which depends on 2a) and 2a to test that 2a is only included once in transitive dependency list

(function(){
   goog.require('3c');
   goog.require('1a');
   goog.provide('1b');
   goog.require('2a');
});