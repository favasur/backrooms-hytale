package com.favasur.wrongyellow.frame;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of ALL frame shape types from the FramedBlocks mod, adapted for Hytale.
 * <p>
 * Maps each conceptual shape (SLAB, SLOPE, STAIRS, etc.) to its in-game
 * empty-frame block ID and optional blockymodel path.  Maintains a
 * bidirectional lookup so the plugin can answer "what shape is this?"
 * and "what's the block ID for this shape?" for any frame block.
 * <p>
 * This is the Hytale equivalent of FramedBlocks' {@code IBlockType}
 * interface + {@code BlockType} enum — a type identifier that bundles
 * shape metadata, model references, and camouflaged-variant registration.
 * <p>
 * Shapes without a blockymodel path (modelPath = null) use DrawType: Cube
 * and have no custom geometry — they exist in the registry for tracking
 * but require a future model pass to get custom geometry.
 *
 * <h3>Naming convention</h3>
 * Enum constants follow FramedBlocks' BlockType enum names, but in
 * Hytale-friendly format.  Block IDs use the pattern:
 * <pre>Frame_Empty_{ShapeName}</pre>
 * e.g. {@code Frame_Empty_Slope}, {@code Frame_Empty_Prism}.
 */
public enum FrameType {

    // ═══════════════════════════════════════════════════════════════
    //  Core base shapes (can use DrawType: Cube or simple models)
    // ═══════════════════════════════════════════════════════════════

    CUBE("Frame_Empty",
            null),

    // ── Slabs ─────────────────────────────────────────────────────

    SLAB("Frame_Empty_Slab",
            "Blocks/Structures/Base_Shapes/HalfBlock.blockymodel"),
    DOUBLE_SLAB("Frame_Empty_DoubleSlab",
            null), // two stacked half-blocks (future model)
    ADJUSTABLE_DOUBLE_SLAB("Frame_Empty_AdjDoubleSlab",
            null),
    ADJ_DOUBLE_COPYCAT_SLAB("Frame_Empty_AdjDoubleCopycatSlab",
            null),
    DIVIDED_SLAB("Frame_Empty_DividedSlab",
            null),
    CENTERED_SLAB("Frame_Empty_CenteredSlab",
            null),
    SLAB_EDGE("Frame_Empty_SlabEdge",
            "Blocks/Structures/Base_Shapes/SlabEdge.blockymodel"),
    SLAB_CORNER("Frame_Empty_SlabCorner",
            null),

    // ── Panels ────────────────────────────────────────────────────

    PANEL("Frame_Empty_Panel",
            "Blocks/Structures/Base_Shapes/Panel_2Tall.blockymodel"),
    PANEL_3TALL("Frame_Empty_Panel3Tall",
            "Blocks/Structures/Base_Shapes/Panel_3Tall.blockymodel"),
    DOUBLE_PANEL("Frame_Empty_DoublePanel",
            null),
    ADJUSTABLE_DOUBLE_PANEL("Frame_Empty_AdjDoublePanel",
            null),
    ADJ_DOUBLE_COPYCAT_PANEL("Frame_Empty_AdjDoubleCopycatPanel",
            null),
    DIVIDED_PANEL_HORIZONTAL("Frame_Empty_DividedPanelH",
            null),
    DIVIDED_PANEL_VERTICAL("Frame_Empty_DividedPanelV",
            null),
    CENTERED_PANEL("Frame_Empty_CenteredPanel",
            null),

    // ── Stairs ────────────────────────────────────────────────────

    STAIRS("Frame_Empty_Stairs",
            "Blocks/Structures/Stairs/Stairs_SimpleUV.blockymodel"),
    DOUBLE_STAIRS("Frame_Empty_DoubleStairs",
            null),
    HALF_STAIRS("Frame_Empty_HalfStairs",
            null),
    DIVIDED_STAIRS("Frame_Empty_DividedStairs",
            null),
    DOUBLE_HALF_STAIRS("Frame_Empty_DoubleHalfStairs",
            null),
    SLICED_STAIRS_SLAB("Frame_Empty_SlicedStairsSlab",
            null),
    SLICED_STAIRS_PANEL("Frame_Empty_SlicedStairsPanel",
            null),
    SLOPED_STAIRS("Frame_Empty_SlopedStairs",
            null),
    SLOPED_DOUBLE_STAIRS("Frame_Empty_SlopedDoubleStairs",
            null),
    VERTICAL_STAIRS("Frame_Empty_VerticalStairs",
            "Blocks/Structures/Base_Shapes/VerticalStairs.blockymodel"),
    VERTICAL_DOUBLE_STAIRS("Frame_Empty_VerticalDoubleStairs",
            null),
    VERTICAL_HALF_STAIRS("Frame_Empty_VerticalHalfStairs",
            null),
    VERTICAL_DIVIDED_STAIRS("Frame_Empty_VerticalDividedStairs",
            null),
    VERTICAL_DOUBLE_HALF_STAIRS("Frame_Empty_VerticalDoubleHalfStairs",
            null),
    VERTICAL_SLICED_STAIRS("Frame_Empty_VerticalSlicedStairs",
            null),
    VERTICAL_SLOPED_STAIRS("Frame_Empty_VerticalSlopedStairs",
            null),
    VERTICAL_SLOPED_DOUBLE_STAIRS("Frame_Empty_VerticalSlopedDoubleStairs",
            null),

    // ── Corner Stairs ─────────────────────────────────────────────

    STAIRS_CORNER_LEFT("Frame_Empty_StairsCornerLeft",
            "Blocks/Structures/Stairs/Stairs_Corner_Left_SimpleUV.blockymodel"),
    STAIRS_CORNER_RIGHT("Frame_Empty_StairsCornerRight",
            "Blocks/Structures/Stairs/Stairs_Corner_Right_SimpleUV.blockymodel"),
    STAIRS_INVERTED_CORNER_LEFT("Frame_Empty_StairsInvertedCornerLeft",
            "Blocks/Structures/Stairs/Stairs_Inverted_Corner_Left_SimpleUV.blockymodel"),
    STAIRS_INVERTED_CORNER_RIGHT("Frame_Empty_StairsInvertedCornerRight",
            "Blocks/Structures/Stairs/Stairs_Inverted_Corner_Right_SimpleUV.blockymodel"),

    // ── Slopes ────────────────────────────────────────────────────

    SLOPE("Frame_Empty_Slope",
            "Blocks/Structures/Base_Shapes/Slope.blockymodel"),
    DOUBLE_SLOPE("Frame_Empty_DoubleSlope",
            null),
    HALF_SLOPE("Frame_Empty_HalfSlope",
            null),
    VERTICAL_HALF_SLOPE("Frame_Empty_VerticalHalfSlope",
            null),
    DIVIDED_SLOPE("Frame_Empty_DividedSlope",
            null),
    DOUBLE_HALF_SLOPE("Frame_Empty_DoubleHalfSlope",
            null),
    VERTICAL_DOUBLE_HALF_SLOPE("Frame_Empty_VerticalDoubleHalfSlope",
            null),
    CORNER_SLOPE("Frame_Empty_CornerSlope",
            null),
    INNER_CORNER_SLOPE("Frame_Empty_InnerCornerSlope",
            null),
    DOUBLE_CORNER("Frame_Empty_DoubleCorner",
            null),
    PRISM_CORNER("Frame_Empty_PrismCorner",
            null),
    INNER_PRISM_CORNER("Frame_Empty_InnerPrismCorner",
            null),
    DOUBLE_PRISM_CORNER("Frame_Empty_DoublePrismCorner",
            null),
    THREEWAY_CORNER("Frame_Empty_ThreewayCorner",
            null),
    INNER_THREEWAY_CORNER("Frame_Empty_InnerThreewayCorner",
            null),
    DOUBLE_THREEWAY_CORNER("Frame_Empty_DoubleThreewayCorner",
            null),

    // ── Slope Edges ───────────────────────────────────────────────

    SLOPE_EDGE("Frame_Empty_SlopeEdge",
            null),
    ELEVATED_SLOPE_EDGE("Frame_Empty_ElevatedSlopeEdge",
            null),
    ELEVATED_DOUBLE_SLOPE_EDGE("Frame_Empty_ElevatedDoubleSlopeEdge",
            null),
    STACKED_SLOPE_EDGE("Frame_Empty_StackedSlopeEdge",
            null),
    CORNER_SLOPE_EDGE("Frame_Empty_CornerSlopeEdge",
            null),
    INNER_CORNER_SLOPE_EDGE("Frame_Empty_InnerCornerSlopeEdge",
            null),
    ELEVATED_CORNER_SLOPE_EDGE("Frame_Empty_ElevatedCornerSlopeEdge",
            null),
    ELEVATED_INNER_CORNER_SLOPE_EDGE("Frame_Empty_ElevatedInnerCornerSlopeEdge",
            null),
    STACKED_CORNER_SLOPE_EDGE("Frame_Empty_StackedCornerSlopeEdge",
            null),
    STACKED_INNER_CORNER_SLOPE_EDGE("Frame_Empty_StackedInnerCornerSlopeEdge",
            null),
    THREEWAY_CORNER_SLOPE_EDGE("Frame_Empty_ThreewayCornerSlopeEdge",
            null),
    INNER_THREEWAY_CORNER_SLOPE_EDGE("Frame_Empty_InnerThreewayCornerSlopeEdge",
            null),
    SLOPE_EDGE_SLAB("Frame_Empty_SlopeEdgeSlab",
            null),
    SLOPE_EDGE_PANEL("Frame_Empty_SlopeEdgePanel",
            null),

    // ── Slope Slabs ───────────────────────────────────────────────

    SLOPE_SLAB("Frame_Empty_SlopeSlab",
            null),
    ELEVATED_SLOPE_SLAB("Frame_Empty_ElevatedSlopeSlab",
            null),
    COMPOUND_SLOPE_SLAB("Frame_Empty_CompoundSlopeSlab",
            null),
    DOUBLE_SLOPE_SLAB("Frame_Empty_DoubleSlopeSlab",
            null),
    ELEVATED_DOUBLE_SLOPE_SLAB("Frame_Empty_ElevatedDoubleSlopeSlab",
            null),
    STACKED_SLOPE_SLAB("Frame_Empty_StackedSlopeSlab",
            null),
    FLAT_SLOPE_SLAB_CORNER("Frame_Empty_FlatSlopeSlabCorner",
            null),
    FLAT_INNER_SLOPE_SLAB_CORNER("Frame_Empty_FlatInnerSlopeSlabCorner",
            null),
    FLAT_ELEVATED_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevatedSlopeSlabCorner",
            null),
    FLAT_ELEVATED_INNER_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevatedInnerSlopeSlabCorner",
            null),
    FLAT_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatDoubleSlopeSlabCorner",
            null),
    FLAT_ELEVATED_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevatedDoubleSlopeSlabCorner",
            null),
    FLAT_ELEVATED_INNER_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevatedInnerDoubleSlopeSlabCorner",
            null),
    FLAT_STACKED_SLOPE_SLAB_CORNER("Frame_Empty_FlatStackedSlopeSlabCorner",
            null),
    FLAT_STACKED_INNER_SLOPE_SLAB_CORNER("Frame_Empty_FlatStackedInnerSlopeSlabCorner",
            null),

    // ── Slope Panels ──────────────────────────────────────────────

    SLOPE_PANEL("Frame_Empty_SlopePanel",
            null),
    EXTENDED_SLOPE_PANEL("Frame_Empty_ExtendedSlopePanel",
            null),
    COMPOUND_SLOPE_PANEL("Frame_Empty_CompoundSlopePanel",
            null),
    DOUBLE_SLOPE_PANEL("Frame_Empty_DoubleSlopePanel",
            null),
    EXTENDED_DOUBLE_SLOPE_PANEL("Frame_Empty_ExtendedDoubleSlopePanel",
            null),
    STACKED_SLOPE_PANEL("Frame_Empty_StackedSlopePanel",
            null),
    FLAT_SLOPE_PANEL_CORNER("Frame_Empty_FlatSlopePanelCorner",
            null),
    FLAT_INNER_SLOPE_PANEL_CORNER("Frame_Empty_FlatInnerSlopePanelCorner",
            null),
    FLAT_EXTENDED_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtendedSlopePanelCorner",
            null),
    FLAT_EXTENDED_INNER_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtendedInnerSlopePanelCorner",
            null),
    FLAT_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatDoubleSlopePanelCorner",
            null),
    FLAT_EXTENDED_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtendedDoubleSlopePanelCorner",
            null),
    FLAT_EXTENDED_INNER_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtendedInnerDoubleSlopePanelCorner",
            null),
    FLAT_STACKED_SLOPE_PANEL_CORNER("Frame_Empty_FlatStackedSlopePanelCorner",
            null),
    FLAT_STACKED_INNER_SLOPE_PANEL_CORNER("Frame_Empty_FlatStackedInnerSlopePanelCorner",
            null),

    // ── Slope Panel Corners ───────────────────────────────────────

    SMALL_CORNER_SLOPE_PANEL("Frame_Empty_SmallCornerSlopePanel",
            null),
    LARGE_CORNER_SLOPE_PANEL("Frame_Empty_LargeCornerSlopePanel",
            null),
    SMALL_INNER_CORNER_SLOPE_PANEL("Frame_Empty_SmallInnerCornerSlopePanel",
            null),
    LARGE_INNER_CORNER_SLOPE_PANEL("Frame_Empty_LargeInnerCornerSlopePanel",
            null),
    EXTENDED_CORNER_SLOPE_PANEL("Frame_Empty_ExtendedCornerSlopePanel",
            null),
    EXTENDED_INNER_CORNER_SLOPE_PANEL("Frame_Empty_ExtendedInnerCornerSlopePanel",
            null),
    SMALL_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_SmallDoubleCornerSlopePanel",
            null),
    LARGE_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_LargeDoubleCornerSlopePanel",
            null),
    EXTENDED_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_ExtendedDoubleCornerSlopePanel",
            null),
    EXTENDED_INNER_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_ExtendedInnerDoubleCornerSlopePanel",
            null),
    STACKED_CORNER_SLOPE_PANEL("Frame_Empty_StackedCornerSlopePanel",
            null),
    STACKED_INNER_CORNER_SLOPE_PANEL("Frame_Empty_StackedInnerCornerSlopePanel",
            null),
    SMALL_PRISM_CORNER_SLOPE_PANEL("Frame_Empty_SmallPrismCornerSlopePanel",
            null),
    LARGE_PRISM_CORNER_SLOPE_PANEL("Frame_Empty_LargePrismCornerSlopePanel",
            null),
    SMALL_INNER_PRISM_CORNER_SLOPE_PANEL("Frame_Empty_SmallInnerPrismCornerSlopePanel",
            null),
    LARGE_INNER_PRISM_CORNER_SLOPE_PANEL("Frame_Empty_LargeInnerPrismCornerSlopePanel",
            null),

    // ── Pillars ───────────────────────────────────────────────────

    PILLAR("Frame_Empty_Pillar",
            "Blocks/Structures/Base_Shapes/Pillar.blockymodel"),
    HALF_PILLAR("Frame_Empty_HalfPillar",
            "Blocks/Structures/Base_Shapes/HalfPillar.blockymodel"),
    CORNER_PILLAR("Frame_Empty_CornerPillar",
            null),
    THREEWAY_CORNER_PILLAR("Frame_Empty_ThreewayCornerPillar",
            null),
    DOUBLE_THREEWAY_CORNER_PILLAR("Frame_Empty_DoubleThreewayCornerPillar",
            null),
    PILLAR_SOCKET("Frame_Empty_PillarSocket",
            null),
    SPLIT_PILLAR_SOCKET("Frame_Empty_SplitPillarSocket",
            null),
    POST("Frame_Empty_Post",
            null),

    // ── Walls / Fences ────────────────────────────────────────────

    WALL("Frame_Empty_Wall",
            null),
    FENCE("Frame_Empty_Fence",
            null),
    FENCE_GATE("Frame_Empty_FenceGate",
            null),
    GATE("Frame_Empty_Gate",
            null),

    // ── Panes / Boards ────────────────────────────────────────────

    PANE("Frame_Empty_Pane",
            "Blocks/Structures/Base_Shapes/Pane.blockymodel"),
    HORIZONTAL_PANE("Frame_Empty_HorizontalPane",
            null),
    BOARD("Frame_Empty_Board",
            null),
    HALF_BOARD("Frame_Empty_HalfBoard",
            null),
    DIVIDED_BOARD("Frame_Empty_DividedBoard",
            null),
    CORNER_BOARD("Frame_Empty_CornerBoard",
            null),
    INNER_CORNER_BOARD("Frame_Empty_InnerCornerBoard",
            null),
    DOUBLE_CORNER_BOARD("Frame_Empty_DoubleCornerBoard",
            null),
    CORNER_STRIP("Frame_Empty_CornerStrip",
            null),
    BARS("Frame_Empty_Bars",
            null),
    LATTICE("Frame_Empty_Lattice",
            null),
    THICK_LATTICE("Frame_Empty_ThickLattice",
            null),

    // ── Prisms ────────────────────────────────────────────────────

    PRISM("Frame_Empty_Prism",
            "Blocks/Structures/Base_Shapes/Prism.blockymodel"),
    SLOPED_PRISM("Frame_Empty_SlopedPrism",
            null),
    ELEVATED_INNER_PRISM("Frame_Empty_ElevatedInnerPrism",
            null),
    ELEVATED_INNER_DOUBLE_PRISM("Frame_Empty_ElevatedInnerDoublePrism",
            null),
    ELEVATED_INNER_SLOPED_PRISM("Frame_Empty_ElevatedInnerSlopedPrism",
            null),
    ELEVATED_INNER_DOUBLE_SLOPED_PRISM("Frame_Empty_ElevatedInnerDoubleSlopedPrism",
            null),

    // ── Doors / Trapdoors ─────────────────────────────────────────

    DOOR("Frame_Empty_Door",
            null),
    IRON_DOOR("Frame_Empty_IronDoor",
            null),
    TRAPDOOR("Frame_Empty_Trapdoor",
            null),
    IRON_TRAPDOOR("Frame_Empty_IronTrapdoor",
            null),

    // ── Interactive (structural shapes) ─────────────────────────—

    LADDER("Frame_Empty_Ladder",
            null),
    BUTTON("Frame_Empty_Button",
            null),
    STONE_BUTTON("Frame_Empty_StoneButton",
            null),
    LARGE_BUTTON("Frame_Empty_LargeButton",
            null),
    LARGE_STONE_BUTTON("Frame_Empty_LargeStoneButton",
            null),
    LEVER("Frame_Empty_Lever",
            null),
    PRESSURE_PLATE("Frame_Empty_PressurePlate",
            null),
    STONE_PRESSURE_PLATE("Frame_Empty_StonePressurePlate",
            null),

    // ── Misc structural ───────────────────────────────────────────

    CHAIN("Frame_Empty_Chain",
            null),
    LADDER_ALT("Frame_Empty_LadderAlt",
            null),
    TUBE("Frame_Empty_Tube",
            null),
    CORNER_TUBE("Frame_Empty_CornerTube",
            null),
    PYRAMID("Frame_Empty_Pyramid",
            null),
    PYRAMID_SLAB("Frame_Empty_PyramidSlab",
            null),
    ELEVATED_PYRAMID_SLAB("Frame_Empty_ElevatedPyramidSlab",
            null),
    UPPER_PYRAMID_SLAB("Frame_Empty_UpperPyramidSlab",
            null),
    STACKED_PYRAMID_SLAB("Frame_Empty_StackedPyramidSlab",
            null),
    MINI_CUBE("Frame_Empty_MiniCube",
            null),
    LAYERED_CUBE("Frame_Empty_LayeredCube",
            null),
    LIGHTNING_ROD("Frame_Empty_LightningRod",
            null),
    PATH("Frame_Empty_Path",
            null),
    TARGET("Frame_Empty_Target",
            null),
    ONE_WAY_WINDOW("Frame_Empty_OneWayWindow",
            null),
    CHISELED_BOOKSHELF("Frame_Empty_ChiseledBookshelf",
            null),
    BOOKSHELF("Frame_Empty_Bookshelf",
            null),
    SHELF("Frame_Empty_Shelf",
            null),
    HOPPER("Frame_Empty_Hopper",
            null),
    CHEST("Frame_Empty_Chest",
            null),
    TANK("Frame_Empty_Tank",
            null),
    COLLAPSIBLE_BLOCK("Frame_Empty_Collapsible",
            null),
    BOUNCY_CUBE("Frame_Empty_BouncyCube",
            null),
    REDSTONE_BLOCK("Frame_Empty_RedstoneBlock",
            null),

    // ── Decorations ───────────────────────────────────────────────

    FLOWER_POT("Frame_Empty_FlowerPot",
            null),
    LANTERN("Frame_Empty_Lantern",
            null),
    SOUL_LANTERN("Frame_Empty_SoulLantern",
            null),
    TORCH("Frame_Empty_Torch",
            null),
    WALL_TORCH("Frame_Empty_WallTorch",
            null),
    SIGN("Frame_Empty_Sign",
            null),
    WALL_SIGN("Frame_Empty_WallSign",
            null),
    HANGING_SIGN("Frame_Empty_HangingSign",
            null),
    WALL_HANGING_SIGN("Frame_Empty_WallHangingSign",
            null),
    BANNER("Frame_Empty_Banner",
            null),
    WALL_BANNER("Frame_Empty_WallBanner",
            null),
    ITEM_FRAME("Frame_Empty_ItemFrame",
            null),

    // ── Checkered / Masonry ───────────────────────────────────────

    CHECKERED_CUBE("Frame_Empty_CheckeredCube",
            null),
    CHECKERED_CUBE_SEGMENT("Frame_Empty_CheckeredCubeSegment",
            null),
    CHECKERED_SLAB("Frame_Empty_CheckeredSlab",
            null),
    CHECKERED_SLAB_SEGMENT("Frame_Empty_CheckeredSlabSegment",
            null),
    CHECKERED_PANEL("Frame_Empty_CheckeredPanel",
            null),
    CHECKERED_PANEL_SEGMENT("Frame_Empty_CheckeredPanelSegment",
            null),
    MASONRY_CORNER("Frame_Empty_MasonryCorner",
            null),
    MASONRY_CORNER_SEGMENT("Frame_Empty_MasonryCornerSegment",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Abbreviated slope / edge variants (mirror FramedBlocks naming)
    // ═══════════════════════════════════════════════════════════════

    // Slope edge abbreviations
    ELEV_DOUBLE_CORNER_SLOPE_EDGE("Frame_Empty_ElevDoubleCornerSlopeEdge",
            null),
    ELEV_DOUBLE_INNER_CORNER_SLOPE_EDGE("Frame_Empty_ElevDoubleInnerCornerSlopeEdge",
            null),

    // Slope slab abbreviations
    FLAT_ELEV_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevSlopeSlabCorner",
            null),
    FLAT_ELEV_INNER_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevInnerSlopeSlabCorner",
            null),
    FLAT_ELEV_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevDoubleSlopeSlabCorner",
            null),
    FLAT_ELEV_INNER_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatElevInnerDoubleSlopeSlabCorner",
            null),
    FLAT_INV_DOUBLE_SLOPE_SLAB_CORNER("Frame_Empty_FlatInvDoubleSlopeSlabCorner",
            null),
    INV_DOUBLE_SLOPE_SLAB("Frame_Empty_InvDoubleSlopeSlab",
            null),

    // Slope panel abbreviations
    FLAT_EXT_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtSlopePanelCorner",
            null),
    FLAT_EXT_INNER_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtInnerSlopePanelCorner",
            null),
    FLAT_EXT_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtDoubleSlopePanelCorner",
            null),
    FLAT_EXT_INNER_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatExtInnerDoubleSlopePanelCorner",
            null),
    FLAT_INV_DOUBLE_SLOPE_PANEL_CORNER("Frame_Empty_FlatInvDoubleSlopePanelCorner",
            null),
    INV_DOUBLE_SLOPE_PANEL("Frame_Empty_InvDoubleSlopePanel",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Wall-mounted slope panel corner variants (suffix _W)
    // ═══════════════════════════════════════════════════════════════

    EXT_CORNER_SLOPE_PANEL("Frame_Empty_ExtCornerSlopePanel",
            null),
    EXT_CORNER_SLOPE_PANEL_W("Frame_Empty_ExtCornerSlopePanelW",
            null),
    EXT_INNER_CORNER_SLOPE_PANEL("Frame_Empty_ExtInnerCornerSlopePanel",
            null),
    EXT_INNER_CORNER_SLOPE_PANEL_W("Frame_Empty_ExtInnerCornerSlopePanelW",
            null),
    EXT_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_ExtDoubleCornerSlopePanel",
            null),
    EXT_DOUBLE_CORNER_SLOPE_PANEL_W("Frame_Empty_ExtDoubleCornerSlopePanelW",
            null),
    EXT_INNER_DOUBLE_CORNER_SLOPE_PANEL("Frame_Empty_ExtInnerDoubleCornerSlopePanel",
            null),
    EXT_INNER_DOUBLE_CORNER_SLOPE_PANEL_W("Frame_Empty_ExtInnerDoubleCornerSlopePanelW",
            null),
    SMALL_CORNER_SLOPE_PANEL_W("Frame_Empty_SmallCornerSlopePanelW",
            null),
    LARGE_CORNER_SLOPE_PANEL_W("Frame_Empty_LargeCornerSlopePanelW",
            null),
    SMALL_INNER_CORNER_SLOPE_PANEL_W("Frame_Empty_SmallInnerCornerSlopePanelW",
            null),
    LARGE_INNER_CORNER_SLOPE_PANEL_W("Frame_Empty_LargeInnerCornerSlopePanelW",
            null),
    SMALL_DOUBLE_CORNER_SLOPE_PANEL_W("Frame_Empty_SmallDoubleCornerSlopePanelW",
            null),
    LARGE_DOUBLE_CORNER_SLOPE_PANEL_W("Frame_Empty_LargeDoubleCornerSlopePanelW",
            null),
    INV_DOUBLE_CORNER_SLOPE_PANEL_W("Frame_Empty_InvDoubleCornerSlopePanelW",
            null),
    STACKED_CORNER_SLOPE_PANEL_W("Frame_Empty_StackedCornerSlopePanelW",
            null),
    STACKED_INNER_CORNER_SLOPE_PANEL_W("Frame_Empty_StackedInnerCornerSlopePanelW",
            null),
    SMALL_PRISM_CORNER_SLOPE_PANEL_W("Frame_Empty_SmallPrismCornerSlopePanelW",
            null),
    LARGE_PRISM_CORNER_SLOPE_PANEL_W("Frame_Empty_LargePrismCornerSlopePanelW",
            null),
    SMALL_INNER_PRISM_CORNER_SLOPE_PANEL_W("Frame_Empty_SmallInnerPrismCornerSlopePanelW",
            null),
    LARGE_INNER_PRISM_CORNER_SLOPE_PANEL_W("Frame_Empty_LargeInnerPrismCornerSlopePanelW",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Rail slopes
    // ═══════════════════════════════════════════════════════════════

    RAIL_SLOPE("Frame_Empty_RailSlope",
            null),
    POWERED_RAIL_SLOPE("Frame_Empty_PoweredRailSlope",
            null),
    DETECTOR_RAIL_SLOPE("Frame_Empty_DetectorRailSlope",
            null),
    ACTIVATOR_RAIL_SLOPE("Frame_Empty_ActivatorRailSlope",
            null),
    FANCY_RAIL("Frame_Empty_FancyRail",
            null),
    FANCY_POWERED_RAIL("Frame_Empty_FancyPoweredRail",
            null),
    FANCY_DETECTOR_RAIL("Frame_Empty_FancyDetectorRail",
            null),
    FANCY_ACTIVATOR_RAIL("Frame_Empty_FancyActivatorRail",
            null),
    FANCY_RAIL_SLOPE("Frame_Empty_FancyRailSlope",
            null),
    FANCY_POWERED_RAIL_SLOPE("Frame_Empty_FancyPoweredRailSlope",
            null),
    FANCY_DETECTOR_RAIL_SLOPE("Frame_Empty_FancyDetectorRailSlope",
            null),
    FANCY_ACTIVATOR_RAIL_SLOPE("Frame_Empty_FancyActivatorRailSlope",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Torches & Lanterns
    // ═══════════════════════════════════════════════════════════════

    SOUL_TORCH("Frame_Empty_SoulTorch",
            null),
    SOUL_WALL_TORCH("Frame_Empty_SoulWallTorch",
            null),
    COPPER_TORCH("Frame_Empty_CopperTorch",
            null),
    COPPER_WALL_TORCH("Frame_Empty_CopperWallTorch",
            null),
    REDSTONE_TORCH("Frame_Empty_RedstoneTorch",
            null),
    REDSTONE_WALL_TORCH("Frame_Empty_RedstoneWallTorch",
            null),
    COPPER_LANTERN("Frame_Empty_CopperLantern",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Pressure plates (all variants)
    // ═══════════════════════════════════════════════════════════════

    WATERLOGGABLE_PRESSURE_PLATE("Frame_Empty_WaterloggablePressurePlate",
            null),
    WATERLOGGABLE_STONE_PRESSURE_PLATE("Frame_Empty_WaterloggableStonePressurePlate",
            null),
    OBSIDIAN_PRESSURE_PLATE("Frame_Empty_ObsidianPressurePlate",
            null),
    WATERLOGGABLE_OBSIDIAN_PRESSURE_PLATE("Frame_Empty_WaterloggableObsidianPressurePlate",
            null),
    GOLD_PRESSURE_PLATE("Frame_Empty_GoldPressurePlate",
            null),
    WATERLOGGABLE_GOLD_PRESSURE_PLATE("Frame_Empty_WaterloggableGoldPressurePlate",
            null),
    IRON_PRESSURE_PLATE("Frame_Empty_IronPressurePlate",
            null),
    WATERLOGGABLE_IRON_PRESSURE_PLATE("Frame_Empty_WaterloggableIronPressurePlate",
            null),

    // ═══════════════════════════════════════════════════════════════
    //  Misc missing shapes
    // ═══════════════════════════════════════════════════════════════

    COLLAPSIBLE_COPYCAT_BLOCK("Frame_Empty_CollapsibleCopycat",
            null),
    LATTICE_BLOCK("Frame_Empty_LatticeBlock",
            null),
    GLOWING_ITEM_FRAME("Frame_Empty_GlowingItemFrame",
            null),
    SECRET_STORAGE("Frame_Empty_SecretStorage",
            null),
    IRON_GATE("Frame_Empty_IronGate",
            null);

    // ═══════════════════════════════════════════════════════════════
    //  Enum fields & initialiser
    // ═══════════════════════════════════════════════════════════════

    private final String emptyBlockId;

    @Nullable
    private final String modelPath;

    private static final Map<String, FrameType> BLOCK_ID_LOOKUP = new HashMap<>();

    /** Shape categories for grouping in creative menu / tooltips. */
    public enum Category { CORE, SLABS_PANELS, STAIRS, SLOPES, PILLARS, PANES, PRISMS, DOORS, MISC }

    private final Category category;

    FrameType(String emptyBlockId, @Nullable String modelPath) {
        this.emptyBlockId = emptyBlockId;
        this.modelPath = modelPath;
        this.category = assignCategory();
    }

    private Category assignCategory() {
        String n = name();
        // Order: most specific patterns first, then general

        // ── Core / Slabs / Panels ──────────────────────────────────────────
        if (n.equals("CUBE") || n.equals("SLAB") || n.startsWith("SLAB_")
                || n.startsWith("PANEL") || n.startsWith("CENTERED")
                || n.startsWith("CHECKERED") || n.startsWith("MASONRY"))
            return Category.SLABS_PANELS;

        // ── Doors / Trapdoors ─────────────────────────────────────────────
        if (n.contains("DOOR") || n.contains("TRAPDOOR")) return Category.DOORS;

        // ── Stairs (check before slopes to avoid false matches) ────────────
        if (n.startsWith("STAIR") || n.startsWith("VERTICAL_STAIR")
                || n.startsWith("VERTICAL_DOUBLE") || n.startsWith("VERTICAL_HALF")
                || n.startsWith("VERTICAL_DIVIDED") || n.startsWith("VERTICAL_SLICED")
                || n.startsWith("VERTICAL_SLOPED"))
            return Category.STAIRS;

        // ── Slopes (including edges, corners, pyramids, prism corners) ─────
        if (n.contains("SLOPE") || n.contains("EDGE") || n.contains("PYRAMID")
                || n.contains("CORNER") || n.startsWith("THREEWAY")
                || n.startsWith("INNER_") || n.startsWith("PRISM_CORNER")
                || n.startsWith("DOUBLE_CORNER") || n.startsWith("DOUBLE_HALF")
                || n.startsWith("DOUBLE_PRISM") || n.startsWith("DOUBLE_THREEWAY")
                || n.startsWith("ELEVATED") || n.startsWith("STACKED")
                || n.startsWith("COMPOUND") || n.startsWith("INVERSE")
                || n.startsWith("EXTENDED") || n.startsWith("FLAT_"))
            return Category.SLOPES;

        // ── Pillars / Walls / Fences ────────────────────────────────────────
        if (n.startsWith("PILLAR") || n.startsWith("POST") || n.startsWith("WALL")
                || n.startsWith("FENCE") || n.startsWith("GATE"))
            return Category.PILLARS;

        // ── Panes / Boards ──────────────────────────────────────────────────
        if (n.startsWith("PANE") || n.startsWith("BOARD") || n.startsWith("CORNER_STRIP")
                || n.startsWith("BARS") || n.startsWith("LATTICE"))
            return Category.PANES;

        // ── Prisms ──────────────────────────────────────────────────────────
        if (n.startsWith("PRISM") || n.startsWith("SLOPED_PRISM"))
            return Category.PRISMS;

        return Category.MISC;
    }

    // ---- static initialiser ------------------------------------------------

    static {
        for (FrameType type : values()) {
            BLOCK_ID_LOOKUP.put(type.emptyBlockId, type);
        }
    }

    // ---- public API --------------------------------------------------------

    @Nullable
    public static FrameType fromBlockId(String blockId) {
        return BLOCK_ID_LOOKUP.get(blockId);
    }

    public static boolean isFrameBlock(String blockId) {
        return BLOCK_ID_LOOKUP.containsKey(blockId);
    }

    /** The empty (un-camouflaged) block ID for this shape. */
    public String emptyBlockId() {
        return emptyBlockId;
    }

    /** The .blockymodel path, or {@code null} if this shape uses DrawType: Cube. */
    @Nullable
    public String modelPath() {
        return modelPath;
    }

    /** Returns {@code true} if this shape has custom geometry (a .blockymodel file). */
    public boolean isModelShape() {
        return modelPath != null;
    }

    /** The shape category for grouping (creative menu, tooltips, etc.). */
    public Category category() {
        return category;
    }

    /**
     * Human-readable display name (e.g. "Slope", "Vertical stairs").
     */
    public String displayName() {
        String lower = name().toLowerCase().replace('_', ' ');
        // Capitalise first letter of each word
        StringBuilder sb = new StringBuilder(lower.length());
        boolean nextUpper = true;
        for (char c : lower.toCharArray()) {
            if (c == ' ') {
                nextUpper = true;
                sb.append(c);
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---- camouflaged variant registration (for future block-swapping) ------

    private final Map<String, String> camoVariants = new HashMap<>();

    public void registerCamoVariant(String materialBlockId, String camoVariantId) {
        camoVariants.put(materialBlockId, camoVariantId);
        BLOCK_ID_LOOKUP.put(camoVariantId, this);
    }

    @Nullable
    public String getCamoVariant(String materialBlockId) {
        return camoVariants.get(materialBlockId);
    }

    public static FrameType[] all() {
        return values();
    }
}
