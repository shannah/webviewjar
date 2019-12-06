/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import java.io.IOException;

/**
 *
 * @author shannah
 */
public class WebView {
    
    private Pointer peer;
    private int w=800, h=600;
    private boolean resizable=true;
    private boolean fullscreen;
    private String title="Browser";
    private String url="https://weblite.ca";
    private JavascriptCallback javascriptCallback;
    private Runnable onLoadCallback;
    private static final String MSG_PREFIX = "WEBVIEW_MSG_98989***:";

    WebView resizable(boolean b) {
        resizable = b;
        return this;
    }
    
    public static interface JavascriptCallback {
        public void run(String arg);
    }
    
    
    
    
    
    
    public WebView() {
        
    }
    
    public WebView url(String url) {
        this.url = url;
        if (peer != null) {
            CWebView.INSTANCE.CgoWebViewEval(peer, "window.location.href='"+url+"'");
        }
        return this;
    }
    
    public String url() {
        return url;
    }
    
    public WebView title(String title) {
        this.title = title;
        if (peer != null) {
            CWebView.INSTANCE.CgoWebViewSetTitle(peer, title);
        }
        return this;
    }
    
    public WebView fullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (peer != null) {
            CWebView.INSTANCE.CgoWebViewSetFullscreen(peer, fullscreen?1:0);
        }
        return this;
                
    }
    
    public boolean fullscreen() {
        return fullscreen;
    }
    
    public WebView size(int w, int h) {
        this.w = w;
        this.h = h;
        return this;
    }
    
    public int w() {
        return w;
    }
    
    public int h() {
        return h;
    }
    
    
    public WebView javascriptCallback(JavascriptCallback callback) {
        this.javascriptCallback = callback;
        return this;
    }
    
    public WebView eval(String js) {
        CWebView.INSTANCE.CgoWebViewEval(peer, js);
        return this;
    }
    
    public WebView injectCSS(String css) {
        CWebView.INSTANCE.CgoWebViewInjectCSS(peer, css);
        return this;
    }
    
    public JavascriptCallback javascriptCallback() {
        return javascriptCallback;
    }
    
    public WebView onLoad(Runnable runnable) {
        onLoadCallback = runnable;
        return this;
    }
    
    public Runnable onLoad() {
        return onLoadCallback;
    }
    
    public WebView dispatch(Runnable r) {
        CWebView.INSTANCE.CgoWebViewDispatch(peer, new Callback() {

            public void dispatch() {
                r.run();
            }
        }, null);
        return this;
    }
    
    
    public void show() {
        //System.out.println("About to show: "+w+","+h+", "+title+", "+url+", "+resizable);
        peer = CWebView.INSTANCE.CgoWebViewCreate(w, h, title, url, resizable?1:0, 1, new CWebViewCallback() {
            @Override
            public void cwebviewcallback(Pointer webview, String arg) {
                CWebView.INSTANCE.CgoWebViewDispatch(peer, new Callback() {

                    public void dispatch() {
                        
                        if (arg.startsWith(MSG_PREFIX)) {
                            String body = arg.substring(MSG_PREFIX.length());
                            if (body.startsWith("url:")) {
                                body = body.substring("url:".length());
                                url = body;
                                if (onLoadCallback != null) {
                                    onLoadCallback.run();
                                }
                            }
                            return;
                        }
                        if (javascriptCallback != null) {
                            javascriptCallback.run(arg);
                        }
                    }
                }, null);
                
            }
        }, new CWebViewOnloadCallback() {
            @Override
            public void onLoad() {
                 url = CWebView.INSTANCE.CgoWebViewURL(peer);
                 if (onLoadCallback != null) {
                     onLoadCallback.run();
                 }
                 CWebView.INSTANCE.CgoWebViewDispatch(peer, new Callback() {
                    
                    public void dispatch() {
                        //CWebView.INSTANCE.CgoWebViewEval(peer, "window.external.invoke('"+MSG_PREFIX+"url:'+window.location.href)");
                        
                        //if (onLoadCallback != null) {
                        //    onLoadCallback.run();
                        //}
                    }
                }, null);
            }
        });
        while (CWebView.INSTANCE.CgoWebViewLoop(peer, 1) == 0) {

                }
        
        
    }
    
    
}
