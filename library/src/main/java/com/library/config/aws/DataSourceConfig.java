package com.library.config.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.library.services.aws.AwsSecretsService;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${RDS_SECRET_ARN:}")
    private String rdsSecretArn;

    @Primary
    @Bean
    @ConditionalOnProperty(name = "RDS_SECRET_ARN")
    public DataSource dataSource(AwsSecretsService secretsService) {
        log.info("Configuring DataSource from Secrets Manager | arn={}", rdsSecretArn);

        JsonNode secret = secretsService.getSecret(rdsSecretArn);

        String host     = secret.get("host").asText();
        String port     = secret.get("port").asText();
        String dbname   = secret.get("dbname").asText();
        String username = secret.get("username").asText();
        String password = secret.get("password").asText();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbname
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        log.info("DataSource configured successfully | host={} | db={}", host, dbname);
        return new HikariDataSource(config);
    }
}
