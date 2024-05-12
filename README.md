# sqlite-zero

This is an example of a pure JVM sqlite library using [Chicory](https://github.com/dylibso/chicory).

> **Note**: This is just an experiment at the moment, but if you're interested in helping complete the sqlite API we'd be willing
> to publish and maintain it.

## Why do this?

Much of the motivation is outlined in [Chicory's README](https://github.com/dylibso/chicory/tree/main?tab=readme-ov-file#development),
the short answer is so that we can run the official sqlite codebase, without ever leaving the JVM, and without asking
our users to install a dependency. It has zero native dependencies.

## The API

Suppose we are looking at the [iris.db](https://github.com/davidjamesknight/SQLite_databases_for_learning_data_science) dataset.
Let's query some observations from this database. See [App.java](src/main/java/com/dylibso/sqlitezero/App.java) for running example.

```java
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
```

Running this yields:

```
Flower[species=setosa,petal_length=1.4,petal_width=0.2,sepal_length=5.1,sepal_width=3.5]
Flower[species=setosa,petal_length=1.4,petal_width=0.2,sepal_length=4.9,sepal_width=3.0]
Flower[species=setosa,petal_length=1.3,petal_width=0.2,sepal_length=4.7,sepal_width=3.2]
```

Assuming we have a `Flower` class like this to capture the results:

```java
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
```

## Compiling

This package uses [maven](https://maven.apache.org/).
You need [wasi-sdk](https://github.com/WebAssembly/wasi-sdk) on your machine.
The makefile assumes it's located at `/opt/wasi-sdk`.

```
make build
```