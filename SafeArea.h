#ifndef SAFEAREA_H
#define SAFEAREA_H

#include <QObject>
#include <QVariantMap>
#include <QQuickWindow>

//This is a private include, make sure to add QT += gui-private to th epro file
#include <QtGui/qpa/qplatformwindow.h>

#if defined (Q_OS_ANDROID)
#include <QtAndroid>
#include <QAndroidJniObject>
#include <QGuiApplication>
#include <QScreen>
#endif

class SafeArea : public QObject
{
    Q_OBJECT
public:
    SafeArea(QObject *parent = nullptr) : QObject(parent) {}

    Q_INVOKABLE QVariantMap getSafeAreaMargins(QQuickWindow *window){
        QVariantMap map;
        QMargins margins;
    #if !defined (Q_OS_ANDROID)
        QPlatformWindow *platformWindow = static_cast<QPlatformWindow *>(window->handle());
        if(!platformWindow)
            return QVariantMap();
        margins = platformWindow->safeAreaMargins();
    #else
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
    #endif
        map["top"] = margins.top();
        map["right"] = margins.right();
        map["bottom"] = margins.bottom();
        map["left"] = margins.left();

        return map;
    }
};

#endif // SAFEAREA_H
