# PaintBehindSystemBars

Have you ever wonndered how Apps like GooleMaps paint the map content behind the status and navigation bar?

## Example GoogleMaps
![alt Text](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/Screenshots/GoogleMapsExample.PNG?raw=true)

This repository show how to expand your basic QML application, so that you can paint behind the system bars and how to know where the bars are, for iOS and Android.

Let's start with the easier of the two:

## iOS

Inside your main.qml specify the window flag `Qt.MaximizeUsingFullscreenGeometryHint
### main.qml
``` qml
flags:Qt.Window | Qt.WindowTitleHint | Qt.WindowSystemMenuHint | (Qt.platform.os === "ios" ? Qt.MaximizeUsingFullscreenGeometryHint : 0)
```
that will allow the Window root component to paint behinde the bars

## Android

For Android the process is a bit more complicated. First of we need to expand QtActivity with our own class: [MyActivity](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/android/src/org/myapp/activity/MyActivity.java) and set the window flags

*WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION*

Than we can set the status and navigation bar to a transparent color.
### MyActivity.java
```java
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
}    
```

make sure to replace, inside the [AndroidManifest.xml](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/android/AndroidManifest.xmlhttps://github.com/DeiVadder/PaintBehindSystemBars/blob/main/android/AndroidManifest.xml) the reference to QtActivity with the extended class MyActivity 
and to add 'com.android.support:appcompat-v7:25.3.1' to your [build.gradle dependencies](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/android/build.gradle)

## Margins
In either iOS or Android you will want to know the new margins you will have to respect. Therefore I introcuded the class [SafeArea](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/SafeArea.h)

The iOS part makes use of a private class QPlatformWindow. To properly use this class this [QT += gui-private](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/QmlPaintBehindSystemBars.pro) needs to be added to your *.pro file
### SafeArea.h
``` c++
Q_INVOKABLE QVariantMap getSafeAreaMargins(QQuickWindow *window){
    QVariantMap map;
    QMargins margins;
#if !defined (Q_OS_ANDROID)
    QPlatformWindow *platformWindow = static_cast<QPlatformWindow *>(window->handle());
    if(!platformWindow)
        return QVariantMap();
    margins = platformWindow->safeAreaMargins();
#else
    ...
#endif
    map["top"] = margins.top();
    map["right"] = margins.right();
    map["bottom"] = margins.bottom();
    map["left"] = margins.left();

    return map;
}
```

Sadly QPlatformWindow has currently no support for Android, it may be added. [You can follow the ticket here!](https://bugreports.qt.io/browse/QTBUG-90346)
So for Android we have to go the extra mile and add a couple of Java functions, to call up on. 

Previously we extended [QtActivity](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/android/src/org/myapp/activity/MyActivity.java) in MyActivity.java.
We expand this class with the custom functions *statusBarHeight*, *safeAreaTop* *getNavBarPosition* to use inside [SafeArea](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/SafeArea.h)

### MyActivity.java
``` java
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
        DisplayCutout cutout = getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
        if(cutout != null) {
            int cutoutHeight = cutout.getSafeInsetTop();
            if(cutoutHeight > 0)
                return cutoutHeight;
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
```

### SafeArea.h
``` c++
  Q_UNUSED(window)
  // We paint behind the Status bar -> StatusBar height is requiered
  const QAndroidJniObject activity = QtAndroid::androidActivity();
  double statusBarHeight = static_cast<double>(activity.callMethod<jdouble>("statusBarHeight"));
  statusBarHeight = qMax(statusBarHeight, static_cast<double>(activity.callMethod<jint>("safeAreaTop")));

  //!WARNING this function block does not yet deal with cutouts correctly, when the device is rotated

  //Since we have AA_EnableHighDpiScaling activated,
  //we will have to devide the returned value by the device pixel ratio, to use the value in QML
  static const double devicePixelRatio = QGuiApplication::primaryScreen()->devicePixelRatio();

  statusBarHeight /= devicePixelRatio;
  margins.setTop(statusBarHeight);

  const int navBarPosition = static_cast<int>(activity.callMethod<jint>("getNavBarPosition"));
  const double navBarHeight = static_cast<double>(activity.callMethod<jdouble>("navBarHeight")) / devicePixelRatio;
  switch (navBarPosition) {
  case -1: break;//No navBar
  case 0: margins.setBottom(navBarHeight);break;
  case 1: margins.setRight(navBarHeight); break;
  case 2: margins.setLeft(navBarHeight);  break;
  case 3:
  default:
      Q_ASSERT_X(false,"SafeArea","NavigationBar position is in an unhandeled position");
      break;
  }
```


Register SafeArea class to be used inside main.qml
```
qmlRegisterType<SafeArea>("SafeArea", 1,0, "SafeArea");
```

The orientation changed signal will most likely be emitted before the SafeArea class can fetch the new margins, therefore the delay via the Timer component
### main.qml
``` qml
    Timer{
        id: timerNewSafeMargins
        running: false
        repeat: false
        interval: 10
        onTriggered:{
            var map = safeMargins.getSafeAreaMargins(root)
            safeMargins.safeMarginLeft = map["left"]
            safeMargins.safeMarginRight = map["right"]
            safeMargins.safeMarginTop = map["top"]
            safeMargins.safeMarginBottom = map["bottom"]
        }
    }
    SafeArea{
        id:safeMargins

        property int safeMarginLeft: getSafeAreaMargins(root)["left"]
        property int safeMarginRight: getSafeAreaMargins(root)["right"]
        property int safeMarginTop: getSafeAreaMargins(root)["top"]
        property int safeMarginBottom: getSafeAreaMargins(root)["bottom"]
        onSafeMarginBottomChanged: console.log("safeMarginBottom", safeMarginBottom)

        readonly property bool isPortrait: (Screen.primaryOrientation === Qt.PortraitOrientation ||
                                            Screen.primaryOrientation === Qt.InvertedPortraitOrientation)
        onIsPortraitChanged:{
            timerNewSafeMargins.start()
        }
```

## Example images
### iOS
![iOS Portrait](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/Screenshots/PortaitIos.png?raw=true)

### Android
![alt Text](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/Screenshots/PortaitNormal.JPG?raw=true)
![alt Text](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/Screenshots/PortaitCutout.JPG?raw=true)
![alt Text](https://github.com/DeiVadder/PaintBehindSystemBars/blob/main/Screenshots/Landscape.JPG?raw=true)
