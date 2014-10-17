package common

public class Handlers {
    def handlers;
    def f

    common.Matchers matchers
    common.Functions func

    public Handlers(handlers, f) {
        this.handlers = handlers
        this.f = f
        func = new common.Functions(handlers: handlers, f:f)
        matchers =  new common.Matchers(handlers: handlers, f:f)
    }

    def addDefaultStartAndEndHandlers() {
        handlers.start htmlOrXmlStart
        handlers.end htmlOrXmlEnd
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
<div class="container" gn-metadata>
'''
        } else {
            return '''
<div class="container" gn-metadata>
'''
        }
    }
    def htmlOrXmlEnd = {
        if (func.hasHtmlParam()) {
            return '''
</div>
</body>
</html>'''
        } else {
            return '''</div>'''
        }
    }

}