package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ExceedMaximumPointException;
import io.hhplus.tdd.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.result.RequestResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    void charge_fail_invalid_point() {
        //db 사용자 포인트 조회 -> 포인트 충전 -> 에러 발생
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, 0L);

        //when & then
        InvalidPointAmountException ex = assertThrows(InvalidPointAmountException.class, () -> pointService.charge(1L, -5000L));

        assertEquals("포인트 충전 금앱은 0보다 커야 합니다.", ex.getMessage());

        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void charge_fail_over_point() {
        //db 사용자 포인트 조회 -> 포인트 충전 -> 에러 발생
        //given
        UserPoint userPoint = new UserPoint(1L, 1000000L, 0L);
        given(userPointTable.selectById(1L)).willReturn(userPoint);

        //when & then
        ExceedMaximumPointException ex = assertThrows(ExceedMaximumPointException.class, () -> pointService.charge(1L, 1000L));

        assertEquals("최대 포인트를 초과하셨습니다.", ex.getMessage());

        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void charge_success() {
        //db 사용자 포인트 조회 -> 포인트 충전 -> 포인트 업데이트 -> 포인트 내역 업데이트
        //given
        Long chargeAmount = 5000L;
        UserPoint existinUserPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(1L, 6000L, System.currentTimeMillis());
        PointHistory pointHistory = new PointHistory(2L, 1L, 5000L, TransactionType.CHARGE, System.currentTimeMillis());

        given(userPointTable.selectById(1L)).willReturn(existinUserPoint);
        given(userPointTable.insertOrUpdate(1L, 6000L)).willReturn(updatedUserPoint);

        //when
        UserPoint result = pointService.charge(1L, 5000L);

        //then
        assertEquals(6000L, result.point());
        then(userPointTable).should().selectById(1L);
        then(userPointTable).should().insertOrUpdate(1L, 6000L);
        then(pointHistoryTable).should(times(1)).insert(eq(1L), eq(5000L), eq(TransactionType.CHARGE), anyLong());

        //호출 순서 검증
        InOrder inOrder = inOrder(userPointTable, pointHistoryTable);

        inOrder.verify(userPointTable).insertOrUpdate(1L, 6000L);
        inOrder.verify(pointHistoryTable).insert(eq(1L), eq(5000L), eq(TransactionType.CHARGE), anyLong());

    }

    @Test
    void use_fail_insufficient_point() {
        long userId = TestFixtures.DEFAULT_USER_ID;
        long useAmount = 5000L;

        given(userPointTable.selectById(userId)).willReturn(TestFixtures.userPoint(3000L));

        InvalidPointAmountException ex = assertThrows(InvalidPointAmountException.class, () -> pointService.use(userId, useAmount));

        assertEquals("보유 포인트가 부족합니다.", ex.getMessage());
        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());
        then(pointHistoryTable).should(never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void user_success() {
        //given
        long userId = TestFixtures.DEFAULT_USER_ID;
        long useAmount = 3000L;

        UserPoint existingUserPoint = TestFixtures.userPoint(5000L);
        UserPoint updatedUserPoint = TestFixtures.userPoint(2000L);

        given(userPointTable.selectById(userId)).willReturn(existingUserPoint);
        given(userPointTable.insertOrUpdate(userId, 2000L)).willReturn(updatedUserPoint);

        //when
        UserPoint result = pointService.use(userId, useAmount);


        //then
        assertEquals(2000L, result.point());

        then(userPointTable).should().selectById(userId);
        then(userPointTable).should().insertOrUpdate(userId, 2000L);
        then(pointHistoryTable).should(times(1)).insert(
                eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong()
        );

        InOrder inOrder = inOrder(userPointTable, pointHistoryTable);
        inOrder.verify(userPointTable).insertOrUpdate(userId, 2000L);
        inOrder.verify(pointHistoryTable).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }

    @Test
    void get_point_success() {
        //given
        long userId = TestFixtures.DEFAULT_USER_ID;
        UserPoint existingUserPoint = TestFixtures.userPoint(5000L);
        given(userPointTable.selectById(userId)).willReturn(existingUserPoint);

        //when
        UserPoint result = pointService.getPoint(userId);

        //then
        assertEquals(5000L, result.point());
        assertEquals(userId, result.id());

        then(userPointTable).should().selectById(userId);
    }

    @Test
    void get_point_history_success() {
        //given
        long userId = TestFixtures.DEFAULT_USER_ID;

        PointHistory history1 = TestFixtures.pointHistory(1L, userId, 2000L, TransactionType.USE);
        PointHistory history2 = TestFixtures.pointHistory(2L, userId, 5000L, TransactionType.CHARGE);

        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(List.of(history1, history2));

        //when
        List<PointHistory> histories = pointService.getPointHistories(userId);

        //then
        assertEquals(2, histories.size());
        assertEquals(2000L, histories.get(0).amount());
        assertEquals(5000L, histories.get(1).amount());

        then(pointHistoryTable).should().selectAllByUserId(userId);
    }

}
