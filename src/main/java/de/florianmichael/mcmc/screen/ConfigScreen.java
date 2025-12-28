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

package de.florianmichael.mcmc.screen;

import com.mojang.datafixers.util.Pair;
import de.florianmichael.mcmc.MyConnectionMyChoice;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 250;
    private static final int PADDING = 3;
    private static final Map<String, Pair<Supplier<Boolean>, Consumer<Boolean>>> settings = new HashMap<>();

    static {
        final MyConnectionMyChoice instance = MyConnectionMyChoice.instance();
        settings.put("keepConnectionInConfirmScreen", new Pair<>(instance::keepConnectionInConfirmScreen, instance::setKeepConnectionInConfirmScreen));
        settings.put("hideTransferConnectionIntent", new Pair<>(instance::hideTransferConnectionIntent, instance::setHideTransferConnectionIntent));
        settings.put("clearCookiesOnTransfer", new Pair<>(instance::clearCookiesOnTransfer, instance::setClearCookiesOnTransfer));
    }

    private final Screen parent;

    public ConfigScreen(final Screen parent) {
        super(Component.translatable("settings.mcmc.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int y = 50;
        for (Map.Entry<String, Pair<Supplier<Boolean>, Consumer<Boolean>>> entry : settings.entrySet()) {
            final String key = entry.getKey().toLowerCase(Locale.ROOT);

            final Component label = Component.translatable("settings.mcmc." + key);
            final Component description = Component.translatable("settings.mcmc." + key + ".description");

            final Supplier<Boolean> supplier = entry.getValue().getFirst();
            addRenderableWidget(Button
                    .builder(
                            getButtonComponent(label, supplier.get()),
                            button -> {
                                entry.getValue().getSecond().accept(!supplier.get());
                                button.setMessage(getButtonComponent(label, supplier.get()));
                            }
                    )
                    .pos(this.width / 2 - BUTTON_WIDTH / 2, y)
                    .size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT)
                    .tooltip(Tooltip.create(description))
                    .build());

            y += Button.DEFAULT_HEIGHT + PADDING;
        }

        addRenderableWidget(Button
                .builder(Component.literal("<-"), button -> minecraft.setScreen(parent))
                .pos(PADDING, this.height - Button.DEFAULT_HEIGHT - PADDING)
                .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                .build());
    }

    private Component getButtonComponent(final Component label, final boolean value) {
        final Component on = Component.translatable("base.mcmc.on");
        final Component off = Component.translatable("base.mcmc.off");

        return Component
                .literal("")
                .append(label)
                .append(": ")
                .append(Component.literal("")
                        .append(value ? on : off)
                        .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED)
                );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        final Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.scale(2.0F, 2.0F);
        guiGraphics.drawString(font, title, this.width / 4 - font.width(title) / 2, 5, -1, true);
        pose.popMatrix();
    }

}
