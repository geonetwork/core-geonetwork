package common

public class Matchers {
    def handlers;
    def f
    def env

    def hasChild(childName) {
        return { !it.children().find {it.name() == childName}.isEmpty() }
    }
}
