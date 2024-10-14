package pedrixzz.barium.mixin.render.chunk;

import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengles.GLES30.*;

@Mixin(Chunk.class)
public class ChunkMixin {

    // Adicionando um campo para armazenar um VBO por chunk
    private int vboId = -1;

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        // Inicializa o VBO para o chunk quando ele é carregado
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        // Aqui, você pode carregar os dados dos vértices do chunk para o VBO
        // Exemplo: use um array de floats com as posições
        float[] vertices = getChunkVertexData();
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0); // Desvincula o buffer
    }

    @Inject(method = "unload", at = @At("HEAD"))
    private void onUnload(CallbackInfo ci) {
        // Limpa o VBO quando o chunk é descarregado
        if (vboId != -1) {
            glDeleteBuffers(vboId);
            vboId = -1;
        }
    }

    private float[] getChunkVertexData() {
        // Esta função deve gerar os vértices do chunk
        // Pode ser uma lógica que percorre os blocos e gera os vértices apropriados
        return new float[]{
            // Exemplo de vértices de um quadrado
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f
        };
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(ChunkRenderer renderer, CallbackInfo ci) {
        // Otimiza a renderização usando VBO com OpenGL ES 3.0
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Renderiza os vértices
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4); // Usando um quadrado como exemplo

        // Limpa o estado de OpenGL
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ci.cancel(); // Evita que o Minecraft continue com sua renderização normal
    }
}
