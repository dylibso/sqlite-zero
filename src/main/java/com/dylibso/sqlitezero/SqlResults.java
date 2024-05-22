package com.dylibso.sqlitezero;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlResults<T> {
    public ArrayList<SqlRow<T>> rows;
    private SqlRow<T> thisRow;

    public SqlResults() {
        rows = new ArrayList<>();
        thisRow = new SqlRow<>();
    }

    public void addProperty(String c, String v) {
        thisRow.addProperty(c, v);
    }

    public void finishRow() {
        rows.add(thisRow);
        thisRow = new SqlRow<>();
    }

    public List<SqlRow<T>> getRows() {
        return rows;
    }

    public List<T> cast(Class<T> clazz) {
        return rows.stream().map(
                r -> {
                    try {
                        return r.cast(clazz);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toList());
    }
}
