package com.thevoxelbox.voxelsniper.brush;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;

import com.thevoxelbox.voxelsniper.vData;
import com.thevoxelbox.voxelsniper.vMessage;
import com.thevoxelbox.voxelsniper.undo.vBlock;

/**
 * 
 * @author Gavjenks, hack job from the other 2d rotation brush blockPositionY piotr
 */
// The X Y and Z variable names in this file do NOT MAKE ANY SENSE. Do not attempt to actually figure out what on earth is going on here. Just go to the
// original 2d horizontal brush if you wish to make anything similar to this, and start there. I didn't bother renaming everything.
public class Rot2Dvert extends Brush {

    protected int mode = 0;
    private int bsize;
    private int brushSize;
    private vBlock[][][] snap;
    private double se;

    private static int timesUsed = 0;

    public Rot2Dvert() {
        this.setName("2D Rotation");
    }

    @Override
    public final int getTimesUsed() {
        return Rot2Dvert.timesUsed;
    }

    @Override
    public final void info(final vMessage vm) {
        vm.brushName(this.getName());
    }

    @Override
    public final void parameters(final String[] par, final com.thevoxelbox.voxelsniper.vData v) {
        this.se = Math.toRadians(Double.parseDouble(par[1]));
        v.sendMessage(ChatColor.GREEN + "Angle set to " + this.se);
    }

    @Override
    public final void setTimesUsed(final int tUsed) {
        Rot2Dvert.timesUsed = tUsed;
    }

    private void getMatrix() {
        this.brushSize = (this.bsize * 2) + 1;

        this.snap = new vBlock[this.brushSize][this.brushSize][this.brushSize];

        final int derp = this.bsize;
        int sx = this.getBlockPositionX() - this.bsize;
        int sy = this.getBlockPositionY() - this.bsize;
        int sz = this.getBlockPositionZ() - this.bsize;
        // double bpow = Math.pow(bsize + 0.5, 2);
        for (int x = 0; x < this.snap.length; x++) {
            sz = this.getBlockPositionZ() - derp;
            // double xpow = Math.pow(x - bsize, 2);
            for (int z = 0; z < this.snap.length; z++) {
                sy = this.getBlockPositionY() - derp;
                // if (xpow + Math.pow(z - bsize, 2) <= bpow) {
                for (int y = 0; y < this.snap.length; y++) {
                    final Block b = this.clampY(sx, sy, sz); // why is this not sx + x, sy + y sz + z?
                    this.snap[x][y][z] = new vBlock(b);
                    b.setTypeId(0);
                    sy++;
                }
                // }
                sz++;
            }
            sx++;
        }
    }

    private void rotate(final vData v) {
        int xx;
        int zz;
        int yy;
        double newx;
        double newz;
        final double bpow = Math.pow(this.bsize + 0.5, 2);
        final double cos = Math.cos(this.se);
        final double sin = Math.sin(this.se);
        final boolean[][] doNotFill = new boolean[this.snap.length][this.snap.length];
        // I put y in the inside loop, since it doesn't have any power functions, should be much faster.
        // Also, new array keeps track of which x and z coords are being assigned in the rotated space so that we can
        // do a targeted filling of only those columns later that were left out.

        for (int x = 0; x < this.snap.length; x++) {
            xx = x - this.bsize;
            final double xpow = Math.pow(xx, 2);
            for (int z = 0; z < this.snap.length; z++) {
                zz = z - this.bsize;
                if (xpow + Math.pow(zz, 2) <= bpow) {
                    newx = (xx * cos) - (zz * sin);
                    newz = (xx * sin) + (zz * cos);

                    doNotFill[(int) newx + this.bsize][(int) newz + this.bsize] = true;

                    for (int y = 0; y < this.snap.length; y++) {
                        yy = y - this.bsize;

                        final vBlock vb = this.snap[y][x][z];
                        if (vb.id == 0) {
                            continue;
                        }
                        this.setBlockIdAt(vb.id, this.getBlockPositionX() + yy, this.getBlockPositionY() + (int) newx, this.getBlockPositionZ() + (int) newz);
                    }
                }
            }
        }
        int A;
        int B;
        int C;
        int D;
        int fx;
        int fy;
        int fz;
        int winner;
        for (int x = 0; x < this.snap.length; x++) {
            final double xpow = Math.pow(x - this.bsize, 2);
            fx = x + this.getBlockPositionX() - this.bsize;
            for (int z = 0; z < this.snap.length; z++) {
                if (xpow + Math.pow(z - this.bsize, 2) <= bpow) {
                    fz = z + this.getBlockPositionZ() - this.bsize;
                    if (!doNotFill[x][z]) {
                        // smart fill stuff

                        for (int y = 0; y < this.snap.length; y++) {
                            fy = y + this.getBlockPositionY() - this.bsize;
                            A = this.getBlockIdAt(fy, fx + 1, fz);
                            D = this.getBlockIdAt(fy, fx - 1, fz);
                            C = this.getBlockIdAt(fy, fx, fz + 1);
                            B = this.getBlockIdAt(fy, fx, fz - 1);
                            if (A == B || A == C || A == D) { // I figure that since we are already narrowing it down to ONLY the holes left behind, it should
                                                              // be fine to do all 5 checks needed to be legit about it.
                                winner = A;
                            } else if (B == D || C == D) {
                                winner = D;
                            } else {
                                winner = B; // blockPositionY making this default, it will also automatically cover situations where B = C;
                            }

                            this.setBlockIdAt(winner, fy, fx, fz);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected final void arrow(final com.thevoxelbox.voxelsniper.vData v) {
        this.setBlockPositionX(this.getTargetBlock().getX());
        this.setBlockPositionY(this.getTargetBlock().getY());
        this.setBlockPositionZ(this.getTargetBlock().getZ());

        this.bsize = v.brushSize;

        switch (this.mode) {
        case 0:
            this.getMatrix();
            this.rotate(v);
            break;

        default:
            v.owner().getPlayer().sendMessage(ChatColor.RED + "Something went wrong.");
            break;
        }
    }

    @Override
    protected void powder(final com.thevoxelbox.voxelsniper.vData v) {
    }
}
