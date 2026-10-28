package Project.Config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;

@Configuration
public class GoogleIdentityConfig {

    @Value("${google.client-id}")
    private String googleClientId;

    @Bean
    GoogleIdTokenVerifier googleIdTokenVerifier()
            throws GeneralSecurityException, IOException {

        NetHttpTransport transport =
                new NetHttpTransport.Builder().build();

        return new GoogleIdTokenVerifier.Builder(
                transport,
                GsonFactory.getDefaultInstance()
        )
                .setAudience(List.of(googleClientId))
                .build();
    }
}