import QtQuick 2
import QtQuick.Window 2.15
//import QtQml 2.12

import SafeArea 1.0

Window {
    id:root
    width: 640
    height: 480
    visible: true
    title: qsTr("Hello World")

    //This Flag is all that is needed for ios
    flags:Qt.Window | Qt.WindowTitleHint | Qt.WindowSystemMenuHint
          | (Qt.platform.os === "ios" ? Qt.MaximizeUsingFullscreenGeometryHint : 0)

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

        //Safe Margin change when orientation of the device changes
        //The orientation changed signal comes before the new safemargins are ready to be read
        //therefore the delay via timer
        readonly property bool isPortrait: (Screen.primaryOrientation === Qt.PortraitOrientation ||
                                            Screen.primaryOrientation === Qt.InvertedPortraitOrientation)
        onIsPortraitChanged:{
            timerNewSafeMargins.start()
        }

    }


    Image {
        anchors.fill: parent
        source: "qrc:/ressources/Maps.png"
        fillMode: Image.PreserveAspectCrop
    }

    Rectangle{
        anchors{
            fill:parent
            topMargin: safeMargins.safeMarginTop
            leftMargin: safeMargins.safeMarginLeft
            rightMargin: safeMargins.safeMarginRight
            bottomMargin: safeMargins.safeMarginBottom
        }

        border.color: "darkred"
        border.width: 2
        color: "#40FFFFFF"

        Text {
            anchors.centerIn: parent
            text: qsTr("The area for your Components")
            font.bold: true
            font.pixelSize: 20
        }
    }
}
