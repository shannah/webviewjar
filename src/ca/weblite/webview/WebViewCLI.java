/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shannah
 */
public class WebViewCLI {
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
    
    private void init() throws IOException {
        String u = oauth ? getFullUrl() : url;
        webview = new WebView()
                .size(width, height)
                .title(title)
                .resizable(resizable)
                .fullscreen(fullscreen)
                .url(u)
                .onLoad(()->{
                    if (oauth) {
                        handleURL(webview.url());
                    }
                })
                ;
        if (port >= 0) {
            WebviewSocketServer serve = new WebviewSocketServer(port, webview);
            new Thread(()->{
                System.out.println("Listening on "+serve.getPort());
            }).start();
            
        } else {
            WebViewController ctrl = new WebViewController(webview, System.in, System.out);
            
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
                oauth.port = Integer.parseInt(arg);
                continue;
            }
            if ("-oauth".equals(arg)) {
                oauth.oauth = true;
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
        WebViewCLI oauth = parseParams(args);
        try {
            oauth.init();
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
      
      // if you don't need console output, just enable these two lines 
      // and delete bits after it. This JVM will then terminate.
      //ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
      //processBuilder.start();
      
      try {
         ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
         processBuilder.inheritIO();
         //processBuilder.redirectErrorStream(true);
         Process process = processBuilder.start();
         
         //InputStream is = process.getInputStream();
         //InputStreamReader isr = new InputStreamReader(is);
         //BufferedReader br = new BufferedReader(isr);
         //String line;
         
         //while ((line = br.readLine()) != null) {
         //   System.out.println(line);
         //}
         
         process.waitFor();
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return true;
   }
}
