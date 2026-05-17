package com.design.order_management_system.annotation;

import com.design.order_management_system.security.JwtAuthenticationFilter;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation in a test class instead of the WebMvcTest
 * annotation in a Web MVC test.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtAuthenticationFilter.class,
                        LogoutFilter.class
                }
        )
)
public @interface WebMvcSliceTest {

    @AliasFor(
            annotation = WebMvcTest.class,
            attribute = "controllers"
    )
    Class<?>[] value() default {};
}
