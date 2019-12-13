/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

//import com.sun.jna.Callback;
//import com.sun.jna.Pointer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class WebView {
    private long peer;
    private int w=800, h=600;
    private boolean resizable=true;
    private boolean fullscreen;
    private String title="Browser";
    private String url="https://weblite.ca";
    private List<String> onBeforeLoad = new ArrayList<String>();
    private Map<String,JavascriptCallback> bindings = new HashMap<String,JavascriptCallback>();

    WebView resizable(boolean b) {
        resizable = b;
        return this;
    }
    
    public static interface JavascriptCallback {
        public void run(String arg);
    }
    
    
    public WebView addOnBeforeLoad(String js) {
        if (peer != 0) {
            WebViewNative.webview_init(peer, js);
        } else {
            onBeforeLoad.add(js);
        }
        return this;
    }
    
    
    
    public WebView() {
        
    }
    
    public WebView url(String url) {
        this.url = url;
        if (peer != 0) {
            WebViewNative.webview_navigate(peer, url);
        }
        return this;
    }
    
    public String url() {
        return url;
    }
    
    public WebView title(String title) {
        this.title = title;
        if (peer != 0) {
            WebViewNative.webview_set_title(peer, title);
        }
        return this;
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
    
    private ArrayList heap = new ArrayList();
    
    public WebView addJavascriptCallback(String name, JavascriptCallback callback) {
        if (peer == 0) {
            bindings.put(name, callback);
        } else {
            bindings.put(name, callback);
            WebViewNativeCallback fn = new WebViewNativeCallback() {
                @Override
                public void invoke(String arg2, long wv) {
                    JavascriptCallback cb = bindings.get(name);
                    if (cb != null) {
                        cb.run(arg2);
                    }
                }
            };
            heap.add(fn);
            WebViewNative.webview_bind(peer, name, fn, peer);
        }
        return this;
    }
    
    public WebView eval(String js) {
        WebViewNative.webview_eval(peer, js);
        return this;
    }
    
    public WebView injectCSS(String css) {
        return this;
    }

    
   
    
    public WebView dispatch(Runnable r) {
        heap.add(r);
        WebViewNative.webview_dispatch(peer, () -> {
            r.run();
            heap.remove(r);
        }, 0);
        return this;
    }
    
    
    
    public void show() {
        peer = WebViewNative.webview_create(0, 0);
        WebViewNative.webview_set_bounds(peer, 0, 0, w, h, 0);
        for (String js : onBeforeLoad) {
            WebViewNative.webview_init(peer, js);
        }
        WebViewNative.webview_set_title(peer, title);
        for (final String key : bindings.keySet()) {
            WebViewNativeCallback fn = (String arg2, long wv) -> {
                JavascriptCallback cb = bindings.get(key);
                if (cb != null) {
                    cb.run(arg2);
                }
            };
            heap.add(fn);
            WebViewNative.webview_bind(peer, key, fn, peer);
        }
        WebViewNative.webview_navigate(peer, url);
        WebViewNative.webview_run(peer);
    }

}
