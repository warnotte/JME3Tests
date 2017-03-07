package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication  {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    private BulletAppState bulletAppState;
 
    Node loadedNode=null;
    
    @Override
    public void simpleInitApp() {
    // For new versions thereafter
    
    initCrossHairs();
    flyCam.setEnabled(true);

    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    bulletAppState.setDebugEnabled(true);
    
       /* Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
        */
       
         /** Load a Node from a .j3o file */
        //String userHome = System.getProperty("user.home");
        BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(assetManager);
        File file = new File("assets/Scenes/newScene.j3o");
        loadedNode=null; 
        try {
          loadedNode = (Node)importer.load(file);
          loadedNode.setName("loaded node");
          
        Geometry sph = (Geometry) loadedNode.getChild("Box");
        ((Geometry) sph).getMesh().scaleTextureCoordinates(new Vector2f(8,8));
          
        rootNode.attachChild(loadedNode);
        /* Make the floor physical with mass 0.0f! */
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
        BoxCollisionShape colshape = new BoxCollisionShape(sph.getLocalScale());
        
        floor_phy.setCollisionShape(colshape);
        sph.addControl(floor_phy);
        
        bulletAppState.getPhysicsSpace().add(floor_phy);
          
          
        } catch (IOException ex) {
          Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "No saved node loaded.", ex);
        } 

        
      
            /** A white, directional light source */ 
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun); 
        
    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    
    DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 2048, 2);
    dlsf.setLight(sun);
    fpp.addFilter(dlsf);
    
    
     /** Show scattered light beams when camera looks into "sun". */
    LightScatteringFilter sunlight = new LightScatteringFilter(new Vector3f(.5f,.5f,.5f).multLocal(-3000));
    fpp.addFilter(sunlight);
    viewPort.addProcessor(fpp); 
    
    
    inputManager.addMapping("Shoot", 
				new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
				new MouseButtonTrigger(0)); // trigger 2: left-button click
    inputManager.addListener(actionListener, "Shoot");
    
  

    }
     
    private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Shoot") && !keyPressed) {
        {
            //Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
            // 1. Reset results list.
            CollisionResults results = new CollisionResults();
            // 2. Aim the ray from cam loc to cam direction.

            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            // 3. Collect intersections between Ray and Shootables in results list.
            loadedNode.collideWith(ray, results);
            if (results.size()!=0)
            {
                float dist = results.getClosestCollision().getDistance();
                Vector3f pt = results.getClosestCollision().getContactPoint();
                int tri = results.getClosestCollision().getTriangleIndex();
                //    Vector3f  norm = results.getCollision(i).getTriangle(new Triangle()).getNormal();
                String hit = results.getClosestCollision().getGeometry().getName();
                
                System.err.println("Geometry name : " + hit);
                System.err.println("Dst : " + dist);
                System.err.println("XYZ : " + pt);
                
                pt.z += 0.1f;
                
                
                //addBox(pt);
                
                makeBrick(pt);
            }
        }
      }
    }

    
        
  };
    
    /** This method creates one individual physical brick. */
  public void makeBrick(Vector3f loc) {
      Box b = new Box(0.25f, 0.25f, 0.25f);
      
    /** Create a brick geometry and attach to scene graph. */
    Geometry brick_geo = new Geometry("brick", b);
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
    brick_geo.setLocalTranslation(loc);
    brick_geo.setMaterial(mat);
    rootNode.attachChild(brick_geo);
    /** Position the brick geometry  */
    
    /** Make brick physical with a mass > 0.0f. */
    RigidBodyControl brick_phy = new RigidBodyControl(0.5f);
    /** Add physical brick to physics space. */
    brick_geo.addControl(brick_phy);
    brick_phy.setPhysicsLocation(loc);
    bulletAppState.getPhysicsSpace().add(brick_phy);
  }

    private void addBox(Vector3f pt) {
        Box b = new Box(0.25f, 0.25f, 0.25f);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(pt);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    /** A plus sign used as crosshairs to help the player with aiming.*/
  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }

}
