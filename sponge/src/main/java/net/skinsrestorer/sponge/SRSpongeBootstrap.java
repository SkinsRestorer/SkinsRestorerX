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
package net.skinsrestorer.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.skinsrestorer.shared.plugin.SRBootstrapper;
import net.skinsrestorer.shared.plugin.SRServerPlugin;
import net.skinsrestorer.sponge.logger.Log4jLoggerImpl;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@SuppressWarnings("unused")
@Plugin("skinsrestorer")
public class SRSpongeBootstrap {
    @Inject
    private PluginContainer container;
    @ConfigDir(sharedRoot = false)
    @Inject
    private Path dataFolder;
    @Inject
    private Injector guiceInjector;
    @Inject
    private Logger logger;
    @Inject
    private Game game;
    @Inject
    private MetricsConfigManager metricsConfigManager;

    @Listener
    public void onInitialize(ConstructPluginEvent event) {
        SRBootstrapper.startPlugin(
                runnable -> {
                },
                injector -> {
                    injector.register(Game.class, game);
                    injector.register(PluginContainer.class, container);
                    injector.register(Injector.class, guiceInjector);
                    injector.register(MetricsConfigManager.class, metricsConfigManager);
                },
                new Log4jLoggerImpl(logger),
                false,
                SRSpongeAdapter.class,
                SRServerPlugin.class,
                dataFolder,
                SRSpongeInit.class
        );
    }
}
