package org.feuyeux.rsocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

/**
 * https://docs.spring.io/spring-security/site/docs/5.3.1.RELEASE/reference/html5/#rsocket
 */
@Configuration
@EnableRSocketSecurity
public class HelloSecurityConfig {
    /**
     * authentication
     *
     * @return
     */
    @Bean
    MapReactiveUserDetailsService authentication() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("pw")
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    /**
     * authorization
     *
     * @param rsocket
     * @return
     */
    @Bean
    PayloadSocketAcceptorInterceptor authorization(RSocketSecurity rsocket) {
        rsocket.authorizePayload(spec -> spec
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        ).simpleAuthentication(Customizer.withDefaults());
        return rsocket.build();
    }

    @Bean
    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler messageHandler = new RSocketMessageHandler();
        AuthenticationPrincipalArgumentResolver resolver = new AuthenticationPrincipalArgumentResolver();
        messageHandler.getArgumentResolverConfigurer().addCustomResolver(resolver);
        messageHandler.setRSocketStrategies(strategies);
        return messageHandler;
    }
}