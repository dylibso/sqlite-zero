package com.dylibso.sqlitezero;

import com.dylibso.chicory.wasi.WasiOptions;
import java.nio.file.Path;

public class Database {
    private final LibSqlite libSqlite;
    private final Path guestPath;

    public Database(Path hostPathToDatabase) {
        var parent = hostPathToDatabase.toAbsolutePath().getParent();
        var guestPath = Path.of("/" + hostPathToDatabase.getFileName());
        System.out.println(parent);
        System.out.println(guestPath);
        var wasiOptions = WasiOptions.builder().withDirectory("/", parent).build();
        libSqlite = new LibSqlite(wasiOptions);
        this.guestPath = guestPath;
    }

    public Database open() {
        libSqlite.open(guestPath);
        return this;
    }

    public void exec(String sql, SqlResults results) {
        libSqlite.exec(sql, results);
    }
}
