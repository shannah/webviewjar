/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import ca.weblite.webview.WebView.JavascriptCallback;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author shannah
 */
public class WebViewServer  implements AutoCloseable {
    private Scanner inputScanner;
    private InputStream input;
    private OutputStream output;
    private String messageBoundary = "Boundary"+System.currentTimeMillis();
    private WebView webview;
    private ExecutorService outputService = Executors.newSingleThreadExecutor();
    private Thread inputThread;
    private boolean closed;
    private boolean useMessageBoundaries;
    
    public WebViewServer(WebView webview, InputStream input, OutputStream output) {
        this.webview = webview;
        this.input = input;
        this.output = output;
        initWebView();
        inputThread = new Thread(()->{
            try {
                listen();
                
            } catch (IOException ex) {
                if (!closed) {
                    ex.printStackTrace(System.err);
                }
            }
            System.out.println("Finished lisening");
        });
        inputThread.start();
        
    }
    
    
    private void initWebView() {
        //JavascriptCallback existingJSCallback = webview.javascriptCallback();
        webview.addJavascriptCallback("postMessageExt", arg->{
            sendMessage(arg, useMessageBoundaries);
        });
        webview.addJavascriptCallback("postMessageExtWithBoundary", arg->{
            sendMessage(arg, true);
        });
        webview.addJavascriptCallback("postMessageExtWithoutBoundary", arg->{
            sendMessage(arg, false);
        });
        webview.addOnBeforeLoad("document.addEventListener('DOMContentLoaded', function(){postMessageExtWithoutBoundary('<<<EVENT:load '+encodeURIComponent(window.location.href)+' >>>');});");
        
    }
    
    
    public WebViewServer useMessageBoundaries(boolean use) {
        useMessageBoundaries = use;
        return this;
    }
    
    
    
    
    private void listen() throws IOException {
        Scanner scanner = new Scanner(input, "UTF-8");
        inputScanner = scanner;
        int state = 0;
        String currentMessageBoundary="";
        StringBuilder currMessage = new StringBuilder();
        output.write("\r\n".getBytes());
        output.flush();
        while (true) {
            if (closed) {
                break;
            }
            if (input.available() <= 0) {
                try {
                    Thread.sleep(30l);
                    continue;
                } catch (Exception ex){
                    
                }
            }
            while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            //System.out.println("Read line "+line);
            switch (state) {
                case 0 : {
                    if (line.startsWith("<<<")) {
                        //System.out.println("Setting mesage boundary");
                        currentMessageBoundary = line.substring(3);
                        state = 1;
                    } else {
                        currentMessageBoundary = "";
                        if (!line.trim().isEmpty()) {
                            executeCommand(line);
                        }
                    }
                    
                    break;
                }
                
                case 1 : {
                    //System.out.println("currentMessageBoundary="+currentMessageBoundary+", line="+line);
                    if (line.equals(currentMessageBoundary)) {
                        if (!currMessage.toString().trim().isEmpty()) {
                            //System.out.println("Sending command "+currMessage);
                            executeCommand(currMessage.toString());
                        }
                        
                        currMessage.setLength(0);
                        state = 0;
                        
                    } else {
                        currMessage.append(line).append("\n");
                    }
                }
            }
        }
        }
        
        scanner.close();
    }
    
    private void sendMessage(String message, boolean addBoundaries) {
        if (addBoundaries) {
            message = "<<<"+messageBoundary+"\n"+message+"\n"+messageBoundary+"\n";
        } else {
            message += "\n";
        }

        String fMessage = message;
        outputService.execute(()->{
            try {
                
                output.write(fMessage.getBytes("UTF-8"));
                output.flush();
                
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        });
                
    }
  
    
    
    private void executeCommand(String command) {
        webview.dispatch(()->{
            webview.eval(command);
        });
    }

    @Override
    public void close() throws Exception {
        closed = true;
        if (output != null) {
            try {
                output.close();
            } catch (Throwable t){}
        
        }
        
        if (input != null) {
            try {
                input.close();
            } catch (Throwable t) {}
        }
        
        outputService.shutdown();
    }
    
   
    
}
