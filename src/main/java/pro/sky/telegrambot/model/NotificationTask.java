package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private long userId;

    private String message;

    private LocalDateTime dateAndTime;

    public NotificationTask(Long id, long userId, String message, LocalDateTime dateAndTime) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.dateAndTime = dateAndTime;
    }

    public NotificationTask(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(LocalDateTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTask)) return false;
        NotificationTask that = (NotificationTask) o;
        return userId == that.userId && id.equals(that.id) && message.equals(that.message) && dateAndTime.equals(that.dateAndTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, message, dateAndTime);
    }
}
