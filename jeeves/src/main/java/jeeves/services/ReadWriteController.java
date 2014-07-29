package jeeves.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the service can make modifications to the database or lucene and thus should not be executed when in read-only mode.
 *
 * @author Jesse on 6/4/2014.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadWriteController {
}
