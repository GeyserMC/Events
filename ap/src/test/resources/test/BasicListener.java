package test;

import org.geysermc.event.Listener;
import org.geysermc.event.TestEvent;
import org.geysermc.event.subscribe.Subscribe;

@Listener
final class BasicListener {
    @Subscribe
    void testEvent(TestEvent event) {}
}