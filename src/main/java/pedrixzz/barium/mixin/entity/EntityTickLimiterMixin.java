package pedrixzz.barium.mixin.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityTickLimiterMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void skipIfOffscreen(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || !self.getWorld().isClient()) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        Box entityBox = self.getBoundingBox();
        if (!entityBox.expand(2.0).intersects(camera.getFocusedEntity().getBoundingBox().expand(20))) {
            ci.cancel(); // pula o tick se estiver longe do campo de visão
        }
    }
}
