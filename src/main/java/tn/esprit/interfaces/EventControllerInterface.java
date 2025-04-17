package tn.esprit.interfaces;

import tn.esprit.models.Events;
import java.util.List;

public interface EventControllerInterface {
    void initialize();
    void saveEvent(Events event);
    void updateEvent(Events event);
    void deleteEvent(int eventId);
    List<Events> getAllEvents();
    Events getEventById(int eventId);
}