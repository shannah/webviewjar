/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 *
 * @author shannah
 */
public interface CWebView extends Library{
    
    public static CWebView INSTANCE = (CWebView)Native.loadLibrary("webview", CWebView.class);
    
    
    //public int webview(String title, String url, int width,
    //                    int height, int resizable, CWebViewCallback callback);
    
    Pointer CgoWebViewCreate(int width, int height, String title, String url, int resizable, int debug, CWebViewCallback webviewExternalInvokeCallback, CWebViewOnloadCallback onLoadCallback);
    int CgoWebViewLoop(Pointer w, int blocking);
    void CgoWebViewTerminate(Pointer w);
    void CgoWebViewExit(Pointer w);
    void CgoWebViewSetTitle(Pointer w, String title);
    void CgoWebViewSetFullscreen(Pointer w, int fullscreen);
    void CgoWebViewSetColor(Pointer w, byte r, byte g, byte b, byte a);
    void CgoDialog(Pointer w, int dlgtype, int flags,
                             String title, String arg, String res, int ressz);
    int CgoWebViewEval(Pointer w, String js);
    void CgoWebViewInjectCSS(Pointer w, String css);
    void CgoWebViewDispatch(Pointer w, Callback cb, Pointer arg);
    String CgoWebViewURL(Pointer w);
    
}
