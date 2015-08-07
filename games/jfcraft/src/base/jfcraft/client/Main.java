package jfcraft.client;

/**
 *
 * @author pquiring
 */

import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import javaforce.JF;
import jfcraft.data.Static;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import javaforce.gl.GL;

import jfcraft.opengl.*;

public class Main {
  // We need to strongly reference callback instances.
  private GLFWErrorCallback errorCallback;
  private GLFWKeyCallback keyCallback;
  private GLFWCursorPosCallback mousePosCallback;
  private GLFWMouseButtonCallback mouseButtonCallback;
  private GLFWScrollCallback scrollCallback;
  private GLFWWindowSizeCallback windowSizeCallback;
  private GLFWWindowCloseCallback windowCloseCallback;

  private float mx, my;
  private int mb;

  // The window handle
  private long window;

  public void run() {
    main = this;
    try {
      init();
      loop();
      // Release window and window callbacks
      glfwDestroyWindow(window);
      keyCallback.release();
      mousePosCallback.release();
      mouseButtonCallback.release();
      scrollCallback.release();
      windowSizeCallback.release();
      windowCloseCallback.release();
    } finally {
      // Terminate GLFW and release the GLFWerrorfun
      glfwTerminate();
      errorCallback.release();
    }
  }

  private void init() {
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (glfwInit() != GL11.GL_TRUE) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure our window
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

    int WIDTH = 512;
    int HEIGHT = 512;

    // Create the window
    window = glfwCreateWindow(WIDTH, HEIGHT, "jfCraft", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    // Get the resolution of the primary monitor
    ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    // Center our window
    glfwSetWindowPos(
      window,
      (GLFWvidmode.width(vidmode) - WIDTH) / 2,
      (GLFWvidmode.height(vidmode) - HEIGHT) / 2
    );

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
//    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);

    // Load GL functions via JavaForce
    GL.glInit();

    // Init the game rendering engine
    new RenderEngine(new Loading()).init();

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
      public void invoke(long window, int key, int scancode, int action, int mods) {
//        Static.log("     key:" + key + "," + scancode + "," + action + "," + mods);
        //convert to VK
        boolean press = action > 0;
        if (press && key > 13 && key < 128) {
          Static.video.keyTyped((char)key);
        }
        if (key >= 65 && key <= 90) {
          //A-Z
          key -= 65;
          key += VK.VK_A;
          if (press) {
            Static.video.keyPressed(key, false);
          } else {
            Static.video.keyReleased(key, false);
          }
          return;
        }
        if (key >= 48 && key <= 57) {
          if (press) {
            Static.video.keyPressed(key, false);
          } else {
            Static.video.keyReleased(key, false);
          }
          return;
        }
        if (key >= 290 && key <= 301) {
          key -= 290;
          key += VK.VK_F1;
          if (press) {
            Static.video.keyPressed(key, false);
          } else {
            Static.video.keyReleased(key, false);
          }
          return;
        }
        if (press) {
          switch (key) {
            case 341: Static.video.keyPressed(VK.VK_CONTROL, false); break;
            case 345: Static.video.keyPressed(VK.VK_CONTROL, true); break;
            case 340: Static.video.keyPressed(VK.VK_SHIFT, false); break;
            case 344: Static.video.keyPressed(VK.VK_SHIFT, true); break;
            case 342: Static.video.keyPressed(VK.VK_ALT, false); break;
            case 346: Static.video.keyPressed(VK.VK_ALT, true); break;
            case 256: Static.video.keyPressed(VK.VK_ESCAPE, false); break;
            case 265: Static.video.keyPressed(VK.VK_UP, false); break;
            case 264: Static.video.keyPressed(VK.VK_DOWN, false); break;
            case 263: Static.video.keyPressed(VK.VK_LEFT, false); break;
            case 262: Static.video.keyPressed(VK.VK_RIGHT, false); break;
            case 32: Static.video.keyPressed(VK.VK_SPACE, false); break;
            case 259:
              Static.video.keyPressed(VK.VK_BACKSPACE, false);
              Static.video.keyTyped((char)8);
              break;
            case 261: Static.video.keyPressed(VK.VK_DELETE, false); break;
            case 268: Static.video.keyPressed(VK.VK_HOME, false); break;
            case 269: Static.video.keyPressed(VK.VK_END, false); break;
          }
        } else {
          switch (key) {
            case 341: Static.video.keyReleased(VK.VK_CONTROL, false); break;
            case 345: Static.video.keyReleased(VK.VK_CONTROL, true); break;
            case 340: Static.video.keyReleased(VK.VK_SHIFT, false); break;
            case 344: Static.video.keyReleased(VK.VK_SHIFT, true); break;
            case 342: Static.video.keyReleased(VK.VK_ALT, false); break;
            case 346: Static.video.keyReleased(VK.VK_ALT, true); break;
            case 256: Static.video.keyReleased(VK.VK_ESCAPE, false); break;
            case 265: Static.video.keyReleased(VK.VK_UP, false); break;
            case 264: Static.video.keyReleased(VK.VK_DOWN, false); break;
            case 263: Static.video.keyReleased(VK.VK_LEFT, false); break;
            case 262: Static.video.keyReleased(VK.VK_RIGHT, false); break;
            case 32: Static.video.keyReleased(VK.VK_SPACE, false); break;
            case 259: Static.video.keyReleased(VK.VK_BACKSPACE, false); break;
            case 261: Static.video.keyReleased(VK.VK_DELETE, false); break;
            case 268: Static.video.keyReleased(VK.VK_HOME, false); break;
            case 269: Static.video.keyReleased(VK.VK_END, false); break;
          }
        }
      }
    });

    glfwSetCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
      public void invoke(long window, int button, int action, int mods) {
//        Static.log("mouseBut:" + button + "," + action + "," + mods);
        switch (button) {
          case 0: button = 1; break;
          case 1: button = 3; break;
          default: return;
        }
        if (action == 1) {
          mb = button;
          Static.video.mouseDown(mx, my, button);
        } else {
          mb = 0;
          Static.video.mouseUp(mx, my, button);
        }
      }
    });

    glfwSetCallback(window, mousePosCallback = new GLFWCursorPosCallback() {
      public void invoke(long window, double x, double y) {
//        Static.log("mousePos:" + x + "," + y);
        mx = (float) x;
        my = (float) y;
        Static.video.mouseMove(mx, my, mb);
      }
    });

    glfwSetCallback(window, windowSizeCallback = new GLFWWindowSizeCallback() {
      public void invoke(long window, int x, int y) {
        Static.video.resize(x, y);
      }
    });

    glfwSetCallback(window, windowCloseCallback = new GLFWWindowCloseCallback() {
      public void invoke(long window) {
        Static.video.windowClosed();
      }
    });

    glfwSetCallback(window, scrollCallback = new GLFWScrollCallback() {
      public void invoke(long window, double x, double y) {
        if (JF.isMac()) {
          //for Mac users
          Static.video.mouseScrolled((int)y);
        } else {
          //for everyone else
          Static.video.mouseScrolled((int)-y);
        }
      }
    });
  }

  private void loop() {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the ContextCapabilities instance and makes the OpenGL
    // bindings available for use.
    GLContext.createFromCurrent();

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (glfwWindowShouldClose(window) == GL_FALSE) {
      Static.video.render();
      // Poll for window events. The key callback above will only be
      // invoked during this call.
      glfwPollEvents();
    }
  }

  public static void main(String[] args) {
    new Main().run();
  }

  public static Main main;

  private void _swap() {
    glfwSwapBuffers(window); // swap the color buffers
  }

  public static void swap() {
    main._swap();
  }

  private void _setCursor(boolean state) {
    if (state) {
      glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    } else {
      glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
  }

  public static void setCursor(boolean state) {
    main._setCursor(state);
  }
}
