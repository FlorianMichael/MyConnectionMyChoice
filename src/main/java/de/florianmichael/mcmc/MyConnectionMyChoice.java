/*
 * This file is part of MyConnectionMyChoice - https://github.com/FlorianMichael/MyConnectionMyChoice
 * Copyright (C) 2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.mcmc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.florianmichael.mcmc.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class MyConnectionMyChoice implements ClientModInitializer, ModMenuApi {

    private /*final*/ static MyConnectionMyChoice INSTANCE;

    private final Logger logger = LogManager.getLogger("MyConnectionMyChoice");
    private final Path config = FabricLoader.getInstance().getConfigDir().resolve("mcmc.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean keepConnectionInConfirmScreen = false;
    private boolean hideTransferConnectionIntent = false;
    private boolean clearCookiesOnTransfer = false;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        if (Files.exists(config)) {
            try {
                final JsonObject object = gson.fromJson(Files.readString(config), JsonObject.class);
                keepConnectionInConfirmScreen = object.get("keepConnectionInConfirmScreen").getAsBoolean();
                hideTransferConnectionIntent = object.get("hideTransferConnectionIntent").getAsBoolean();
                clearCookiesOnTransfer = object.get("clearCookiesOnTransfer").getAsBoolean();

            } catch (Exception e) {
                logger.error("Failed to read file: {}!", config.toString(), e);
            }
        }
    }

    private void save() {
        try {
            final JsonObject object = new JsonObject();
            object.addProperty("keepConnectionInConfirmScreen", keepConnectionInConfirmScreen);
            object.addProperty("hideTransferConnectionIntent", hideTransferConnectionIntent);
            object.addProperty("clearCookiesOnTransfer", clearCookiesOnTransfer);

            Files.writeString(config, gson.toJson(object), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to create file: {}!", config.toString(), e);
        }
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    public boolean keepConnectionInConfirmScreen() {
        return keepConnectionInConfirmScreen;
    }

    public void setKeepConnectionInConfirmScreen(boolean keepConnectionInConfirmScreen) {
        this.keepConnectionInConfirmScreen = keepConnectionInConfirmScreen;
        save();
    }

    public boolean hideTransferConnectionIntent() {
        return hideTransferConnectionIntent;
    }

    public void setHideTransferConnectionIntent(boolean hideTransferConnectionIntent) {
        this.hideTransferConnectionIntent = hideTransferConnectionIntent;
        save();
    }

    public boolean clearCookiesOnTransfer() {
        return clearCookiesOnTransfer;
    }

    public void setClearCookiesOnTransfer(boolean clearCookiesOnTransfer) {
        this.clearCookiesOnTransfer = clearCookiesOnTransfer;
        save();
    }

    public static MyConnectionMyChoice instance() {
        return INSTANCE;
    }

}
