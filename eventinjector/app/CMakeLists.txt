cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})


add_library( EventInjector
             SHARED
             src/main/cpp/EventInjector.c )

find_library(log-lib
             log )

target_link_libraries( EventInjector
                       ${log-lib} )

add_library( KeyTouchInjector
             SHARED
             src/main/cpp/KeyTouchInjector.c )

target_link_libraries( KeyTouchInjector
                       ${log-lib} )

target_link_libraries( EventInjector
                       ${log-lib} )