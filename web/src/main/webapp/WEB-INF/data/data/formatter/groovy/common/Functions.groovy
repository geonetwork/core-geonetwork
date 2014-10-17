package common

public class Functions {
    def handlers;
    def f

    def hasHtmlParam = { f.param('html').any{it.toBool()} }

}