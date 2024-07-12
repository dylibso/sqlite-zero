package com.dylibso.sqlitezero;

import java.nio.file.Path;

class Track {
    public int TrackId;
    public String Name;
    public String Composer;
    public String toString() { return "Track[id="+TrackId+",composer="+Composer+",name="+Name+"]";}
}

public class App
{
    public static void main(String[] args)
    {
        var databasePath = Path.of("/Users/ben/Downloads/Chinook_Sqlite.sqlite");
        var db = new Database(databasePath).open();
        var results = new SqlResults<Track>();
        var sql = """
                SELECT TrackId, Name, Composer FROM Track WHERE Composer like '%Glass%';
                """;
        var start = System.nanoTime();
        db.exec(sql, results);
        var rows = results.cast(Track.class);
        var end = System.nanoTime() - start;
        System.out.println("Queried in seconds: " + (end / 1000000000.0));
        for (var r : rows) {
            System.out.println(r);
        }
    }
}
