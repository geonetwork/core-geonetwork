package org.fao.geonet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for tests that make no changes to Geonetwork database or index within the test method.  All methods marked
 * with the @ReadOnlyTest annotation will be executed together with the @Before and @After methods only ran once around them so the tests
 * must not interfere with one another.  The order of execution is not predictable so do not count on any ordering.
 *
 * @author Jesse on 10/23/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadOnlyTest {
}
