package com.oa;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@SpringBootApplication
public class OaFlowApplication {

    private static final Logger log = LoggerFactory.getLogger(OaFlowApplication.class);

    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/oa_flow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "root";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) {
        initializeFlowableDatabase();
        SpringApplication.run(OaFlowApplication.class, args);
    }

    private static void initializeFlowableDatabase() {
        try {
            log.info("========================================");
            log.info("开始预初始化 Flowable 数据库...");
            log.info("数据库 URL: {}", DEFAULT_DB_URL);
            log.info("数据库用户: {}", DEFAULT_DB_USERNAME);
            log.info("========================================");

            DataSource dataSource = createDataSource(DEFAULT_DB_URL, DEFAULT_DB_USERNAME, DEFAULT_DB_PASSWORD, DEFAULT_DB_DRIVER);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            boolean flowableTablesExist = checkFlowableTablesExist(jdbcTemplate);

            if (!flowableTablesExist) {
                log.info("Flowable 表不存在，开始创建...");
                createFlowableTables(dataSource);
                log.info("========================================");
                log.info("Flowable 表创建成功！");
                log.info("========================================");
            } else {
                log.info("Flowable 表已存在，跳过预初始化");
            }

        } catch (Exception e) {
            log.warn("========================================");
            log.warn("Flowable 数据库预初始化失败");
            log.warn("错误信息: {}", e.getMessage());
            log.warn("========================================");
            log.info("如果您的数据库配置不是默认值，请手动修改 OaFlowApplication.java 中的数据库连接信息");
            log.info("或者手动执行 Flowable 数据库初始化脚本");
            log.info("系统将继续尝试使用 Spring Boot 自动配置...");
        }
    }

    private static DataSource createDataSource(String url, String username, String password, String driver) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private static boolean checkFlowableTablesExist(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM ACT_GE_PROPERTY LIMIT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void createFlowableTables(DataSource dataSource) {
        StandaloneProcessEngineConfiguration config = new StandaloneProcessEngineConfiguration();
        config.setDataSource(dataSource);
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        config.setDbHistoryUsed(true);
        config.setAsyncExecutorActivate(false);

        log.info("使用 schemaUpdate 策略: {}", ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine tempEngine = config.buildProcessEngine();
        tempEngine.close();
    }
}
