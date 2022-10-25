package cy.jdkdigital.generatorgalore.util;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.ItemStackHandler;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneratorUtil
{
    public static ResourceLocation EMPTY_TAG = new ResourceLocation(GeneratorGalore.MODID, "empty");
    public static String FUEL_SOLID = "SOLID";
    public static String FUEL_FLUID = "FLUID";
    public static String FUEL_FOOD = "FOOD";
    public static final Path LOCK_FILE = createCustomPath("");
    public static final Path GENERATORS = createCustomPath("generators");

    private static Path createCustomPath(String pathName) {
        Path customPath = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), GeneratorGalore.MODID, pathName);
        createDirectory(customPath, pathName);
        return customPath;
    }

    private static void createDirectory(Path path, String dirName) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException e) { //ignored
        } catch (IOException e) {
            GeneratorGalore.LOGGER.error("failed to create \""+dirName+"\" directory");
        }
    }

    public static void replaceGenerator(Level level, BlockPos pos, GeneratorObject generator) {
        BlockState existingGenerator = level.getBlockState(pos);
        BlockState newGenerator = generator.getBlockSupplier().get()
                .defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, existingGenerator.getValue(HorizontalDirectionalBlock.FACING))
                .setValue(BlockStateProperties.LIT, existingGenerator.getValue(BlockStateProperties.LIT));

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GeneratorBlockEntity generatorBlockEntity) {
            CompoundTag tag = generatorBlockEntity.saveWithoutMetadata();
            generatorBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                if (handler instanceof ItemStackHandler itemHandler) {
                    itemHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, ItemStack.EMPTY);
                }
            });

            level.setBlockAndUpdate(pos, newGenerator);
            level.getBlockEntity(pos).load(tag);
        }
    }
}
