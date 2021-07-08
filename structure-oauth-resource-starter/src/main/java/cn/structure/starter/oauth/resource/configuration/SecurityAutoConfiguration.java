package cn.structure.starter.oauth.resource.configuration;

import cn.structure.common.constant.AuthConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * @author CHUCK
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import(value = {ResourceServerConfig.class})
public class SecurityAutoConfiguration {

    @Bean
    @Qualifier("tokenStore")
    @ConditionalOnMissingBean(TokenStore.class)
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    @Qualifier("tokenStore")
    @ConditionalOnMissingBean(TokenStore.class)
    @ConditionalOnProperty(value = "structure.security.jwt", havingValue = "true")
    public TokenStore tokenStoreJwt() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    @ConditionalOnMissingBean(JwtAccessTokenConverter.class)
    @ConditionalOnProperty(value = "structure.security.jwt", havingValue = "true")
    protected JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter =  new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource(AuthConstant.PUBLIC_CERT);
        String publicKey ;
        try {
            publicKey = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        //读取自定义
        converter.setAccessTokenConverter(new CustomerAccessTokenConverter());
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new AuthExceptionEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }
}
