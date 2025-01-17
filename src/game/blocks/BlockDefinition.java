package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static game.chunk.Chunk.*;
import static game.falling.FallingEntity.addFallingEntity;

import static game.item.ItemDefinition.registerItem;
import static game.item.ItemEntity.createItem;
import static game.tnt.TNTEntity.createTNT;
import static engine.sound.SoundAPI.playSound;

public class BlockDefinition {

    private final static BlockDefinition[] blockIDs = new BlockDefinition[256];

    //0: normal,
    private final static BlockShape[] blockShapeMap = new BlockShape[128];

    //fixed fields for the class
    private static final byte atlasSizeX = 32;
    private static final byte atlasSizeY = 32;

    //actual block object fields
    public int     ID;
    public String  name;
    public boolean dropsItem;
    public float[] frontTexture;  //front
    public float[] backTexture;   //back
    public float[] rightTexture;  //right
    public float[] leftTexture;   //left
    public float[] topTexture;    //top
    public float[] bottomTexture; //bottom
    public boolean walkable;
    public boolean steppable;
    public boolean isLiquid;
    public int drawType;
    public String placeSound;
    public String digSound;
    public BlockModifier blockModifier;
    public boolean isRightClickable;
    public boolean isOnPlaced;
    public float viscosity;
    public boolean pointable;

    public BlockDefinition(
            int ID,String name,
            boolean dropsItem,
            int[] front,
            int[] back,
            int[] right,
            int[] left,
            int[] top,
            int[] bottom,
            int drawType,
            boolean walkable,
            boolean steppable,
            boolean isLiquid,
            BlockModifier blockModifier,
            String placeSound,
            String digSound,
            boolean isRightClickable,
            boolean isOnPlaced,
            float viscosity,
            boolean pointable

    ){

        this.ID   = ID;
        this.name = name;
        this.dropsItem = dropsItem;
        this.frontTexture  = calculateTexture(  front[0],  front[1] );
        this.backTexture   = calculateTexture(   back[0],   back[1] );
        this.rightTexture  = calculateTexture(  right[0],  right[1] );
        this.leftTexture   = calculateTexture(   left[0],   left[1] );
        this.topTexture    = calculateTexture(    top[0],    top[1] );
        this.bottomTexture = calculateTexture( bottom[0], bottom[1] );
        this.drawType = drawType;
        this.walkable = walkable;
        this.steppable = steppable;
        this.isLiquid = isLiquid;
        this.blockModifier = blockModifier;
        this.placeSound = placeSound;
        this.digSound = digSound;
        this.isRightClickable = isRightClickable;
        this.isOnPlaced = isOnPlaced;
        this.viscosity = viscosity;
        this.pointable = pointable;
        blockIDs[ID] = this;

        registerItem(name, ID);
    }

    public static void onDigCall(int ID, Vector3d pos) {
        if(blockIDs[ID] != null){
            if(blockIDs[ID].dropsItem){
                createItem(blockIDs[ID].name, pos.add(0.5d,0.5d,0.5d), 1, 2.5f);
            }
            if(blockIDs[ID].blockModifier != null){
                try {
                    blockIDs[ID].blockModifier.onDig(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!blockIDs[ID].digSound.equals("")) {
                playSound(blockIDs[ID].digSound);
            }
        }
    }

    public static void onPlaceCall(int ID, Vector3d pos) {
        if(blockIDs[ID] != null && blockIDs[ID].blockModifier != null) {
            try {
                blockIDs[ID].blockModifier.onPlace(pos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!blockIDs[ID].placeSound.equals("")) {
            playSound(blockIDs[ID].placeSound);
        }
    }

    private static float[] calculateTexture(int x, int y){
        float[] texturePoints = new float[4];
        texturePoints[0] = (float)x/(float)atlasSizeX;     //min x (-)
        texturePoints[1] = (float)(x+1)/(float)atlasSizeX; //max x (+)

        texturePoints[2] = (float)y/(float)atlasSizeY;     //min y (-)
        texturePoints[3] = (float)(y+1)/(float)atlasSizeY; //max y (+)
        return texturePoints;
    }

    public static String getBlockName(int ID){
        return blockIDs[ID].name;
    }

    public static boolean getRightClickable(int ID){
        return(blockIDs[ID].isRightClickable);
    }

    public static boolean getIsOnPlaced(int ID){
        return(blockIDs[ID].isOnPlaced);
    }

    public static int getBlockDrawType(int ID){
        if (ID < 0){
            return 0;
        }
        return blockIDs[ID].drawType;
    }

    public static boolean getIfLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static double[][] getBlockShape(int ID, byte rot){

        double[][] newBoxes = new double[blockShapeMap[blockIDs[ID].drawType].getBoxes().length][6];


        int index = 0;

        //automated as base, since it's the same
        if (rot == 0) {
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                for (int i = 0; i < 6; i++) {
                    newBoxes[index][i] = thisShape[i];
                }
                index++;
            }
        }

        if (rot == 2){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                double blockDiffZ =  1d - thisShape[5];
                double widthZ = thisShape[5] - thisShape[2];

                double blockDiffX =  1d - thisShape[3];
                double widthX = thisShape[3] - thisShape[0];

                newBoxes[index][0] = blockDiffX;
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = blockDiffZ; // -z

                newBoxes[index][3] = blockDiffX + widthX;
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = blockDiffZ + widthZ; //+z
                index++;
            }
        }


        if (rot == 1){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                double blockDiffZ =  1d - thisShape[5];
                double widthZ = thisShape[5] - thisShape[2];

                newBoxes[index][0] = blockDiffZ;
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = thisShape[0]; // -z

                newBoxes[index][3] = blockDiffZ + widthZ;
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = thisShape[3]; //+z
                index++;
            }
        }


        if (rot == 3){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                double blockDiffX =  1d - thisShape[3];
                double widthX = thisShape[3] - thisShape[0];

                newBoxes[index][0] = thisShape[2];
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = blockDiffX; // -z

                newBoxes[index][3] = thisShape[5];
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = blockDiffX + widthX; //+z
                index++;
            }
        }

        return newBoxes/*blockShapeMap.get(blockIDs[ID].drawType).getBoxes()*/;
    }

    public static boolean isWalkable(int ID){
        return blockIDs[ID].walkable;
    }

    public static boolean isSteppable(int ID){
        return blockIDs[ID].steppable;
    }

    public static void initializeBlocks() throws Exception {

        //air
        blockShapeMap[0] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});


        //normal
        blockShapeMap[1] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

        //stair
        blockShapeMap[2] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f},
                                {0f,0f,0f,1f,1f,0.5f}
                        });

        //slab
        blockShapeMap[3] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f}
                        });

        //allfaces
        blockShapeMap[4] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,1f,1f}
                        });


        new BlockDefinition(
                0,
                "air",
                false,
                new int[]{-1,-1}, //front
                new int[]{-1,-1}, //back
                new int[]{-1,-1}, //right
                new int[]{-1,-1}, //left
                new int[]{-1,-1}, //top
                new int[]{-1,-1},  //bottom
                0,
                false,
                false,
                false,
                null,
                "",
                "",
                false,
                false,
                0,
                false
        );

        new BlockDefinition(
                1,
                "dirt",
                true,
                new int[]{0,0}, //front
                new int[]{0,0}, //back
                new int[]{0,0}, //right
                new int[]{0,0}, //left
                new int[]{0,0}, //top
                new int[]{0,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                2,
                "grass",
                true,
                new int[]{5,0}, //front
                new int[]{5,0}, //back
                new int[]{5,0}, //right
                new int[]{5,0}, //left
                new int[]{4,0}, //top
                new int[]{0,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                3,
                "stone",
                true,
                new int[]{1,0}, //front
                new int[]{1,0}, //back
                new int[]{1,0}, //right
                new int[]{1,0}, //left
                new int[]{1,0}, //top
                new int[]{1,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                4,
                "cobblestone",
                true,
                new int[]{2,0}, //front
                new int[]{2,0}, //back
                new int[]{2,0}, //right
                new int[]{2,0}, //left
                new int[]{2,0}, //top
                new int[]{2,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                5,
                "bedrock",
                false,
                new int[]{6,0}, //front
                new int[]{6,0}, //back
                new int[]{6,0}, //right
                new int[]{6,0}, //left
                new int[]{6,0}, //top
                new int[]{6,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_1",
                false,
                false,
                0,
                true
        );


        //tnt explosion
        BlockModifier kaboom = new BlockModifier() {
            @Override
            public void onDig(Vector3d pos) throws Exception {
                createTNT(pos, 0, true);
            }
        };

        new BlockDefinition(
                6,
                "tnt",
                false,
                new int[]{7,0}, //front
                new int[]{7,0}, //back
                new int[]{7,0}, //right
                new int[]{7,0}, //left
                new int[]{8,0}, //top
                new int[]{9,0},  //bottom
                1,
                true,
                false,
                false,
                kaboom,
                "dirt_1",
                "wood_2",
                false,
                false,
                0,
                true
        );

        //water thing
        /*
        BlockModifier splash = new BlockModifier() {
            @Override
            public void onPlace(Vector3f pos) {
                for(int y = 0; y < 128; y++){
                    setBlock((int)Math.floor(pos.x), y, (int)Math.floor(pos.z),7, 0);
                }
            }
        };
         */

        new BlockDefinition(
                7,
                "water",
                true,
                new int[]{10,0}, //front
                new int[]{10,0}, //back
                new int[]{10,0}, //right
                new int[]{10,0}, //left
                new int[]{10,0}, //top
                new int[]{10,0},  //bottom
                1,
                false,
                false,
                true,
                null,
                "",
                "",
                false,
                false,
                40,
                false
        );

        new BlockDefinition(
                8,
                "coal ore",
                true,
                new int[]{11,0}, //front
                new int[]{11,0}, //back
                new int[]{11,0}, //right
                new int[]{11,0}, //left
                new int[]{11,0}, //top
                new int[]{11,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                9,
                "iron ore",
                true,
                new int[]{12,0}, //front
                new int[]{12,0}, //back
                new int[]{12,0}, //right
                new int[]{12,0}, //left
                new int[]{12,0}, //top
                new int[]{12,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                10,
                "gold ore",
                true,
                new int[]{13,0}, //front
                new int[]{13,0}, //back
                new int[]{13,0}, //right
                new int[]{13,0}, //left
                new int[]{13,0}, //top
                new int[]{13,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                11,
                "diamond ore",
                true,
                new int[]{14,0}, //front
                new int[]{14,0}, //back
                new int[]{14,0}, //right
                new int[]{14,0}, //left
                new int[]{14,0}, //top
                new int[]{14,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                12,
                "emerald ore",
                true,
                new int[]{15,0}, //front
                new int[]{15,0}, //back
                new int[]{15,0}, //right
                new int[]{15,0}, //left
                new int[]{15,0}, //top
                new int[]{15,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                13,
                "lapis lazuli",
                true,
                new int[]{16,0}, //front
                new int[]{16,0}, //back
                new int[]{16,0}, //right
                new int[]{16,0}, //left
                new int[]{16,0}, //top
                new int[]{16,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                14,
                "sapphire ore",
                true,
                new int[]{17,0}, //front
                new int[]{17,0}, //back
                new int[]{17,0}, //right
                new int[]{17,0}, //left
                new int[]{17,0}, //top
                new int[]{17,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                15,
                "ruby ore",
                true,
                new int[]{18,0}, //front
                new int[]{18,0}, //back
                new int[]{18,0}, //right
                new int[]{18,0}, //left
                new int[]{18,0}, //top
                new int[]{18,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                16,
                "cobblestone stair",
                true,
                new int[]{2,0}, //front
                new int[]{2,0}, //back
                new int[]{2,0}, //right
                new int[]{2,0}, //left
                new int[]{2,0}, //top
                new int[]{2,0},  //bottom
                2,
                true,
                true,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true
        );


        new BlockDefinition(
                17,
                "pumpkin",
                true,
                new int[]{19,0}, //front
                new int[]{19,0}, //back
                new int[]{19,0}, //right
                new int[]{19,0}, //left
                new int[]{20,0}, //top
                new int[]{20,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true
        );
        new BlockDefinition(
                18,
                "jack 'o lantern unlit",
                true,
                new int[]{21,0}, //front
                new int[]{19,0}, //back
                new int[]{19,0}, //right
                new int[]{19,0}, //left
                new int[]{20,0}, //top
                new int[]{20,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true
        );
        new BlockDefinition(
                19,
                "jack 'o lantern lit",
                true,
                new int[]{22,0}, //front
                new int[]{19,0}, //back
                new int[]{19,0}, //right
                new int[]{19,0}, //left
                new int[]{20,0}, //top
                new int[]{20,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true
        );

        //falling sand
        BlockModifier fallSand = new BlockModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 0) {
                    digBlock((int) pos.x, (int) pos.y, (int) pos.z);
                    addFallingEntity(new Vector3d(pos.x + 0.5d, pos.y, pos.z + 0.5d), new Vector3f(0, 0, 0), 20);
                }
            }
        };
        new BlockDefinition(
                20,
                "sand",
                true,
                new int[]{23,0}, //front
                new int[]{23,0}, //back
                new int[]{23,0}, //right
                new int[]{23,0}, //left
                new int[]{23,0}, //top
                new int[]{23,0},  //bottom
                1,
                true,
                false,
                false,
                fallSand,
                "sand_1",
                "sand_2",
                false,
                false,
                0,
                true
        );

        //door open
        blockShapeMap[5] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,0f,2f/16f,1f,1f}
                        }
                );



        new BlockDefinition(
                21,
                "doorOpenTop",
                false,
                new int[]{24,0}, //front
                new int[]{24,0}, //back
                new int[]{24,0}, //right
                new int[]{24,0}, //left
                new int[]{24,0}, //top
                new int[]{24,0},  //bottom
                5,
                true,
                false,
                false,
                new BlockModifier() {
                    @Override
                    public void onDig(Vector3d pos) throws Exception {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, 23,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, 24,rot);
                            playSound("door_close", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true
        );

        new BlockDefinition(
                22,
                "doorOpenBottom",
                false,
                new int[]{25,0}, //front
                new int[]{25,0}, //back
                new int[]{25,0}, //right
                new int[]{25,0}, //left
                new int[]{25,0}, //top
                new int[]{25,0},  //bottom
                5,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) throws Exception {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, 23,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, 24,rot);
                            playSound("door_close", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true
        );

        //door closed
        blockShapeMap[6] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,14f/16f,1f,1f,1f}
                        }
                );

        new BlockDefinition(
                23,
                "doorClosedTop",
                false,
                new int[]{24,0}, //front
                new int[]{24,0}, //back
                new int[]{24,0}, //right
                new int[]{24,0}, //left
                new int[]{24,0}, //top
                new int[]{24,0},  //bottom
                6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) throws Exception {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, 21,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, 22,rot);
                            playSound("door_open", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true
        );

        new BlockDefinition(
                24,
                "doorClosedBottom",
                false,
                new int[]{25,0}, //front
                new int[]{25,0}, //back
                new int[]{25,0}, //right
                new int[]{25,0}, //left
                new int[]{25,0}, //top
                new int[]{25,0},  //bottom
                6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) throws Exception {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, 21,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, 22,rot);
                            playSound("door_open", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true
        );

        new BlockDefinition(
                25,
                "Tree",
                true,
                new int[]{26,0}, //front
                new int[]{26,0}, //back
                new int[]{26,0}, //right
                new int[]{26,0}, //left
                new int[]{27,0}, //top
                new int[]{27,0},  //bottom
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true
        );

        new BlockDefinition(
                26,
                "Leaves",
                true,
                new int[]{28,0}, //front
                new int[]{28,0}, //back
                new int[]{28,0}, //right
                new int[]{28,0}, //left
                new int[]{28,0}, //top
                new int[]{28,0},  //bottom
                4, //allfaces
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true
        );
    }

    public static BlockDefinition getBlockDefinition(int ID){
        return blockIDs[ID];
    }

    public static BlockDefinition getBlockDefinition(String name){
        for(BlockDefinition thisBlockDefinition : blockIDs){
            if (thisBlockDefinition.name.equals(name)){
                return thisBlockDefinition;
            }
        }
        return null;
    }

    public static boolean blockHasOnRightClickCall(int ID){
        return(blockIDs[ID].isRightClickable && blockIDs[ID].blockModifier != null);
    }

    public static float[] getFrontTexturePoints(int ID, byte rotation){
        switch (rotation){
            case 0:
                return blockIDs[ID].frontTexture;
            case 1:
                return blockIDs[ID].rightTexture;
            case 2:
                return blockIDs[ID].backTexture;
            case 3:
                return blockIDs[ID].leftTexture;
        }
        return blockIDs[ID].frontTexture;
    }
    public static float[] getBackTexturePoints(int ID, byte rotation){
        switch (rotation){
            case 0:
                return blockIDs[ID].backTexture;
            case 1:
                return blockIDs[ID].leftTexture;
            case 2:
                return blockIDs[ID].frontTexture;
            case 3:
                return blockIDs[ID].rightTexture;
        }

        return blockIDs[ID].backTexture;
    }
    public static float[] getRightTexturePoints(int ID, byte rotation){
        switch (rotation){
            case 0:
                return blockIDs[ID].rightTexture;
            case 1:
                return blockIDs[ID].backTexture;
            case 2:
                return blockIDs[ID].leftTexture;
            case 3:
                return blockIDs[ID].frontTexture;
        }
        return blockIDs[ID].rightTexture;
    }
    public static float[] getLeftTexturePoints(int ID, byte rotation){
        switch (rotation){
            case 0:
                return blockIDs[ID].leftTexture;
            case 1:
                return blockIDs[ID].frontTexture;
            case 2:
                return blockIDs[ID].rightTexture;
            case 3:
                return blockIDs[ID].backTexture;
        }
        return blockIDs[ID].leftTexture;
    }

    public static boolean isBlockLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static float getBlockViscosity(int ID){
        return blockIDs[ID].viscosity;
    }
    public static float[] getTopTexturePoints(int ID){
        return blockIDs[ID].topTexture;
    }
    public static float[] getBottomTexturePoints(int ID){
        return blockIDs[ID].bottomTexture;
    }

    public static boolean isBlockPointable(int ID){
        return blockIDs[ID].pointable;
    }
}
