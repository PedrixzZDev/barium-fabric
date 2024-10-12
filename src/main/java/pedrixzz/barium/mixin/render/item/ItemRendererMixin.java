package pedrixzz.barium.mixin.render.item;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    // Um cache simples para armazenar resultados de renderização
    private final Map<ItemStack, Boolean> renderCache = new HashMap<>();

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void beforeRenderItem(ItemStack stack, CallbackInfo ci) {
        // Se já renderizamos este item recentemente, cancela a renderização
        if (renderCache.containsKey(stack)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void afterRenderItem(ItemStack stack, CallbackInfo ci) {
        // Adiciona o item ao cache após renderizar
        renderCache.put(stack, true);
    }

    // Limpa o cache de vez em quando, pode adicionar lógica para controlar isso
    private void clearCache() {
        renderCache.clear();
    }
}
