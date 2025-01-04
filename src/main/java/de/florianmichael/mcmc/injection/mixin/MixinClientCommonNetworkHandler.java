/*
 * This file is part of MyConnectionMyChoice - https://github.com/FlorianMichael/MyConnectionMyChoice
 * Copyright (C) 2024-2025 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class MixinClientCommonNetworkHandler {

    @Shadow
    protected boolean transferring;

    @Shadow public abstract void onServerTransfer(ServerTransferS2CPacket packet);

    @Unique
    private boolean mcmc$selfInflicted = false;

    @Unique
    private void mcmc$openConfirmScreen(final Consumer<Boolean> action, final String host, final boolean connectionAlive) {
        MinecraftClient.getInstance().setScreen(new ConfirmScreen(
                action::accept,
                Text.translatable("base.mcmc.screen.title"),
                Text.translatable("base.mcmc.screen.description", Formatting.GOLD + host),
                Text.translatable("base.mcmc.screen.accept"),
                connectionAlive ? Text.translatable("base.mcmc.screen.ignore") : Text.translatable("base.mcmc.screen.cancel")
        ));
    }

    @Inject(method = "onServerTransfer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;disconnect(Lnet/minecraft/text/Text;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void hookConfirmScreen(ServerTransferS2CPacket packet, CallbackInfo ci) {
        if (this.mcmc$selfInflicted) {
            this.mcmc$selfInflicted = false;
            return;
        }

        if (MyConnectionMyChoice.instance().keepConnectionInConfirmScreen()) {
            this.transferring = false; // Revert state
            mcmc$openConfirmScreen(accepted -> {
                if (accepted) {
                    this.mcmc$selfInflicted = true;
                    this.onServerTransfer(packet);
                } else {
                    MinecraftClient.getInstance().setScreen(null);
                }
            }, packet.host() + ":" + packet.port(), true);
            ci.cancel();
        }
    }

    @WrapOperation(method = "onServerTransfer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/ConnectScreen;connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;ZLnet/minecraft/client/network/CookieStorage;)V"))
    private void hookConfirmScreen(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay, CookieStorage cookieStorage, Operation<Void> original) {
        if (MyConnectionMyChoice.instance().clearCookiesOnTransfer()) {
            cookieStorage.cookies().clear();
        }

        final CookieStorage storage = MyConnectionMyChoice.instance().hideTransferConnectionIntent() ? null : cookieStorage;
        if (!MyConnectionMyChoice.instance().keepConnectionInConfirmScreen()) {
            mcmc$openConfirmScreen(accepted -> {
                if (accepted) {
                    original.call(screen, client, address, info, quickPlay, storage);
                } else {
                    client.setScreen(new MultiplayerScreen(new TitleScreen()));
                }
            }, address.getAddress() + ":" + address.getPort(), false);
            return;
        }

        original.call(screen, client, address, info, quickPlay, storage);
    }

}
