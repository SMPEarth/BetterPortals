package com.lauriethefish.betterportals.events;

import java.util.List;

import com.lauriethefish.betterportals.BetterPortals;
import com.lauriethefish.betterportals.PortalDirection;
import com.lauriethefish.betterportals.PortalPos;
import com.lauriethefish.betterportals.VisibilityChecker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.util.Vector;

// This event is called whenever a portal is created, either by a player,
// or other source
// This class deals with creating the portal in the BetterPortals format
public class PortalCreate implements Listener {
    private BetterPortals pl;
    public PortalCreate(BetterPortals pl)   {
        this.pl = pl;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        // If the portal was created by the vanilla portal generator (due to a player manually generating one)
        // then return from this event. This should not happen, but this is here in case it does
        if(event.getReason() == CreateReason.OBC_DESTINATION)   {
            return;
        }

        // Get all of the blocks associated with the portal. This includes some of the obsidian and all of the portal blocks
        List<Block> blocks = event.getBlocks();

        // Find the portal block closest to the bottom left and top right, this is used for positioning the portal
        Vector largestLocation = null;
        Vector smallestLocation = null;

        // Loop through all of the associated blocks
        for(Block block : blocks)   {
            // If the block is obsidian, skip it as we only care about portal blocks
            if(block.getType() == Material.OBSIDIAN)  {continue;}

            // Get the position of the portal as a block vector, so that all of the coodinates are rounded down
            Vector blockLoc = block.getLocation().toVector().toBlockVector();

            // Update the bottom left block
            if(smallestLocation == null || VisibilityChecker.vectorGreaterThan(smallestLocation, blockLoc)) {
                smallestLocation = blockLoc;
            }
            if(largestLocation == null || VisibilityChecker.vectorGreaterThan(blockLoc, largestLocation))   {
                largestLocation = blockLoc;
            }
        }

        // Get the direction of the portal, based on wheather the blocks are on the same z coordinate
        PortalDirection direction = largestLocation.getZ() == smallestLocation.getZ() ? PortalDirection.EAST_WEST : PortalDirection.NORTH_SOUTH;
        // Get the location of the bottom left of the portal blocks
        Location location = smallestLocation.toLocation(event.getWorld());

        // Get the size of the portal on the x and y coordinates, this requires flipping them if the portal faces north/south
        Vector portalSize = VisibilityChecker.orientVector(direction, largestLocation.clone().subtract(smallestLocation))
            .add(new Vector(1.0, 1.0, 0.0));

        // Check that the portal is smaller than the max size
        Vector maxPortalSize = pl.config.maxPortalSize;
        if(portalSize.getX() > maxPortalSize.getX() || portalSize.getY() > maxPortalSize.getY())    {
            // Cancel the event
            event.setCancelled(true);
            return;
        }

        // Subtract 1 from the x and y of the location to get the location relative to the bottom left block of obisidan
        // This changes to z and y if the portal is oriented north/south
        location.subtract(VisibilityChecker.orientVector(direction, new Vector(1.0, 1.0, 0.0)));

        // Find a suitable location for spawning the portal
        Location spawnLocation = pl.spawningSystem.findSuitablePortalLocation(location, direction, portalSize);
        
        // If no location found - due to no link existing with this world,
        // cancel the event and return
        if(spawnLocation == null)   {
            event.setCancelled(true);
            return;
        }

        // Spawn a portal in the opposite world and the right location
        pl.spawningSystem.spawnPortal(spawnLocation, direction, portalSize);
        // Fill in any missing corners of the current portal with stone,
        // because lack of corners can break the illusion
        pl.spawningSystem.fixPortalCorners(location.clone(), direction, portalSize);

        // Add to the portals position, as the PlayerRayCast requires coordinates to be at
        // the absolute center of the portal
        // Swap around the x and z offsets if the portal is facing a different direction
        Vector portalAddAmount = VisibilityChecker.orientVector(direction, portalSize.clone().multiply(0.5).add(new Vector(1.0, 1.0, 0.5)));
        location.add(portalAddAmount);
        spawnLocation.add(portalAddAmount);

        // Add the two new ends of the portal to the rayCastingSystem,
        // so that the portal effect can be active!
        pl.rayCastingSystem.portals.add(new PortalPos(pl,
            location.clone(), direction,
            spawnLocation.clone(), direction, portalSize
        ));
        pl.rayCastingSystem.portals.add(new PortalPos(pl,
            spawnLocation, direction,
            location, direction, portalSize
        ));
    }    
}