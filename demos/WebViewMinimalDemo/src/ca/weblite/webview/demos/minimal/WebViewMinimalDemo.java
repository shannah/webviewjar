/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview.demos.minimal;

import ca.weblite.webview.WebView;
import ca.weblite.webview.WebViewCLI;

/**
 *
 * @author shannah
 */
public class WebViewMinimalDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (WebViewCLI.restartJVM(args)) {
            // Mac requires the -XrunOnFirstThread option to be set.
            // This restarts the JVM with that option if it is absent.
            return;
        }
        WebView wv = new WebView();
        wv.show();
    }
    
}
