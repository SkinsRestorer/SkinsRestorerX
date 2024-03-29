/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.skinsrestorer.shared.api.event;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.event.EventBus;
import net.skinsrestorer.api.event.SkinsRestorerEvent;
import net.skinsrestorer.shared.plugin.SRPlatformAdapter;

import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EventBusImpl implements EventBus {
    private final Queue<EventSubscription<?>> subscriptions = new ConcurrentLinkedQueue<>();
    private final SRPlatformAdapter<Object, ?> platformAdapter;

    @Override
    public <E extends SkinsRestorerEvent> void subscribe(Object plugin, Class<E> eventClass, Consumer<E> listener) {
        platformAdapter.extendLifeTime(plugin, listener);
        subscriptions.add(new EventSubscription<>(new WeakReference<>(plugin), eventClass, new WeakReference<>(listener)));
    }

    @SuppressWarnings("unchecked")
    public void callEvent(SkinsRestorerEvent event) {
        subscriptions.removeIf(subscription -> subscription.plugin().get() == null || subscription.listener().get() == null);

        for (EventSubscription<?> subscription : subscriptions) {
            if (!subscription.eventClass().isAssignableFrom(event.getClass())) {
                continue;
            }

            Consumer<?> listener = subscription.listener().get();

            if (listener == null) {
                continue;
            }

            try {
                ((Consumer<SkinsRestorerEvent>) listener).accept(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
