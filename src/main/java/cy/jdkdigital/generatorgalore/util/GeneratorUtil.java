package cy.jdkdigital.generatorgalore.util;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GeneratorUtil
{
    public enum FuelType implements StringRepresentable
    {
        SOLID("SOLID"),
        FLUID("FLUID"),
        FOOD("FOOD"),
        ENCHANTMENT("ENCHANTMENT"),
        POTION("POTION");

        private final String key;

        public static EnumCodec<FuelType> CODEC = StringRepresentable.fromEnum(GeneratorUtil.FuelType::values);

        FuelType(String key) {
            this.key = key;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }
    }
    public static ResourceLocation EMPTY_TAG = new ResourceLocation(GeneratorGalore.MODID, "empty");
    public static String FUEL_SOLID = "SOLID";
    public static String FUEL_FLUID = "FLUID";
    public static String FUEL_FOOD = "FOOD";
    public static String FUEL_ENCHANTMENT = "ENCHANTMENT";
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

    public static Pair<Float, Integer> calculateFoodGenerationRate(GeneratorObject generator, ItemStack stack) {
        FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        if (foodProperties != null) {
            int value = foodProperties.getNutrition();
            float saturation = foodProperties.getSaturationModifier();
            double totalRF = value * saturation * 8000;

            return Pair.of((float) (value * generator.getOriginalGenerationRate()), (int) (totalRF / generator.getGenerationRate()));
        }
        return Pair.of((float) generator.getGenerationRate(), (int) generator.getConsumptionRate());
    }

    public static Pair<Float, Integer> calculateEnchantmentGenerationRate(GeneratorObject generator, ItemStack stack) {
        if (stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem) {
            double totalRF = 0;
            var enchantments = EnchantmentHelper.getEnchantments(stack);
            for(var entry : enchantments.entrySet()) {
                var enchantment = entry.getKey();
                float level = (float) entry.getValue();
                float max =(float) enchantment.getMaxLevel();
                float min = (float) enchantment.getMinCost(entry.getValue());
                float weight = switch(enchantment.getRarity()) {
                    case COMMON: yield 10f;
                    case UNCOMMON: yield 5f;
                    case RARE: yield 2f;
                    case VERY_RARE: yield 1f;
                };

                totalRF = totalRF + Math.abs(Math.sqrt(Math.min(level + 1d, max) / max) * Math.pow(max, 2) * (level + 1) * (min/Math.sqrt(weight))) * 400;
            }

            return Pair.of((float) generator.getGenerationRate(), (int) (totalRF / generator.getGenerationRate()));
        }
        return Pair.of((float) generator.getGenerationRate(), (int) generator.getConsumptionRate());
    }

    public static Pair<Float, Integer> calculatePotionGenerationRate(GeneratorObject generator, ItemStack stack) {
        int steps = getBrewingSteps(PotionUtil.getUniquePotionName(stack), new HashSet<>());
        double totalRF = 100 * Math.pow(4, steps);
        return Pair.of((float) generator.getGenerationRate(), (int) (totalRF / generator.getGenerationRate()));
    }

    private static int getBrewingSteps(String potionOutputUid, Set<String> previousSteps) {
        var potionMap = PotionUtil.getPotionMap();

        Integer cachedBrewingSteps = PotionUtil.brewingStepCache.get(potionOutputUid);
        if (cachedBrewingSteps != null) {
            return cachedBrewingSteps;
        }

        if (!previousSteps.add(potionOutputUid)) {
            return Integer.MAX_VALUE;
        }

        Collection<String> prevPotions = potionMap.get(potionOutputUid);
        int minPrevSteps = prevPotions.stream()
                .mapToInt(prevPotion -> getBrewingSteps(prevPotion, previousSteps))
                .min()
                .orElse(Integer.MAX_VALUE);

        int brewingSteps = minPrevSteps == Integer.MAX_VALUE ? Integer.MAX_VALUE : minPrevSteps + 1;
        PotionUtil.brewingStepCache.put(potionOutputUid, brewingSteps);
        return brewingSteps;
    }
}
