/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import ca.weblite.webview.nanojson.JsonObject;
import ca.weblite.webview.nanojson.JsonParser;
import ca.weblite.webview.nanojson.JsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author shannah
 */
public class WebViewClient implements AutoCloseable {
    boolean closed;
    private ExecutorService dispatchService = Executors.newSingleThreadExecutor();
    private Thread dispatchThread;
    private InputStream input;
    private OutputStream output;
    private Thread readerThread;
    private final Map<String,List<WebEventListener>> listeners = new HashMap<>();
    private String url;
    private boolean ready;
    
    
    public abstract static class Builder {
        protected String title="Web View";
        protected String url="https://www.codenameone.com";
        protected int w=800;
        protected int h=600;
        protected boolean resizable;
        protected StringBuilder onBeforeLoad = new StringBuilder();
        public abstract WebViewClient build();
        
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        
        public Builder size(int w, int h) {
            this.w = w;
            this.h = h;
            return this;
        }
        
        public StringBuilder onBeforeLoad() {
            return onBeforeLoad;
        }
        
        public Builder resizable(boolean r) {
            resizable = r;
            return this;
        }
    }
    
    public class WebEvent {
        
        
    }
    
    public class OnLoadWebEvent extends WebEvent {
        private String url;
        public OnLoadWebEvent(String url) {
            this.url = url;
        }
    }
    
    public class MessageEvent extends WebEvent {
        private String content;
        public MessageEvent(String content) {
            this.content = content;
        }
        
        public String getMessage() {
            return content;
        }
    }
    
    public static interface WebEventListener<T extends WebEvent> {
        public void handleEvent(T evt);
    }
    
    public WebViewClient addEventListener(String type, WebEventListener l) {
        if (!isDispatchThread()) {
            dispatch(()->addEventListener(type, l));
            return this;
        }
        List<WebEventListener> typeListeners = listeners.get(type);
        if (typeListeners == null) {
            typeListeners = new ArrayList<WebEventListener>();
            listeners.put(type, typeListeners);
        }
        typeListeners.add(l);
        return this;
    }
    
    public WebViewClient removeEventListener(String type, WebEventListener l) {
        if (!isDispatchThread()) {
            dispatch(()->removeEventListener(type, l));
            return this;
        }
        List<WebEventListener> typeListeners = listeners.get(type);
        if (typeListeners != null) {
            typeListeners.remove(l);
        }
        return this;
    }
    
    public WebViewClient addLoadListener(WebEventListener<OnLoadWebEvent> l) {
        return addEventListener("load", l);
    } 
    
    public WebViewClient removeLoadListener(WebEventListener<OnLoadWebEvent> l) {
        return removeEventListener("load", l);
    }
    
    public WebViewClient addMessageListener(WebEventListener<MessageEvent> l) {
        return addEventListener("message", l);
    }
    
    public WebViewClient removeMessageListener(WebEventListener<MessageEvent> l) {
        return removeEventListener("message", l);
    }
    
    public ReadyRequest ready() {
        ReadyRequest out = new ReadyRequest();
        if (ready) {
            out.complete(this);
        } else {
            WebEventListener<OnLoadWebEvent> listener = new WebEventListener<OnLoadWebEvent>() {
                @Override
                public void handleEvent(OnLoadWebEvent evt) {
                    removeLoadListener(this);
                    if (!out.isDone()) {
                        out.complete(WebViewClient.this);
                    }
                }
            };
            addLoadListener(listener);
        }
        return out;
        
    }
    
    public class ReadyRequest extends CompletableFuture<WebViewClient> {
        
    }
    
    
    public class EvalRequest extends CompletableFuture<String> {
        
    }

    public EvalRequest eval(String js) {
        return eval(js, new EvalRequest());
    }
    
    private EvalRequest eval(final String _js, final EvalRequest out)  {
        if (!isDispatchThread()) {
            dispatch(()->{
                if (out.isDone()) {
                    return;
                }
                eval(_js, out);
            });
            return out;
        }
        String js = _js;
        js += "\npostMessageExt('hello there');\n";
        if (!js.contains("complete(")) {
             js += "\n" + "complete('saasfldfksjd');";
        }
        js = "try { "+js+"} catch(e) { postMessageExt({error:''+e, errorType:'javascriptError', content: e});}";
        String requestId = UUID.randomUUID().toString();

        String completeFn = "function complete(val) {val = {evalId: '"+requestId+"', message: val}; postMessageExt(val)};";
        js = "(function(){ "+completeFn+" " + js + " })()";
        WebEventListener<MessageEvent> listener = new WebEventListener<MessageEvent>() {
            @Override
            public void handleEvent(MessageEvent evt) {
                //System.out.println("in Handle Event "+evt.getMessage());
                if (out.isDone()) {
                    removeMessageListener(this);
                    return;
                }
                String msg = evt.content;
                if (msg.contains(requestId)) {
                    removeMessageListener(this);
                    try {
                        JsonObject msgObj = JsonParser.array().from(msg).getObject(0);
                        Object messageValue = msgObj.get("message");
                        out.complete(JsonWriter.string(messageValue));
                        
                    } catch (Exception ex) {
                        String errorObj = JsonWriter.string()
                                .object()
                                    .value("error", ex.getMessage())
                                    .value("errorType", "parseError")
                                    .value("content", msg)
                                .end()
                                .done();
                        out.complete(errorObj);
                    }
                }
                

            }
        };
        addMessageListener(listener);
        js = "<<<"+requestId+"\n"+js+"\n"+requestId+"\n"; 
        try {
            //System.out.println("Writing "+js);
            output.write(js.getBytes("UTF-8"));
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return out;
    }
    
    public void dispatch(Runnable r) {
        
        dispatchService.execute(r);
    }
    
    private void fireOnLoad(String url) {
        ready = true;
        if (!isDispatchThread()) {
            dispatchService.execute(()->fireOnLoad(url));
            return;
        }
        //System.out.println("Firing onLoad "+url);
        OnLoadWebEvent evt = new OnLoadWebEvent(url);
        List<WebEventListener> typeListeners = listeners.get("load");
        if (typeListeners != null) {
            List<WebEventListener> tmp = new ArrayList<WebEventListener>(typeListeners);
            for (WebEventListener l : tmp) {
                l.handleEvent(evt);
            }
        }
        
    }
    
    private void fireMessage(String message) {
        if (!isDispatchThread()) {
            dispatch(()->fireMessage(message));
            return;
        }
        //System.out.println("Firing message "+message);
        MessageEvent evt = new MessageEvent(message);
        List<WebEventListener> typeListeners = listeners.get("message");
        if (typeListeners != null) {
            List<WebEventListener> tmp = new ArrayList<>(typeListeners);
            for (WebEventListener l : tmp) {
                l.handleEvent(evt);
            }
        }
    }
    
    public boolean isDispatchThread() {
        return Thread.currentThread() == dispatchThread;
    }
    
    protected WebViewClient() {
        
    }
    
    protected WebViewClient init(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        dispatchService.execute(()->{
            dispatchThread = Thread.currentThread();
        });
        readerThread = new Thread(()->{
            try {
                Scanner scanner = new Scanner(input, "UTF-8");

                String loadPrefix = "[\"<<<EVENT:load ";
                int state = 0;
                String boundaryPrefix = "<<<";
                StringBuilder messageBuffer = new StringBuilder();
                String messageBoundary = "";
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String trimmed = line.trim();
                    if (state == 0) {
                        if (line.startsWith(loadPrefix)) {
                            String params[] = line.substring(loadPrefix.length()).trim().split(" ");
                            try {
                                String result = java.net.URLDecoder.decode(params[0], StandardCharsets.UTF_8.name());
                                fireOnLoad(result);
                            } catch (UnsupportedEncodingException e) {
                                // not going to happen - value came from JDK's own StandardCharsets
                            }
                        } else if (line.startsWith(boundaryPrefix)) {
                            messageBuffer.setLength(0);
                            messageBoundary = line.substring(boundaryPrefix.length()).trim();
                            state = 1;
                        } else {
                            fireMessage(line);
                        }
                    } else if (state == 1) {
                        
                        if (trimmed.equals(messageBoundary)) {
                            fireMessage(messageBuffer.toString());
                            state = 0;
                        } else {
                            messageBuffer.append(line);
                        }
                    }

                }
            } catch (Throwable t) {
                if (!closed) {
                    t.printStackTrace();
                }
            }
        });
        readerThread.start();
        return this;
        
    }
    
    
    
    
    @Override
    public void close() throws Exception {
        closed = true;
        try {
            input.close();
        } catch (Throwable t){}
        try {
            output.close();
        } catch (Throwable t) {}
        dispatchService.shutdown();
    }

}
