package com.github.mdsina.corona.db;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import javax.sql.DataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;

@Factory
public class DbConfigurationFactory {

    @Bean
    public DSLContext dslContext(Configuration configuration) {
        return new DefaultDSLContext(configuration);
    }

    @Bean
    public Configuration configuration(DataSource dataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.setDataSource(dataSource);
        return configuration;
    }
}
