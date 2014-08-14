package com.trein.gtfs.jpa;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.trein.gtfs.jpa.datasource.JpaPropertiesResolver;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableJpaRepositories
@ComponentScan(basePackages = { "com.trein.gtfs.jpa" })
public class JpaRepositoryConfig {
    
    @Bean
    public JpaPropertiesResolver jpaResolver() {
        return new JpaPropertiesResolver();
    }
    
    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        JpaPropertiesResolver resolver = jpaResolver();

        dataSource.setDriverClass(resolver.getDiverClassName());
        dataSource.setJdbcUrl(resolver.getJdbcUrl());
        dataSource.setUser(resolver.getUsername());
        dataSource.setPassword(resolver.getPassword());

        dataSource.setMaxPoolSize(resolver.getMaxPoolSize());
        dataSource.setMinPoolSize(resolver.getMinPoolSize());
        dataSource.setMaxStatements(resolver.getMaxStatements());
        dataSource.setIdleConnectionTestPeriod(resolver.getIdleConnectionTestPeriod());

        return dataSource;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws PropertyVetoException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource());

        /**
         * Renamed to jpa-persistence.xml cause Spring will manage the DataSource and not the
         * container.
         */
        entityManagerFactoryBean.setPersistenceXmlLocation("classpath*:META-INF/jpa-persistence.xml");
        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect());
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return entityManagerFactoryBean;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() throws PropertyVetoException {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }
    
    @Bean
    public PersistenceAnnotationBeanPostProcessor persistenceAnnotation() {
        return new PersistenceAnnotationBeanPostProcessor();
    }
    
}
