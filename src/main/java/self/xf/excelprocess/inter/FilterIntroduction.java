package self.xf.excelprocess.inter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可见
public @interface FilterIntroduction {
    String description() default "";
}
