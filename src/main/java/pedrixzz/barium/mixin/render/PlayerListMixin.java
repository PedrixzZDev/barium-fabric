package pedrixzz.barium.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(
   value = {PlayerListHud.class},
   priority = 10
)
public abstract class PlayerListMixin {
   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"
)
   )
   private boolean renderHeads(MinecraftClient instance) {
      return true;
   }
}
