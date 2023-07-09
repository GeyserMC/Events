package test;

import java.util.ArrayList;
import java.util.List;
import org.geysermc.event.PostOrder;
import org.geysermc.event.TestEvent;
import org.geysermc.event.bus.impl.util.GeneratedSubscriberInfo;

/**
 * Automatically generated event method references
 */
final class $BasicListener {
    private static final List<GeneratedSubscriberInfo> events;

    static {
        events = new ArrayList<>();
        events.add(new GeneratedSubscriberInfo<>(TestEvent.class, PostOrder.NORMAL, false, BasicListener::testEvent));
    }
}