package jfcraft.block;

/** Nether Portal
 *
 * @author pquiring
 *
 * Created : Mar 31, 2014
 */

import java.util.ArrayList;

import javaforce.gl.*;

import jfcraft.client.*;
import jfcraft.data.*;
import jfcraft.entity.*;
import jfcraft.opengl.*;
import static jfcraft.data.Direction.*;

public class BlockNetherPortal extends BlockBase {
  private ArrayList<Box> xyBoxes, zyBoxes;
  private static GLModel model;
  public BlockNetherPortal(String id, String names[], String images[]) {
    super(id, names, images);
    isOpaque = false;
    isAlpha = true;
    isComplex = true;
    isSolid = false;
    isDir = true;
    isDirXZ = true;
    emitLight = 15;
    dropID = 0;
    resetBoxes(Type.BOTH);
    xyBoxes = new ArrayList<Box>();
    xyBoxes.add(new Box(0,0,4, 16,16,16-4));
    zyBoxes = new ArrayList<Box>();
    zyBoxes.add(new Box(4,0,0, 16-4,16,16));
    model = Assets.getModel("portal").model;
  }

  public void buildBuffers(RenderDest dest, RenderData data) {
    RenderBuffers buf = dest.getBuffers(buffersIdx);
    buildBuffers(model.getObject("PORTAL"), buf, data, textures[0]);
  }

  public ArrayList<Box> getBoxes(Coords c, Type type) {
    if (type == Type.ENTITY) return boxListEmpty;
    int bits = c.chunk.getBits(c.gx, c.gy, c.gz);
    int dir = Chunk.getDir(bits);
    if (dir == N || dir == S) {
      //xy plane
      return xyBoxes;
    } else {
      //zy plane
      return zyBoxes;
    }
  }
  private static final int maxSize = 15;
  public void destroy(Client client, Coords c, boolean doDrop) {
    //must destroy all adjacent portal blocks
    Coords p = c.clone();
    int bits = c.chunk.getBits(c.gx, c.gy, c.gz);
    int dir = Chunk.getDir(bits);
    int dx,dy,dz;
    if (dir == N) {
      //xy plane
      while (c.chunk.getID(p.gx-1, p.gy, p.gz) == id) {
        p.gx--;
        p.x--;
      }
      while (c.chunk.getID(p.gx, p.gy-1, p.gz) == id) {
        p.gy--;
        p.y--;
      }
      dx = 1;
      for(int x=1;x<maxSize;x++) {
        if (c.chunk.getID(p.gx + x, p.gy, p.gz) != id) break;
        dx++;
      }
      dy = 1;
      for(int y=1;y<maxSize;y++) {
        if (c.chunk.getID(p.gx, p.gy + y, p.gz) != id) break;
        dy++;
      }
      for(int x=0;x<dx;x++) {
        for(int y=0;y<dy;y++) {
          c.chunk.clearBlock(p.gx + x, p.gy + y, p.gz);
          Static.server.broadcastClearBlock(c.chunk.dim, p.x + x, p.y + y, p.z);
        }
      }
    } else {
      //zy plane
      while (c.chunk.getID(p.gx, p.gy, p.gz-1) == id) {
        p.gz--;
        p.z--;
      }
      while (c.chunk.getID(p.gx, p.gy-1, p.gz) == id) {
        p.gy--;
        p.y--;
      }
      dz = 1;
      for(int z=1;z<maxSize;z++) {
        if (c.chunk.getID(p.gx, p.gy, p.gz + z) != id) break;
        dz++;
      }
      dy = 1;
      for(int y=1;y<maxSize;y++) {
        if (c.chunk.getID(p.gx, p.gy + y, p.gz) != id) break;
        dy++;
      }
      for(int z=0;z<dz;z++) {
        for(int y=0;y<dy;y++) {
          c.chunk.clearBlock(p.gx, p.gy + y, p.gz + z);
          Static.server.broadcastClearBlock(c.chunk.dim, p.x, p.y + y, p.z + z);
        }
      }
    }
  }
  private Coords c = new Coords();
  public void tick(Chunk chunk, Tick tick) {
    //check if portal is still intact
    int bits = chunk.getBits(tick.x, tick.y, tick.z);
    int dir = Chunk.getDir(bits);
    char tid;
    if (dir == N || dir == S) {
      //xy plane
      tid = chunk.getID(tick.x+1, tick.y, tick.z);
      if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
        destroy(null, tick.toWorldCoords(chunk, c), true);
        return;
      }
      tid = chunk.getID(tick.x-1, tick.y, tick.z);
      if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
        destroy(null, tick.toWorldCoords(chunk, c), true);
        return;
      }
    } else {
      //zy plane
      tid = chunk.getID(tick.x, tick.y, tick.z+1);
      if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
        destroy(null, tick.toWorldCoords(chunk, c), true);
        return;
      }
      tid = chunk.getID(tick.x, tick.y, tick.z-1);
      if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
        destroy(null, tick.toWorldCoords(chunk, c), true);
        return;
      }
    }
    tid = chunk.getID(tick.x, tick.y-1, tick.z);
    if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
      destroy(null, tick.toWorldCoords(chunk, c), true);
      return;
    }
    tid = chunk.getID(tick.x, tick.y+1, tick.z);
    if (tid != Blocks.PORTAL && tid != Blocks.OBSIDIAN) {
      destroy(null, tick.toWorldCoords(chunk, c), true);
      return;
    }
    super.tick(chunk, tick);
  }

  public void etick(EntityBase e, Coords c) {
    if (e.teleportTimer > 0) {
      e.teleportTimer = 20;
    } else {
      Coords p = c.clone();
      Static.log("teleport:" + c);
      Static.server.teleport(e, p, Dims.NETHER);
    }
  }

  /** Find or create portal in current dim.
   * @param e = entity already in new dim
   * @param c = coords of portal block from old dimension
   */
  public void teleport(EntityBase e, Coords c) {
    Static.log("teleport:" + e + "@" + c);
    Coords p = c.clone();
    int dir = Chunk.getDir(p.bits);
    //for now just convert coords as is
    Chunk chunk = Static.server.world.chunks.getChunk2(e.dim, p.cx, p.cz, true, true, true);
    if (chunk.getID(p.gx, p.gy, p.gz) == Blocks.PORTAL) {
      //found existing portal
      while (chunk.getID(p.gx, p.gy-1, p.gz) == Blocks.PORTAL) {
        p.gy--;
        p.y--;
        e.pos.y-=1.0f;
      }
    } else {
      //create portal at entity coords
      Static.log("no portal found in other dim, creating new portal");
      p.gy--;
      if (dir == N) {
        //xy plane
        p.gx--;
        for(int x=0;x<=3;x++) {
          for(int y=0;y<=4;y++) {
            for(int z=-2;z<=2;z++) {
              if (z == 0) {
                if (x == 0 || x == 3 || y == 0 || y == 4) {
                  chunk.setBlock(p.gx + x, p.gy + y, p.gz, Blocks.OBSIDIAN, 0);
                } else {
                  chunk.setBlock(p.gx + x, p.gy + y, p.gz, Blocks.PORTAL, Chunk.makeBits(dir, 0));
                }
              } else {
                if (x != 0 && x != 3 && y != 0 && y != 4) {
                  chunk.clearBlock(p.gx + x, p.gy + y, p.gz + z);
                }
              }
            }
          }
        }
      } else {
        //zy plane
        p.gz--;
        for(int z=0;z<=3;z++) {
          for(int y=0;y<=4;y++) {
            for(int x=-2;x<=2;x++) {
              if (x == 0) {
                if (z == 0 || z == 3 || y == 0 || y == 4) {
                  chunk.setBlock(p.gx, p.gy + y, p.gz + z, Blocks.OBSIDIAN, 0);
                } else {
                  chunk.setBlock(p.gx, p.gy + y, p.gz + z, Blocks.PORTAL, Chunk.makeBits(dir, 0));
                }
              } else {
                if (z != 0 && z != 3 && y != 0 && y != 4) {
                  chunk.clearBlock(p.gx + x, p.gy + y, p.gz + z);
                }
              }
            }
          }
        }
      }
    }
    chunk.addEntity(e);
  }
}
