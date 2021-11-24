package com.lauriethefish.betterportals.bukkit.nms;

import com.lauriethefish.betterportals.bukkit.util.VersionUtil;
import com.lauriethefish.betterportals.shared.util.NewReflectionUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Used to add simple marker tags to items, for example, the portal wand
 */
public class NBTTagUtil {
    private static final String MARKER_PREFIX = "BetterPortals_marker_";
    private static final String MARKER_VALUE = "marked";
    private static final Class<?> CRAFT_ITEM_STACK = MinecraftReflectionUtil.findCraftBukkitClass("inventory.CraftItemStack");

    private static final Class<?> NBT_TAG_STRING;
    private static final Class<?> NBT_TAG_COMPOUND;
    private static final Class<?> ITEM_STACK;
    private static final Class<?> NBT_BASE;

    private static final Method HAS_TAG;
    private static final Method GET_TAG;
    private static final Method GET_STRING;
    private static final Method TAG_SET;
    private static final Method AS_NMS_COPY;
    private static final Method AS_BUKKIT_COPY;
    private static final Constructor<?> STRING_TAG_CTOR;
    private static final Constructor<?> TAG_COMPOUND_CTOR;

    static  {
        if(VersionUtil.isMcVersionAtLeast("1.17.0")) {
            NBT_TAG_STRING = NewReflectionUtil.findClass("net.minecraft.nbt.NBTTagString");
            NBT_TAG_COMPOUND = NewReflectionUtil.findClass("net.minecraft.nbt.NBTTagCompound");
            ITEM_STACK = NewReflectionUtil.findClass("net.minecraft.world.item.ItemStack");
            NBT_BASE = NewReflectionUtil.findClass("net.minecraft.nbt.NBTBase");
        }   else    {
            NBT_TAG_STRING = MinecraftReflectionUtil.findVersionedNMSClass("NBTTagString");
            NBT_TAG_COMPOUND = MinecraftReflectionUtil.findVersionedNMSClass("NBTTagCompound");
            ITEM_STACK = MinecraftReflectionUtil.findVersionedNMSClass("ItemStack");
            NBT_BASE = NewReflectionUtil.findClass("NBTBase");
        }

        HAS_TAG = NewReflectionUtil.findMethod(ITEM_STACK, "hasTag");
        GET_TAG = NewReflectionUtil.findMethod(ITEM_STACK, "getTag");
        GET_STRING = NewReflectionUtil.findMethod(NBT_TAG_COMPOUND, "getString", String.class);
        TAG_SET = NewReflectionUtil.findMethod(NBT_TAG_COMPOUND, "set", String.class, NBT_BASE);
        AS_NMS_COPY = NewReflectionUtil.findMethod(CRAFT_ITEM_STACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = NewReflectionUtil.findMethod(CRAFT_ITEM_STACK, "asBukkitCopy", ITEM_STACK);
        STRING_TAG_CTOR = NewReflectionUtil.findConstructor(NBT_TAG_STRING, String.class);
        TAG_COMPOUND_CTOR = NewReflectionUtil.findConstructor(NBT_TAG_COMPOUND);
    }

    /**
     * Adds a marker NBT tag to <code>item</code>.
     * @param item Item to add the tag to
     * @param name Name of the tag
     * @return A new {@link ItemStack} with the tag. (original is unmodified)
     */
    @NotNull
    public static ItemStack addMarkerTag(@NotNull ItemStack item, @NotNull String name) {
        Object nmsItem = getNMSItemStack(item);

        // Get the NBT tag, or create one if the item doesn't have one
        Object itemTag = ((boolean) NewReflectionUtil.invokeMethod(nmsItem, HAS_TAG)) ? NewReflectionUtil.invokeMethod(nmsItem, GET_TAG) : NewReflectionUtil.invokeConstructor(TAG_COMPOUND_CTOR);
        Object stringValue = NewReflectionUtil.invokeConstructor(STRING_TAG_CTOR, MARKER_VALUE);

        NewReflectionUtil.invokeMethod(itemTag, TAG_SET, MARKER_PREFIX + name, stringValue); // Set the value

        return getBukkitItemStack(nmsItem);
    }

    /**
     * Checks if <code>item</code> has a marker tag with <code>name</code>.
     * @param item The item to check
     * @param name The name of the NBT marker tag
     * @return Whether it has the tag
     */
    public static boolean hasMarkerTag(@NotNull ItemStack item, @NotNull String name)	{
        Object nmsItem = getNMSItemStack(item);

        if(!(boolean) NewReflectionUtil.invokeMethod(nmsItem, HAS_TAG)) {return false;} // Return null if it has no NBT data
        Object itemTag = NewReflectionUtil.invokeMethod(nmsItem, GET_TAG); // Otherwise, get the item's NBT tag

        String value = (String) NewReflectionUtil.invokeMethod(itemTag, GET_STRING, MARKER_PREFIX + name);

        return MARKER_VALUE.equals(value); // Return the value of the key
    }

    @NotNull
    private static Object getNMSItemStack(@NotNull ItemStack item) {
        return NewReflectionUtil.invokeMethod(null, AS_NMS_COPY, item);
    }

    @NotNull
    private static ItemStack getBukkitItemStack(@NotNull Object nmsItem) {
        return (ItemStack) NewReflectionUtil.invokeMethod(null, AS_BUKKIT_COPY, nmsItem);
    }
}
