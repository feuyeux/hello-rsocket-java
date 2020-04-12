//package org.feuyeux.rsocket;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.rsocket.RSocketStrategies;
//import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
//import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
//import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
//import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
//
//@Configuration
//@EnableRSocketSecurity
//public class HelloSecurityConfig {
//    @Bean
//    MapReactiveUserDetailsService authentication() {
//        UserDetails jlong = User.withDefaultPasswordEncoder().username("jlong").password("pw").roles("USER").build();
//        UserDetails rwinch = User.withDefaultPasswordEncoder().username("rwinch").password("pw").roles("ADMIN",
//        "USER")
//            .build();
//        return new MapReactiveUserDetailsService(jlong, rwinch);
//    }
//
//    @Bean
//    PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
//        PayloadSocketAcceptorInterceptor interceptor = security
//            .authorizePayload(spec -> spec
//                .route("hello-response").authenticated()
//                .anyExchange().permitAll()
//            )
//            .simpleAuthentication(Customizer.withDefaults())
//            .build();
//        return interceptor;
//    }
//
//    @Bean
//    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
//        RSocketMessageHandler mh = new RSocketMessageHandler();
//        AuthenticationPrincipalArgumentResolver authenticationPrincipalArgumentResolver
//            = new AuthenticationPrincipalArgumentResolver();
//        mh.getArgumentResolverConfigurer().addCustomResolver(authenticationPrincipalArgumentResolver);
//        mh.setRSocketStrategies(strategies);
//        return mh;
//    }
//}