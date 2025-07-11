package net.dravigen.tesseractUtils.utils;

import net.dravigen.tesseractUtils.TesseractUtilsAddon;
import net.dravigen.tesseractUtils.enums.EnumConfig;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

import static net.dravigen.tesseractUtils.utils.PacketUtils.*;
import static net.minecraft.src.CommandBase.getPlayer;

public class ListsUtils {

    public static Map<Class<?>, String> CLASS_TO_STRING_MAPPING;

    public static Map<String,String> blocksMap = new HashMap<>();
    public static Map<String,String> itemsMap = new HashMap<>();
    public static Map<String,Byte> potionsMap = new HashMap<>();
    public static Map<String,Short> entitiesMap = new HashMap<>();


    public record SavedBlock(int x, int y, int z, int id, int meta) {}

    public static ListsUtils instance;

    public static ListsUtils getInstance() {
        return instance == null ? (new ListsUtils()) : instance;
    }

    public static MovingObjectPosition getBlockPlayerIsLooking(ICommandSender sender) {
        EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
        Vec3 var3 = player.getPosition(TesseractUtilsAddon.partialTick);
        var3.yCoord += player.getEyeHeight();
        Vec3 var4 = player.getLook(TesseractUtilsAddon.partialTick);
        int reach = Integer.parseInt(playersInfoServer.get(player.getEntityName()).get(EnumConfig.REACH.ordinal()));
        Vec3 var5 = var3.addVector(var4.xCoord * reach, var4.yCoord * reach, var4.zCoord * reach);
        return player.worldObj.clip(var3, var5);
    }

    public static BlockInfo getRandomBlockFromOdds(List<BlockInfo> blocks) {
        Random rand = new Random();
        int totalOdd=0;
        for (BlockInfo block: blocks){
            totalOdd+=block.odd;
        }
        int randNum = rand.nextInt(totalOdd)+1;
        int blockPosInList=0;
        for (int i = 0; i< blocks.size(); i++){
            if (blocks.get(i).odd<randNum){
                randNum-=blocks.get(i).odd;
            }else {
                blockPosInList = i;
                break;
            }
        }

        return blocks.get(blockPosInList);
    }

    public static void initBlocksNameList() {
        blocksMap.clear();
        blocksMap.put("Air", "0");
        List<ItemStack> subBlocks = new ArrayList<>();
        Map<String,String> map = new HashMap<>();
        for (int i = 0; i < Block.blocksList.length; i++) {
            Block block = Block.blocksList[i];
            if (block == null||block.blockID==74) continue;
            try {
                block.getSubBlocks(block.blockID, null, subBlocks);
                if (subBlocks.isEmpty()) {
                    subBlocks.add(new ItemStack(block));
                }
            } catch (Throwable e) {
                System.err.println("Error getting sub-blocks for Block ID " + i + " (" + block.getClass().getName() + "): " + e.getMessage());
                subBlocks.clear();
                subBlocks.add(new ItemStack(block));
            }
        }
        for (ItemStack stack : subBlocks) {
            if (stack != null&&stack.getItem() != null) {
                String entry = stack.getDisplayName().replace(" ", "_");
                if (entry.contains("Old")||entry.contains("BlockCandle")||entry.contains("null")) continue;
                String previousString = "";
                for (ItemStack stack2 : subBlocks) {
                    if (stack2==null||stack2.getItem()==null||subBlocks.indexOf(stack)==subBlocks.indexOf(stack2))continue;
                    String name = stack2.getDisplayName().replace(" ", "_");
                    if (name.equalsIgnoreCase(entry)) {
                        if (stack.itemID == stack2.itemID) {
                            entry += (stack.getHasSubtypes() ? "/" + stack.getItemDamage() : "");
                        } else {
                            entry += "|" + stack.itemID;
                        }
                        break;
                    }
                }
                String finalItemName = entry.replace("tile.", "").replace("fc", "").replace("name.", "").replace(".name", "").replace("item.", "").replace("btw:", "").replace(".siding", "").replace(".corner", "").replace(".moulding", "");
                map.put(finalItemName, stack.itemID + "/" + stack.getItemDamage());
            }
        }
        blocksMap.putAll(map);
    }

    public static @NotNull List<String> getBlockNameList(String[] strings, String username) {
        List<String> finalList = new ArrayList<>();
        String var1 = strings[strings.length - 1];
        String var2 = ";";
        boolean afterSemiColon = var2.regionMatches(true, 0, var1, var1.length()-1, 1);

        for (String string : playersBlocksMapServer.get(username).keySet()) {
            List<String> split = new ArrayList<>(List.of(strings[strings.length - 1].split(";")));
            String lastString = split.get(split.size()-1);
            if (!afterSemiColon) split.remove(split.size()-1);
            String firstString = afterSemiColon ? var1 : split.isEmpty() ? "" : String.join(";",split)+";";

            if (afterSemiColon){
                lastString = "";
            }
            if (string.split("=")[0].toLowerCase().startsWith(lastString.toLowerCase())){
                finalList.add(firstString+string.split("=")[0]);
            }
        }
        for (String string : playersBlocksMapServer.get(username).keySet()) {
            List<String> split = new ArrayList<>(List.of(strings[strings.length - 1].split(";")));
            String lastString = split.get(split.size()-1);
            if (!afterSemiColon) split.remove(split.size()-1);
            String firstString = afterSemiColon ? var1 : split.isEmpty() ? "" : String.join(";",split)+";";

            if (afterSemiColon){
                lastString = "";
            }
            if (!string.split("=")[0].toLowerCase().startsWith(lastString.toLowerCase())&&string.split("=")[0].toLowerCase().contains(lastString.toLowerCase())){
                finalList.add(firstString+string.split("=")[0]);
            }
        }
        return finalList;
    }

    public static List<BlockInfo> getBlockInfo(String string, String username) {
        List<String> differentBlock = Arrays.stream(string.split(";")).toList();
        List<BlockInfo> blocksInfos = new ArrayList<>();
        for (String diff:differentBlock){
            Block block;
            String blockName = "";
            int id = -1;
            int meta=0;
            String[] blockNodd = diff.split(":");
            int odd = blockNodd.length==2 ? Integer.parseInt(blockNodd[1]) : 1;
            String[] idMeta = blockNodd[0].split("/");
            try {
                id = Integer.parseInt(idMeta[0]);
                if (idMeta.length==2){
                    meta= Integer.parseInt(idMeta[1]);
                }
            } catch (NumberFormatException ignored) {
                try {
                    String[] idMetaF = playersBlocksMapServer.get(username).get(blockNodd[0]).split("/");
                    id = Integer.parseInt(idMetaF[0]);
                    meta = Integer.parseInt(idMetaF[1]);
                    if (id!=0&&new ItemStack(id, 0, 0).getHasSubtypes() && idMeta.length == 2) {
                        meta = Integer.parseInt(idMeta[1]);
                    }
                } catch (Exception ignored1) {
                    return null;
                }
            }
            if (id!=0) {
                blockName = new ItemStack(id, 1, meta).getDisplayName().replace("tile.", "").replace("fc", "").replace(".name", "").replace("item.", "").replace("btw:", "").replace(".siding", "").replace(".corner", "").replace(".moulding", "");
            }else {
                blockName = "Air";
            }
            blocksInfos.add(new BlockInfo(blockName,id,meta,odd));
        }
        return blocksInfos;
    }

    public static void initItemsNameList() {
        itemsMap.clear();
        List<Item> itemList = new ArrayList<>();
        List<ItemStack> subItems = new ArrayList<>();
        Map<String,String> map = new HashMap<>();
        for (Item item : Item.itemsList) {
            if (item != null) itemList.add(item);
        }
        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);
            if (item == null || item.itemID == 74) continue;
            try {
                item.getSubItems(item.itemID, null, subItems);
                if (subItems.isEmpty() && !item.getHasSubtypes()) {
                    subItems.add(new ItemStack(item, 1, 0));
                }
            } catch (Throwable e) {
                System.err.println("Error getting sub-items for Item ID " + i + " (" + item.getClass().getName() + "): " + e.getMessage());
                subItems.clear();
                subItems.add(new ItemStack(item, 1, 0));
            }
        }
        for (ItemStack stack : subItems) {
            if (stack != null&&stack.getItem() != null) {
                String entry = stack.getDisplayName().replace(" ", "_");
                if (entry.contains("Old")||entry.contains("BlockCandle")) continue;
                String previousString = "";
                for (ItemStack stack2 : subItems) {
                    if (stack2==null||stack2.getItem()==null||subItems.indexOf(stack)==subItems.indexOf(stack2))continue;
                    String name = stack2.getDisplayName().replace(" ", "_");
                    if (name.equalsIgnoreCase(entry)) {
                        if (stack.itemID == stack2.itemID) {
                            entry += (stack.getHasSubtypes() ? "/" + stack.getItemDamage() : "");
                        } else {
                            entry += "|" + stack.itemID;
                        }
                        break;
                    }
                }
                String finalItemName = entry.replace("tile.", "").replace("fc", "").replace(".name", "").replace("item.", "").replace("btw:", "").replace(".siding", "").replace(".corner", "").replace(".moulding", "");
                map.put(finalItemName, stack.itemID + "/" + stack.getItemDamage());
            }
        }
        itemsMap.putAll(map);
    }

    public @NotNull List<String> getItemNameList(String[] strings, String username) {
        List<String > finalList = new ArrayList<>();
        for (String string: playersItemsNameMapServer.get(username).keySet()){
            if (string.split("=")[0].toLowerCase().startsWith(strings[1].toLowerCase())){
                finalList.add(string.split("=")[0]);
            }
        }
        for (String string: playersItemsNameMapServer.get(username).keySet()){
            if (string.split("=")[0].toLowerCase().contains(strings[1].toLowerCase())&&!string.split("=")[0].toLowerCase().startsWith(strings[1].toLowerCase())){
                finalList.add(string.split("=")[0]);
            }
        }
        return finalList;
    }

    public static ItemInfo getItemInfo(String[] strings, String username) {
        Item item;
        int id = 99999;
        int meta = 0;
        String itemName="";
        String[] idMeta = strings[1].split("/");
        try {
            id = Integer.parseInt(idMeta[0]);
            if (idMeta.length==2){
                meta= Integer.parseInt(idMeta[1]);
            }
        } catch (NumberFormatException ignored) {
            try {
                String[] idMetaF = playersItemsNameMapServer.get(username).get(strings[1]).split("/");
                id = Integer.parseInt(idMetaF[0]);
                meta = Integer.parseInt(idMetaF[1]);
                if (new ItemStack(id, 0, 0).getHasSubtypes() && idMeta.length == 2) {
                    meta = Integer.parseInt(idMeta[1]);
                }
            } catch (Exception ignored1) {
                return null;
            }
        }
        return new ItemInfo(id,meta,strings[1]);
    }

    public static void initEntityList(){
        entitiesMap.clear();
        if (CLASS_TO_STRING_MAPPING!=null) {
            for (Map.Entry<Class<?>, String> entry : CLASS_TO_STRING_MAPPING.entrySet()) {
                Class<?> entityClass = entry.getKey();
                String base = CLASS_TO_STRING_MAPPING.get(entityClass);
                String unlocalizedName = "entity." + base + ".name";
                String translated = StringTranslate.getInstance().translateKey(unlocalizedName);
                if (translated.equalsIgnoreCase(unlocalizedName)) {
                    translated = base;
                }
                String finalName = translated.replace("Entity", "").replace("addon", "").replace("fc", "").replace(StringTranslate.getInstance().translateKey("entity.villager.name"), "").replace("DireWolf", "The_Beast").replace("JungleSpider", "Jungle_Spider").replace("arrow", "Arrow").replace(" ", "_");
                entitiesMap.put(finalName, (short) EntityList.getEntityIDFromClass(entityClass));
            }
        }
    }

    public @NotNull List<String> getEntityName(String[] strings, String username) {
        List<String> finalList = new ArrayList<>();
        String var1 = strings[strings.length - 1];
        String var2 = ":";
        boolean afterColon = var2.regionMatches(true, 0, var1, var1.length()-1, 1);

        for (String string : playersEntitiesNameMapServer.get(username).keySet()) {
            List<String> split = new ArrayList<>(List.of(strings[strings.length - 1].split(":")));
            String lastString = split.get(split.size()-1);
            if (!afterColon) split.remove(split.size()-1);
            String firstString = afterColon ? var1 : split.isEmpty() ? "" : String.join(":",split)+":";

            if (afterColon){
                lastString = "";
            }
            if (string.toLowerCase().startsWith(lastString.toLowerCase())){
                finalList.add(firstString+string);
            }
        }
        return finalList;
    }

    public static void initPotionList(){
        for (Potion potion:Potion.potionTypes){
            if (potion==null)continue;
            String name = StringTranslate.getInstance().translateKey(potion.getName());
            potionsMap.put(name.replace(" ","_"), (byte) potion.id);
        }
    }

    public record BlockInfo(String blockName, int id, int meta, int odd) { }

    public record ItemInfo (int id, int meta, String itemName){ }

    public static class BlockPos {
        public int x, y, z;
        public BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPos blockPos = (BlockPos) o;
            return x == blockPos.x && y == blockPos.y && z == blockPos.z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    public static List<BlockPos> findConnectedBlocksInPlane(World world, int startX, int startY, int startZ, int referenceBlockId, int referenceBlockMeta, int clickedFace, boolean fuzzy, int extrudeLimit) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        BlockPos startPos = new BlockPos(startX, startY, startZ);

        queue.add(startPos);
        visited.add(startPos);

        int[][] planeOffsets;
        if (fuzzy){
            switch (clickedFace) {
                case 0,1: //tranversal
                    planeOffsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}};
                    break;
                case 2,3: //frontal
                    planeOffsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0},{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}};
                    break;
                case 4,5: //sagittal
                    planeOffsets = new int[][]{{0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},{0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
                    break;
                default:
                    return foundBlocks;
            }
        }else switch (clickedFace) {
            case 0: // Bottom face clicked (+Y direction of extrusion) -> plane is XZ (Y is constant)
            case 1: // Top face clicked (-Y direction of extrusion) -> plane is XZ (Y is constant)
                planeOffsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}}; // Check X,Z neighbors
                break;
            case 2: // North face clicked (+Z direction of extrusion) -> plane is XY (Z is constant)
            case 3: // South face clicked (-Z direction of extrusion) -> plane is XY (Z is constant)
                planeOffsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}}; // Check X,Y neighbors
                break;
            case 4: // West face clicked (+X direction of extrusion) -> plane is YZ (X is constant)
            case 5: // East face clicked (-X direction of extrusion) -> plane is YZ (X is constant)
                planeOffsets = new int[][]{{0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}; // Check Y,Z neighbors
                break;
            default:
                return foundBlocks;
        }
        while (!queue.isEmpty() && foundBlocks.size() < extrudeLimit) {
            BlockPos current = queue.poll();
            foundBlocks.add(current);
            for (int[] offset : planeOffsets) {
                int nextX = current.x + offset[0];
                int nextY = current.y + offset[1];
                int nextZ = current.z + offset[2];
                BlockPos neighbor = new BlockPos(nextX, nextY, nextZ);
                if (nextY < 0 || nextY >= world.getHeight()) continue;
                int newBlockX = nextX;
                int newBlockY = nextY;
                int newBlockZ = nextZ;

                switch (clickedFace) {
                    case 0: newBlockY--; break; // Bottom face: place below
                    case 1: newBlockY++; break; // Top face: place above
                    case 2: newBlockZ--; break; // North face: place North
                    case 3: newBlockZ++; break; // South face: place South
                    case 4: newBlockX--; break; // West face: place West
                    case 5: newBlockX++; break; // East face: place East
                }
                int targetBlockId = world.getBlockId(newBlockX, newBlockY, newBlockZ);
                if (!visited.contains(neighbor)&&targetBlockId==0) {
                    int neighborId = world.getBlockId(nextX, nextY, nextZ);
                    int neighborMeta = world.getBlockMetadata(nextX, nextY, nextZ);
                    if (neighborId == referenceBlockId&& neighborMeta == referenceBlockMeta) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
        return foundBlocks;
    }

    private static final int[][] CARDINAL_OFFSETS = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    private static final int[][] FUZZY_3D_OFFSETS;
    static {
        List<int[]> offsetsList = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    offsetsList.add(new int[]{dx, dy, dz});
                }
            }
        }
        FUZZY_3D_OFFSETS = offsetsList.toArray(new int[0][]);
    }

    public static List<BlockPos> findConnectedBlocksIn3D(World world, int startX, int startY, int startZ, int referenceBlockId, int referenceBlockMeta, boolean fuzzy, int extrudeLimit) {

        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos startPos = new BlockPos(startX, startY, startZ);

        queue.add(startPos);
        visited.add(startPos);

        int[][] neighborsOffsets = fuzzy ? FUZZY_3D_OFFSETS : CARDINAL_OFFSETS;
        while (!queue.isEmpty() && foundBlocks.size() < extrudeLimit) {
            BlockPos current = queue.poll();
            foundBlocks.add(current);
            for (int[] offset : neighborsOffsets) {
                int nextX = current.x + offset[0];
                int nextY = current.y + offset[1];
                int nextZ = current.z + offset[2];
                BlockPos neighbor = new BlockPos(nextX, nextY, nextZ);
                if (nextY < 0 || nextY >= world.getHeight()) continue;
                if (!visited.contains(neighbor)) {
                    int neighborId = world.getBlockId(nextX, nextY, nextZ);
                    int neighborMeta = world.getBlockMetadata(nextX, nextY, nextZ);
                    if (neighborId == referenceBlockId && neighborMeta == referenceBlockMeta) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
        return foundBlocks;
    }

    public static Map<String, Integer> sortMapInt(Map<String, Float> unsortedMap) {
        List<Map.Entry<String, Float>> listOfEntries = new ArrayList<>(unsortedMap.entrySet());
        listOfEntries.sort((entry1, entry2) -> {
            int valueComparison = Float.compare(entry1.getValue(), entry2.getValue());
            if (valueComparison == 0) return entry1.getKey().compareTo(entry2.getKey());
            return valueComparison;
        });
        Map<String, Integer> sortedMapByValue = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : listOfEntries) {
            float a = entry.getValue();
            Integer value = (int) a;
            sortedMapByValue.put(entry.getKey(), value);
        }
        return sortedMapByValue;
    }

    public static Map<String, Float> sortMapFloat(Map<String, Float> unsortedMap) {
        List<Map.Entry<String, Float>> listOfEntries = new ArrayList<>(unsortedMap.entrySet());
        listOfEntries.sort((entry1, entry2) -> {
            int valueComparison = Float.compare(entry1.getValue(), entry2.getValue());
            if (valueComparison == 0) return entry1.getKey().compareTo(entry2.getKey());
            return valueComparison;
        });
        Map<String, Float> sortedMapByValue = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : listOfEntries) {
            sortedMapByValue.put(entry.getKey(), entry.getValue());
        }
        return sortedMapByValue;
    }

    public static <K> Map<K, String> sortMapByStringValues(Map<K, String> map) {
        return sortMapByStringValues(map, null);
    }
    public static <K> Map<K, String> sortMapByStringValues(Map<K, String> map, Comparator<String> valueComparator) {
        if (map == null || map.isEmpty()) {
            return new LinkedHashMap<>();
        }

        List<Map.Entry<K, String>> entryList = new ArrayList<>(map.entrySet());

        Comparator<Map.Entry<K, String>> entryComparator;
        if (valueComparator == null) {
            entryComparator = Map.Entry.comparingByValue();
        } else {
            entryComparator = Map.Entry.comparingByValue(valueComparator);
        }

        entryList.sort(entryComparator);

        Map<K, String> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, String> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}

