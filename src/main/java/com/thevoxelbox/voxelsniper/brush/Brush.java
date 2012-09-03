package com.thevoxelbox.voxelsniper.brush;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;

import com.thevoxelbox.voxelsniper.HitBlox;
import com.thevoxelbox.voxelsniper.vData;
import com.thevoxelbox.voxelsniper.vMessage;
import com.thevoxelbox.voxelsniper.brush.perform.PerformBrush;
import com.thevoxelbox.voxelsniper.undo.vBlock;

/**
 * The abstract class Brush Base of all the brushes.
 * 
 * @author Piotr
 */
public abstract class Brush {

    /**
     * Pointer to the world the current action is being executed.
     */
    private World world;
    /**
     * Targeted reference point X.
     */
    private int blockPositionX;
    /**
     * Targeted reference point Y.
     */
    private int blockPositionY;
    /**
     * Targeted reference point Z.
     */
    private int blockPositionZ;
    /**
     * Brush'world Target Block Derived from getTarget().
     */
    private Block targetBlock;
    /**
     * Brush'world Target 'Last' Block Block at the face of the block clicked ColDerived from getTarget().
     */
    private Block lastBlock;
    /**
     * Brush'world private name.
     */
    private String name = "Undefined";

    /**
     * @param x
     * @param y
     * @param z
     * @return {@link Block}
     */
    public final Block clampY(final int x, final int y, final int z) {
        int _y = y;
        if (_y < 0) {
            _y = 0;
        } else if (y > this.getWorld().getMaxHeight()) {
            _y = this.getWorld().getMaxHeight();
        }

        return this.getWorld().getBlockAt(x, y, z);
    }

    /**
     * @return the name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return int
     */
    public abstract int getTimesUsed();

    /**
     * 
     * @param vm
     */
    public abstract void info(vMessage vm);

    /**
     * A Brush's custom command handler.
     * 
     * @param par
     *            Array of string containing parameters
     * @param v
     *            vSniper caller
     */
    public void parameters(final String[] par, final vData v) {
        v.sendMessage(ChatColor.DARK_GREEN + "This brush doesn't take any extra parameters.");
    }

    /**
     * 
     * @param action
     * @param v
     * @param heldItem
     * @param clickedBlock
     * @param clickedFace
     * @return boolean
     */
    public boolean perform(final Action action, final vData v, final Material heldItem, final Block clickedBlock, final BlockFace clickedFace) {
        switch (action) {
        case RIGHT_CLICK_AIR:
        case RIGHT_CLICK_BLOCK:
            switch (heldItem) {
            case ARROW:
                this.setTimesUsed(this.getTimesUsed() + 1);
                if (this.getTarget(v, clickedBlock, clickedFace)) {
                    this.updateScale();
                    if (this instanceof PerformBrush) {
                        ((PerformBrush) this).initP(v);
                    }
                    this.arrow(v);
                    return true;
                }
                break;

            case SULPHUR:
                this.setTimesUsed(this.getTimesUsed() + 1);
                if (this.getTarget(v, clickedBlock, clickedFace)) {
                    this.updateScale();
                    if (this instanceof PerformBrush) {
                        ((PerformBrush) this).initP(v);
                    }
                    this.powder(v);
                    return true;
                }
                break;

            default:
                return false;
            }
            break;

        case LEFT_CLICK_AIR:

            break;

        case LEFT_CLICK_BLOCK:

            break;

        case PHYSICAL:
            break;

        default:
            v.sendMessage(ChatColor.RED + "Something is not right. Report this to przerwap. (Perform Error)");
            return true;
        }
        return false;
    }

    /**
     * @param name
     *            the name to set
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * @param timesUsed
     */
    public abstract void setTimesUsed(int timesUsed);

    /**
     * 
     */
    public void updateScale() {
    }

    /**
     * The arrow action. Executed when a player RightClicks with an Arrow
     * 
     * @param v
     *            vSniper caller
     */
    protected void arrow(final vData v) {
    }

    /**
     * Returns the block at the passed coordinates.
     * 
     * @param ax
     *            X coordinate
     * @param ay
     *            Y coordinate
     * @param az
     *            Z coordinate
     * @return int
     */
    protected final int getBlockIdAt(final int ax, final int ay, final int az) {
        return this.getWorld().getBlockAt(ax, ay, az).getTypeId();
    }

    /**
     * @return the lastBlock
     */
    protected final Block getLastBlock() {
        return this.lastBlock;
    }

    /**
     * Overridable getTarget method.
     * 
     * @param v
     * @param clickedBlock
     * @param clickedFace
     * @return boolean
     */
    protected final boolean getTarget(final vData v, final Block clickedBlock, final BlockFace clickedFace) {
        this.setWorld(v.getWorld());
        if (clickedBlock != null) {
            this.setTargetBlock(clickedBlock);
            this.setLastBlock(clickedBlock.getRelative(clickedFace));
            if (this.getLastBlock() == null) {
                v.sendMessage(ChatColor.RED + "You clicked outside of your sniping range.");
                return false;
            }
            if (v.owner().isLightning()) {
                this.getWorld().strikeLightning(this.getTargetBlock().getLocation());
            }
            return true;
        } else {
            HitBlox hb = null;
            if (v.owner().isDistRestrict()) {
                hb = new HitBlox(v.owner().getPlayer(), this.getWorld(), v.owner().getRange());
                this.setTargetBlock(hb.getRangeBlock());
            } else {
                hb = new HitBlox(v.owner().getPlayer(), this.getWorld());
                this.setTargetBlock(hb.getTargetBlock());
            }
            if (this.getTargetBlock() != null) {
                this.setLastBlock(hb.getLastBlock());
                if (this.getLastBlock() == null) {
                    v.sendMessage(ChatColor.RED + "You clicked outside of your sniping range.");
                    return false;
                }
                if (v.owner().isLightning()) {
                    this.getWorld().strikeLightning(this.getTargetBlock().getLocation());
                }
                return true;
            } else {
                v.sendMessage(ChatColor.RED + "You clicked outside of your sniping range.");
                return false;
            }
        }
    }

    /**
     * @return the targetBlock
     */
    protected final Block getTargetBlock() {
        return this.targetBlock;
    }

    /**
     * @return the world
     */
    protected final World getWorld() {
        return this.world;
    }

    /**
     * The powder action. Executed when a player RightClicks with Gunpowder
     * 
     * @param v
     *            vSniper caller
     */
    protected void powder(final vData v) {
    }

    /**
     * 
     * @param v
     */
    protected final void setBlock(final vBlock v) {
        this.getWorld().getBlockAt(v.x, v.y, v.z).setTypeId(v.id);
    }

    /**
     * Sets the Id of the block at the passed coordinate.
     * 
     * @param t
     *            The id the block will be set to
     * @param ax
     *            X coordinate
     * @param ay
     *            Y coordinate
     * @param az
     *            Z coordinate
     */
    protected final void setBlockIdAt(final int t, final int ax, final int ay, final int az) {
        this.getWorld().getBlockAt(ax, ay, az).setTypeId(t);
    }

    /**
     * @param lastBlock
     *            the lastBlock to set
     */
    protected final void setLastBlock(final Block lastBlock) {
        this.lastBlock = lastBlock;
    }

    /**
     * @param targetBlock
     *            the targetBlock to set
     */
    protected final void setTargetBlock(final Block targetBlock) {
        this.targetBlock = targetBlock;
    }

    /**
     * @param world
     *            the world to set
     */
    protected final void setWorld(final World world) {
        this.world = world;
    }

    /**
     * @return the blockPositionX
     */
    protected int getBlockPositionX() {
        return blockPositionX;
    }

    /**
     * @param blockPositionX the blockPositionX to set
     */
    protected void setBlockPositionX(int blockPositionX) {
        this.blockPositionX = blockPositionX;
    }

    /**
     * @return the blockPositionY
     */
    protected int getBlockPositionY() {
        return blockPositionY;
    }

    /**
     * @param blockPositionY the blockPositionY to set
     */
    protected void setBlockPositionY(int blockPositionY) {
        this.blockPositionY = blockPositionY;
    }

    /**
     * @return the blockPositionZ
     */
    protected int getBlockPositionZ() {
        return blockPositionZ;
    }

    /**
     * @param blockPositionZ the blockPositionZ to set
     */
    protected void setBlockPositionZ(int blockPositionZ) {
        this.blockPositionZ = blockPositionZ;
    }
}
