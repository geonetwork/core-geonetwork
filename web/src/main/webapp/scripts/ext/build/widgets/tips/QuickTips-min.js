/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.QuickTips=function(){var tip,locks=[];return{init:function(autoRender){if(!tip){if(!Ext.isReady){Ext.onReady(function(){Ext.QuickTips.init(autoRender);});return;}
tip=new Ext.QuickTip({elements:'header,body'});if(autoRender!==false){tip.render(Ext.getBody());}}},enable:function(){if(tip){locks.pop();if(locks.length<1){tip.enable();}}},disable:function(){if(tip){tip.disable();}
locks.push(1);},isEnabled:function(){return tip!==undefined&&!tip.disabled;},getQuickTip:function(){return tip;},register:function(){tip.register.apply(tip,arguments);},unregister:function(){tip.unregister.apply(tip,arguments);},tips:function(){tip.register.apply(tip,arguments);}}}();