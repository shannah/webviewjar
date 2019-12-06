/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author shannah
 */
public class WebViewTest {
  
    
    private static Pointer w;
    
    public static void main(String[] args) throws Exception  {
        
        WebView wv = new WebView();
        wv.show();
        
    }
    
    public static void main3(String[] args) throws Exception  {
        Thread t = new Thread(()->{
            WebView wv = new WebView();
            wv.dispatch(()->{
                wv.show();
            });
            
        });
        t.start();
        t.join();
        
        
    }
    public static void main2(String[] args) {
        System.out.println("Creating webview");
        w = CWebView.INSTANCE.CgoWebViewCreate(500, 500, "Hello", "https://weblite.ca", 1, 1, new CWebViewCallback() {
            @Override
            public void cwebviewcallback(Pointer webview, String arg) {
                System.out.println("Hello world "+arg);
                
            }
        }, new CWebViewOnloadCallback() {
            @Override
            public void onLoad() {
                
                CWebView.INSTANCE.CgoWebViewDispatch(w, new Callback() {

                    public void dispatch() {
                        CWebView.INSTANCE.CgoWebViewEval(w, "window.external.invoke(window.location.href)");
                    }
                }, null);
            }
        });
        ExecutorService edt = Executors.newFixedThreadPool(1);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                CWebView.INSTANCE.CgoWebViewDispatch(w, new Callback() {

                    public void dispatch() {
                        CWebView.INSTANCE.CgoWebViewEval(w, "window.external.invoke('foobar2')");
                    }
                }, null);
            }
            
        }, 2000);
        edt.submit(()->{
            System.out.println("Submitting");
            CWebView.INSTANCE.CgoWebViewDispatch(w, new Callback() {

                public void dispatch() {
                    CWebView.INSTANCE.CgoWebViewEval(w, "window.addEventListener('load', function(){window.external.invoke('foobar')})");
                }
            }, null);
        });
        
        while (CWebView.INSTANCE.CgoWebViewLoop(w, 1) == 0) {
            /*
            CWebView.INSTANCE.CgoWebViewDispatch(w, new Callback() {

                public void dispatch(Pointer w, Pointer arg) {
                    System.out.println("On dispatch");
                }
            }, null);
            */
        }
        CWebView.INSTANCE.CgoWebViewExit(w);
        System.out.println("Webview created");
    }
    
    
    
    /*
    static inline void *CgoWebViewCreate(int width, int height, char *title, char *url, int resizable, int debug) {
	struct webview *w = (struct webview *) calloc(1, sizeof(*w));
	w->width = width;
	w->height = height;
	w->title = title;
	w->url = url;
	w->resizable = resizable;
	w->debug = debug;
	w->external_invoke_cb = (webview_external_invoke_cb_t) _webviewExternalInvokeCallback;
	if (webview_init(w) != 0) {
		CgoWebViewFree(w);
		return NULL;
	}
	return (void *)w;
    }
    */
    
    
   
}
