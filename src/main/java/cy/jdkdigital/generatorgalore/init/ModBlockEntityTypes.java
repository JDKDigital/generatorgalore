package cy.jdkdigital.generatorgalore.init;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityTypes
{
//    public static Supplier<BlockEntityType<GeneratorBlockEntity>> GENERATOR;
//    public static void registerGeneratorBlockEntities() {
//        GENERATOR = register("generator", () -> createBlockEntityType((pos, state) -> new GeneratorBlockEntity(pos, state), generator.getBlockSupplier().get())));
//    }

    public static <E extends BlockEntity, T extends BlockEntityType<E>> Supplier<T> register(String id, Supplier<T> supplier) {
        return GeneratorGalore.BLOCK_ENTITIES.register(id, supplier);
    }

    public static <E extends BlockEntity> BlockEntityType<E> createBlockEntityType(BlockEntityType.BlockEntitySupplier<E> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory, blocks).build(null);
    }
}
