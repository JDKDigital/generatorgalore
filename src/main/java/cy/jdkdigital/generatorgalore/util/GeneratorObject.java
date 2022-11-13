package cy.jdkdigital.generatorgalore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.function.Supplier;

public class GeneratorObject
{
    private final ResourceLocation id;
    private Supplier<Block> blockSupplier;
    private Supplier<BlockEntityType<GeneratorBlockEntity>> blockEntityType;
    private Supplier<MenuType<GeneratorMenu>> menuType;
    private final GeneratorUtil.FuelType fuelType;
    private final double generationRate;
    private double modifiedGenerationRate =  0;
    private final double transferRate;
    private final double consumptionRate;
    private final int bufferCapacity;
    private final ResourceLocation fuelTag;
    private Map<ResourceLocation, GeneratorCreator.Fuel> fuelList;

    public GeneratorObject(ResourceLocation id, GeneratorUtil.FuelType fuelType, double generationRate, double transferRate, double consumptionRate, int bufferCapacity, ResourceLocation fuelTag) {
        this.id = id;
        this.fuelType = fuelType;
        this.generationRate = generationRate;
        this.transferRate = transferRate;
        this.consumptionRate = consumptionRate;
        this.bufferCapacity = bufferCapacity;
        this.fuelTag = fuelTag;
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

    public Supplier<MenuType<GeneratorMenu>> getMenuType() {
        return menuType;
    }

    public void setMenuType(Supplier<MenuType<GeneratorMenu>> menuType) {
        this.menuType = menuType;
    }

    public GeneratorUtil.FuelType getFuelType() {
        return this.fuelType;
    }

    public double getGenerationRate() {
        return modifiedGenerationRate > 0 ? modifiedGenerationRate : generationRate;
    }

    public double getOriginalGenerationRate() {
        return generationRate;
    }

    public double getTransferRate() {
        return transferRate;
    }

    public double getConsumptionRate() {
        return consumptionRate;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public ResourceLocation getFuelTag() {
        return this.fuelTag;
    }

    public static Codec<GeneratorObject> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").orElse(id).forGetter(GeneratorObject::getId),
            GeneratorUtil.FuelType.CODEC.fieldOf("fuelType").orElse(GeneratorUtil.FuelType.SOLID).forGetter(GeneratorObject::getFuelType),
            Codec.DOUBLE.fieldOf("generationRate").forGetter(GeneratorObject::getOriginalGenerationRate),
            Codec.DOUBLE.fieldOf("transferRate").forGetter(GeneratorObject::getTransferRate),
            Codec.DOUBLE.fieldOf("consumptionRate").forGetter(GeneratorObject::getConsumptionRate),
            Codec.INT.fieldOf("bufferCapacity").forGetter(GeneratorObject::getBufferCapacity),
            ResourceLocation.CODEC.fieldOf("fuelTag").orElse(GeneratorUtil.EMPTY_TAG).forGetter(GeneratorObject::getFuelTag)
        ).apply(instance, GeneratorObject::new));
    }

    public void setGenerationRate(double generationRate) {
        this.modifiedGenerationRate = generationRate;
    }

    public void setFuelList(Map<ResourceLocation, GeneratorCreator.Fuel> fuelList) {
        this.fuelList = fuelList;
    }

    public Map<ResourceLocation, GeneratorCreator.Fuel> getFuelList() {
        return fuelList;
    }
}
