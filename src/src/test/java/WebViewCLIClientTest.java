/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ca.weblite.webview.WebView;
import ca.weblite.webview.WebViewCLIClient;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;


/**
 * @author shannah
 */
public class WebViewCLIClientTest {
    WebViewCLIClient client;

    public WebViewCLIClientTest() {
    }

    public static void main(String[] args) {

        WebView webview = new WebView()
                .size(800, 600)
                .title("Test")
                .resizable(true)
                .url("https://theoryofgeek.com/")
                .addJavascriptCallback("callback", x ->
                {
                    System.out.println(x);
                });

        webview.show();
    }

    @Before
    public void setUp() {
        client = (WebViewCLIClient) new WebViewCLIClient.Builder()
                .url("http://solutions.weblite.ca")
                .build();


    }

    public void tearDown() {
        try {
            client.close();
        } catch (Exception ex) {
        }
    }

    @Test
    public void testEval() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("eval");
        //Thread.sleep(2000l);
        client.ready().get(5000, TimeUnit.MILLISECONDS);
        String title = client.eval("complete(document.title)").get(5000, TimeUnit.MILLISECONDS);
        assertEquals("\"Web Lite Solutions Corp. | Vancouver, British Columbia, Canada\"", title);
    }

}
