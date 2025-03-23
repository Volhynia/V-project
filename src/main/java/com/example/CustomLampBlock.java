package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CustomLampBlock extends Block {

    public static final IntegerProperty HIGHLANDER = IntegerProperty.create("highlander", 0, 3);

    private static final VoxelShape TOP_SHAPE = Shapes.box(
            1.0 / 16.0, 12.0 / 16.0, 1.0 / 16.0,
            15.0 / 16.0, 15.0 / 16.0, 15.0 / 16.0
    );
    private static final VoxelShape MID_SHAPE = Shapes.box(
            1.0 / 16.0, 4.0 / 16.0, 1.0 / 16.0,
            15.0 / 16.0, 12.0 / 16.0, 15.0 / 16.0
    );
    private static final VoxelShape BOTTOM_SHAPE = Shapes.box(
            1.0 / 16.0, 1.0 / 16.0, 1.0 / 16.0,
            15.0 / 16.0, 4.0 / 16.0, 15.0 / 16.0
    );

    public CustomLampBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(HIGHLANDER) * 5));// 亮度 = highlander值 *5
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HIGHLANDER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HIGHLANDER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(TOP_SHAPE, MID_SHAPE, BOTTOM_SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HIGHLANDER, 0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // 确保使用动态模型渲染
    }
}


