package com.manning.ssia.ssiamilestone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;

@Configuration
@EnableAuthorizationServer
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {


    @Value("${password}")
    private String password;

    @Value("${privateKey}")
    private String privateKey;

    @Value("${alias}")
    private String alias;


    @Resource(name="authenticationManagerBean")
    private AuthenticationManager authenticationManager;



    @Override
    public void configure(  ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()
                .withClient("client")
                .secret("secret")
                .authorizedGrantTypes("password","authorization_code","client_credentials","refresh_token")
                .scopes("read");
    }




    @Override
    public void configure(
            AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore())
                .accessTokenConverter(
                        jwtAccessTokenConverter());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(
                jwtAccessTokenConverter());
    }

//    @Bean
//    public JwtAccessTokenConverter jwtAccessTokenConverter() {
//        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setSigningKey(jwtKey);
//        return converter;
//    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(
                        new ClassPathResource(privateKey),
                        password.toCharArray()
                );

        converter.setKeyPair(
                keyStoreKeyFactory.getKeyPair(alias));

        return converter;
    }

}
