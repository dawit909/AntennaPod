#----------------------------------------------------------------
# Generated CMake target import file for configuration "RelWithDebInfo".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "Chromaprint::chromaprint" for configuration "RelWithDebInfo"
set_property(TARGET Chromaprint::chromaprint APPEND PROPERTY IMPORTED_CONFIGURATIONS RELWITHDEBINFO)
set_target_properties(Chromaprint::chromaprint PROPERTIES
  IMPORTED_LINK_INTERFACE_LANGUAGES_RELWITHDEBINFO "C;CXX"
  IMPORTED_LOCATION_RELWITHDEBINFO "${_IMPORT_PREFIX}/lib/libchromaprint.a"
  )

list(APPEND _IMPORT_CHECK_TARGETS Chromaprint::chromaprint )
list(APPEND _IMPORT_CHECK_FILES_FOR_Chromaprint::chromaprint "${_IMPORT_PREFIX}/lib/libchromaprint.a" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
