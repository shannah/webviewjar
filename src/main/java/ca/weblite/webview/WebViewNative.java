/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import ca.weblite.webview.nativelib.NativeLoader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shannah
 */
public class WebViewNative {
    
    static {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                NativeLoader.loadLibrary("WebView2Loader");
            }
            NativeLoader.loadLibrary("webview");
        } catch (IOException ex) {
            Logger.getLogger(WebViewNative.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     // Creates a new webview instance. If debug is non-zero - developer tools will
// be enabled (if the platform supports them). Window parameter can be a
// pointer to the native window handle. If it's non-null - then child WebView
// is embedded into the given parent window. Otherwise a new window is created.
// Depending on the platform, a GtkWindow, NSWindow or HWND pointer can be
// passed here.
native static long webview_create(int debug, long window);

// Destroys a webview and closes the native window.
native static void webview_destroy(long w);

// Runs the main loop until it's terminated. After this function exits - you
// must destroy the webview.
native static void webview_run(long w);

// Stops the main loop. It is safe to call this function from another other
// background thread.
native static void webview_terminate(long w);

// Posts a function to be executed on the main thread. You normally do not need
// to call this function, unless you want to tweak the native window.
native static void
webview_dispatch(long w, Runnable callback, long arg);

// Returns a native window handle pointer. When using GTK backend the pointer
// is GtkWindow pointer, when using Cocoa backend the pointer is NSWindow
// pointer, when using Win32 backend the pointer is HWND pointer.
native static long webview_get_window(long w);

// Updates the title of the native window. Must be called from the UI thread.
native static void webview_set_title(long w, String title);

// Updates native window position and size.
// TODO: implement x/y and describe possible flags.
native static void webview_set_bounds(long w, int x, int y, int width,
                                    int height, int flags);



// Navigates webview to the given URL. URL may be a data URI, i.e.
// "data:text/text,<html>...</html>". It is often ok not to url-encode it
// properly, webview will re-encode it for you.
native static void webview_navigate(long w, String url);

// Injects JavaScript code at the initialization of the new page. Every time
// the webview will open a the new page - this initialization code will be
// executed. It is guaranteed that code is executed before window.onload.
native static void webview_init(long w, String js);

// Evaluates arbitrary JavaScript code. Evaluation happens asynchronously, also
// the result of the expression is ignored. Use RPC bindings if you want to
// receive notifications about the results of the evaluation.
native static void webview_eval(long w, String js);

// Binds a native C callback so that it will appear under the given name as a
// global JavaScript function. Internally it uses webview_init(). Callback
// receives a request string and a user-provided argument pointer. Request
// string is a JSON array of all the arguments passed to the JavaScript
// function.
native static void webview_bind(long w, String name,
                              WebViewNativeCallback fn, long arg);


    
}
