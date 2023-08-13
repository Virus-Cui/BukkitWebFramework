package cn.mrcsh.bukkitwebframework.Bukkit.Annotation;

import cn.mrcsh.bukkitwebframework.Bukkit.Enum.HTTPType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String path() default "";

    String value() default "";
    HTTPType method() default HTTPType.GET;
}
