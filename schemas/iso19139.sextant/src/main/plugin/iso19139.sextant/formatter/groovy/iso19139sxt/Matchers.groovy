package iso19139sxt

public class Matchers {
    def handlers;
    def f
    def env

    def isDataQualityInfo = { el ->
        !el.'gmd:dataQualityInfo'.isEmpty()
    }
}
