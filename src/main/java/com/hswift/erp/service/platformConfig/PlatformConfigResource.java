package com.hswift.erp.service.platformConfig;

import com.hswift.erp.service.ResourceInfo;

import java.lang.annotation.*;

/**
 * @author Jay Shen   2020-10-16 22:26:27
 */
@ResourceInfo(value = "platformConfig")
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformConfigResource {
}
