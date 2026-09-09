package cy.jdkdigital.generatorgalore.common.item;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class GeneratorBlockItem extends BlockItem
{
    private final GeneratorObject generator;
    private final int modifier;

    public GeneratorBlockItem(Block block, Item.Properties properties, GeneratorObject generator, int modifier) {
        super(block, properties);
        this.generator = generator;
        this.modifier = modifier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, consumer, flag);

        consumer.accept(Component.translatable(GeneratorGalore.MODID + ".screen.generation_rate", generator.getGenerationRate() * this.modifier).withStyle(ChatFormatting.BLUE));
        consumer.accept(Component.translatable(GeneratorGalore.MODID + ".screen.transfer_rate", generator.getTransferRate() * this.modifier).withStyle(ChatFormatting.BLUE));
        consumer.accept(Component.translatable(GeneratorGalore.MODID + ".screen.max_energy", generator.getBufferCapacity() * this.modifier).withStyle(ChatFormatting.BLUE));
        consumer.accept(Component.translatable(GeneratorGalore.MODID + ".screen.fuel_type", generator.getFuelType().getSerializedName()).withStyle(ChatFormatting.BLUE));
    }
}
