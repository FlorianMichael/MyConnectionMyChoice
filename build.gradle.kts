import de.florianmichael.baseproject.*

plugins {
    id("fabric-loom")
    id("de.florianmichael.baseproject.BaseProject")
}

setupProject()
setupFabric()
includeFabricApiModules("fabric-api-base", "fabric-resource-loader-v0", "fabric-screen-api-v1", "fabric-key-binding-api-v1", "fabric-lifecycle-events-v1")

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
}
