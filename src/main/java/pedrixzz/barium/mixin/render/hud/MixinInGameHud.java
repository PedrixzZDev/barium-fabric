package pedrixzz.barium.mixin.render.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    private List<Runnable> cachedRenderTasks = new ArrayList<>();
    private boolean needsRebuild = true;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, float tickDelta, CallbackInfo ci) {
         // Verificar se o cache precisa ser reconstruído
        if (this.needsRebuild) {
            this.cachedRenderTasks = buildRenderTasks();
            this.needsRebuild = false;
        }

    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, float tickDelta, CallbackInfo ci) {
        // Executar as tarefas de renderização cacheadas
        this.cachedRenderTasks.forEach(Runnable::run);
    }
    private List<Runnable> buildRenderTasks() {
        InGameHud hud = (InGameHud) (Object) this; // Obtenha a instância de InGameHud
        List<Runnable> tasks = new ArrayList<>();

        // Exemplo de como você pode adicionar tarefas de renderização otimizadas.
        // É necessário analizar e identificar todos os elementos a serem renderizados e suas condições.
        tasks.add(() -> {
            if (hud.shouldRenderCrosshair()) { // Exemplo, verificar se a mira deve ser renderizada
                hud.renderCrosshair(context);
            }
        });

        tasks.add(() -> {
            hud.renderHotbar(context, tickDelta); // hotbar
        });

        tasks.add(() -> {
            if (hud.overlayMessage != null) { // Exemplo, verificar se há mensagem
                hud.renderOverlayMessage(context);
            }
        });
        // Adicione as renderizações de outros componentes da HUD aqui, com suas próprias condições.

        return tasks;
    }

    // Você também pode adicionar lógica para marcar `needsRebuild` como verdadeiro em eventos que
    // modificam os elementos da HUD, como mudanças no inventário, nível de vida, etc.

    // Exemplo de um método para informar que a HUD precisa ser reconstruida.
    public void markNeedsRebuild() {
       this.needsRebuild = true;
    }
}
