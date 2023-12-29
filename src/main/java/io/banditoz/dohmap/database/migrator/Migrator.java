package io.banditoz.dohmap.database.migrator;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class Migrator {
    private final ResourcePatternResolver resourcePatternResolver;
    private final PlatformTransactionManager platformTransactionManager;
    private final MigratorMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(Migrator.class);
    private static final String USER = System.getProperty("user.name", "unknown");

    @Autowired
    @SuppressWarnings("unused")
    public Migrator(ResourcePatternResolver resourcePatternResolver,
                    PlatformTransactionManager platformTransactionManager,
                    MigratorMapper migratorMapper) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.platformTransactionManager = platformTransactionManager;
        this.mapper = migratorMapper;
    }

    // cleaner way to do this in a mybatis transaction during @PostConstruct?
    @PostConstruct
    public void go() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            try {
                initializeMigrationTables();
                migrateNonExisting();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private void initializeMigrationTables() throws Exception {
        log.info("Running base migration table SQL file...");
        Resource resource = resourcePatternResolver.getResource("classpath:sql/0000-migration-table.sql");
        String contentAsString = resource.getContentAsString(StandardCharsets.UTF_8);
        List<String> migrationSql = getIndividualSQLStatements(String.join(" ", contentAsString));
        executeBatch(migrationSql, mapper, false);
        log.info("Base migration SQL finished.");
    }

    private void migrateNonExisting() throws Exception {
        List<String> allMigrations = mapper.getAllMigrations()
                .stream()
                .map(Migration::filename)
                .toList();
        List<Resource> migrationsToRun = Arrays.stream(resourcePatternResolver.getResources("classpath:sql/*"))
                .filter(resource -> !resource.getFilename().contains("0000-migration-table.sql"))
                .filter(resource -> !allMigrations.contains(resource.getFilename()))
                .sorted(Comparator.comparing(Resource::getFilename))
                .toList();
        if (migrationsToRun.isEmpty()) {
            log.info("Up-to-date on migrations. Nothing to run.");
            return;
        }
        log.info("Have {} migration(s) to run.", migrationsToRun.size());
        for (Resource resource : migrationsToRun) {
            log.info("Running migration SQL file {}", resource.getFilename());
            String contentAsString = resource.getContentAsString(StandardCharsets.UTF_8);
            List<String> migrationSql = getIndividualSQLStatements(String.join(" ", contentAsString));
            executeBatch(migrationSql, mapper, true);
            mapper.insertMigration(new Migration(resource.getFilename(), USER, null));
        }
        log.info("Migrations finished.");
    }

    private void executeBatch(List<String> sqls, MigratorMapper migratorMapper, boolean doLog) {
        for (String sql : sqls) {
            Integer ret = migratorMapper.execute(sql);
            if (doLog) {
                log.info("Executed SQL \"{}\" with return value {}", sql.replaceAll("[\\t\\n\\r]+", ""), ret);
            }
        }
    }

    private List<String> getIndividualSQLStatements(String statements) {
        return Arrays.asList(statements.split(";"));
    }
}
