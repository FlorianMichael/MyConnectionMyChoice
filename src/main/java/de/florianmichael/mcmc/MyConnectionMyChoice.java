/*
 * This file is part of MyConnectionMyChoice - https://github.com/FlorianMichael/MyConnectionMyChoice
 * Copyright (C) 2024-2026 FlorianMichael/EnZaXD <git@florianmichael.de> and contributors
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianmichael.mcmc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MyConnectionMyChoice implements ClientModInitializer {

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
