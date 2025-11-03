cmake_minimum_required(VERSION 3.22.1)

# Declare library name
project(secrets)

# Create shared library
add_library(
    secrets
    SHARED
    secrets.cpp
)

# Find required libraries
find_library(
    log-lib
    log
)

# Link libraries
target_link_libraries(
    secrets
    ${log-lib}
)

