package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeEach
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @ParameterizedTest
    @CsvSource({
            "50, Inactive",
            "90, Active",
            "150, Highly active"
    })
    void testGetUserStatus(long activityTime, String expectedStatus) {
        String userId = "user1";

        when(userAnalyticsService.getTotalActivityTime(userId)).thenReturn(activityTime);
        String status = userStatusService.getUserStatus(userId);

        assertEquals(expectedStatus, status);
        verify(userAnalyticsService).getTotalActivityTime(userId);
    }

    @Test
    void testGetUserLastSessionDate_Success() {
        LocalDateTime logoutTime = LocalDateTime.of(2024, 3, 5, 15, 30);
        UserAnalyticsService.Session session = new UserAnalyticsService.Session(LocalDateTime.of(2024, 3, 5, 14, 0), logoutTime);
        when(userAnalyticsService.getUserSessions("user1")).thenReturn(List.of(session));

        Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user1");
        assertTrue(lastSessionDate.isPresent());
        assertEquals("2024-03-05", lastSessionDate.get());

        verify(userAnalyticsService).getUserSessions("user1");
    }
}
