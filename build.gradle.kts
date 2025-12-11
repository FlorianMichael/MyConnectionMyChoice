import de.florianmichael.baseproject.*

plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("de.florianmichael.baseproject.BaseProject")
}

setupProject()
setupFabricRemap()
setupPublishing()
includeFabricApiModules("fabric-api-base", "fabric-resource-loader-v0", "fabric-screen-api-v1", "fabric-key-binding-api-v1", "fabric-lifecycle-events-v1")

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    modCompileOnly("com.terraformersmc:modmenu:15.0.0")
}
