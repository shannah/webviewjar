/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author shannah
 */
public class WebViewCLIClientTest {
    WebViewCLIClient client;
    public WebViewCLIClientTest() {
    }
    
    @Before
    public void setUp() {
        client = (WebViewCLIClient)new WebViewCLIClient.Builder()
                .url("http://solutions.weblite.ca")
                .build();
        
        
        
    }
    
    
    
    

    public void tearDown() {
        try {
            client.close();
        } catch (Exception ex){}
    }

    
    
    @Test
    public void testEval() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("eval");
        //Thread.sleep(2000l);
        client.ready().get(5000, TimeUnit.MILLISECONDS);
        String title = client.eval("complete(document.title)").get(5000, TimeUnit.MILLISECONDS);
        assertEquals("\"Web Lite Solutions Corp. | Vancouver, British Columbia, Canada\"", title);
    }
    
    public static void main(String[] args) throws Exception {
         WebViewCLIClient client = (WebViewCLIClient)new WebViewCLIClient.Builder()
                .url("http://solutions.weblite.ca")
                .build();
        Thread inputThread = new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                client.eval(scanner.nextLine()).thenRun(()->{
                    System.out.println("[DONE]");
                });
            }
        });
        
        client.addMessageListener(evt->{
            System.out.println("Received message: "+evt.getMessage());
        });
        inputThread.start();
        
    }
    
}
