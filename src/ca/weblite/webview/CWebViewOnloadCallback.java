/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.webview;

import com.sun.jna.Callback;

/**
 *
 * @author shannah
 */
public interface CWebViewOnloadCallback extends Callback {
    public void onLoad();
}
