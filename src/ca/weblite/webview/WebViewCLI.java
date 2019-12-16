/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * A command-line wrapper for the {@link WebView}.
 * @author shannah
 */
public class WebViewCLI implements AutoCloseable {
    private int width=800;
    private int height=600;
    private boolean fullscreen = false;
    private boolean resizable = true;
    private WebView webview;
    private String redirectURI;
    private String clientId;
    private String clientSecret;
    private String responseType;
    private String url;
    private String scope;
    private String title="Login";
    private int port = -1;
    private Hashtable<String,String> additionalParams = new Hashtable<String,String>();
    private boolean oauth;
    private File oauthOutputFile;
    private String onLoad;
    private boolean useMessageBoundaries;
    private WebViewServer server;
    private void init() throws IOException {
        String u = oauth ? getFullUrl() : url;
        webview = new WebView()
                .size(width, height)
                .title(title)
                .resizable(resizable)
                .url(u)
                ;
        if (onLoad != null) {
            webview.addOnBeforeLoad(onLoad);
        }
        if (port >= 0) {
            WebviewSocketServer serve = new WebviewSocketServer(port, webview)
                    .useMessageBoundaries(useMessageBoundaries);
            new Thread(()->{
                System.out.println("Listening on "+serve.getPort());
            }).start();
            
        } else {
            server = new WebViewServer(webview, System.in, System.out)
                    .useMessageBoundaries(useMessageBoundaries);
            
        }
        webview.show();

                
    }
    
    private String getFullUrl() {
        String URL = url + "?client_id=" + clientId
                + "&redirect_uri=" + encodeUrl(redirectURI);
        if (scope != null) {
            URL += "&scope=" + encodeUrl(scope);
        }
        
        if (responseType == null) {
            if (clientSecret != null) {
                URL += "&response_type=code";
            } else {
                URL += "&response_type=token";
            }
        } else {
            URL += "&response_type="+encodeUrl(responseType);
        }
        
        if (additionalParams != null) {
            Enumeration e = additionalParams.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String val = additionalParams.get(key).toString();
                URL += "&" + encodeUrl(key) + "=" + encodeUrl(val);
            }
        }
        
        return URL;
    }
    
    
    private static WebViewCLI parseParams(String[] args) {
        String[] flags = extractFlags(args);
        //System.out.println("flags="+Arrays.toString(flags));
        args = extractArgs(args);
        //System.out.println("args="+Arrays.toString(args));
        
        WebViewCLI oauth = new WebViewCLI();
        for (int i=0; i<flags.length-1; i++) {
            
            String arg = flags[i];
            String val = ((i < flags.length-1) && (flags[i+1].charAt(0) != '-')) ? flags[++i] : "true";
            if (val.charAt(0) == '-') {
                continue;
            }
            if ("-redirect_uri".equals(arg)) {
                oauth.redirectURI = val;
                continue;
            }
            if ("-client_id".equals(arg)) {
                oauth.clientId = val;
                continue;
            }
            if ("-response_type".equals(arg)) {
                oauth.responseType = val;
                continue;
                
            }
            if ("-client_secret".equals(arg)) {
                oauth.clientSecret = val;
                continue;
            }
            if ("-scope".equals(arg)) {
                oauth.scope = val;
                continue;
            }
            if ("-title".equals(arg)) {
                oauth.title = val;
                continue;
            }
            if ("-fullscreen".equals(arg)) {
                oauth.fullscreen = true;
                continue;
            }
            if ("-resizable".equals(arg)) {
                oauth.resizable = true;
                continue;
            }
            if ("-port".equals(arg)) {
                oauth.port = Integer.parseInt(val);
                continue;
            }
            if ("-oauth".equals(arg)) {
                oauth.oauth = true;
                continue;
            }
            if ("-w".equals(arg) || "-width".equals(arg)) {
                oauth.width = Integer.parseInt(val);
                continue;
            }
            if ("-h".equals(arg) || "-height".equals(arg)) {
                oauth.height = Integer.parseInt(val);
                continue;
            }
            if ("-onLoad".equalsIgnoreCase(arg)) {
                if (oauth.onLoad == null) {
                    oauth.onLoad = "";
                }
                oauth.onLoad += "\n" + val;
                continue;
                
            }
            if ("-useMessageBoundaries".equalsIgnoreCase(arg)) {
                oauth.useMessageBoundaries = true;
            }
            if ("-onLoadFile".equalsIgnoreCase(arg)) {
                File f = new File(val);
                byte[] buf = new byte[(int)f.length()];
                try (FileInputStream fis = new FileInputStream(new File(val))) {
                    fis.read(buf);
                    if (oauth.onLoad == null) {
                        oauth.onLoad = "";
                    }
                    oauth.onLoad += "\n" + new String(buf, "UTF-8");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
            oauth.additionalParams.put(arg.substring(1), val);
            
            
        }
        oauth.url = args[0];
        if (args.length > 1 && oauth.oauth) {
            oauth.oauthOutputFile = new File(args[1]);
            File parent = oauth.oauthOutputFile.getParentFile();
            if (parent == null) {
                try {
                    parent = oauth.oauthOutputFile.getCanonicalFile().getParentFile();
                } catch (IOException ex) {
                    throw new IllegalArgumentException("OAuth output file "+oauth.oauthOutputFile+" cannot be found.  Parent directory must exist");
                }
            }
            if (!parent.isDirectory()) {
                throw new IllegalArgumentException("OAuth output file "+oauth.oauthOutputFile+" cannot be found.  Parent directory must exist");
            }
            if (oauth.oauthOutputFile.exists()) {
                throw new IllegalArgumentException("OAuath output file "+oauth.oauthOutputFile+" already exists.");
            }
        }
        if (oauth.oauth) {
            if (oauth.clientId == null) {
                throw new IllegalArgumentException("client_id is missing");
            }
            if (oauth.redirectURI == null) {
                throw new IllegalArgumentException("redirect_uri is missing");
            }
            if (oauth.scope == null) {
                throw new IllegalArgumentException("scope is missing");
            }
            if (oauth.oauthOutputFile == null) {
                throw new IllegalArgumentException("You must provide an output file file as the second argument where the oauth credentials will be written");
            }
        }
        
        
        return oauth;
        
    }
    
    private static String[] extractFlags(String[] args) {
        List<String> out = new ArrayList<>();
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                out.add(arg);
                if (i < args.length-1) {
                    if (args[i+1].charAt(0) != '-') {
                        out.add(args[i+1]);
                        i++;
                    }
                }
            }
        }
        return out.toArray(new String[out.size()]);
    }
    
    private static String[] extractArgs(String[] args) {
        List<String> out = new ArrayList<>();
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                if (i < args.length-1 && args[i+1].charAt(0) != '-') {
                    i++;
                }
                continue;
            } else {
                out.add(arg);
            }
        }
        return out.toArray(new String[out.size()]);
    }
    
    public static void main(String[] args) {
        if (restartJVM(args)) {
            return;
        }
        
        try (WebViewCLI oauth = parseParams(args)) {
            oauth.init();
            System.out.println("Fin");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        
    }
    
    
    private static boolean charArrayBugTested;
    private static boolean charArrayBug;
    
    private static String encode(char[] buf, String spaceChar) {
        return encode(buf, spaceChar, null);
    }
    
    private static String encode(char[] buf, String spaceChar, String doNotEncode) {
        final StringBuilder sbuf = new StringBuilder(buf.length * 3);
        int blen = buf.length;
        for (int i = 0; i < blen; i++) {
            final char ch = buf[i];
            
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') ||
                    (ch == '-' || ch == '_' || ch == '.' || ch == '~' || ch == '!'
          || ch == '*' || ch == '\'' || ch == '(' || ch == ')' || ignoreCharsWhenEncoding.indexOf(ch) > -1) || (doNotEncode != null && doNotEncode.indexOf(ch) > -1)) {
                sbuf.append(ch);
            } else if (ch == ' ') {
                sbuf.append(spaceChar);
            } else {
                appendHex(sbuf, ch);
            }
        }
        return sbuf.toString();
    }
    
    /**
     * toCharArray should return a new array always, however some devices might
     * suffer a bug that allows mutating a String (serious security hole in the JVM)
     * hence this method simulates the proper behavior
     * @param s a string
     * @return the contents of the string as a char array guaranteed to be a copy of the current array
     */
    public static char[] toCharArray(String s) {
        // toCharArray should return a new array always, however some devices might
        // suffer a bug that allows mutating a String (serious security hole in the JVM)
        // hence this method simulates the proper behavior
        if(!charArrayBugTested) {
            charArrayBugTested = true;
            if(s.toCharArray() == s.toCharArray()) {
                charArrayBug = true;
            }
        }
        if(charArrayBug) {
            char[] c = new char[s.length()];
            System.arraycopy(s.toCharArray(), 0, c, 0, c.length);
            return c;
        }
        return s.toCharArray();
    }

    private static String encode(String str, String spaceChar) {
        if (str == null) {
            return null;
        }
        return encode(toCharArray(str), spaceChar);
    }
    
     /**
     * Encode a string for HTML requests
     *
     * @param str none encoded string
     * @return encoded string
     */
    public static String encodeUrl(final String str) {
        return encode(str, "%20");
    }
    
    /**
     * Encodes the provided string as a URL (with %20 for spaces).
     * @param str The URL to encode
     * @param doNotEncodeChars A string whose characters will not be encoded.
     * @return 
     */
    public static String encodeUrl(final String str, String doNotEncodeChars) {
        return encode(str.toCharArray(), "%20", doNotEncodeChars);
    }
    
    private void handleURL(String url) {
        if ((url.startsWith(redirectURI)) && url.indexOf("?") >= 0) {
            if (url.indexOf("code=") > -1) {
                Hashtable params = getParamsFromURL(url);
                try (PrintWriter out = new PrintWriter(oauthOutputFile, "UTF-8")) {
                    for (String key : (Set<String>)params.keySet()) {
                        out.println(key+"="+params.get(key));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
                
                System.exit(0);
            } else {
                System.exit(1);
            }
        }
    }
    
    private Hashtable getParamsFromURL(String url) {
        int paramsStarts = url.indexOf('?');
        if (paramsStarts > -1) {
            url = url.substring(paramsStarts + 1);
        }
        Hashtable retVal = new Hashtable();

        String[] params = Util.split(url, "&");
        int plen = params.length;
        for (int i = 0; i < plen; i++) {
            if (params[i].indexOf("=") > 0) {
                String[] keyVal = Util.split(params[i], "=");
                retVal.put(keyVal[0], keyVal[1]);
            }
        }
        return retVal;
    }

    @Override
    public void close() throws Exception {
        if (server != null) {
            try {
                server.close();
            } catch (Exception ex){}
        }
    }
    
    private static class Util {
        static String[] split(String str, String sep) {
            return str.split(sep);
        }
    }
    
    private static void appendHex(StringBuilder sbuf, char ch) {
        int firstLiteral = ch / 256;
        int secLiteral = ch % 256;
        if(firstLiteral == 0 && secLiteral < 127) {
            sbuf.append("%");
            String s = Integer.toHexString(secLiteral).toUpperCase();
            if(s.length() == 1) {
                sbuf.append("0");
            } 
            sbuf.append(s);
            return;
        }
        if (ch <= 0x07ff) {
            // 2 literals unicode
            firstLiteral = 192 + (firstLiteral << 2) +(secLiteral >> 6);
            secLiteral=128+(secLiteral & 63);
            sbuf.append("%");
            sbuf.append(Integer.toHexString(firstLiteral).toUpperCase());
            sbuf.append("%");
            sbuf.append(Integer.toHexString(secLiteral).toUpperCase());
        } else {
            // 3 literals unicode
            int thirdLiteral = 128 + (secLiteral & 63);
            secLiteral = 128 + ((firstLiteral % 16) << 2) + (secLiteral >> 6);
            firstLiteral=224+(firstLiteral>>4);
            sbuf.append("%");
            sbuf.append(Integer.toHexString(firstLiteral).toUpperCase());
            sbuf.append("%");
            sbuf.append(Integer.toHexString(secLiteral).toUpperCase());
            sbuf.append("%");
            sbuf.append(Integer.toHexString(thirdLiteral).toUpperCase());
        }
    }
    
    /**
     * Fix for RFE 427: http://java.net/jira/browse/LWUIT-427
     * Allows determining chars that should not be encoded
     */
    private static String ignoreCharsWhenEncoding = "";

    /**
     *  These chars will not be encoded by the encoding method in this class
     * as requested in RFE 427 http://java.net/jira/browse/LWUIT-427
     * @param s set of characters to skip when encoding
     */
    public static void setIgnorCharsWhileEncoding(String s) {
        ignoreCharsWhenEncoding = s;
    }

    /**
     *  These chars will not be encoded by the encoding method in this class
     * as requested in RFE 427 http://java.net/jira/browse/LWUIT-427
     * @return chars skipped
     */
    public static String getIgnorCharsWhileEncoding() {
        return ignoreCharsWhenEncoding;
    }

    
    
    public static boolean restartJVM(String[] args) {
      
      String osName = System.getProperty("os.name");
      
      // if not a mac return false
      if (!osName.startsWith("Mac") && !osName.startsWith("Darwin")) {
         return false;
      }
      
      // get current jvm process pid
      String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
      // get environment variable on whether XstartOnFirstThread is enabled
      String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);
      
      // if environment variable is "1" then XstartOnFirstThread is enabled
      if (env != null && env.equals("1")) {
         return false;
      }
      
      // restart jvm with -XstartOnFirstThread
      String separator = System.getProperty("file.separator");
      String classpath = System.getProperty("java.class.path");
      String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
      String jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java";
      
      List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
      
      ArrayList<String> jvmArgs = new ArrayList<String>();
      
      jvmArgs.add(jvmPath);
      jvmArgs.add("-XstartOnFirstThread");
      jvmArgs.addAll(inputArguments);
      jvmArgs.add("-cp");
      jvmArgs.add(classpath);
      jvmArgs.add(mainClass);
      for (String arg : args) {
          jvmArgs.add(arg);
      }
      
      try {
         ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
         processBuilder.inheritIO();
         Process process = processBuilder.start();

         process.waitFor();
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return true;
   }
}
