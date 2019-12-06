/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import com.sun.jna.Platform;

/**
 *
 * @author shannah
 */
public class OAuth2CLITest {
    private static final String AppleClientID = "ca.weblite.signindemosvc";
    // Client secret was generated using instructions from https://developer.okta.com/blog/2019/06/04/what-the-heck-is-sign-in-with-apple 
    private static final String AppleClientSecret = "eyJraWQiOiIiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiIiLCJpYXQiOjE1NzUzOTM1MjIsImV4cCI6MTU5MDk0NTUyMiwiYXVkIjoiaHR0cHM6Ly9hcHBsZWlkLmFwcGxlLmNvbSIsInN1YiI6IiJ9.NoxRRw8M-t6QA10mbscRWq8bCeRt3LA5Qcp2y_TEa59ExAzZgwlRLZOY5c3XO44vvh5tZQRSG06OT7C1L_ls1A";
    private static final String AppleRedirectURI = "https://weblite.ca/cn1tests/signindemo";
     private static final String OAUTH_URL = "https://appleid.apple.com/auth/authorize";
    public static void main(String[] args) {
        WebViewCLI.main(new String[]{
            "-client_id", AppleClientID,
            "-client_secret", AppleClientSecret,
            "-redirect_uri", AppleRedirectURI,
            "-scope", "name email",
            "-response_mode", "form_post",
            "-response_type", "code",
            "-oauth",
            "-state", "foofofofofofo",
            OAUTH_URL,
            "oauth_details.txt"
        });
    }
}
