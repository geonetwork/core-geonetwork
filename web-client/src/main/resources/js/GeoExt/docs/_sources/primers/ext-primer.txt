.. _ext-primer: 

=============
 Primer: Ext
=============

GeoExt extends `Ext JS <http://www.sencha.com/products/js/>`_, a rich library of web UI
widgets and helper classes.  Using GeoExt requires a working knowledge
of Ext's idioms.  This tutorial provides a quick overview of core Ext concepts.

.. _ext-getting-started:

Getting Started
===============

To start using Ext, you will first have to `download
<http://www.sencha.com/products/js/download.php>`_ it.
For more complete instructions about how configure a web page to use
Ext, you can check the :doc:`../tutorials/quickstart` tutorial.

When you download Ext, you also get their excellent
`Examples <http://dev.sencha.com/deploy/dev/examples/>`_ and
`API Documentation <http://dev.sencha.com/deploy/dev/docs/>`_, which you can also
look at on-line for education and reference.

In order to get Ext running on a page you will need to have something
like the following in the ``<head>`` of an HTML page in a directory
that is published by your web server.

.. code-block:: html

    <script src="ext-3.2.1/adapter/ext/ext-base.js" type="text/javascript"></script>
    <script src="ext-3.2.1/ext-all.js"  type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="ext-3.2.1/resources/css/ext-all.css"></link>

This will load the code and styles for Ext.  Change the paths
according to where you have put the Ext files.

When writing Ext code, most of what you will be doing is instantiating
classes with constructors that takes a single object--its
configuration object--as an argument.  This snippet demonstrates this
coding pattern:

.. code-block:: javascript

    Ext.onReady(function(){
        var myPanel = new Ext.Panel({
            title: 'Hello World!',
            html: '<i>Hello World!</i> Please enjoy this primer on Ext!',
            collapsible: true,
            width:300,
            renderTo: 'panelDiv'
        });        
    });

There are a few things to note about this example:

* This code uses Ext's ``onReady`` method to trigger the method when the
  document's body is ready. (This is cleaner than using body's ``onready``
  event, and with ``Ext.onReady`` several functions can be queued for execution
  before the page loads.)

* When the page is ready, the ``Ext.Panel`` constructor is called with a
  single configuration object as argument.  The Panel's structure should
  be familiar from your desktop experience.  It has a ``title`` which
  runs across the top, and some content which in this case is ``html``
  provided in the configuration.

* Many configuration options (best explored in the Ext examples and API
  documention) are available.  Here, they are represented by the
  ``collapsible`` property, which allows the user to collapse the panel
  much like you can minimize your browser's window, and the ``width`` of
  the panel specified in pixels.

* Lastly, this code assumes that somewhere in the DOM of the page is a
  ``div`` with the id ``panelDiv``.  When the Panel is constructed, it
  will be automatically rendered in this div because of the ``renderTo``
  option. (This option can be left out and panels rendered manually, if
  desired.)

.. _ext-basic-layout:

Basic Layout
============

Ext makes it easy to separate out your UI into logical blocks.
Most often you will be using one or more nested *Containers*.  The
``Ext.Panel`` built above is the most common kind of container.  You
can nest panels using the ``items`` property.  For example:

.. code-block:: javascript

    Ext.onReady(function(){
        var myPanel = new Ext.Panel({
            title: 'Top level',
            layout: 'border',
            items: [{
                xtype:'panel',
                title:'Sub1',
                html:'Contents of sub panel 1',
                region: 'east'
            },{
                xtype:'panel',
                title: 'Sub2',
                html:'Contents of sub panel 2',
                region: 'center'
            }],
            width:300,
            height:200,
            renderTo:'panelDiv'
        });        
    });

This code introduces some new concepts:

* Each of the objects in the ``items`` array is a configuration
  object for a panel like the one in the earlier example. 

* The ``Ext.Panel`` constructor is never called, however.  Instead,
  the ``xtype`` option is used.  By setting the xtype, you tell Ext
  what class the configuration is for, and Ext instantiates that class
  when appropriate.

* The ``layout`` property on the outer container determines the
  position of the items within it.  Here, we have set the layout to be
  a *border* layout, which requires that items be given a ``region``
  property like "center", "north", "south", "east", or "west".

Ext provides a variety of other layouts, including a Tab layout and a
Wizard layout.  The best way to explore these layouts is using the `Ext
Layout Browser
<http://dev.sencha.com/deploy/dev/examples/layout-browser/layout-browser.html>`_
, which demonstrates each layout and provides sample code.

