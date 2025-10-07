package com.SleepUp.SU.utils.schedule;


import com.SleepUp.SU.utils.Schedule.ScheduledTasks;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ScheduleScheduledTasksTest {

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    @Nested
    class dailyMaintenanceTask {

        @Test
        void dailyMaintenanceTask_shouldExecuteWithoutErrors() {
            assertDoesNotThrow(() -> scheduledTasks.dailyMaintenanceTask());
        }
    }

    @Nested
    class hourlySystemCheck {

        @Test
        void hourlySystemCheck_shouldExecuteWithoutErrors() {
            assertDoesNotThrow(() -> scheduledTasks.hourlySystemCheck());
        }
    }

    @Nested
    class weeklyReportTask {

        @Test
        void weeklyReportTask_shouldExecuteWithoutErrors() {
            assertDoesNotThrow(() -> scheduledTasks.weeklyReportTask());
        }
    }

    @Nested
    class allScheduledTasks {

        @Test
        void allScheduledTasks_shouldExecuteMultipleTimes() {
            assertDoesNotThrow(() -> {
                scheduledTasks.dailyMaintenanceTask();
                scheduledTasks.hourlySystemCheck();
                scheduledTasks.weeklyReportTask();

                // Execute again to verify idempotency
                scheduledTasks.dailyMaintenanceTask();
                scheduledTasks.hourlySystemCheck();
                scheduledTasks.weeklyReportTask();
            });
        }
    }
}