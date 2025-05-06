import de.florianmichael.baseproject.*

plugins {
    id("fabric-loom")
    id("de.florianmichael.baseproject.BaseProject")
}

setupProject()
setupFabric()
coreFabricApiModules()

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
}
