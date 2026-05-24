
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("fingerprinter")

# --- CHROMAPRINT CONFIGURATION ---
# Force Chromaprint to use its built-in KISS FFT engine so we don't need FFmpeg
set(FFT_LIB "kissfft" CACHE STRING "FFT library to use" FORCE)
set(BUILD_TESTS OFF CACHE BOOL "Disable tests" FORCE)
set(BUILD_TOOLS OFF CACHE BOOL "Disable command line tools" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "Build Chromaprint as a static library" FORCE)

set(KISSFFT_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/kissfft" CACHE PATH "Path to KissFFT" FORCE)

# Tell CMake to read the CMakeLists.txt inside the chromaprint folder
add_subdirectory(chromaprint)

# --- YOUR JNI BRIDGE ---
# Compile your C++ file into a shared library (.so) named libfingerprinter.so
add_library(${CMAKE_PROJECT_NAME} SHARED fingerprinter.cpp)

# Make sure your C++ file can find the chromaprint headers
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE chromaprint/src)

# Link your library against Chromaprint and the native Android logging library
target_link_libraries(${CMAKE_PROJECT_NAME}
        chromaprint
        log)