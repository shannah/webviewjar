/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;


/**
 *
 * @author shannah
 */
public class WebViewTest {
  
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
