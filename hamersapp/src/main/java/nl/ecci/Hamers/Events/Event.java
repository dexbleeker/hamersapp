package nl.ecci.Hamers.Events;

public class Event {

    private int id;
    private String title;
    private String beschrijving;
    private String location;
    private String date;
    private String end_time;

    public Event(int id, String title, String beschrijving, String location, String date, String end_time) {
        super();
        this.id = id;
        this.title = title;
        this.beschrijving = beschrijving;
        this.location = location;
        this.date = date;
        this.end_time = end_time;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getEnd_time() {
        return end_time;
    }

}