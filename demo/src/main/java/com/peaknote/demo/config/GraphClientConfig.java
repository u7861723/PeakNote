package com.peaknote.demo.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import lombok.RequiredArgsConstructor;
import okhttp3.Request;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class GraphClientConfig {

    private final AzureProperties azureProperties;
    private final WebhookProperties webhookProperties;

    @Bean
    @Qualifier("webhookGraphClient")
    public GraphServiceClient<Request>  webhookGraphClient() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(webhookProperties.getClientId())
                .clientSecret(webhookProperties.getClientSecret())
                .tenantId(webhookProperties.getTenantId())
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
    public GraphServiceClient<Request>graphClient() {
    ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(azureProperties.getClientId())
            .clientSecret(azureProperties.getClientSecret())
            .tenantId(azureProperties.getTenantId())
            .build();

    TokenCredentialAuthProvider authProvider =
            new TokenCredentialAuthProvider(List.of("https://graph.microsoft.com/.default"), credential);

    return GraphServiceClient
            .builder()
            .authenticationProvider(authProvider)
            .buildClient();
        }

    @Bean
    public String getAccessToken() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(azureProperties.getClientId())
                .clientSecret(azureProperties.getClientSecret())
                .tenantId(azureProperties.getTenantId())
                .build();

        TokenRequestContext requestContext = new TokenRequestContext()
                .addScopes("https://graph.microsoft.com/.default");

        // 注意这里要 block() 等待
        AccessToken accessToken = credential.getToken(requestContext).block();
        return accessToken.getToken();
    }
}
