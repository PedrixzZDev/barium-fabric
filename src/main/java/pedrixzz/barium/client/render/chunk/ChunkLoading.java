package pedrixzz.barium.client.render.chunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe para otimização do carregamento e descarregamento de chunks no Minecraft usando Fabric.
 * Esta classe gerencia o carregamento preguiçoso (lazy loading) e descarregamento automático de chunks.
 */
public class ChunkLoading {

    // Distância máxima em chunks para descarregar chunks que estão fora do alcance do jogador
    private static final int UNLOAD_DISTANCE = 10;

    /**
     * Inicializa os eventos relacionados ao carregamento e descarregamento de chunks.
     * Deve ser chamado no momento de inicialização do mod (por exemplo, em ModInitializer).
     */
    public static void init() {
        // Registro do evento de carregamento de chunks
        ServerChunkEvents.CHUNK_LOAD.register(ChunkLoading::onChunkLoad);
        
        // Registro do evento de descarregamento de chunks
        ServerChunkEvents.CHUNK_UNLOAD.register(ChunkLoading::onChunkUnload);
    }

    /**
     * Método chamado quando um chunk é carregado.
     * @param world O mundo onde o chunk foi carregado.
     * @param chunk O chunk que está sendo carregado.
     */
    private static void onChunkLoad(ServerWorld world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        System.out.println("Chunk carregado: " + chunkPos.x + ", " + chunkPos.z);

        // Realiza o lazy loading dos chunks ao redor
        lazyLoadChunk(world, chunkPos);
    }

    /**
     * Método chamado quando um chunk é descarregado.
     * @param world O mundo onde o chunk foi descarregado.
     * @param chunk O chunk que está sendo descarregado.
     */
    private static void onChunkUnload(ServerWorld world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        System.out.println("Chunk descarregado: " + chunkPos.x + ", " + chunkPos.z);
    }

    /**
     * Implementa o lazy loading, carregando apenas os chunks próximos ao jogador.
     * @param world O mundo onde os chunks estão localizados.
     * @param chunkPos A posição do chunk central (onde o jogador está ou um chunk de interesse).
     */
    private static void lazyLoadChunk(ServerWorld world, ChunkPos chunkPos) {
        // Obtém os chunks próximos ao chunk atual dentro da distância configurada
        Set<ChunkPos> nearbyChunks = getNearbyChunks(world, chunkPos);

        // Força o carregamento dos chunks próximos que ainda não estão carregados
        for (ChunkPos pos : nearbyChunks) {
            if (!world.getChunkManager().isChunkLoaded(pos)) {
                world.getChunkManager().setChunkForced(pos.x, pos.z, true);
                System.out.println("Chunk carregado (lazy): " + pos.x + ", " + pos.z);
            }
        }
    }

    /**
     * Retorna uma lista de chunks dentro de um raio especificado em torno de um chunk central.
     * @param world O mundo onde os chunks estão localizados.
     * @param chunkPos A posição do chunk central.
     * @return Um conjunto de posições de chunks dentro da área definida.
     */
    private static Set<ChunkPos> getNearbyChunks(ServerWorld world, ChunkPos chunkPos) {
        Set<ChunkPos> nearbyChunks = new HashSet<>();

        // Varre os chunks em torno do chunk central
        for (int dx = -UNLOAD_DISTANCE; dx <= UNLOAD_DISTANCE; dx++) {
            for (int dz = -UNLOAD_DISTANCE; dz <= UNLOAD_DISTANCE; dz++) {
                ChunkPos pos = new ChunkPos(chunkPos.x + dx, chunkPos.z + dz);
                nearbyChunks.add(pos);
            }
        }

        return nearbyChunks;
    }

    /**
     * Descarrega automaticamente os chunks que estão fora da distância máxima dos jogadores.
     * @param world O mundo onde os chunks estão localizados.
     */
    public static void autoUnloadChunks(ServerWorld world) {
        // Itera sobre todos os chunks carregados e força o descarregamento dos que estão longe
        for (ChunkHolder chunkHolder : world.getChunkManager().getLoadedChunks()) {
            ChunkPos chunkPos = chunkHolder.getPos();
            if (!isChunkWithinDistance(world, chunkPos)) {
                world.getChunkManager().setChunkForced(chunkPos.x, chunkPos.z, false);
                System.out.println("Chunk descarregado automaticamente: " + chunkPos.x + ", " + chunkPos.z);
            }
        }
    }

    /**
     * Verifica se um chunk está dentro da distância permitida de qualquer jogador.
     * @param world O mundo onde os chunks estão localizados.
     * @param chunkPos A posição do chunk que está sendo verificado.
     * @return True se o chunk está dentro da distância de qualquer jogador, false caso contrário.
     */
    private static boolean isChunkWithinDistance(ServerWorld world, ChunkPos chunkPos) {
        return world.getPlayers().stream().anyMatch(player -> {
            ChunkPos playerChunkPos = new ChunkPos(player.getBlockPos());
            return playerChunkPos.getChebyshevDistance(chunkPos) <= UNLOAD_DISTANCE;
        });
    }
}
