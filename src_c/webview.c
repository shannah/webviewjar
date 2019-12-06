#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#define WEBVIEW_STATIC
#define WEBVIEW_IMPLEMENTATION
#include "webview.h"


void CgoWebViewFree(void *w) {
    free((void *)((struct webview *)w)->title);
    free((void *)((struct webview *)w)->url);
    free(w);
}

void *CgoWebViewCreate(int width, int height, char *title, char *url, int resizable, int debug, 
                       webview_external_invoke_cb_t webviewExternalInvokeCallback, webview_onload_cb_t webviewOnloadCallback) {
    struct webview *w = (struct webview *) calloc(1, sizeof(*w));
    w->width = width;
    w->height = height;
    w->title = title;
    w->url = url;
    w->resizable = resizable;
    w->debug = debug;
    w->external_invoke_cb = webviewExternalInvokeCallback;
    w->onload_cb = webviewOnloadCallback;
    if (webview_init(w) != 0) {
        CgoWebViewFree(w);
        return NULL;
    }
    return (void *)w;
}

const char* CgoWebViewURL(struct webview *w) {
    return w->url;
}

int CgoWebViewLoop(void *w, int blocking) {
    return webview_loop((struct webview *)w, blocking);
}

void CgoWebViewTerminate(void *w) {
    webview_terminate((struct webview *)w);
}

void CgoWebViewExit(void *w) {
    webview_exit((struct webview *)w);
}

void CgoWebViewSetTitle(void *w, char *title) {
    webview_set_title((struct webview *)w, title);
}

void CgoWebViewSetFullscreen(void *w, int fullscreen) {
    webview_set_fullscreen((struct webview *)w, fullscreen);
}

void CgoWebViewSetColor(void *w, uint8_t r, uint8_t g, uint8_t b, uint8_t a) {
    webview_set_color((struct webview *)w, r, g, b, a);
}

void CgoDialog(void *w, int dlgtype, int flags,
                             char *title, char *arg, char *res, size_t ressz) {
    webview_dialog(w, dlgtype, flags,
                   (const char*)title, (const char*) arg, res, ressz);
}

int CgoWebViewEval(void *w, char *js) {
    return webview_eval((struct webview *)w, js);
}

void CgoWebViewInjectCSS(void *w, char *css) {
    webview_inject_css((struct webview *)w, css);
}
void CgoWebViewDispatch(struct webview *w, webview_dispatch_fn fn,
                        void *arg) {
    
    webview_dispatch(w, fn, arg);
}

