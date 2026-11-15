package Project.Config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    /*
        khi có một thành phần trong spring boot thì nó muốn Gemini của gg trả lời 
        request thì nó gọi đến client xong client sử dụng api key để cho 
        Gemini của gg biết ứng dụng nào cần trả lời.
     */
    @Bean
    public Client geminiClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
