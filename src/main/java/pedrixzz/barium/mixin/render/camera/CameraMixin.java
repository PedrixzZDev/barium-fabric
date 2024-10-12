package pedrixzz.barium.mixin.render.camera;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    // Cache do último vetor de posição da câmera
    @Unique
    private Vec3d lastCameraPos = null;

    // Método injetado para otimizar a atualização da posição da câmera
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void optimizeCameraUpdate(double x, double y, double z, CallbackInfo ci) {
        // Cacheando a posição da câmera para evitar recalcular se a posição não mudou
        if (lastCameraPos != null && lastCameraPos.x == x && lastCameraPos.y == y && lastCameraPos.z == z) {
            // Pula a atualização se a posição não mudou
            ci.cancel();
        } else {
            // Atualiza a posição da câmera
            lastCameraPos = new Vec3d(x, y, z);
        }
    }
}
