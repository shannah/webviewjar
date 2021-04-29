package ca.weblite.webview.demo;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ca.weblite.webview.WebViewCLIClient;
import ca.weblite.webview.WebViewClient.EvalRequest;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * This demo shows how to create and control a WebView from inside a Swing App
 * using the WebViewCLIClient class.
 * @author shannah
 */
public class WebViewSwingDemo implements Runnable {

    private class ViewImpl {
        
        JFrame frame = new JFrame("Swing WebView Test");
        JButton openWebView = new JButton("Open WebView");
        JButton closeWebView = new JButton("Close WebView");
        JTextPane jsInputField = new JTextPane();
        {
            jsInputField.setPreferredSize(new Dimension(800, 300));
            
        }
        JTextPane jsOutputField = new JTextPane();
        {
            jsOutputField.setPreferredSize(new Dimension(800, 300));
        }
        JButton executeButton = new JButton("Execute");
        WebViewCLIClient client;
        ViewImpl() 
        {
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.getContentPane().add(openWebView);
            frame.getContentPane().add(closeWebView);
            frame.getContentPane().add(new JLabel("Javascript Input:"));
            frame.getContentPane().add(new JScrollPane(jsInputField));
            frame.getContentPane().add(new JLabel("Javascript Output:"));
            jsOutputField.setEditable(false);
            frame.getContentPane().add(new JScrollPane(jsOutputField));
            frame.getContentPane().add(executeButton);

            openWebView.addActionListener(e->{
                if (client != null) {
                    try {

                        client.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                client = (WebViewCLIClient)new WebViewCLIClient.Builder()
                .url("https://www.codenameone.com")
                .title("Codename One")
                .size(800, 600)
                .build();
                client.addLoadListener(evt->{
                    System.out.println("Loaded "+evt.getURL());
                });
                client.addMessageListener(evt->{
                    System.out.println(evt.getMessage());
                });

            });
            closeWebView.addActionListener(e->{
                if (client != null) {
                    try {
                        client.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    client = null;
                }
            });
            executeButton.addActionListener(e->{
                if (client != null) {
                    EvalRequest req = client.eval(jsInputField.getText());
                    req.thenAccept(str->{
                        EventQueue.invokeLater(()->{
                            jsOutputField.setText(str);
                        });
                    });
                }
            });

        }
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new WebViewSwingDemo());
    }
    
    @Override
    public void run() {
        ViewImpl view = new ViewImpl();
        
        view.frame.pack();
        view.frame.setVisible(true);
    }
    
}
