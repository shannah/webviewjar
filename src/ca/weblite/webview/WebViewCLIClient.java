/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class WebViewCLIClient extends WebViewClient {
    private Process process;
    
    private static String[] append(String[] arr, String... args) {
        String[] out = new String[arr.length + args.length];
        System.arraycopy(arr, 0, out, 0, arr.length);
        System.arraycopy(args, 0, out, arr.length, args.length);
        return out;
    }
    
    public static class Builder extends WebViewClient.Builder {

        @Override
        public WebViewCLIClient build() {
            String[] args = new String[] {
                "-title", title,
                "-w", ""+w,
                "-h", ""+h,
                "-useMessageBoundaries", "true",
            };
            if (onBeforeLoad.length() > 0) {
                args = append(args, "-onLoad", onBeforeLoad.toString());
            }
            args = append(args, url);
            return new WebViewCLIClient(args);
        }
        
    }
    
    public WebViewCLIClient(String[] args) {
        String osName = System.getProperty("os.name");
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            try {
                process.destroyForcibly();
            } catch (Throwable t){}
        }));

        // get current jvm process pid
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        // get environment variable on whether XstartOnFirstThread is enabled
        String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);


        // restart jvm with -XstartOnFirstThread
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String mainClass = "ca.weblite.webview.WebViewCLI";
        String jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        ArrayList<String> jvmArgs = new ArrayList<String>();

        jvmArgs.add(jvmPath);
        if (osName.toLowerCase().contains("mac")) {
            jvmArgs.add("-XstartOnFirstThread");
            
        }
        jvmArgs.addAll(inputArguments);
        jvmArgs.add("-cp");
        jvmArgs.add(classpath);
        jvmArgs.add(mainClass);
        for (String arg : args) {
            jvmArgs.add(arg);
        }

        try {
           ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
           
           process = processBuilder.start();
           InputStream input = process.getInputStream();
           OutputStream output = process.getOutputStream();
           init(input, output);
        } catch (Exception e) {
           e.printStackTrace();
        }


    }

    @Override
    public void close() throws Exception {
        super.close();
        try {
            process.destroyForcibly();
        } catch (Exception ex) {}
    }
    
    
}
