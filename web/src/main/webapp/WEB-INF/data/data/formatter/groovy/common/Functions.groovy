package common

public class Functions {
    def handlers;
    def f
    def env

    def hasHtmlParam = { env.param('html').any{it.toBool()} }

}