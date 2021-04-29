import ca.weblite.webview.WebView;
import ca.weblite.webview.WebViewCLIClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author shannah
 */
public class WebViewCLIClientTest {
    WebViewCLIClient client;

    /**
     * You can use this to run as a test application in your IDE
     */
    public static void main(String[] args) {

        WebView webview = new WebView()
                .size(800, 600)
                .title("Test")
                .resizable(true)
                .url("https://github.com/shannah/webviewjar")
                .addJavascriptCallback("callback", x ->
                {
                    System.out.println(x);
                });

        webview.show();
    }

    @BeforeEach
    public void setUp() {
        client = (WebViewCLIClient) new WebViewCLIClient.Builder()
                .url("https://github.com/shannah/webviewjar")
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    @Timeout(6000)
    public void testEval() throws InterruptedException, ExecutionException, TimeoutException {
        client.ready().get(5000, TimeUnit.MILLISECONDS);
        String title = client.eval("complete(document.title)").get(5000, TimeUnit.MILLISECONDS);
        assertThat(title).contains("webviewjar");
    }

}
