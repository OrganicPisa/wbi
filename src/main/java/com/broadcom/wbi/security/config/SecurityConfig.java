package com.broadcom.wbi.security.config;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.EmployeePermission;
import com.broadcom.wbi.security.filter.JwtTokenAuthenticationFilter;
import com.broadcom.wbi.security.handler.*;
import com.broadcom.wbi.service.jpa.EmployeePermissionService;
import com.broadcom.wbi.service.jpa.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String LDAP_SEARCH_FILTER = "(sAMAccountName={0})";
    private static final String LDAP_SEARCH_BASE = "OU=Users,OU=Accounts,DC=Broadcom,DC=net";
    private static final String LDAP_URL = "ldap://WLVNVMGC01.broadcom.net:389";
    private static final String LDAP_USERNAME = "CN=Svcpms Prodldap01,OU=Generic,OU=ServiceAccounts,OU=Accounts,DC=Broadcom,DC=net";
    private static final String LDAP_PASSWORD = "Pm5$Ldap^acct01";

    private static final String[] IGNORED_RESOURCE_LIST = new String[]{"/fonts/**", "/webjars/**", "/css/**",
            "/files/**", "/js/**", "/api/auth/**", "/favicon.ico", "/login", "/logout"};
//    private static final String[] IGNORED_UI_LIST = new String[]{ "/", "/index", "/home",
//            "/login", "/logout"};
//            , "/header", "/footer", "/resources/**", "/segment/**", "/program/**", "/report/**", "/resource/**" };

    @Value("${cookie.jwt}")
    private String TOKEN_COOKIE;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeePermissionService employeePermissionService;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .headers().disable()
                .csrf().disable()
                .addFilterAfter(jwtTokenAuthenticationFilter(), ExceptionTranslationFilter.class)
                .addFilterAfter(corsFilter(), ExceptionTranslationFilter.class)
                .authorizeRequests()
                .antMatchers(IGNORED_RESOURCE_LIST).permitAll()
//                    .antMatchers(IGNORED_UI_LIST).permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler())
                .deleteCookies(TOKEN_COOKIE)
                .and()
                /*
                  anonymous() consider no authentication as being anonymous instead of null in the security context.
                 */
                .anonymous()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPointHandler())
                .accessDeniedHandler(authenticationAccessDeniedHandler())
                .and()
                /* No Http session is used to get the security context */
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter() {
        return new JwtTokenAuthenticationFilter("/**");
    }

    /************************************************************************
     * LDAP Authentication
     ************************************************************************/
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//	    auth.eraseCredentials(true);
//		auth.inMemoryAuthentication()
//			.withUser("admin").password("@dm1n@123").roles("admin")
//			.and().withUser("viewer").password("viewpassword").roles("view")
//			.and().withUser("pm").password("csgnpi").roles("pm");
//		auth.authenticationProvider(ldapAuthProvider());

        auth.ldapAuthentication()
                .userSearchBase(LDAP_SEARCH_BASE)
                .userSearchFilter(LDAP_SEARCH_FILTER)
                .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator())
                .contextSource(contextSource());
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        /*1
          Specificity here : we don't get the Role by reading the members of available groups (which is implemented by
          default in Spring security LDAP), but we retrieve the groups from the field memberOf of the user.
         */
        class MyLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
            SpringSecurityLdapTemplate ldapTemplate;

            MyLdapAuthoritiesPopulator(ContextSource contextSource) {
                ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
            }

            @Override
            public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations dirContextOperations, String username) {
                Set<GrantedAuthority> gas = new HashSet<GrantedAuthority>();
                String id = username.substring(3);
                Employee e = employeeService.findById(Integer.parseInt(id));
                if (e != null) {
                    List<EmployeePermission> permissionList = employeePermissionService.findByEmployee(e);
                    if (permissionList != null && !permissionList.isEmpty()) {
                        for (EmployeePermission permission : permissionList) {
                            gas.add(new SimpleGrantedAuthority(permission.getPermission().toString().toUpperCase()));
                        }
                    }
                }
                return gas;
            }
        }
        return new MyLdapAuthoritiesPopulator(contextSource());
    }

    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(LDAP_URL);
        contextSource.setUserDn(LDAP_USERNAME);
        contextSource.setPassword(LDAP_PASSWORD);
        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contextSource;
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new SignoutSuccessHandler();
    }

    @Bean
    public AuthenticationAccessDeniedHandler authenticationAccessDeniedHandler() {
        return new AuthenticationAccessDeniedHandler();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandler();
    }

    @Bean
    public AuthenticationEntryPointHandler authenticationEntryPointHandler() {
        return new AuthenticationEntryPointHandler();
    }

    @Bean
    public AuthenticationUnauthorizedEntryPointHandler authenticationUnauthorizedEntryPointHandler() {
        return new AuthenticationUnauthorizedEntryPointHandler();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler();
    }

    @Bean
    public CorsFilter corsFilter() {
        /*
         CORS requests are managed only if headers Origin and Access-Control-Request-Method are available on OPTIONS requests
         (this filter is simply ignored in other cases).
         This filter can be used as a replacement for the @Cors annotation.
        */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("ORIGIN");
        config.addAllowedHeader("CONTENT_TYPE");
        config.addAllowedHeader("ACCEPT");
        config.addAllowedHeader("AUTHORIZATION");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("DELETE");
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
