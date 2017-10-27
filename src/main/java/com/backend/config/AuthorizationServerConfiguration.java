package com.backend.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Value("${resource.id:spring-boot-application}")
    private String                resourceId;

    @Value("${access_token.validity_period:3600}")
    int                           accessTokenValiditySeconds = 3600;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource            dataSource;

    @Bean
    public JdbcTokenStore tokenStore() {

        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        return new JwtAccessTokenConverter();
    }

    /**
     * @Ref : http://projects.spring.io/spring-security-oauth/docs/oauth2.html AuthorizationServerEndpointsConfigurer: defines the
     *      authorization and token endpoints and the token services.
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager).tokenStore(tokenStore());
        // .accessTokenConverter(accessTokenConverter());

    }

    /**
     * AuthorizationServerSecurityConfigurer: defines the security constraints on the token endpoint.
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
                .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
    }

    /**
     * ClientDetailsServiceConfigurer: a configurer that defines the client details service. Client details can be initialized, or
     * you can just refer to an existing store.
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // Database
        // clients.with
        clients.jdbc(dataSource);
    }

    public ClientDetailsService clientDetailsService() {
        return new ClientDetailsService() {
            @Override
            public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
                BaseClientDetails details = new BaseClientDetails();
                details.setClientId(clientId);
                details.setAuthorizedGrantTypes(Arrays.asList("authorization_code"));
                details.setScope(Arrays.asList("read, trust"));
                details.setRegisteredRedirectUri(Collections.singleton("http://anywhere.com"));
                details.setResourceIds(Arrays.asList("oauth2-resource"));
                Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
                authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
                details.setAuthorities(authorities);
                return details;
            }
        };
    }

}