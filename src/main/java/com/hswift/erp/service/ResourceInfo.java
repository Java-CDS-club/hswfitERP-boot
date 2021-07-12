package com.hswift.erp.service;

import java.lang.annotation.*;

/**
 * @author Jay Shen 2018-10-7 15:25:39
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ResourceInfo {
    String value();
}