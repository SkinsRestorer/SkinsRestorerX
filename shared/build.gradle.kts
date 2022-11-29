dependencies {
    implementation(projects.skinsrestorerApi)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")

    implementation("com.google.code.gson:gson:2.10")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.9")
    api("com.github.InventivetalentDev.Spiget-Update:core:1.4.6-SNAPSHOT")

    implementation("org.fusesource.jansi:jansi:2.4.0")

    api("com.github.SkinsRestorer:ConfigMe:beefdbdf7e")
    api("net.skinsrestorer:axiom:1.1.2-SNAPSHOT")
    implementation("com.github.aikar:locales:5f204c3afb")

    compileOnly("co.aikar:acf-core:0.5.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.5")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
