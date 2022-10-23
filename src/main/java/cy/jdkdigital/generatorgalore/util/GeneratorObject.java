package cy.jdkdigital.generatorgalore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.common.container.GeneratorMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class GeneratorObject
{
    private final ResourceLocation id;
    private Supplier<Block> blockSupplier;
    private Supplier<BlockEntityType<GeneratorBlockEntity>> blockEntityType;
    private Supplier<MenuType<GeneratorMenu>> menuType;
    private final String fuelType;
    private final double generationRate;
    private final double transferRate;
    private final double consumptionRate;
    private final int bufferCapacity;
    private final ResourceLocation fuelTag;

    public GeneratorObject(ResourceLocation id, String fuelType, double generationRate, double transferRate, double consumptionRate, int bufferCapacity, ResourceLocation fuelTag) {
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

    public String getFuelType() {
        return this.fuelType;
    }

    public double getGenerationRate() {
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
            Codec.STRING.fieldOf("fuelType").orElse(GeneratorUtil.FUEL_SOLID).forGetter(GeneratorObject::getFuelType),
            Codec.DOUBLE.fieldOf("generationRate").forGetter(GeneratorObject::getGenerationRate),
            Codec.DOUBLE.fieldOf("transferRate").forGetter(GeneratorObject::getTransferRate),
            Codec.DOUBLE.fieldOf("consumptionRate").forGetter(GeneratorObject::getConsumptionRate),
            Codec.INT.fieldOf("bufferCapacity").forGetter(GeneratorObject::getBufferCapacity),
            ResourceLocation.CODEC.fieldOf("fuelTag").orElse(GeneratorUtil.EMPTY_TAG).forGetter(GeneratorObject::getFuelTag)
        ).apply(instance, GeneratorObject::new));
    }
}
