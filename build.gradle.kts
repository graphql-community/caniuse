plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "caniuse"
version = "0.1.0"

dependencies {
    implementation(libs.flexmark.all)
    implementation(libs.kotlinx.html)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

val generateHtml = tasks.register("generateHtml", JavaExec::class.java) {
    classpath(kotlin.target.compilations.getByName("main").output.classesDirs)
    classpath(configurations.named("runtimeClasspath"))
    mainClass.set("caniuse.MainKt")
    inputs.files(fileTree("data")).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("data")
}

val copyStaticData = tasks.register("copyStaticData", Copy::class.java) {
    from("static")
    into("build/site/")
}

val generatePageFind = tasks.register("generatePageFind", Exec::class.java) {
    dependsOn(generateHtml)
    commandLine("npx", "pagefind", "--site", "build/site")
}

tasks.register("buildSite") {
    dependsOn(generateHtml, copyStaticData)
}

tasks.register("buildSiteAndPageFind") {
    dependsOn(generatePageFind, copyStaticData)
}

tasks.register("serveSite", JavaExec::class.java) {
    description = "Builds the site and serves it at http://localhost:8080/ (use -Pport=... to change)"
    dependsOn(generateHtml, copyStaticData)
    classpath(kotlin.target.compilations.getByName("main").output.classesDirs)
    classpath(configurations.named("runtimeClasspath"))
    mainClass.set("caniuse.serve.ServeKt")
    args("build/site", providers.gradleProperty("port").getOrElse("8080"))
}
