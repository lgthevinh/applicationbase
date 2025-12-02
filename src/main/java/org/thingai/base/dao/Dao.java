package org.thingai.base.dao;

import java.util.Map;

public abstract class Dao {
    public static final String SQLITE = "sqlite";
    public static final String MYSQL = "mysql";
    public static final String POSTGRESQL = "postgresql";
    public static final String MONGODB = "mongodb";
    public static final String IN_MEMORY = "in_memory";
    public static final String FILE = "file";

    public abstract void initDao(Class[] classes);
    public abstract <T> T[] readAll(Class<T> clazz);
    public abstract <T> void insertOrUpdate(T t);
    public abstract <T> void insertOrUpdate(Class<T> clazz, T t);
    public abstract <T> void insertBatch(T[] t);
    public abstract <T, K> void delete(Class<T> clazz, K id);
    public abstract <T> void delete(T t);
    public abstract <T> void deleteByColumn(Class<T> clazz, String column, String value);
    public abstract <T> void deleteAll(Class<T> clazz);
    public abstract <T> T[] query(Class<T> clazz, String column, String value);
    public abstract <T> T[] query(Class<T> clazz, String[] column, String[] value);
    public abstract <T> T[] query(Class<T> clazz, String query);

    public abstract Map<String, Object>[] queryRaw(String query);
}
