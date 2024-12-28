package pedrixzz.barium.mixin.render.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashMap;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    private BossHealthOverlay bossOverlay;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract ItemRenderer getItemRenderer();


    // Cache para texturas de itens (para otimizar o render de itens)
    private final Map<ResourceLocation, ResourceLocation> textureCache = new HashMap<>();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        ProfilerFiller profiler = this.minecraft.getProfiler();
        profiler.push("InGameHud_opt");
        // Verificação inicial se precisamos renderizar o HUD
        if (minecraft.options.hideGui) {
            ci.cancel(); // Cancela renderização se o HUD estiver escondido
        }
    }


    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(GuiGraphics guiGraphics, float f, CallbackInfo ci){
        ProfilerFiller profiler = this.minecraft.getProfiler();
        profiler.pop(); // Removendo do profiler
    }



    // Removendo renderização desnecessária da barra de boss se ela estiver vazia
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"), cancellable = true)
    private void onRenderBossbar(GuiGraphics guiGraphics, float f, CallbackInfo ci){
        if (this.bossOverlay.getBosses().isEmpty()){
            ci.cancel();
        }
    }


     // Cache de textura de itens, renderização otimizada
    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void onRenderHotbar(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        ItemRenderer itemRenderer = this.getItemRenderer();
        if(itemRenderer == null) return;

        if (minecraft.player == null || minecraft.level == null || minecraft.options == null) return;

        for (int i = 0; i < 9; ++i) {
            var stack = minecraft.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            ResourceLocation itemTexture = itemRenderer.getModel(stack, minecraft.level, null, 0).getParticleIcon();
           if(itemTexture == null) continue;
            ResourceLocation cachedTexture = textureCache.get(itemTexture);
            if (cachedTexture == null) {
                cachedTexture =  itemRenderer.getModel(stack, minecraft.level, null, 0).getParticleIcon();
                textureCache.put(itemTexture, cachedTexture);
                }

        }
    }


}
