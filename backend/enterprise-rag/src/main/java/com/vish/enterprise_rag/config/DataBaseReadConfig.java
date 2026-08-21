package com.vish.enterprise_rag.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.vish.enterprise_rag.repository.read", entityManagerFactoryRef = "readEntityManagerFactory", transactionManagerRef = "readTransactionManager")
public class DataBaseReadConfig {

    @Primary
    @Bean(name = "readDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "readDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.read.hikari")
    HikariDataSource readDataSource(@Qualifier("readDataSourceProperties") DataSourceProperties properties) {
        log.info("Read DataSource Properties {}", properties);
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(name = "readEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("readDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.vish.enterprise_rag.entities") // Specify the package where your entity classes are located
                .persistenceUnit("read")
                .build();
    }

    @Primary
    @Bean(name = "readTransactionManager")
    JpaTransactionManager transactionManager(@Qualifier("readEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
