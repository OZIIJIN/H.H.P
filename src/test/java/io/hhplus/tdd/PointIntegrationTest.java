package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PointIntegrationTest {

    @Autowired
    PointService pointService;

    @Test
    void charge_point_success() {
        pointService.charge(1L, 5000L);
        UserPoint result = pointService.getPoint(1L);
        assertEquals(5000L, result.point());

        List<PointHistory> histories = pointService.getPointHistories(1L);
        assertEquals(1, histories.size());
        assertEquals(TransactionType.CHARGE, histories.get(0).type());
    }

    @Test
    void use_point_success() {
        pointService.charge(1L, 7000L);
        pointService.use(1L, 3000L);
        UserPoint result = pointService.getPoint(1L);
        assertEquals(4000L, result.point());

        List<PointHistory> histories = pointService.getPointHistories(1L);
        assertEquals(2, histories.size());
        assertTrue(histories.stream().anyMatch(h -> h.amount() == 3000L && h.type() == TransactionType.USE));
    }

    @Test
    void get_point_success() {
        pointService.charge(1L, 1000L);
        UserPoint point = pointService.getPoint(1L);
        assertEquals(1000L, point.point());
    }

    @Test
    void get_point_history_success() {
        pointService.charge(1L, 4000L);
        pointService.use(1L, 1000L);

        List<PointHistory> result = pointService.getPointHistories(1L);
        assertEquals(2, result.size());
    }

}
