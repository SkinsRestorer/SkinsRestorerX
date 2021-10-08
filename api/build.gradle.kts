plugins {
    id("java-library")
    id("net.kyori.blossom")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.11")
    compileOnly("net.md-5:bungeecord-api:1.17-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-proxy:1.17-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly("org.spongepowered:spongeapi:7.3.0")
}

blossom {
    tokenReplacementsGlobalLocations.clear();
    replaceTokenIn("src/main/java/net/skinsrestorer/api/builddata/BuildData.java")

    replaceToken("{version}", version)
    replaceToken("{description}", rootProject.description)
    replaceToken("{url}", "https://skinsrestorer.net/")
}