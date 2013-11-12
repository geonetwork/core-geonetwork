/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 * @class Ext.util.DelayedTask
 * Provides a convenient method of performing setTimeout where a new
 * timeout cancels the old timeout. An example would be performing validation on a keypress.
 * You can use this class to buffer
 * the keypress events for a certain number of milliseconds, and perform only if they stop
 * for that amount of time.
 * @constructor The parameters to this constructor serve as defaults and are not required.
 * @param {Function} fn (optional) The default function to timeout
 * @param {Object} scope (optional) The default scope of that timeout
 * @param {Array} args (optional) The default Array of arguments
 */
Ext.util.DelayedTask = function(fn, scope, args){
    var id = null;

    var call = function(){
        id = null;
        fn.apply(scope, args || []);
    };
    /**
     * Cancels any pending timeout and queues a new one
     * @param {Number} delay The milliseconds to delay
     * @param {Function} newFn (optional) Overrides function passed to constructor
     * @param {Object} newScope (optional) Overrides scope passed to constructor
     * @param {Array} newArgs (optional) Overrides args passed to constructor
     */
    this.delay = function(delay, newFn, newScope, newArgs){
        if(id){
            this.cancel();
        }
        fn = newFn || fn;
        scope = newScope || scope;
        args = newArgs || args;
        if(!id){
            id = setTimeout(call, delay);
        }
    };

    /**
     * Cancel the last queued timeout
     */
    this.cancel = function(){
        if(id){
            clearTimeout(id);
            id = null;
        }
    };
};