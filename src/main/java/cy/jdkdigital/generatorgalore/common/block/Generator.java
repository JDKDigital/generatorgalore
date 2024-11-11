package cy.jdkdigital.generatorgalore.common.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.common.block.entity.GeneratorBlockEntity;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Generator extends BaseEntityBlock
{
    public static final MapCodec<Generator> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    propertiesCodec(),
                    GeneratorObject.codec(ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, "generator_codec")).fieldOf("generator").forGetter(generator -> generator.generator),
                    Codec.INT.fieldOf("modifier").forGetter(generator -> generator.modifier)
            )
            .apply(builder, Generator::new)
    );

    GeneratorObject generator;
    private final int modifier;

    public Generator(Properties properties, GeneratorObject generator, int modifier) {
        super(properties);
        this.generator = generator;
        this.modifier = modifier;
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.LIT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, generator.getBlockEntityType().get(), GeneratorBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GeneratorBlockEntity(generator, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) && pStack.getCapability(Capabilities.FluidHandler.ITEM) != null) {
            if (FluidUtil.interactWithFluidHandler(pPlayer, pHand, pLevel, pPos, null)) {
                pPlayer.swing(pHand);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof GeneratorBlockEntity generatorBlockEntity) {
            if (!pLevel.isClientSide) {
                pPlayer.openMenu(generatorBlockEntity, packetBuffer -> packetBuffer.writeBlockPos(pPos));
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHitResult);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean something) {
        BlockEntity generatorTile = level.getBlockEntity(pos);
        if (generatorTile instanceof GeneratorBlockEntity generatorBlockEntity) {
            generatorBlockEntity.refreshConnectedTileEntityCache();
        }
        super.onPlace(state, level, pos, newState, something);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        BlockEntity generatorTile = level.getBlockEntity(pos);
        if (generatorTile instanceof GeneratorBlockEntity generatorBlockEntity) {
            generatorBlockEntity.refreshConnectedTileEntityCache();
        }
        return super.updateShape(state, direction, newState, level, pos, facingPos);
    }

    @Override
    public void animateTick(BlockState pState, Level level, BlockPos pos, RandomSource random) {
        if (pState.getValue(BlockStateProperties.LIT)) {
            if (random.nextInt(11) == 0) {
                for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                    switch (generator.getFuelType()) {
                        case FLUID:
                            break;
                        case FOOD:
                            double d0 = (double) pos.getX() + 0.4D + (double) random.nextFloat() * 0.2D;
                            double d1 = (double) pos.getY() + 0.7D + (double) random.nextFloat() * 0.3D;
                            double d2 = (double) pos.getZ() + 0.4D + (double) random.nextFloat() * 0.2D;
                            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                            break;
                        case ENCHANTMENT:
//                            level.addParticle(ModParticles.RISING_ENCHANT_PARTICLE.get(), (double) pos.getX() + 0.5D, (double) pos.getY() + 1.0D, (double) pos.getZ() + 0.5D, random.nextFloat() / 2.0F, 5.0E-5D, random.nextFloat() / 2.0F);
                            break;
                        default:
                            level.addParticle(ParticleTypes.LAVA, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.0D, (double) pos.getZ() + 0.5D, random.nextFloat() / 2.0F, 5.0E-5D, random.nextFloat() / 2.0F);
                    }
                }
            }

            if (random.nextInt(55) == 0) {
                for (int i = 0; i < level.random.nextInt(2) + 2; ++i) {
                    double d0 = (double) pos.getX() + 0.5D;
                    double d1 = pos.getY();
                    double d2 = (double) pos.getZ() + 0.5D;
                    if (random.nextDouble() < 0.1D) {
                        level.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    }

                    level.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock() && !level.isClientSide && level.getBlockEntity(pos) instanceof GeneratorBlockEntity generatorBlockEntity) {
            // Drop inventory
            for (int slot = 0; slot < generatorBlockEntity.inventoryHandler.getSlots(); ++slot) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), generatorBlockEntity.inventoryHandler.getStackInSlot(slot));
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(pLevel.getBlockEntity(pPos));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTootipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTootipComponents, pTooltipFlag);

        pTootipComponents.add(Component.translatable(GeneratorGalore.MODID + ".screen.generation_rate", generator.getGenerationRate() * this.modifier).withStyle(ChatFormatting.BLUE));
        pTootipComponents.add(Component.translatable(GeneratorGalore.MODID + ".screen.transfer_rate", generator.getTransferRate() * this.modifier).withStyle(ChatFormatting.BLUE));
        pTootipComponents.add(Component.translatable(GeneratorGalore.MODID + ".screen.max_energy", generator.getBufferCapacity() * this.modifier).withStyle(ChatFormatting.BLUE));
        pTootipComponents.add(Component.translatable(GeneratorGalore.MODID + ".screen.fuel_type", generator.getFuelType().getSerializedName()).withStyle(ChatFormatting.BLUE));
    }

    public int getModifier() {
        return modifier;
    }
}
