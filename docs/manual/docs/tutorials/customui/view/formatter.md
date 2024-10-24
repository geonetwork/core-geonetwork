# Create a new formatter {#tuto-formatter}

## Objectives

-   oversee groovy formatter mechnism
-   understand handlers, matchers concepts
-   create a small formatter

## Exercices

1.  Add a new formatter that display all text information of the metadata in basic layout.
2.  Update previous formatter to display labels of the text fields.
3.  Use formatter templating system to render the handler's view
4.  Add styling to your formatter view.
5.  Create a tree for rendering elements
6.  Use translation of iso elements
7.  Use default tree styling
8.  Use all default view formatter elements
9.  Customise default formatter view
10. Overload handlers methods

## Corrections

Go to <https://github.com/fgravin/core-geonetwork/commits/foss4g>

1.  Create a new formatter

    -   create a new folder named 'foss4g" in /schemas/iso19139/src/main/plugin/iso19139/formatter

    -   create a new groovy file in this new folder

    -   text information are stored in `gco:CharacterString`

        ``` groovy
        handlers.add 'gco:CharacterString', {el -> "<div>${el.text()}</div>"}
        ```

2.  Add a matcher and play with `name` and `text` properties.

    ``` groovy
    handlers.add select: {el -> !el.'gco:CharacterString'.text().isEmpty()}, {el ->
        "<div><b>${el.name()}</b> - ${el.text()}</div>"
    }
    ```

3.  Use `handlers.fileResult` function

    -   view.groovy

        ``` groovy
        handlers.add select: {el -> !el.'gco:CharacterString'.text().isEmpty()}, {el ->
            handlers.fileResult('foss4g/elem.html', [name: el.name(), text: el.text()])
        }
        ```

    -   elem.html

        ``` html
        <dl>
        <dt>{{name}}</dt>
        <dd>{{text}}</dd>
        </dl>
        ```

4.  Add a custom less file in wro4j inspected folders and link it to your formatter

    -   formatter.less

        ``` css
        dt {
            width: 230px;
            font-weight: normal;
            font-style: italic;
            color: #555555;
            clear: none;
            padding-left: 15px;
            text-align: left;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            float: left;
          }

          dd {
            margin-left: 250px;
            border-left: 1px solid #999999;
            padding-left: 1em;
            background: #eeeeee;
        }
        ```

    -   view.groovy

        ``` groovy
        handlers.start {
            '''<link rel="stylesheet" href="../../static/formatter.css"/>
              <div class="container">'''
        }
        handlers.end {
            '</div>'
        }

        handlers.add select: {el -> !el.'gco:CharacterString'.text().isEmpty()}, {el ->
            handlers.fileResult('foss4g/elem.html', [name: el.name(), text: el.text()])
        }
        ```

5.  Use `fmt-repeat-only-children` in template and `prent()` function.

    -   view.groovy

        ``` groovy
        ...
        handlers.add select: {el -> !el.'gco:CharacterString'.text().isEmpty()},
                  group: true, {els ->
              def elements = els.collect {el ->
                  [name: el.name(), text: el.text()]
              }
              handlers.fileResult('foss4g/elem.html',
                      [elements: elements, parent: els[0].parent().name()])
        }
        ```

    -   elem.html

        ``` html
        <dl>
            <h3>{{parent}}</h3>
            <div fmt-repeat="el in elements" fmt-repeat-only-children="true">
              <dt>{{el.name}}</dt>
              <dd>{{el.text}}</dd>
            </div>
        </dl>
        ```

6.  See `nodeLabel` function

    -   view.groovy

        ``` groovy
        ...
        handlers.add select: {el -> !el.'gco:CharacterString'.text().isEmpty()},
                 group: true, {els ->
             def elements = els.collect {el ->
                 [name: f.nodeLabel(el), text: el.text()]
             }
             handlers.fileResult('foss4g/elem.html',
                     [elements: elements, parent: f.nodeLabel(els[0].parent())])
         }
        ```

7.  Add `gn-metadata-view` class to your container update your handler.

    -   view.groovy

        ``` groovy
        handlers.start {
           '''<div class="gn-metadata-view container">'''
        }
        handlers.end {
           '</div>'
        }

        def isoHandlers = new iso19139.Handlers(handlers, f, env)

        handlers.add select: isoHandlers.matchers.isTextEl, isoHandlers.isoTextEl
        handlers.add name: 'Container Elements',
               select: isoHandlers.matchers.isContainerEl,
               priority: -1,
               isoHandlers.commonHandlers.entryEl(f.&nodeLabel,
                                                  isoHandlers.addPackageViewClass)
        isoHandlers.addExtentHandlers()
        ```

8.  See `SummaryFactory` class.

    -   view.groovy

        ``` groovy
        import iso19139.SummaryFactory

        def isoHandlers = new iso19139.Handlers(handlers, f, env)

        SummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

        isoHandlers.addDefaultHandlers()
        ```

9.  Add custom option to the `SummaryFactory`

    -   view.groovy

        ``` groovy
        import iso19139.SummaryFactory

        def isoHandlers = new iso19139.Handlers(handlers, f, env)

        def factory = new SummaryFactory(isoHandlers, {summary ->
           summary.title = "My Title"
           summary.addCompleteNavItem = false
           summary.addOverviewNavItem = false
           summary.associated.clear()
        })


        handlers.add name: "Summary Handler",
               select: {it.parent() is it.parent()},
               {factory.create(it).getResult()}
        isoHandlers.addDefaultHandlers()
        ```

10. Add custom behavior to `iso19139.Handlers` constructor

    -   view.groovy

        ``` groovy
        def isoHandlers = new iso19139.Handlers(handlers, f, env) {
            {
                def oldImpl = super.isoTextEl
                isoTextEl = { el ->
                    "----------- ${oldImpl(el)}"
                }
            }
        }
        ```
