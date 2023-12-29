package io.banditoz.dohmap.database.migrator;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MigratorMapper {
    class PureSqlProvider {
        public String sql(String sql) {
            return sql;
        }
    }

    /** Execute freeform SQL. */
    @SelectProvider(type = PureSqlProvider.class, method = "sql")
    Integer execute(String query);

    @Select("""
            SELECT filename, executed_by, executed_when
            FROM databasechangelog;
            """)
    List<Migration> getAllMigrations();

    @Insert("""
            INSERT INTO databasechangelog
            (filename, executed_by)
            VALUES
            (#{filename}, #{executedBy});
            """)
    Integer insertMigration(Migration migration);

}
