package com.broadcom.wbi.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.broadcom.wbi.repository.mssql", entityManagerFactoryRef = "mssqlEntityManagerFactory", transactionManagerRef = "mssqlTransactionManager")
@EnableTransactionManagement
public class MSSQLConfig {

    @Bean(name = "mssqlDataSource")
    @ConfigurationProperties(prefix = "mssql.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mssqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("mssqlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.broadcom.wbi.model.mssql")
                .persistenceUnit("mssqlPU")
                .build();
    }

    @Bean(name = "mssqlTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("mssqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

//    private static final String PROPERTY_DATABASE_DRIVER = "mssql.driver";
//    private static final String PROPERTY_DATABASE_USERNAME = "mssql.username";
//    private static final String PROPERTY_DATABASE_PASSWORD = "mssql.password";
//    private static final String PROPERTY_DATABASE_URL = "mssql.url";
//    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "mssql.hibernate.dialect";
//
//    private static final String PROPERTY_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
//    private static final String PROPERTY_HIBERNATE_DDL_AUTO = "hibernate.ddl_auto";
//    private static final String PROPERTY_HIBERNATE_NAMING = "hibernate.naming.strategy";
//
//    @Resource
//    private Environment env;
//
//    @Bean
//    public HibernateJpaVendorAdapter jpaVendorAdapter(){
//        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
//        hibernateJpaVendorAdapter.setShowSql(false);
//        hibernateJpaVendorAdapter.setGenerateDdl(false);
//        hibernateJpaVendorAdapter.setDatabasePlatform(env.getProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
//        return hibernateJpaVendorAdapter;
//    }
//    @Bean
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .url(env.getRequiredProperty(PROPERTY_DATABASE_URL))
//                .driverClassName(env.getRequiredProperty(PROPERTY_DATABASE_DRIVER))
//                .username(env.getRequiredProperty(PROPERTY_DATABASE_USERNAME))
//                .password(env.getRequiredProperty(PROPERTY_DATABASE_PASSWORD))
//                .build();
//    }
//
//    @Bean(name = "mssqlEntityManager")
//    public EntityManager entityManager(){
//        return entityManagerFactory().createEntityManager();
//
//    }
//
//    @Bean
//    public EntityManagerFactory entityManagerFactory() {
//        final HashMap<String, Object> properties = new HashMap<String, Object>();
//        properties.put("hibernate.hbm2ddl.auto", env.getProperty(PROPERTY_HIBERNATE_DDL_AUTO));
//        properties.put("hibernate.show_sql", env.getProperty(PROPERTY_HIBERNATE_SHOW_SQL));
//
//        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
//        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
//        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
//        localContainerEntityManagerFactoryBean.setPersistenceUnitName("default");
//        localContainerEntityManagerFactoryBean.setPackagesToScan("com.broadcom.wbi.model.mssql");
//        localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);
//        localContainerEntityManagerFactoryBean.afterPropertiesSet();
//
//        return localContainerEntityManagerFactoryBean.getObject();
//    }
//
//
//    @Bean(name = "mssqlTransactionManager")
//    public JpaTransactionManager mssqlTransactionManager() {
//        return new JpaTransactionManager(entityManagerFactory());
//    }

}
