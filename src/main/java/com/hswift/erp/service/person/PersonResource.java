package com.hswift.erp.service.person;

import com.hswift.erp.service.ResourceInfo;

import java.lang.annotation.*;

/**
 * @author Jay Shen   2018-10-7 15:26:27
 */
@ResourceInfo(value = "person")
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersonResource {
}
