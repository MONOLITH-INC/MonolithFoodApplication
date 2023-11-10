package pe.edu.upc.MonolithFoodApplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import pe.edu.upc.MonolithFoodApplication.filters.JwtAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private AuthenticationProvider authProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // ? CSRF: Es el que se encarga de manejar los tokens CSRF (Spring Security) ? //
            .csrf(csrf -> csrf.disable())
            // ? CORS: Permite que la aplicación acepte solicitudes CORS de cualquier origen (*) para métodos HTTP comunes (GET, HEAD, POST) ? //
            // .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration configuration = new CorsConfiguration();
                // Define los métodos permitidos
                configuration.setAllowedMethods(Arrays.asList(
                    HttpMethod.GET.name(),
                    HttpMethod.POST.name(),
                    HttpMethod.PUT.name(),
                    HttpMethod.DELETE.name(),
                    HttpMethod.PATCH.name(),
                    HttpMethod.OPTIONS.name()
                ));
                // Permite cualquier origen y cualquier encabezado
                configuration.setAllowedOrigins(Arrays.asList("*"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                return configuration;
            }))
            // ? Permite o bloquea la conexión a los endpoints ? //
            .authorizeHttpRequests(authRequest -> {
                authRequest.requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll();
                authRequest.requestMatchers("/favicon.ico", "/error").permitAll(); // OAuth2
                authRequest.requestMatchers("/v3/api-docs/**").permitAll(); // Swagger API
                authRequest.requestMatchers("/doc/swagger-ui/**").permitAll(); // Swagger UI
                authRequest.anyRequest().authenticated();
            })
            // ? Oauth2 Login
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> 
                    response.sendRedirect("/auth/oauth2"))
                .failureHandler((request, response, exception) -> 
                    response.sendRedirect("/login"))
            )
            // .oauth2Login(oauth2 -> oauth2
            //     .successHandler((request, response, authentication) -> {
            //         response.sendRedirect("http://localhost:4200/login");
            //     })
            //     .failureHandler((request, response, exception) -> {
            //         response.sendRedirect("http://localhost:4200/login?error");
            //     })
            // )
            // ? Authentication Provider: Es el que se encarga de validar las credenciales de los usuarios ? //
            .authenticationProvider(authProvider)
            .formLogin(withDefaults())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();

    }
}
