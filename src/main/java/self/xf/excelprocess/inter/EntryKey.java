package self.xf.excelprocess.inter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可见
@Target(ElementType.FIELD)
public @interface EntryKey {
}
