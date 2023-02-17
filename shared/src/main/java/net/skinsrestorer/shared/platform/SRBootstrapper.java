/*
 * SkinsRestorer
 *
 * Copyright (C) 2022 SkinsRestorer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.skinsrestorer.shared.platform;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import net.skinsrestorer.shared.interfaces.SRPlatformAdapter;
import net.skinsrestorer.shared.interfaces.SRPlatformLogger;
import net.skinsrestorer.shared.interfaces.SRPlatformStarter;
import net.skinsrestorer.shared.serverinfo.Platform;
import net.skinsrestorer.shared.update.UpdateCheck;
import net.skinsrestorer.shared.utils.log.SRLogLevel;
import net.skinsrestorer.shared.utils.log.SRLogger;

import java.nio.file.Path;
import java.util.function.Function;

public class SRBootstrapper {
    public static void startPlugin(SRPlatformLogger isrLogger, boolean loggerColor,
                                   Function<Injector, SRPlatformAdapter> createAdapter,
                                   Class<? extends UpdateCheck> updateCheck, Class<?> srPlatformClass,
                                   String version, Path dataFolder, Platform platform,
                                   Class<? extends SRPlatformStarter> starterCLass) {
        Injector injector = null;
        try {
            injector = new InjectorBuilder().addDefaultHandlers("net.skinsrestorer").create();

            injector.register(SRLogger.class, new SRLogger(isrLogger, loggerColor));

            injector.register(SRPlatformAdapter.class, createAdapter.apply(injector));
            SRPlugin srPlugin = new SRPlugin(injector, version, dataFolder, platform, updateCheck);
            injector.getSingleton(srPlatformClass);

            srPlugin.startupStart();
            injector.getSingleton(starterCLass).pluginStartup();
            srPlugin.startupEnd();
        } catch (Exception e) {
            e.printStackTrace();
            isrLogger.log(SRLogLevel.SEVERE, "An unexpected error occurred while starting the plugin. Please check the console for more details.");

            if (SRPlugin.isUnitTest()) {
                throw new AssertionError("Failed to start plugin: " + e.getMessage());
            }
        } finally {
            if (injector != null) {
                SRPlugin plugin = injector.getIfAvailable(SRPlugin.class);
                if (plugin != null && !plugin.isUpdaterInitialized() && platform == Platform.BUKKIT) {
                    isrLogger.log(SRLogLevel.INFO, "Updater was not initialized, a error occurred while starting the plugin. Forcing updater to initialize.");
                    try {
                        plugin.initUpdateCheck();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
