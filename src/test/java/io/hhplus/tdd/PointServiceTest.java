package io.hhplus.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointTable);
    }

    @Test
    void charge_whenAmountIsNegative_thenThrowException() {
        //db 사용자 조회 -> 포인트 충전 -> 에러 발생
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, 0L);
        given(userPointTable.selectById(1L)).willReturn(userPoint);

        //when & then
        InvalidPointAmountException ex = assertThrows(InvalidPointAmountException.class, () ->
                pointService.charge(1L, -5000L));

        assertEquals("포인트 충전 금앱은 0보다 커야 합니다.", ex.getMessage());

        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());
    }

}
