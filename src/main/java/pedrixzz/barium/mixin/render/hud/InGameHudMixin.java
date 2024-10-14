package pedrixzz.barium.mixin.render.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MathHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.UnaryOperator;
import net.minecraft.world.effect.StatusEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private float spyglassScale;

    // Sobrescrevendo o método renderMiscOverlays para aplicar otimizações
    @Inject(method = "renderMiscOverlays", at = @At("HEAD"), cancellable = true)
    private void optimizeRenderMiscOverlays(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Reduzindo a taxa de atualização da escala da luneta para economizar processamento
        if (this.client.options.getPerspective().isFirstPerson()) {
            if (this.client.player.isUsingSpyglass()) {
                float f = tickCounter.getLastFrameDuration();
                this.spyglassScale = MathHelper.lerp(0.2F * f, this.spyglassScale, 1.125F);  // Diminuir a intensidade de interpolação
            } else {
                this.spyglassScale = 0.5F;
            }
        }

        // Evitar renderizar efeitos se o jogador não estiver congelado
        if (this.client.player.getFrozenTicks() > 0) {
            this.renderOverlay(context, new Identifier("minecraft", "textures/misc/powder_snow_outline.png"), this.client.player.getFreezingScale());
        }

        // Otimizar a renderização do efeito de náusea, limitando quando não for necessário
        float nauseaIntensity = MathHelper.lerp(tickCounter.getTickDelta(false), this.client.player.prevNauseaIntensity, this.client.player.nauseaIntensity);
        if (nauseaIntensity > 0.0F) {
            if (!this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {
                this.renderPortalOverlay(context, nauseaIntensity);
            } else {
                float distortionScale = this.client.options.getDistortionEffectScale().getValue().floatValue();
                if (distortionScale < 1.0F) {
                    float adjustedNausea = nauseaIntensity * (1.0F - distortionScale);
                    this.renderNauseaOverlay(context, adjustedNausea);
                }
            }
        }

        // O restante do método é executado normalmente
    }

    // Assumindo que você tem métodos renderOverlay, renderPortalOverlay e renderNauseaOverlay definidos ou acessíveis via @Shadow
    @Shadow
    private void renderOverlay(DrawContext context, Identifier texture, float scale) {}

    @Shadow
    private void renderPortalOverlay(DrawContext context, float scale) {}

    @Shadow
    private void renderNauseaOverlay(DrawContext context, float scale) {}
  }
