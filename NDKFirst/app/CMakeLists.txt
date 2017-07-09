cmake_minimum_required(VERSION 3.4.1)
#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

add_library( # Sets the name of the library.
             ndk-test-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/ndk_test.cpp
             )


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
                       ndk-test-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

add_library( # Sets the name of the library.
            uninstall-tool

            # Sets the library as a shared library.
            SHARED

            # Provides a relative path to your source file(s).
            src/main/cpp/uninstall_tool.cpp
            )

target_link_libraries( # Specifies the target library.
                      uninstall-tool

                      # Links the target library to the log library
                      # included in the NDK.
                      ${log-lib} )