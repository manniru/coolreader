
set(SRC_LIST
    langstat.cpp
)

set(crengine_part_SRC_LIST
    ../../src/cp_stats.cpp
    ../../src/crtxtenc.cpp
    ../../src/lvmemman.cpp
    ../../src/lvstream.cpp
    ../../src/lvstring.cpp
    ../../src/crlog.cpp
    ../../src/serialbuf.cpp
)

add_definitions(-DBUILD_LITE=1)

if(WIN32)
    add_definitions(-DWIN32 -D_CONSOLE)
    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -mconsole")
endif(WIN32)

add_executable(langstat ${SRC_LIST} ${crengine_part_SRC_LIST})
target_link_libraries(langstat ${STD_LIBS})
