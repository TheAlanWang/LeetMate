package com.leetmate.platform.notification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final List<NotificationEvent> events = new ArrayList<>();

    public NotificationEvent record(NotificationPayload payload) {
        NotificationEvent event = new NotificationEvent(
                payload.targetUserId(),
                payload.message(),
                OffsetDateTime.now()
        );
        events.add(event);
        return event;
    }

    public List<NotificationEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clear() {
        events.clear();
    }
}
