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
        
        // Public key: Backend có thể lấy khóa này để kiểm tra chữ ký của Google.
        // Private Key chỉ Google nắm giữ, không chia sẻ cho ứng dụng của bạn. Dùng để ký GG ID Token
        // Public Key được Google công bố để backend có thể xác minh chữ ký. Dùng Public Key kiểm tra Signature trong ID Token.
        // transport cung cấp phương tiện HTTP để thư viện thực hiện việc giao tiếp cần thiết với Google.
        // Cung cấp phương tiện HTTP để thư viện lấy các Public Key của Google.
        NetHttpTransport transport =
                new NetHttpTransport.Builder().build();

        return new GoogleIdTokenVerifier.Builder(
                transport,
                GsonFactory.getDefaultInstance() //GsonFactory.getDefaultInstance() cung cấp cơ chế chuyển đổi dữ liệu JSON sang Java Object và ngược lại.
        )
                .setAudience(List.of(googleClientId)) // Cấu hình GoogleIdTokenVerifier chỉ chấp nhận ID Token có Audience khớp với Client ID của ứng dụng mình.
                .build();
    }
}