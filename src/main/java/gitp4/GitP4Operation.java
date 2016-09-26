package gitp4;

import gitp4.cli.GitP4OperationOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chriskang on 8/25/2016.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface GitP4Operation {
    Class<? extends GitP4OperationOption> option();
    String operationName() default "";
    String description() default "";
}
