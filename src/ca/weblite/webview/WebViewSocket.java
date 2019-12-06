/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author shannah
 */
public class WebViewSocket implements AutoCloseable {
    private Socket sock;
    private WebViewController controller;
    
    public WebViewSocket(Socket sock, WebView webview) throws IOException {
        this.sock = sock;
        this.controller = new WebViewController(webview, sock.getInputStream(), sock.getOutputStream());
               
    }

    @Override
    public void close() throws Exception {
        try {
            controller.close();
        } catch (Exception ex){}
        try {
            sock.close();
        } catch (Exception ex){}
    }
}
