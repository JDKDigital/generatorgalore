package cy.jdkdigital.generatorgalore.util;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import cy.jdkdigital.generatorgalore.common.datamap.PotionComponentIngredient;
import cy.jdkdigital.generatorgalore.common.datamap.SolidFuelMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    public static ResourceLocation EMPTY_TAG = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "empty");
    public static String FUEL_SOLID = "SOLID";
    public static String FUEL_FLUID = "FLUID";
    public static String FUEL_FOOD = "FOOD";
    public static String FUEL_ENCHANTMENT = "ENCHANTMENT";
    public static final Path LOCK_FILE = createCustomPath("");
    public static final Path GENERATORS = createCustomPath("generator");

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
            CompoundTag tag = generatorBlockEntity.saveWithoutMetadata(level.registryAccess());

            if (generatorBlockEntity.inventoryHandler instanceof ItemStackHandler itemHandler) {
                itemHandler.setStackInSlot(GeneratorMenu.SLOT_FUEL, ItemStack.EMPTY);
                itemHandler.setStackInSlot(GeneratorMenu.SLOT_CHARGE, ItemStack.EMPTY);
            }

            level.setBlockAndUpdate(pos, newGenerator);
            level.getBlockEntity(pos).loadCustomOnly(tag, level.registryAccess());
        }
    }

    public static Pair<Float, Integer> calculateFoodGenerationRate(GeneratorObject generator, ItemStack stack) {
        FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        if (foodProperties != null) {
            int value = foodProperties.nutrition();
            float saturation = foodProperties.saturation();
            double totalRF = value * saturation * 8000;

            return Pair.of((float) (value * generator.getOriginalGenerationRate()), (int) (totalRF / generator.getGenerationRate()));
        }
        return Pair.of((float) generator.getGenerationRate(), (int) generator.getConsumptionRate());
    }

    public static Pair<Float, Integer> calculateEnchantmentGenerationRate(GeneratorObject generator, ItemStack stack) {
        if (stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem) {
            double totalRF = 0;
            var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            for(var entry : enchantments.entrySet()) {
                var enchantment = entry.getKey().value();
                float level = (float) entry.getValue();
                float max =(float) enchantment.getMaxLevel();
                float min = (float) enchantment.getMinCost(entry.getValue());
                float weight = enchantment.getWeight();

                totalRF = totalRF + Math.abs(Math.sqrt(Math.min(level + 1d, max) / max) * Math.pow(max, 2) * (level + 1) * (min/Math.sqrt(weight))) * 400;
            }

            return Pair.of((float) generator.getGenerationRate(), (int) (totalRF / generator.getGenerationRate()));
        }
        return Pair.of((float) generator.getGenerationRate(), (int) generator.getConsumptionRate());
    }

    public static Pair<Float, Integer> calculatePotionGenerationRate(Level level, GeneratorObject generator, ItemStack stack) {
        List<SolidFuelMap.SolidFuel> fuels = GeneratorUtil.getPotionFuels(level.registryAccess());
        for (SolidFuelMap.SolidFuel fuel : fuels) {
            if (fuel.item().test(stack)) {
                return Pair.of((float) fuel.generationRate(), (int) fuel.consumptionRate());
            }
        }
        return Pair.of(0f, 1);
    }

    public static List<SolidFuelMap.SolidFuel> getPotionFuels(HolderLookup.Provider provider) {
        List<SolidFuelMap.SolidFuel> potionFuels = new ArrayList<>();
        provider.lookup(Registries.POTION).ifPresent(
            potionRegistryLookup -> {
                generatePotionEffectTypes(
                        potionFuels,
                        potionRegistryLookup,
                        Items.POTION
                );
                generatePotionEffectTypes(
                        potionFuels,
                        potionRegistryLookup,
                        Items.SPLASH_POTION
                );
                generatePotionEffectTypes(
                        potionFuels,
                        potionRegistryLookup,
                        Items.LINGERING_POTION
                );
            }
        );
        return potionFuels;
    }

    private static void generatePotionEffectTypes(List<SolidFuelMap.SolidFuel> potionFuels, HolderLookup<Potion> potions, Item item) {
        potions.listElements()
                .map(potion -> {
                    var stack = PotionContents.createItemStack(item, potion);
                    int burnTime = 0;
                    for (MobEffectInstance mobEffectInstance : stack.get(DataComponents.POTION_CONTENTS).getAllEffects()) {
                        burnTime += 3 * (1 + mobEffectInstance.getAmplifier()) * (mobEffectInstance.getDuration() * 3) + (potion.getKey().location().getPath().contains("strong_") ? 6000 : 0);
                    }
                    return new SolidFuelMap.SolidFuel(PotionComponentIngredient.of(stack), 1.0f, burnTime, 8);
                })
                .forEach(potionFuels::add);
    }
}
