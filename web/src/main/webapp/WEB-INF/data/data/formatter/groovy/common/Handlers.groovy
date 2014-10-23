package common

public class Handlers {
    private def handlers;
    private def f
    private def env

    common.Matchers matchers
    common.Functions func

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        func = new common.Functions(handlers: handlers, f:f, env:env)
        matchers =  new common.Matchers(handlers: handlers, f:f, env:env)
    }

    def addDefaultStartAndEndHandlers() {
        handlers.start htmlOrXmlStart
        handlers.end htmlOrXmlEnd
    }

    /**
     * Creates a function that will process all children and sort then according to the sorter that applies to the elements. Then
     * returns the default html for the container elements.
     *
     * @param labeller a function for creating a label from the element
     */
    def entryEl(labeller) {
        return { el ->
            def childData = handlers.processElements(el.children(), el);

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', [label: labeller(el), childData: childData])
            }
            return null
        }
    }
    /**
     * Creates a function which will:
     *
     * 1. Select a single element using the selector function
     * 2. Process all children of the element selected in step 1 with sorter that applies to the element selected in step 1
     * 3. Create a label using executing the labeller on the element passed to handler functions (not element selected in step 1)
     *
     * @param selector a function that will select a single element from the descendants of the element passed to it
     * @param labeller a function for creating a label from the element
     */
    def flattenedEntryEl(selector, labeller) {
        return { parentEl ->
            def el = selector(parentEl)
            def childData = handlers.processElements(el.children(), el);

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', [label: labeller(el), childData: childData])
            }
            return null
        }
    }
    /**
     * Return a function that will find the children of the element and apply the handlerFunc to the first child.  If there is not
     * exactly one child then an error will be thrown.
     */
    static def applyToChild(handlerFunc, name) {
        return {el ->
            def children = el[name]
            if (children.size() == 1) {
                return handlerFunc(children[0])
            } else {
                throw new IllegalStateException("There is supposed to be only a single child when this method is called")
            }
        }
    }
    /**
     * Returns a function that checks if the text is empty, if not then it executes the handlerFunction to process the
     * data from the element and returns that data.
     *
     * @param handlerFunc the function for processing the element.
     * @return
     */
    static def nonEmpty(handlerFunc) {
        def nonEmptyText = {!it.text().isEmpty()}
        when (nonEmptyText, handlerFunc)
    }
    /**
     * Returns a function (usable as a handler) that checks if the text is empty, if not then it executes the
     * handlerFunction to process the data from the element and returns that data.
     *
     * @param test the test to check if the handler should be ran
     * @param handlerFunc  the function for processing the element.
     * @return
     */
    static def when(test, handlerFunc) {
        return {el ->
            if (test(el)) {
                return handlerFunc(el)
            }
        }
    }

    /**
     * Creates function that creates a span containing the information obtained from the element (el) by calling the valueFunc with
     * el as the parameter
     */
    def span(valueFunc) {
        return { el ->
            f.html {
                it.span(valueFunc(el))
            }
        }
    }
    def htmlOrXmlStart = {
        if (func.hasHtmlParam()) {
            return '''
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    <link rel="stylesheet" href="metadata.css"/>
</head>
<body>
<div class="container" gn-metadata="">
'''
        } else {
            return '''
<div class="container" gn-metadata="">
'''
        }
    }
    def htmlOrXmlEnd = {
        if (func.hasHtmlParam()) {
            return '''
</div>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
<script>
    $('.toggler').on('click', function() {
        $(this).toggleClass('closed');
        $(this).parent().nextAll('.target').first().toggle();
    });
</script>
</body>
</html>'''
        } else {
            return '''</div>'''
        }
    }

}