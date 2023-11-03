package it.polito.wa2.server.security

import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
class WebSecurityConfig(private val jwtAuthConverter: JwtAuthConverter) {
    companion object {
        const val CUSTOMER = "customer"
        const val EXPERT = "expert"
        const val MANAGER = "manager"
    }

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()

        http.cors()

        http.authorizeHttpRequests()
            // PUBLIC
            .requestMatchers(HttpMethod.POST,  "/API/login", "/API/refreshLogin", "API/signup", "API/experts")
            .permitAll()

            // MONITORING
            .requestMatchers(HttpMethod.GET, "/actuator/prometheus")
            .permitAll()

            // PRODUCTS
            .requestMatchers(HttpMethod.GET, "/products", "/API/products")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.GET, "/products/**", "/API/products/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            // PROFILES
            .requestMatchers(HttpMethod.GET, "/profiles/**", "/API/profiles/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/profiles/**", "/API/profiles/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/editProfile", "/API/profiles")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            // EXPERTS
            .requestMatchers(HttpMethod.GET, "/experts", "/API/experts")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.GET, "/experts/**", "/API/experts/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/experts/**", "/API/experts/**")
            .hasAnyRole(EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/editExpert", "/API/experts")
            .hasAnyRole(EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PATCH, "/experts/**", "/API/experts/**")
            .hasAnyRole(MANAGER)

            .requestMatchers(HttpMethod.POST, "/experts/**", "/API/experts/**")
            .hasAnyRole(EXPERT)

            .requestMatchers(HttpMethod.DELETE, "/experts/**", "/API/experts/**")
            .hasAnyRole(EXPERT, MANAGER)

            // MANAGERS
            .requestMatchers(HttpMethod.GET, "/managers", "/API/managers")
            .hasAnyRole(MANAGER)

            .requestMatchers(HttpMethod.GET, "/managers/**", "/API/managers/**")
            .hasAnyRole(MANAGER)

            .requestMatchers(HttpMethod.PUT, "/managers/**", "/API/managers/**")
            .hasAnyRole(MANAGER)

            .requestMatchers(HttpMethod.PUT, "/editManager", "/API/managers")
            .hasAnyRole(MANAGER)

            // LOGS
            .requestMatchers(HttpMethod.GET, "/logs/**", "/API/logs/**")
            .hasAnyRole(MANAGER)

            // PURCHASES
            .requestMatchers(HttpMethod.GET, "/purchases", "/API/purchases")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.GET, "/purchases/**", "/API/purchases/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.POST, "/purchases", "/API/purchases")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.POST, "/purchases/**", "/API/purchases/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/purchases/**", "/API/purchases/**")
            .hasAnyRole(EXPERT, MANAGER)

            // TICKETS
            .requestMatchers(HttpMethod.GET, "/tickets", "/API/tickets")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.GET, "/tickets/**", "/API/tickets/**")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.POST, "/tickets", "/API/tickets")
            .hasAnyRole(CUSTOMER)

            .requestMatchers(HttpMethod.PUT, "/tickets", "/API/tickets")
            .hasAnyRole(CUSTOMER, EXPERT, MANAGER)

            .requestMatchers(HttpMethod.PUT, "/tickets/properties", "/API/tickets/properties")
            .hasAnyRole(EXPERT, MANAGER)

            .requestMatchers(HttpMethod.POST, "/tickets/expert", "/API/tickets/expert")
            .hasAnyRole(MANAGER)

            // OTHER REQUESTS
            .requestMatchers(HttpMethod.GET, "**")
            .permitAll()

            .anyRequest()
            .authenticated()


        http.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthConverter)

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        return http.build()
    }
}