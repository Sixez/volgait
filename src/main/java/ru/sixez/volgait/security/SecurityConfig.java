package ru.sixez.volgait.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import ru.sixez.volgait.controller.*;
import ru.sixez.volgait.service.AccountDetailsService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
    @Autowired
    private JwtAuthFilter authFilter;
    @Autowired
    private AccountDetailsService accountDetailsService;

    @Bean
    public SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.anonymous(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(conf -> {
            CorsConfiguration confSource = new CorsConfiguration();
            confSource.setAllowedMethods(Arrays.asList(
                    HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name()
            ));
            confSource.applyPermitDefaultValues();
            return confSource;
        }));

        http.authorizeHttpRequests(request -> {
            // wide open endpoints
            request.requestMatchers(
                    AccountController.SIGN_IN_ENDPOINT, AccountController.SIGN_UP_ENDPOINT,
                    "/swagger-ui/**"
            ).permitAll();

            request.requestMatchers(HttpMethod.GET, TransportController.ROUTE + "/*").permitAll();
            request.requestMatchers(HttpMethod.GET, RentController.TRANSPORT_ENDPOINT).permitAll();

            // account endpoints open only for authenticated users
            request.requestMatchers(
                    AccountController.ME_ENDPOINT, AccountController.SIGN_OUT_ENDPOINT,
                    AccountController.UPDATE_ENDPOINT, PaymentController.HESOYAM_ENDPOINT
            ).hasAuthority("USER");

            // transport endpoints open only for authenticated users,
            // except transport info endpoint widely open
            request.requestMatchers(HttpMethod.POST, TransportController.ROUTE).hasAuthority("USER");
            request.requestMatchers(HttpMethod.PUT, TransportController.ROUTE + "/*").hasAuthority("USER");
            request.requestMatchers(HttpMethod.DELETE, TransportController.ROUTE + "/*").hasAuthority("USER");

            // rent endpoints is
            request.requestMatchers(
                    RentController.MY_HISTORY_ENDPOINT, RentController.TRANSPORT_HISTORY_ENDPOINT + "/*",
                    RentController.NEW_RENT_ENDPOINT + "/*", RentController.END_RENT_ENDPOINT + "/*"
            ).hasAuthority("USER");

            request.requestMatchers(HttpMethod.GET, RentController.ROUTE + "/{rentId}").hasAuthority("USER");

            // hesoyam
            request.requestMatchers(PaymentController.ROUTE + "/**").hasAuthority("USER");

            // admin endpoints open only for admins
            request.requestMatchers(AdminController.ROUTE + "/**").hasAuthority("ADMIN");

            request.anyRequest().permitAll();
        });

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement(mgr -> mgr.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(accountDetailsService);
        provider.setPasswordEncoder(getPasswordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
