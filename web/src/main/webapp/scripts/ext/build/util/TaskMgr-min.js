/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.util.TaskRunner=function(interval){interval=interval||10;var tasks=[],removeQueue=[];var id=0;var running=false;var stopThread=function(){running=false;clearInterval(id);id=0;};var startThread=function(){if(!running){running=true;id=setInterval(runTasks,interval);}};var removeTask=function(t){removeQueue.push(t);if(t.onStop){t.onStop.apply(t.scope||t);}};var runTasks=function(){if(removeQueue.length>0){for(var i=0,len=removeQueue.length;i<len;i++){tasks.remove(removeQueue[i]);}
removeQueue=[];if(tasks.length<1){stopThread();return;}}
var c,now=new Date().getTime();for(var i=0,len=tasks.length;i<len;++i){var t=tasks[i];var itime=now-t.taskRunTime;if(t.interval<=itime){c=++t.taskRunCount;var rt=t.run.apply(t.scope||t,t.args||[c]);t.taskRunTime=now;if(rt===false||c===t.repeat){removeTask(t);return;}}
if(t.duration&&t.duration<=(now-t.taskStartTime)){removeTask(t);}}};this.start=function(task){tasks.push(task);task.taskStartTime=new Date().getTime();task.taskRunTime=0;task.taskRunCount=0;startThread();return task;};this.stop=function(task){removeTask(task);return task;};this.stopAll=function(){stopThread();for(var i=0,len=tasks.length;i<len;i++){if(tasks[i].onStop){tasks[i].onStop();}}
tasks=[];removeQueue=[];};};Ext.TaskMgr=new Ext.util.TaskRunner();