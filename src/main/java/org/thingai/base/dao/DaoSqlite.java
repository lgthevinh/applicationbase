package org.thingai.base.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.thingai.base.dao.annotations.DaoColumn;
import org.thingai.base.dao.annotations.DaoTable;
import org.thingai.base.log.ILog;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;

public class DaoSqlite implements Dao {
    private static final String TAG = "DaoSqlite";
    private final HikariDataSource dataSource;
    private final String dbPath;

    public DaoSqlite(String dbPath) {
        this.dbPath = dbPath;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        this.dataSource = new HikariDataSource(config);
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    protected static Field[] getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        // Filter out fields that are not annotated with DaoColumn
        fields.removeIf(field -> !field.isAnnotationPresent(DaoColumn.class));

        return fields.toArray(new Field[0]);
    }

    @Override
    public void initDao(Class[] classes) {
        ILog.d(TAG, "Initializing SQLite DAO with database at: ", dbPath);
        for (Class clazz : classes) {
            DaoTable daoTable = (DaoTable) clazz.getAnnotation(DaoTable.class);
            String query = "CREATE TABLE IF NOT EXISTS ";
            if (daoTable != null) {
                query += daoTable.name();
            } else {
                query += clazz.getSimpleName();
            }
            query += " (";

            Field[] fields = getAllFields(clazz);
            for (Field field : fields) {
                DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                if (daoColumn != null) {
                    if (daoColumn.name().isEmpty()) {
                        query += field.getName() + " ";
                    } else {
                        query += daoColumn.name() + " ";
                    }

                    // column type
                    if (field.getType() == String.class) {
                        query += "TEXT";
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        query += "INTEGER";
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        query += "INTEGER";
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        query += "REAL";
                    } else {
                        query += "BLOB";
                    }

                    // constrains
                    if (daoColumn.primaryKey()) {
                        query += " PRIMARY KEY";
                    }
                    if (!daoColumn.nullable()) {
                        query += " NOT NULL";
                    }
                    if (daoColumn.unique()) {
                        query += " UNIQUE";
                    }
                    if (daoColumn.autoIncrement()) {
                        query += " AUTOINCREMENT";
                    }
                    if (!daoColumn.defaultValue().isEmpty()) {
                        query += " DEFAULT " + daoColumn.defaultValue() + " ";
                    }
                }
                query += ", ";
            }

            // Remove trailing comma and space
            if (query.endsWith(", ")) {
                query = query.substring(0, query.length() - 2);
            }
            query += ");";
            ILog.d(TAG, "Executing query: ", query);

            // execute query
            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    var statement = connection.createStatement();
                    statement.executeUpdate(query);
                } else {
                    throw new IllegalStateException("Database connection is not established.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public <T> void insertOrUpdate(T t) {
        Class<?> clazz = t.getClass();
        if (t == null) {
            throw new IllegalArgumentException("Cannot insert null object.");
        }
        String query = "INSERT OR REPLACE INTO " + clazz.getAnnotation(DaoTable.class).name() + " (";
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        Field[] fields = getAllFields(clazz);
        for (Field field : fields) {
            DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
            if (daoColumn != null) {
                columns.append(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()).append(", ");
                placeholders.append("?, ");
            }
        }

        // Remove trailing comma and space
        if (columns.length() > 0) {
            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);
        }

        query += columns + ") VALUES (" + placeholders + ");";

        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            int index = 1;
            for (Field field : fields) {
                DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                if (daoColumn != null) {
                    field.setAccessible(true);
                    Object value = field.get(t);
                    preparedStatement.setObject(index++, value);
                }
            }
            ILog.d(TAG, "Executing query: ", preparedStatement.toString());

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void insertOrUpdate(Class<T> clazz, T t) {
        if (t == null) {
            throw new IllegalArgumentException("Cannot insert null object.");
        }
        String query = "INSERT OR REPLACE INTO " + clazz.getAnnotation(DaoTable.class).name() + " (";
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        Field[] fields = getAllFields(clazz);
        for (Field field : fields) {
            DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
            if (daoColumn != null) {
                columns.append(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()).append(", ");
                placeholders.append("?, ");
            }
        }

        // Remove trailing comma and space
        if (columns.length() > 0) {
            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);
        }

        query += columns + ") VALUES (" + placeholders + ");";

        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            int index = 1;
            for (Field field : fields) {
                DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                if (daoColumn != null) {
                    field.setAccessible(true);
                    Object value = field.get(t);
                    preparedStatement.setObject(index++, value);
                }
            }
            ILog.d(TAG, "Executing query: ", preparedStatement.toString());

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void insertBatch(T[] t) {
        for (T item : t) {
            insertOrUpdate(item);
        }
    }

    @Override
    public <T> T[] readAll(Class<T> clazz) {
        String query = "SELECT * FROM " + clazz.getAnnotation(DaoTable.class).name() + ";";
        ILog.d(TAG, "Executing query: " + query);
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = getAllFields(clazz);
                for (Field field : fields) {
                    DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                    if (daoColumn != null) {
                        field.setAccessible(true);
                        field.set(instance, resultSet.getObject(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()));
                    }
                }
                results.add(instance);
            }

            T[] array = (T[]) Array.newInstance(clazz, results.size());
            return results.toArray(array);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read all records.");
        }
    }

    @Override
    public <T, K> void delete(Class<T> clazz, K id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot delete with null id.");
        }

        String query = "DELETE FROM " + clazz.getAnnotation(DaoTable.class).name() + " WHERE id = ?;";
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, id);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void delete(T t) {
        String query = "DELETE FROM " + t.getClass().getAnnotation(DaoTable.class).name();
        String whereClause = " WHERE ";
        Field[] fields = getAllFields(t.getClass());
        List<Object> values = new ArrayList<>();
        for (Field field : fields) {
            DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
            if (daoColumn != null && daoColumn.primaryKey()) {
                whereClause += (daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()) + " = ? AND ";
                field.setAccessible(true);
                try {
                    values.add(field.get(t));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        // Remove trailing " AND "
        if (whereClause.endsWith(" AND ")) {
            whereClause = whereClause.substring(0, whereClause.length() - 5);
        }
        query += whereClause + ";";
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < values.size(); i++) {
                preparedStatement.setObject(i + 1, values.get(i));
            }
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void deleteByColumn(Class<T> clazz, String column, String value) {
        String query = "DELETE FROM " + clazz.getAnnotation(DaoTable.class).name() + " WHERE " + column + " = ?;";
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, value);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void deleteAll(Class<T> clazz) {
        String query = "DELETE FROM " + clazz.getAnnotation(DaoTable.class).name() + ";";
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T[] query(Class<T> clazz, String column, String value) {
        if (column == null || value == null) {
            throw new IllegalArgumentException("Cannot read with null column or value.");
        }

        String query = "SELECT * FROM " + clazz.getAnnotation(DaoTable.class).name() + " WHERE " + column + " = ?;";
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, value);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = getAllFields(clazz);
                for (Field field : fields) {
                    DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                    if (daoColumn != null) {
                        field.setAccessible(true);
                        field.set(instance, resultSet.getObject(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()));
                    }
                }
                results.add(instance);
            }

            T[] array = (T[]) Array.newInstance(clazz, results.size());
            return results.toArray(array);
        } catch (Exception e) {
            e.printStackTrace();
            return (T[]) Array.newInstance(clazz, 0);
        }
    }

    @Override
    public <T> T[] query(Class<T> clazz, String[] column, String[] value) {
        if (column == null || value == null) {
            throw new IllegalArgumentException("Cannot read with null column or value.");
        }

        String query = "SELECT * FROM " + clazz.getAnnotation(DaoTable.class).name() + " WHERE ";
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < column.length; i++) {
            whereClause.append(column[i]).append(" = ?");
            if (i < column.length - 1) {
                whereClause.append(" AND ");
            }
        }
        query += whereClause + ";";

        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < value.length; i++) {
                preparedStatement.setObject(i + 1, value[i]);
            }
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = getAllFields(clazz);
                for (Field field : fields) {
                    DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                    if (daoColumn != null) {
                        field.setAccessible(true);
                        field.set(instance, resultSet.getObject(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()));
                    }
                }
                results.add(instance);
            }

            T[] array = (T[]) Array.newInstance(clazz, results.size());
            return results.toArray(array);
        } catch (Exception e) {
            e.printStackTrace();
            return (T[]) Array.newInstance(clazz, 0);
        }
    }

    @Override
    public <T> T[] query(Class<T> clazz, String query) {
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            var resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = getAllFields(clazz);
                for (Field field : fields) {
                    DaoColumn daoColumn = field.getAnnotation(DaoColumn.class);
                    if (daoColumn != null) {
                        field.setAccessible(true);
                        field.set(instance, resultSet.getObject(daoColumn.name().isEmpty() ? field.getName() : daoColumn.name()));
                    }
                }
                results.add(instance);
                T[] array = (T[]) Array.newInstance(clazz, results.size());
                return results.toArray(array);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T[]) Array.newInstance(clazz, 0);
    }

    @Override
    public Map[] queryRaw(String query) {
        try (Connection connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement(query);
            var resultSet = preparedStatement.executeQuery();

            List<Map<String, Object>> results = new ArrayList<>();
            var metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            return results.toArray(new Map[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Map[0];
    }
}

