cmake_minimum_required(VERSION 3.21)

project(PaintBehindSystemBars VERSION 2.0 LANGUAGES CXX)

# SafeArea (QML) and Qt::ExpandedClientAreaHint exist since Qt 6.9; the project targets Qt 6.12.
find_package(Qt6 6.9 REQUIRED COMPONENTS Quick)

qt_standard_project_setup(REQUIRES 6.9)

qt_add_executable(PaintBehindSystemBars
    main.cpp
)

qt_add_qml_module(PaintBehindSystemBars
    URI PaintBehindSystemBars
    QML_FILES
        Main.qml
    RESOURCES
        ressources/Maps.png
)

set_target_properties(PaintBehindSystemBars PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER org.qtproject.example.PaintBehindSystemBars
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_link_libraries(PaintBehindSystemBars
    PRIVATE Qt6::Quick
)

include(GNUInstallDirs)
install(TARGETS PaintBehindSystemBars
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
