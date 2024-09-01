/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenpb.app.security;

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Should configuration should only works under below condition:
 *
 * * Inside plugin {@link ApplicationContext}
 * * SecurityConfig is enabled on app.
 *
 * Since {@link SecurityConfig} is disabled in plugin, `sbp.security.enabled`
 * property is used to send security configuration enabling status via
 * {@link SpringBootstrap#addPresetProperty(String, Object)} to plugin. It need to be set
 * programmatically on initializing {@link SpringBootstrap}.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnBean(SpringBootPlugin.class)
@ConditionalOnMissingBean(PermissionCheckingAspect.class)
@ConditionalOnProperty(prefix = "sbp.security", name = "enabled", havingValue = "true")
public class PermissionCheckingAspectConfig {

    @Bean
    public PermissionCheckingAspect permissionCheckingAspect() {
        return new PermissionCheckingAspect();
    }

}
