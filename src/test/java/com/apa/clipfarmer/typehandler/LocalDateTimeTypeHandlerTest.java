package com.apa.clipfarmer.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LocalDateTimeTypeHandler}.
 *
 * @author alexpages
 */
@ExtendWith(MockitoExtension.class)
class LocalDateTimeTypeHandlerTest {

    @InjectMocks
    private LocalDateTimeTypeHandler handler;

    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private CallableStatement callableStatement;

    private static final LocalDateTime SAMPLE = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

    @Test
    void setNonNullParameter_setsTimestampOnPreparedStatement() throws Exception {
        handler.setNonNullParameter(preparedStatement, 1, SAMPLE, null);

        verify(preparedStatement).setTimestamp(1, Timestamp.valueOf(SAMPLE));
    }

    @Test
    void getNullableResult_byColumnName_convertsTimestampToLocalDateTime() throws Exception {
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(SAMPLE));

        LocalDateTime result = handler.getNullableResult(resultSet, "created_at");

        assertThat(result).isEqualTo(SAMPLE);
    }

    @Test
    void getNullableResult_byColumnName_nullTimestamp_returnsNull() throws Exception {
        when(resultSet.getTimestamp("created_at")).thenReturn(null);

        LocalDateTime result = handler.getNullableResult(resultSet, "created_at");

        assertThat(result).isNull();
    }

    @Test
    void getNullableResult_byColumnIndex_convertsTimestampToLocalDateTime() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.valueOf(SAMPLE));

        LocalDateTime result = handler.getNullableResult(resultSet, 1);

        assertThat(result).isEqualTo(SAMPLE);
    }

    @Test
    void getNullableResult_byColumnIndex_nullTimestamp_returnsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        LocalDateTime result = handler.getNullableResult(resultSet, 1);

        assertThat(result).isNull();
    }

    @Test
    void getNullableResult_callableStatement_convertsTimestampToLocalDateTime() throws Exception {
        when(callableStatement.getTimestamp(1)).thenReturn(Timestamp.valueOf(SAMPLE));

        LocalDateTime result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isEqualTo(SAMPLE);
    }

    @Test
    void getNullableResult_callableStatement_nullTimestamp_returnsNull() throws Exception {
        when(callableStatement.getTimestamp(1)).thenReturn(null);

        LocalDateTime result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isNull();
    }
}
