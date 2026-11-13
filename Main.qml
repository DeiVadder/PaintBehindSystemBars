import QtQuick

Window {
    id: root
    width: 640
    height: 480
    visible: true
    title: qsTr("Paint behind system bars")

    // ExpandedClientAreaHint (Qt 6.9+) lets the window content extend behind
    // the status bar / navigation bar (Android, iOS) and the title bar (desktop).
    // It replaces Qt.MaximizeUsingFullscreenGeometryHint and the custom Android activity.
    // NoTitleBarBackgroundHint removes the title bar background on desktop platforms.
    flags: Qt.Window | Qt.ExpandedClientAreaHint | Qt.NoTitleBarBackgroundHint

    // Painted behind the system bars: fills the whole window.
    Image {
        id: background
        anchors.fill: parent
        source: "ressources/Maps.png"
        fillMode: Image.PreserveAspectCrop
    }

    // Content area: respects the safe area. The SafeArea margins update
    // automatically on rotation, so no timer workaround is needed anymore.
    Rectangle {
        id: contentArea
        anchors {
            fill: parent
            topMargin: root.contentItem.SafeArea.margins.top
            leftMargin: root.contentItem.SafeArea.margins.left
            rightMargin: root.contentItem.SafeArea.margins.right
            bottomMargin: root.contentItem.SafeArea.margins.bottom
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
