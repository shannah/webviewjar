/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shannah
 */
public class WebviewSocketServer implements AutoCloseable {
    private int port = -1;
    private ServerSocket serverSock;
    private Thread serverThread;
    private ArrayList<WebViewSocket> sockets = new ArrayList<WebViewSocket>();
    
    public WebviewSocketServer(int port, WebView webview) {
        serverThread = new Thread(()->{
            try {
                serverSock = new ServerSocket(port);
                WebviewSocketServer.this.port = serverSock.getLocalPort();
                Socket sock;
                while ((sock = serverSock.accept()) != null) {
                    WebViewSocket webSock = new WebViewSocket(sock, webview);
                    sockets.add(webSock);
                }
            } catch (IOException ex) {
                Logger.getLogger(WebviewSocketServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        serverThread.start();
    }

    public int getPort() {
        while (port <= 0) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return port;
    }
    
    @Override
    public void close() throws Exception {
        while (!sockets.isEmpty()) {
            try {
                sockets.remove(0).close();
            } catch (Exception ex){}
        }
    }
    
}
