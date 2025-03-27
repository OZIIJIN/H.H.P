package io.hhplus.tdd;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

public class TestFixtures {
    public static final long DEFAULT_USER_ID = 1L;
    public static final long NOW = 956297L;

    public static UserPoint userPoint(long amount) {
        return new UserPoint(DEFAULT_USER_ID, amount, NOW);
    }

    public static UserPoint userPoint(long userId, long amount) {
        return new UserPoint(userId, amount, NOW);
    }

    public static PointHistory pointHistory(long id, long userId, long amount, TransactionType type) {
        return new PointHistory(id, userId, amount, type, NOW);
    }
}
