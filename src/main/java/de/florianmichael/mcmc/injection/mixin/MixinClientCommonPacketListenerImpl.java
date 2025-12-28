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

package de.florianmichael.mcmc.injection.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.florianmichael.mcmc.MyConnectionMyChoice;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinClientCommonPacketListenerImpl {

    @Shadow
    protected boolean isTransferring;

    @Shadow
    public abstract void handleTransfer(final ClientboundTransferPacket packet);

    @Unique
    private boolean mcmc$selfInflicted = false;

    @Unique
    private void mcmc$openConfirmScreen(final Consumer<Boolean> action, final String host, final boolean connectionAlive) {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                action::accept,
                Component.translatable("base.mcmc.screen.title"),
                Component.translatable("base.mcmc.screen.description", ChatFormatting.GOLD + host),
                Component.translatable("base.mcmc.screen.accept"),
                connectionAlive ? Component.translatable("base.mcmc.screen.ignore") : Component.translatable("base.mcmc.screen.cancel")
        ));
    }

    @Inject(method = "handleTransfer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void hookConfirmScreen(ClientboundTransferPacket packet, CallbackInfo ci) {
        if (this.mcmc$selfInflicted) {
            this.mcmc$selfInflicted = false;
            return;
        }

        if (MyConnectionMyChoice.instance().keepConnectionInConfirmScreen()) {
            this.isTransferring = false; // Revert state
            mcmc$openConfirmScreen(accepted -> {
                if (accepted) {
                    this.mcmc$selfInflicted = true;
                    this.handleTransfer(packet);
                } else {
                    Minecraft.getInstance().setScreen(null);
                }
            }, packet.host() + ":" + packet.port(), true);
            ci.cancel();
        }
    }

    @WrapOperation(method = "handleTransfer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ConnectScreen;startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;ZLnet/minecraft/client/multiplayer/TransferState;)V"))
    private void hookConfirmScreen(Screen parent, Minecraft minecraft, ServerAddress serverAddress, ServerData serverData, boolean isQuickPlay, TransferState transferState, Operation<Void> original) {
        if (MyConnectionMyChoice.instance().clearCookiesOnTransfer()) {
            transferState.cookies().clear();
        }

        final TransferState state = MyConnectionMyChoice.instance().hideTransferConnectionIntent() ? null : transferState;
        if (!MyConnectionMyChoice.instance().keepConnectionInConfirmScreen()) {
            mcmc$openConfirmScreen(accepted -> {
                if (accepted) {
                    original.call(parent, minecraft, serverAddress, serverData, isQuickPlay, state);
                } else {
                    minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                }
            }, serverAddress.getHost() + ":" + serverAddress.getPort(), false);
            return;
        }

        original.call(parent, minecraft, serverAddress, serverData, isQuickPlay, state);
    }

}
