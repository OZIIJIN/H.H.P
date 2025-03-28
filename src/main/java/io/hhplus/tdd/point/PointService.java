package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ExceedMaximumPointException;
import io.hhplus.tdd.exception.InvalidPointAmountException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }


    public UserPoint charge(long userId, long amount) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("포인트 충전 금앱은 0보다 커야 합니다.");
        }

        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint.point() + amount > 1000000L) {
            throw new ExceedMaximumPointException("최대 포인트를 초과하셨습니다.");
        }

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userPoint.id(), userPoint.point() + amount);
        pointHistoryTable.insert(userPoint.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updatedUserPoint;
    }

    public UserPoint use(Long userId, Long useAmount) {
        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint.point() - useAmount < 0) {
            throw new InvalidPointAmountException("보유 포인트가 부족합니다.");
        }

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userPoint.id(), userPoint.point() - useAmount);
        pointHistoryTable.insert(userPoint.id(), useAmount, TransactionType.USE, System.currentTimeMillis());

        return updatedUserPoint;
    }

    public UserPoint getPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
