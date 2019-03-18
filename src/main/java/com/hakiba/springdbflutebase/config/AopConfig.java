package com.hakiba.springdbflutebase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author hakiba
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true) // TODO: proxyTargetClass = true の意味
public class AopConfig {
}
