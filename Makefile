WASI_SDK_PATH=/opt/wasi-sdk/

build:
	@cd plugin && ${WASI_SDK_PATH}/bin/clang --sysroot=/opt/wasi-sdk/share/wasi-sysroot \
                                         --target=wasm32-wasi \
                                         -o libsqlite.wasm \
                                         sqlite3.c sqlite_wrapper.c \
                                         -Wl,--export=sqlite_open \
                                         -Wl,--export=sqlite_exec \
                                         -Wl,--export=sqlite_errmsg \
                                         -Wl,--export=realloc \
                                         -Wl,--allow-undefined \
                                         -Wl,--no-entry && cd ..
	@mv plugin/libsqlite.wasm src/main/resources
	@mvn clean install