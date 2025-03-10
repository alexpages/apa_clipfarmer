package com.apa.clipfarmer.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

@ExtendWith(MockitoExtension.class)
class MyBatisConfigTest {

    @Mock
    private SqlSessionFactoryBuilder sqlSessionFactoryBuilder;

    @Mock
    private InputStream inputStream;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @InjectMocks
    private MyBatisConfig myBatisConfig;


}
