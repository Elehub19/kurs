package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Query("SELECT t FROM NotificationTask t WHERE t.dateTime = :dateTime")
    List<NotificationTask> findAllByDateTime(@Param("dateTime") LocalDateTime dateTime);
}
