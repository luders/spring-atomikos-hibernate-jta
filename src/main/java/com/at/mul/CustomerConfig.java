package com.at.mul;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.at.mul.repository.customer.CustomerDatasourceProperties;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

@Configuration
@DependsOn("transactionManager")
@EnableJpaRepositories(basePackages = "com.at.mul.repository.customer", entityManagerFactoryRef = "customerEntityManager", transactionManagerRef = "transactionManager")
@EnableConfigurationProperties(CustomerDatasourceProperties.class)
public class CustomerConfig {

	@Autowired
	private JpaVendorAdapter jpaVendorAdapter;

	@Autowired
	private CustomerDatasourceProperties customerDatasourceProperties;

	@Bean(name = "customerDataSource", initMethod = "init", destroyMethod = "close")
	public DataSource customerDataSource() {
		MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
		mysqlXaDataSource.setURL(customerDatasourceProperties.getUrl());
		mysqlXaDataSource.setUser(customerDatasourceProperties.getUser());
		mysqlXaDataSource.setPassword(customerDatasourceProperties.getPassword());
		mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);

		AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
		xaDataSource.setXaDataSource(mysqlXaDataSource);
		xaDataSource.setUniqueResourceName("datasource2");
//		xaDataSource.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		return xaDataSource;
	}

	@Bean(name = "customerEntityManager")
	@DependsOn("transactionManager")
	public LocalContainerEntityManagerFactoryBean customerEntityManager() throws Throwable {

		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("hibernate.transaction.jta.platform", AtomikosJtaPlatform.class.getName());
		properties.put("javax.persistence.transactionType", "JTA");

		LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
		entityManager.setJtaDataSource(customerDataSource());
		entityManager.setJpaVendorAdapter(jpaVendorAdapter);
		entityManager.setPackagesToScan("com.at.mul.domain.customer");
		entityManager.setPersistenceUnitName("customerPersistenceUnit");
		entityManager.setJpaPropertyMap(properties);
		return entityManager;
	}

}
