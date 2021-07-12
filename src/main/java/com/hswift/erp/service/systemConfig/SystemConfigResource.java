package com.hswift.erp.service.systemConfig;

import com.hswift.erp.service.ResourceInfo;

import java.lang.annotation.*;

/**
 * @author Jay Shen   2018-10-7 15:26:27
 */
@ResourceInfo(value = "systemConfig")
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemConfigResource {
}
