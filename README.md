# WebView

This is a Java port of the fantastic, tiny, light-weight [WebView](https://github.com/zserge/webview)
by [Serge Zaitsev](https://zserge.com).

To build the Java sources, you will need Java and Apache Maven. To build the native code you will also
need the appropriate native C/C++ tooling.

`mvn install` will build the jar and install it into your local Maven repository for use in other projects.

The jar suitable for command-line execution will be in your target directory (as per Maven usual).

## Synopsis

Cross-platform WebView that can be opened and controlled via CLI or as JavaAPI.

## Installation

Download [WebView.jar](bin/WebView.jar)

This jar can be run directly as an executable jar file (e.g. `java -jar WebView.jar [OPTIONS]`), or using the Java API
by adding the jar to your classpath.

## Platform Support

Runs on Windows (32 or 64), Linux (64), and Mac. Other platforms (e.g. Linux 32) can be supported. Simply need to build
the native libs for that platform.

## Running in Separate Process

You can run the WebView in a separate process by running:

~~~~
$ java -jar WebView.jar http://www.example.com
~~~~

This will open the web browser in its own window pointing to http://www.example.com.

**CLI Usage Instructions**

~~~~
Usage: java -jar WebView.jar [OPTIONS] <url>

  <url> - A URL to a webpage to show in the webview.  
Note: You can use a data url here.

Options:
  -title <Window Title>
  -w <window width px>
  -h <window height px>
  -onLoad <js to run on page load>
  -onLoadFile <path to js file to run on page load>
  -useMessageBoundaries    Use message boundaries for wrapping messages from the webview.   Makes it easier to parse.

Examples:

java -jar WebView.jar https://example.com
  Opens webview with starting page https://example.com

java -jar WebView.jar "data:text/html,%3Chtml%3Ehello%3C%2Fhtml%3E"
  Opens webview that says 'hello html'

java -jar WebView.jar https://google.com \
   -onLoad "window.addEventListener('load', function(){postMessageExt('loaded '+location.href)})"
  Opens a webview, and prints out URL of each page on window load event.
~~~~

### Interacting with the Browser Environment

You can interact with the browser environment by typing into the console while the browser is running. The browser
listens on STDIN, for any input, and it will evaluate any input as Javascript in the context of the current page. E.g.
Type "alert('foo')" then `[ENTER]` to open an alert popup.

If you need to enter a multi-line Javascript command, then begin your input with `<<<SOME_BOUNDARY`, and end it
with `SOME_BOUNDARY`.

For example:

~~~~
<<<END
var url = window.location.href;
alert('You are at '+url);
END
~~~~

NOTE:  If you give it an empty boundary, then it will simply use a blank line as your boundary.

### Getting Information From The Browser

There are two ways get the browser to communicate back to the outside world:

1. The onLoad callback. Whenever the user nagivates to a new page, it will output `loaded [URL]` to STDOUT. E.g. If you
   navigate to google.com, then it will output `loaded https://google.com` to STDOUT.
2. Call `window.postMessageExt("some message")`. This will cause the browser print "some message" to STDOUT. All
   messages of this kind are wrapped with beginning and ending boundaries to make the output easier to parse, in case
   you are writing a program to interact with the browser.

Here is an example of a session, where I load google.com, and then get its page title via `window.external.invoke()`:

~~~~
$ java -jar WebView-shaded.jar "https://www.google.com"

loaded https://www.google.com/
window.external.invoke(document.title)
<<<Boundary1575660241187
Google
Boundary1575660241187
~~~~

A few things to notice here:

1. When the page is loaded, it informed us with "loaded https://www.google.com" in STDOUT
2. I typed the "window.external.invoke(document.title)" command.
3. It responded to my command with an open boundary `<<<Boundary1575660241187` followed by the message ("Google"),
   followed by the closing boundary `Boundary1575660241187`

## Using Java API

If you want to use the webview directly in your Java app, you can do this also.

A simple usage example:

~~~~
webview = new WebView()
    .size(width, height)
    .title(title)
    .resizable(resizable)
    .fullscreen(fullscreen)
    .url(u)
    .onLoad(()->{
       //.. Do something on page load.
	   // You can get the url of the page via webview.url()
    })
    .javascriptCallback(message->{
        // Handle a message sent via window.external.invoke(message)
        // message is a string.
    })
    .show();
~~~~

NOTE: The `show()` method will start a blocking event loop.

WARNING: Currently the WebView is picky about being started on the main application thread. On Mac you may need to add
the "-XstartOnFirstThread" flag in the JVM.

## Using Java API from Swing, JavaFX, or other UI Toolkit

The WebView class cannot be used from Swing, JavaFX, or any other existing UI toolkit because it starts its own event
loop. If you want to make use of the WebView from within such an app, you'll need to use the WebViewCLIClient class,
which provides an interface to create and manage a WebView which runs inside its own subprocess.

See the [Swing Demo](demos/ca.weblite.webview.demo.WebViewSwingDemo/README.md) for a full example of this.

The basics are:

~~~~

// Opening the webview
WebViewCLIClient webview = (WebViewCLIClient)new WebViewCLIClient.Builder()
    .url("https://www.codenameone.com")
    .title("Codename One")
    .size(800, 600)
    .build();
    
// Adding a load listener (fired whenever a page loads)
webview.addLoadListener(evt->{
    System.out.println("Loaded "+evt.getURL());
});

// Adding a message listener (fired whenever any js calls window.postMessageExt(msg))
webview.addMessageListener(evt->{
    System.out.println(evt.getMessage());
});

// Evaluate javascript on the current page.  Implicit callback() method
// allows you to return result in CompetableFuture.
webview.eval("callback(window.location.href)")
    .thenAccept(str->{
        System.out.println("Current URL is "+str);
    });
    
    

    
// Closing the webview later
webview.close();
~~~~

#### Demos

1. [Swing Demo](demos/ca.weblite.webview.demo.WebViewSwingDemo/README.md) - A simple demo showing how to create and
   control a WebView from a Swing App.
2. [Minimal Demo](demos/ca.weblite.webview.demo.WebViewMinimalDemo/README.md) - A simple demo that only launches a
   WebView on the main thread.

## Supported Platforms

This should work on Mac, Linux, and Windows.

## Building Sources

~~~
git clone https://github.com/shannah/webviewjar
cd webviewjar
ant jar
~~~

This will create dist/WebView.jar, which can be run as an executable jar.

### Troubleshooting

ANT requires that the `platforms.JDK_1.8.home` system property is set to your JAVA_HOME. If it complains about this, you
can fix the issue by changing the `ant jar` command, above, to `ant jar -Dplatforms.JDK_1.8.home="$JAVA_HOME"`.

### Rebuilding Native Libs

The repo comes with pre-built native libs in the src/windows_32, src/windows_64, src/osx_64, and src/linux_64. If you
want to make changes to these native libs, then the following information may be of use to you.

1. Use the `build-xxx.sh` (where xxx is your current platform) scripts to rebuild the native sources, and copy them into
   the appropriate place in the src directory.
2. Mac and linux native sources are located in the src_c directory. Windows native sources are in the windows directory.
3. On Windows, you'll need to have Visual Studio installed (I use VS 2019, but earlier versions probably work).
   Additionally, I use git bash on Windows, which is why the build-windows.sh is a bash script, and not a .bat script.

## License

MIT

## Credits

1. This library created by [Steve Hannah](https://sjhannah.com)
2. Original webview library by [Serge Zaitsev](https://zserge.com)
