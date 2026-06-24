package fit.tdc.edu.DoAnJava2.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAction {
    String actionType();
    String description() default "";
}
