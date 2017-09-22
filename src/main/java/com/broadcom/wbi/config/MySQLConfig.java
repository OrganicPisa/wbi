package com.broadcom.wbi.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.broadcom.wbi.repository.mysql", entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
@EnableTransactionManagement
public class MySQLConfig {

//    private static final String PROPERTY_DATABASE_DRIVER = "mysql.driver";
//    private static final String PROPERTY_DATABASE_USERNAME = "mysql.username";
//    private static final String PROPERTY_DATABASE_PASSWORD = "mysql.password";
//    private static final String PROPERTY_DATABASE_URL = "mysql.url";
//    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "mysql.hibernate.dialect";
//
//    private static final String PROPERTY_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
//    private static final String PROPERTY_HIBERNATE_DDL_AUTO = "hibernate.ddl_auto";
//    private static final String PROPERTY_HIBERNATE_NAMING = "hibernate.naming.strategy";

    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "mysql.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mysqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("mysqlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.broadcom.wbi.model.mysql")
                .persistenceUnit("mysqlPU")
                .build();
    }

    @Primary
    @Bean(name = "mysqlTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("mysqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

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
//
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
//    @Bean(name = "mysqlEntityManager")
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
//        localContainerEntityManagerFactoryBean.setPackagesToScan("com.broadcom.wbi.model.mysql");
//        localContainerEntityManagerFactoryBean.setJpaPropertyMap(properties);
//        localContainerEntityManagerFactoryBean.afterPropertiesSet();
//
//        return localContainerEntityManagerFactoryBean.getObject();
//    }
//
//    @Bean(name = "mysqlTransactionManager")
//    public JpaTransactionManager mssqlTransactionManager() {
//        return new JpaTransactionManager(entityManagerFactory());
//    }


}
