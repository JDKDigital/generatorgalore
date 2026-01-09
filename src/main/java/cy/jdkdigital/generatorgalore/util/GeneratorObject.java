package cy.jdkdigital.generatorgalore.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import cy.jdkdigital.generatorgalore.init.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class GeneratorObject
{
    private final ResourceLocation id;
    private Supplier<Block> blockSupplier;
    private Supplier<BlockEntityType<GeneratorBlockEntity>> blockEntityType;
    private Supplier<Item> upgradeSupplier;
    private Supplier<MenuType<GeneratorMenu>> menuType;
    private final GeneratorUtil.FuelType fuelType;
    private final int generationRate;
    private int modifiedGenerationRate =  0;
    private final int transferRate;
    private int modifiedConsumptionRate;
    private final int consumptionRate;
    private final int bufferCapacity;
    private final boolean hasChargeSlot;
    private final ResourceLocation fuelTag;
    private final boolean has8x;
    private final boolean has64x;
    private Map<ResourceLocation, GeneratorCreator.Fuel> fuelList;

    public GeneratorObject(ResourceLocation id, GeneratorUtil.FuelType fuelType, int generationRate, int transferRate, int consumptionRate, int bufferCapacity, boolean hasChargeSlot, ResourceLocation fuelTag, boolean has8x, boolean has64x) {
        this.id = id;
        this.fuelType = fuelType;
        this.generationRate = generationRate;
        this.transferRate = transferRate;
        this.consumptionRate = consumptionRate;
        this.bufferCapacity = bufferCapacity;
        this.hasChargeSlot = hasChargeSlot;
        this.fuelTag = fuelTag;
        this.has8x = has8x;
        this.has64x = has64x;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Supplier<Block> getBlockSupplier() {
        return blockSupplier;
    }

    public void setBlockSupplier(Supplier<Block> blockSupplier) {
        this.blockSupplier = blockSupplier;
    }

    public Supplier<BlockEntityType<GeneratorBlockEntity>> getBlockEntityType() {
        return blockEntityType;
    }

    public void setBlockEntityType(Supplier<BlockEntityType<GeneratorBlockEntity>> blockEntityType) {
        this.blockEntityType = blockEntityType;
    }

    public Supplier<Item> getUpgradeSupplier() {
        return upgradeSupplier;
    }

    public void setUpgradeSupplier(Supplier<Item> upgradeSupplier) {
        this.upgradeSupplier = upgradeSupplier;
    }

    public Supplier<MenuType<GeneratorMenu>> getMenuType() {
        return menuType;
    }

    public void setMenuType(Supplier<MenuType<GeneratorMenu>> menuType) {
        this.menuType = menuType;
    }

    public GeneratorUtil.FuelType getFuelType() {
        return this.fuelType;
    }

    public int getGenerationRate() {
        return modifiedGenerationRate > 0 ? modifiedGenerationRate : generationRate;
    }

    public int getConsumptionRate() {
        return modifiedConsumptionRate > 0 ? modifiedConsumptionRate : consumptionRate;
    }

    public void setGenerationRate(int generationRate) {
        this.modifiedGenerationRate = generationRate;
    }

    public void setConsumptionRate(int consumptionRate) {
        this.modifiedConsumptionRate = consumptionRate;
    }

    public int getOriginalGenerationRate() {
        return generationRate;
    }

    public int getOriginalConsumptionRate() {
        return consumptionRate;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public boolean hasChargeSlot() {
        return hasChargeSlot;
    }

    public ResourceLocation getFuelTag() {
        return this.fuelTag;
    }

    public static Codec<GeneratorObject> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").orElse(id).forGetter(GeneratorObject::getId),
            GeneratorUtil.FuelType.CODEC.fieldOf("fuelType").orElse(GeneratorUtil.FuelType.SOLID).forGetter(GeneratorObject::getFuelType),
            Codec.INT.fieldOf("generationRate").forGetter(GeneratorObject::getOriginalGenerationRate),
            Codec.INT.fieldOf("transferRate").forGetter(GeneratorObject::getTransferRate),
            Codec.INT.fieldOf("consumptionRate").forGetter(GeneratorObject::getConsumptionRate),
            Codec.INT.fieldOf("bufferCapacity").forGetter(GeneratorObject::getBufferCapacity),
            Codec.BOOL.fieldOf("hasChargeSlot").orElse(true).forGetter(GeneratorObject::hasChargeSlot),
            ResourceLocation.CODEC.fieldOf("fuelTag").orElse(GeneratorUtil.EMPTY_TAG).forGetter(GeneratorObject::getFuelTag),
            Codec.BOOL.fieldOf("has8x").orElse(true).forGetter(GeneratorObject::has8x),
            Codec.BOOL.fieldOf("has64x").orElse(true).forGetter(GeneratorObject::has64x)
        ).apply(instance, GeneratorObject::new));
    }

    public void setFuelList(Map<ResourceLocation, GeneratorCreator.Fuel> fuelList) {
        this.fuelList = fuelList;
    }

    public Map<ResourceLocation, GeneratorCreator.Fuel> getFuelList() {
        return fuelList;
    }

    public boolean has8x() {
        return has8x;
    }

    public boolean has64x() {
        return has64x;
    }

    public boolean isValidFuelItem(@NotNull ItemStack stack) {
        var fuelData = getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.SOLID_FUEL_MAP);
        if (fuelData != null) {
            return !fuelData.fuels().stream().filter(solidFuel -> solidFuel.item().test(stack)).toList().isEmpty();
        }

        if (!getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
            return stack.is(ModTags.getItemTag(getFuelTag()));
        }
        if (getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
            return stack.get(DataComponents.FOOD) != null;
        }
        if (getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
            return !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
        }
        if (getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
            return stack.getItem() instanceof PotionItem;
        }
        if (getFuelList() != null) {
            return getFuelList().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }

        // Modern approach: Check for burn time using Data Components or fallback to legacy method
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public boolean isValidFuelFluid(FluidStack stack) {
        var fuelData = getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.FLUID_FUEL_MAP);
        if (fuelData != null) {
            return !fuelData.fuels().stream().filter(solidFuel -> solidFuel.fluid().test(stack)).toList().isEmpty();
        }

        if (!getFuelTag().equals(GeneratorUtil.EMPTY_TAG)) {
            return stack.getFluid().is(ModTags.getFluidTag(getFuelTag()));
        }

        return false;
    }

    public Pair<Float, Integer> getGenerationRateForItem(Level level, ItemStack fuelStack) {
        var fuelData = getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.SOLID_FUEL_MAP);
        if (fuelData != null) {
            var validFuels = fuelData.fuels().stream().filter(solidFuel -> solidFuel.item().test(fuelStack)).toList();
            return validFuels.isEmpty() ?
                    new Pair<>((float) getGenerationRate(), (int) (fuelStack.getBurnTime(RecipeType.SMELTING) * getConsumptionRate())) :
                    new Pair<>((float) validFuels.getFirst().generationRate(), (int) (validFuels.getFirst().burnTime() * validFuels.getFirst().consumptionRate()));
        }

        Pair<Float, Integer> rate;
        if (getFuelType().equals(GeneratorUtil.FuelType.ENCHANTMENT)) {
            rate = GeneratorUtil.calculateEnchantmentGenerationRate(this, fuelStack);
        } else if (getFuelType().equals(GeneratorUtil.FuelType.POTION)) {
            rate = GeneratorUtil.calculatePotionGenerationRate(level, this, fuelStack);
        } else if (getFuelType().equals(GeneratorUtil.FuelType.FOOD)) {
            rate = GeneratorUtil.calculateFoodGenerationRate(this, fuelStack);
        } else if (getFuelList() != null) {
            var fuel = getFuelList().get(BuiltInRegistries.ITEM.getKey(fuelStack.getItem()));
            rate = new Pair<>(fuel.rate() > 0 ? fuel.rate() : (float)getOriginalGenerationRate(), fuel.burnTime());
        } else {
            rate = new Pair<>((float) getGenerationRate(), (int) (fuelStack.getBurnTime(RecipeType.SMELTING) * getConsumptionRate()));
        }
        return rate;
    }

    public Pair<Integer, Integer> getGenerationRateForFluid(FluidStack fluidStack) {
        if (isValidFuelFluid(fluidStack)) {
            var fuelData = getBlockSupplier().get().builtInRegistryHolder().getData(GeneratorGalore.FLUID_FUEL_MAP);
            if (fuelData != null) {
                var validFuels = fuelData.fuels().stream().filter(solidFuel -> solidFuel.fluid().test(fluidStack)).toList();
                return validFuels.isEmpty() ?
                        Pair.of(getGenerationRate(), getConsumptionRate()) :
                        Pair.of((int)validFuels.getFirst().generationRate(), (int)validFuels.getFirst().consumptionRate());
            }
            return Pair.of(getGenerationRate(), getConsumptionRate());
        }
        return null;
    }
}
