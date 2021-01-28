package org.myapp.activity;

import org.qtproject.qt5.android.QtNative;

import org.qtproject.qt5.android.bindings.QtActivity;
import android.os.*;
import android.content.*;
import android.app.*;

import android.content.res.Resources;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.graphics.Color;

public class MyActivity extends QtActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setCustomStatusAndNavBar();
    } // onCreate

    void setCustomStatusAndNavBar() {
        //First check sdk version, custom/transparent System_bars are only available after LOLLIPOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            //The Window flag 'FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS' will allow us to paint the background of the status bar ourself and automatically expand the canvas
            //If you want to simply set a custom background color for the navbar, use the following addFlags call
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            //The Window flag 'FLAG_TRANSLUCENT_NAVIGATION' will allow us to paint the background of the navigation bar ourself
            //But we will also have to deal with orientation and OEM specifications, as the nav bar may or may not depend on the orientation of the device
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //Set Statusbar Transparent
            window.setStatusBarColor(Color.TRANSPARENT);
            //Statusbar background is now transparent, but the icons and text are probably white and not really readable, as we have a bright background color
            //We set/force a light theme for the status bar to make those dark
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            //Set Navbar to desired color (0xAARRGGBB) set alpha value to 0 if you want a solid color
            window.setNavigationBarColor(0xFFD3D3D3);
        }
    }

    /*If we decide to draw behind the statusbar, we need to know the new safea rea on top,
      to not draw text or position buttons behind the status bar.
      Those would be unclickable and potentially unreadable
      This function returns the hight of the statusbar */
    public double statusBarHeight() {
        double result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimension(resourceId);
        }

        return result;
    }

    public int safeAreaTop() {
        //For devices that have a display cutout, the cutout may be bigger than the statusbar height
        //See 'Tall cutout' setting on a device's Developer options
        //To compensate, fetch the SafeInset for the top and use the maximum of the safeInsert and the statusbarHeight as safe margin
        //This is only necessary when the android api is >= 28 (Anroid P), before that, there were no cutouts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
            if(cutout != null) {
                int cutoutHeight = cutout.getSafeInsetTop();
                if(cutoutHeight > 0)
                    return cutoutHeight;
            }
        }
        return 0;
    }

    /*If we decide to draw the behind the navbar, we need to know the new safearea,
      so to not draw text or position buttons behind the nav bar.
    Those would be unclickable and potentially unreadable.
    ATTENTION:
    Compared to the statusbar, there is no guarantee that the navbar will be at the bottom of your screen
    This function returns the hight of the Navigation bar */
    public double navBarHeight() {
        double result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimension(resourceId);
        }
        return result;
    }

    public int getNavBarPosition() {
        Resources res= getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        boolean hasMenu = false;
        if (resourceId > 0)
            hasMenu =  res.getBoolean(resourceId);

        if(!hasMenu)
           return -1;

        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();

        switch (rotation) {
            case Surface.ROTATION_90:
                return 1;
            case Surface.ROTATION_180:
                return 3;
            case Surface.ROTATION_270:
                return 2;
            default:
                return 0;
        }
    }

    //Standalone function to set the statusbar transparent, if you do not care about the navigation bar
/*
    void setTranparentStatusBar() {
        Window window = getWindow();
        //Kitkat has concept of a statusbar, but Qt does no longer support that anyway
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
*/

}
