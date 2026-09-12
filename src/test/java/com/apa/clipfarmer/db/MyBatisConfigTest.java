package com.apa.clipfarmer.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.apa.clipfarmer.mapper.TwitchClipMapper;
import com.apa.clipfarmer.mapper.TwitchHighlightMapper;
import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

class MyBatisConfigTest {

    @Test
    void sqlSessionFactoryRegistersAllMappersFromClasspathXml() throws Exception {
        MyBatisConfig myBatisConfig = new MyBatisConfig();
        DataSource dataSource = mock(DataSource.class);

        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory(dataSource);

        assertThat(sqlSessionFactory).isNotNull();
        assertThat(sqlSessionFactory.getConfiguration().hasMapper(TwitchClipMapper.class)).isTrue();
        assertThat(sqlSessionFactory.getConfiguration().hasMapper(TwitchHighlightMapper.class)).isTrue();
        assertThat(sqlSessionFactory.getConfiguration().hasMapper(TwitchStreamerMapper.class)).isTrue();
    }
}
