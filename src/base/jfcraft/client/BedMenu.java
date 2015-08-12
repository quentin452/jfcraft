package jfcraft.client;

/** Bed Menu
 *
 * @author pquiring
 *
 * Created : Sept 24, 2014
 */

import static javaforce.gl.GL.*;

import jfcraft.opengl.*;
import jfcraft.data.*;

public class BedMenu extends RenderScreen {
  public BedMenu() {
    id = Client.BED;
  }

  public void init() {
    super.init();
    addButton("Leave Bed", 56, 390, 400, new Runnable() {public void run() {
      Static.client.clientTransport.leaveMenu();
      leaveMenu();
    }});
  }

  public void setup() {
    setCursor(true);
    Static.inGame = false;
  }

  public void render(int width, int height) {
    Static.game.render(width, height);
    setMenuSize(512, 512);
    reset();
    glUniformMatrix4fv(Static.uniformMatrixView, 1, GL_FALSE, Static.identity.m);  //view matrix
    glUniformMatrix4fv(Static.uniformMatrixModel, 1, GL_FALSE, Static.identity.m);  //model matrix
    renderShade();
    setOrtho();
    renderButtons();
    renderText();
  }

  public void resize(int width, int height) {
    Static.game.resize(width, height);
  }

  public void mousePressed(int x, int y, int button) {
    super.mousePressed(x, y, button);
  }

  public void mouseReleased(int x, int y, int button) {
  }

  public void mouseMoved(int x, int y, int button) {
  }

  public void mouseWheel(int delta) {
  }
}