package com.lauriethefish.betterportals.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

// Useful functions for dealing with vectors
public class MathUtils {
    public static final double EPSILON = 0.000001;

    public static Vector round(Vector vec)  {
        return new Vector(Math.round(vec.getX()), Math.round(vec.getY()), Math.round(vec.getZ()));
    }

    public static Vector abs(Vector vec)    {
        return new Vector(Math.abs(vec.getX()), Math.abs(vec.getY()), Math.abs(vec.getZ()));
    }

    public static boolean greaterThanEq(Vector a, Vector b)   {
        return a.getX() >= b.getX() && a.getY() >= b.getY() && a.getZ() >= b.getZ();
    }

    public static boolean lessThanEq(Vector a, Vector b)   {
        return a.getX() <= b.getX() && a.getY() <= b.getY() && a.getZ() <= b.getZ();
    }
    
    public static Vector moveToCenterOfBlock(Vector vec)  {
        return new Vector(Math.floor(vec.getX()) + 0.5, Math.floor(vec.getY()) + 0.5, Math.floor(vec.getZ()) + 0.5);
    }

    public static Location moveToCenterOfBlock(Location loc)  {
        return new Location(loc.getWorld(), Math.floor(loc.getX()) + 0.5, Math.floor(loc.getY()) + 0.5, Math.floor(loc.getZ()) + 0.5);
    }
}
