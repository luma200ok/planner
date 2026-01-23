package com.planner.dayclose.application;

import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;
import com.planner.task.domain.TaskTemplate;
import com.planner.task.event.TaskEvent;
import com.planner.task.repository.TaskEventRepository;
import com.planner.task.repository.TaskRepository;
import com.planner.task.repository.TaskTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.planner.dayclose.application.dto.DayCloseDto.*;
import static com.planner.task.event.TaskEventType.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DayCloseService {

    private final TaskRepository taskRepository;
    private final TaskTemplateRepository templateRepository;
    private final TaskEventRepository eventRepository;

    public DayCloseResponse close(LocalDate date, boolean carryOver, LocalDate carryTo) {
        LocalDate nextDate = (carryTo != null) ? carryTo : date.plusDays(1);
        LocalDateTime now = LocalDateTime.now();

        List<Task> planned = taskRepository.findAllByScheduledDateAndStatus(date, TaskStatus.PLANNED);
        int plannedFound = planned.size();

        int autoSkippedRecurring =0;
        int rollOverOneOff = 0;
        int autoSkippedOneOff = 0;
        int generatedNext = 0;

        // 미완료 마감일 정리
        for (Task t : planned) {
            boolean recurring = (t.getTemplate() != null);

            if (recurring) {
                t.skip(now);
                autoSkippedRecurring++;

                eventRepository.save(TaskEvent.of(
                        t.getId(), AUTO_SKIP, now, null,
                        "date= " + date + ", reason= day-close"
                ));
            } else {
                // 단발성: carryOver면 이월, 아니면 AUTO_SKIP
                if (carryOver) {
                    LocalDate from = t.getScheduledDate();
                    t.moveScheduledDate(nextDate);
                    rollOverOneOff++;

                    eventRepository.save(TaskEvent.of(
                            t.getId(), ROLLOVER, now, null,
                            "from" + from + ",to" + nextDate
                    ));

                } else {
                    t.skip(now);
                    autoSkippedOneOff++;
                    eventRepository.save(TaskEvent.of(
                            t.getId(), AUTO_SKIP, now, null,
                            "date= " + date + ", reason= day-close"
                    ));
                }
            }
        }

        List<TaskTemplate> templates = templateRepository.findAll();
        for (TaskTemplate template : templates) {
            if (!template.matches(nextDate)) continue;

            boolean exists = taskRepository.findByTemplateIdAndScheduledDate(template.getId(), nextDate).isPresent();
            if (exists) continue;

            Task task = new Task(template.getTitle(), nextDate);
            task.attachTemplate(template);
            Task save = taskRepository.save(task);
            generatedNext++;

            eventRepository.save(TaskEvent.of(
                    save.getId(), GENERATE, now, null,
                    "templateId=" + template.getId() + ",date=" + date
            ));
        }

        return new DayCloseResponse(
                date,
                nextDate,
                carryOver,
                plannedFound,
                autoSkippedRecurring,
                rollOverOneOff,
                autoSkippedOneOff,
                generatedNext
        );
    }
}
