package it.unibo.ds.lab.consensus.client;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Objects;

public class Message {
    private final String username;
    private final String body;
    private final LocalDate date;
    private final LocalTime time;

    public Message(String username, byte[] body) {
        this.username = username;
        this.body = Base64.getEncoder().encodeToString(body);
        this.date = LocalDate.now();
        this.time = LocalTime.now();
    }

    public Message(String username, byte[] body, LocalDate date, LocalTime time) {
        this.username = username;
        this.body = Base64.getEncoder().encodeToString(body);
        this.date = date;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return new String(Base64.getDecoder().decode(this.body));
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(username, message.username) && Objects.equals(body, message.body) && Objects.equals(date, message.date) && Objects.equals(time, message.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, body, date, time);
    }

    @Override
    public String toString() {
        return "Message{" +
                "username='" + username + '\'' +
                ", body=" + this.getBody() +
                ", date=" + date +
                ", time=" + time +
                '}';
    }

    public String toPrettyString() {
        return username +  " [" + date + " " + time + "]:\n> " + this.getBody() + '\n';
    }
}
