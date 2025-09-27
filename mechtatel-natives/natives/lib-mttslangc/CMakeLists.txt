cmake_minimum_required(VERSION 3.15)
project(mttslangc LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF) # optional, avoids /std:gnu++17

# Build shared library
add_library(mttslangc SHARED mtt-main.cpp)

# Headers in current directory
target_include_directories(mttslangc PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
# Headers in include directory
target_include_directories(mttslangc PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/slang/include)

# Link to slang
target_link_directories(mttslangc PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/lib)
target_link_libraries(mttslangc PRIVATE slang)
