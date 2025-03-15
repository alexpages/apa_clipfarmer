package com.apa.clipfarmer.db;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Class to configure MyBatis, scan mappers and set up the SqlSessionFactory.
 *
 * @author alexpages
 */
@Configuration
@MapperScan("com.apa.clipfarmer.mapper")
public class MyBatisConfig {

    /**
     * Creates and configures the SqlSessionFactory bean for MyBatis.
     *
     * @param dataSource The DataSource to be used by MyBatis.
     * @return Configured SqlSessionFactory instance.
     * @throws Exception if an error occurs while setting up the SqlSessionFactory.
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/*.xml")
        );
        return factoryBean.getObject();
    }
}
