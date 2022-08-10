package com.ironinstruction.api.security;

import com.ironinstruction.api.user.UserService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class WebSecurityConfig {
    private final static CustomAuthenticationFailureHandler failureHandler = new CustomAuthenticationFailureHandler();

    // I'm unsure whether it is safe to have security beans outside of security chain. will research further
    // but for now this is a fine solution
    @Bean
    public FilterRegistrationBean<JWTAuthenticationFilter> authenticationFilter(UserService userService) {
        CustomAuthenticationManager authenticationManager = new CustomAuthenticationManager(userService);
        FilterRegistrationBean<JWTAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JWTAuthenticationFilter(authenticationManager, failureHandler));
        registrationBean.addUrlPatterns("/api/v1/login");
        registrationBean.setOrder(1);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JWTAuthorizationFilter> authorizationFilter (UserService userService) {
        CustomAuthenticationManager authenticationManager = new CustomAuthenticationManager(userService);
        FilterRegistrationBean<JWTAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JWTAuthorizationFilter(authenticationManager, failureHandler));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable(); // disable csrf because I use JWT instead

        return http.build();
    }
}
