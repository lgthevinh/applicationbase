package org.thingai.base.dao;

import java.util.Map;

public interface Dao {
    String SQLITE = "sqlite";
    String MYSQL = "mysql";
    String POSTGRESQL = "postgresql";
    String MONGODB = "mongodb";
    String IN_MEMORY = "in_memory";
    String FILE = "file";

    void initDao(Class[] classes);
    <T> T[] readAll(Class<T> clazz);
    <T> void insertOrUpdate(T t);
    <T> void insertOrUpdate(Class<T> clazz, T t);
    <T> void insertBatch(T[] t);
    <T, K> void delete(Class<T> clazz, K id);
    <T> void delete(T t);
    <T> void deleteByColumn(Class<T> clazz, String column, String value);
    <T> void deleteAll(Class<T> clazz);
    <T> T[] query(Class<T> clazz, String column, String value);
    <T> T[] query(Class<T> clazz, String[] column, String[] value);
    <T> T[] query(Class<T> clazz, String query);

    Map<String, Object>[] queryRaw(String query);
}
