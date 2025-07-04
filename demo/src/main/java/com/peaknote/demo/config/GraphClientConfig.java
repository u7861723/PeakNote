package com.peaknote.demo.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Configuration
public class GraphClientConfig {

    @Value("${webhook.tenant-id}")
    private String tenantId;

    @Value("${webhook.client-id}")
    private String clientId;

    @Value("${webhook.client-secret}")
    private String clientSecret;


    public String token;

    @Bean
    @Qualifier("webhookGraphClient")
    public GraphServiceClient<Request>  webhookGraphClient() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(List.of("https://graph.microsoft.com/.default"), credential);

        return GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    @Bean
    @Qualifier("graphClient")
    public GraphServiceClient<Request>graphClient(
        @Value("${azure.client-id}") String subClientId,
        @Value("${azure.client-secret}") String subClientSecret,
        @Value("${azure.tenant-id}") String subTenantId
) {
    ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(subClientId)
            .clientSecret(subClientSecret)
            .tenantId(subTenantId)
            .build();

    TokenCredentialAuthProvider authProvider =
            new TokenCredentialAuthProvider(List.of("https://graph.microsoft.com/.default"), credential);

    return GraphServiceClient
            .builder()
            .authenticationProvider(authProvider)
            .buildClient();
}

    @Bean
    public ThreadPoolTaskExecutor transcriptTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("transcript-pool-");
        executor.initialize();
        return executor;
    }

    @Bean
    @Qualifier("getAccessToken")
    public static String getAccessToken(@Value("${azure.client-id}") String ClientId,
        @Value("${azure.client-secret}") String ClientSecret,
        @Value("${azure.tenant-id}") String TenantId) {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(ClientId)
                .clientSecret(ClientSecret)
                .tenantId(TenantId)
                .build();

        TokenRequestContext requestContext = new TokenRequestContext()
                .addScopes("https://graph.microsoft.com/.default");

        // 注意这里要 block() 等待
        AccessToken accessToken = credential.getToken(requestContext).block();
        return accessToken.getToken();
    }
}
