package com.dylibso.sqlitezero;

import java.nio.file.Path;

class Flower {
    public float petal_length;
    public float petal_width;
    public float sepal_length;
    public float sepal_width;
    public String species;

    public String toString() {
        return "Flower[species="+species+",petal_length="+petal_length+",petal_width="+petal_width+",sepal_length="+sepal_length+",sepal_width="+sepal_width+"]";
    }
}

public class App
{
    public static void main(String[] args)
    {
        var databasePath = Path.of("/Users/ben/Code/sqlite-dbs/iris.db");
        var db = new Database(databasePath).open();
        var results = new SqlResults<Flower>();
        var sql = " SELECT \n" +
                "     O.petal_length, \n" +
                "     O.petal_width, \n" +
                "     O.sepal_length, \n" +
                "     O.sepal_width, \n" +
                "     S.species\n" +
                "     FROM Observation AS O\n" +
                "     JOIN Species AS S ON S.species_id = O.species_id LIMIT 3";
        db.exec(sql, results);

        var flowers = results.cast(Flower.class);

        for (var f : flowers) {
            System.out.println(f);
        }
    }
}
