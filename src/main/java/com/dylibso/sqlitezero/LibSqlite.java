package com.dylibso.sqlitezero;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;

import java.nio.file.Path;
import java.util.List;

public class LibSqlite {

    private static int OK = 0;
    private static int PTR_SIZE = 4;

    private ExportFunction realloc;
    private ExportFunction open;
    private ExportFunction exec;
    private ExportFunction errmsg;
    private Instance instance;
    private int dbPtrPtr;
    private Integer dbPtr;

    // note: this is not thread safe
    private SqlResults results;

    public LibSqlite(WasiOptions wasiOpts) {
        var logger = new SystemLogger();
        var wasi = new WasiPreview1(logger, wasiOpts);
        var extra =
                new HostFunction[] {
                        this.createCallback(),
                        // unsure what this one does but we need to satisfy the interface
                        new HostFunction(
                                (Instance instance, Value... args) -> {
                                    logger.trace("Init called?");
                                    return new Value[] {Value.i32(0)};
                                },
                                "env",
                                "__main_argc_argv",
                                List.of(ValueType.I32, ValueType.I32),
                                List.of(ValueType.I32)),
                };
        var wasiFuncs = wasi.toHostFunctions();
        HostFunction[] allImports = new HostFunction[wasiFuncs.length + extra.length];
        System.arraycopy(wasiFuncs, 0, allImports, 0, wasiFuncs.length);
        System.arraycopy(extra, 0, allImports, wasiFuncs.length, extra.length);

        var imports = new HostImports(allImports);
        var module = Module.builder("./libsqlite.wasm")
                .withLogger(new SystemLogger())
                .withHostImports(imports)
                .withMachineFactory(instance -> new AotMachine(instance))
                .build();
        this.instance = module.instantiate();
        this.realloc = instance.export("realloc");
        this.open = instance.export("sqlite_open");
        this.exec = instance.export("sqlite_exec");
        this.errmsg = instance.export("sqlite_errmsg");
    }

    public void open(Path dbPath) {
        var path = dbPath.toAbsolutePath().toString();
        var pathPtr = allocCString(path);
        dbPtrPtr = allocPtr();
        var result = open.apply(Value.i32(pathPtr), Value.i32(dbPtrPtr))[0].asInt();
        if (result != OK) {
            throw new RuntimeException(errmsg());
        }
    }

    public void exec(String sql, SqlResults results) {
        this.results = results;
        var sqlPtr = allocCString(sql);
        this.exec.apply(Value.i32(getDbPtr()), Value.i32(sqlPtr));
        this.results = null;
    }

    public String errmsg() {
        var errPtr = errmsg.apply(Value.i32(getDbPtr()))[0].asInt();
        return readCString(errPtr);
    }

    // TODO can replace in next chicory release
    private String readCString(int ptr) {
        var mem = instance.memory();
        int c = ptr;
        while (mem.read(c) != '\0') { c++; }
        return mem.readString(ptr, c - ptr);
    }

    // TODO can replace in next chicory release
    private void writeCString(int ptr, String s) {
        instance.memory().writeString(ptr, s + '\0');
    }

    private int getDbPtr() {
        // todo: for the sake of this demo we can assume this won't change
        // and we can memoize this
        if (dbPtr == null) {
            dbPtr = instance.memory().readI32(dbPtrPtr).asInt();
        }
        return dbPtr;
    }

    public int allocCString(String s) {
        var ptr = malloc(s.length());
        writeCString(ptr, s);
        return ptr;
    }

    public int allocPtr() {
        return malloc(PTR_SIZE);
    }

    public int malloc(int size) {
        return realloc.apply(Value.i32(0), Value.i32(size))[0].asInt();
    }

    public int free(int ptr) {
        return realloc.apply(Value.i32(ptr), Value.i32(0))[0].asInt();
    }

    HostFunction createCallback() {
        return new HostFunction(
                (Instance instance, Value... args) -> {
                    if (results == null) throw new RuntimeException("Null results");
                    var argc = args[0].asInt();
                    var argv = args[1].asInt();
                    var azColName = args[2].asInt();
                    for (int i = 0; i < argc; i++) {
                        var colNamePtr =
                                instance.memory().readI32(azColName + (i * 4)).asInt();
                        var colName = readCString(colNamePtr);
                        var argvPtr =
                                instance.memory().readI32(argv + (i * 4)).asInt();
                        var value = readCString(argvPtr);
                        results.addProperty(colName, value);
                    }
                    results.finishRow();
                    return new Value[] {Value.i32(0)};
                },
                "env",
                "sqlite_callback",
                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(ValueType.I32));
    }

}
