package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.export.binary.BinaryExporter;
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
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements PhysicsCollisionListener {
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    private BulletAppState bulletAppState;
    
    Node loadedNode = null;
    
    RayTracePathTracer tracer = null;
    
    private boolean Raytrace;
    
    DirectionalLight sun;
    private WaterFilter water;
    
    private RigidBodyControl ball_phy;
    private Sphere ball;
    Material ball_mat;
    ParticleEmitter fire;
    
    Map<ParticleEmitter, Long> list_pe = new HashMap<>();
    
    BitmapText helloText;
    
    int paused = 0;
    
    //Node world;
    
    @Override
    public void simpleInitApp() {
        // For new versions thereafter
 
        /** Write text on the screen (HUD) */
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        helloText = new BitmapText(guiFont, false);
        helloText.setSize(guiFont.getCharSet().getRenderedSize());
        helloText.setText("Hello World");
        helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);
        
        
        initCrossHairs();
        flyCam.setEnabled(true);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        initKeys();
        
        getCamera().setLocation(new Vector3f(9.125253f, 3.074246f, 7.440006f));
        getCamera().lookAt(new Vector3f(-0.7466748f, -0.2017244f, -0.6338644f), Vector3f.ZERO);

        /**
         * Load a Node from a .j3o file
         */
        BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(assetManager);
        
        loadedNode = (Node) assetManager.loadModel("Scenes/newScene.j3o");
        loadedNode.setName("loaded node");
        Geometry sph = (Geometry) loadedNode.getChild("Box");
        ((Geometry) sph).getMesh().scaleTextureCoordinates(new Vector2f(8, 8));
        rootNode.attachChild(loadedNode);

        /* Make the floor physical with mass 0.0f! */
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
        BoxCollisionShape colshape = new BoxCollisionShape(sph.getLocalScale());
        
        floor_phy.setCollisionShape(colshape);
        sph.addControl(floor_phy);
        
        bulletAppState.getPhysicsSpace().add(floor_phy);
        
        initLights();
        initFilters();
        initWall(new Vector3f(0,0,0));
        
        fire = (ParticleEmitter) rootNode.getChild("Emitter");
        fire.scale(0.1f);
        
        //TODO : Rename Collision listener...
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }
    
    
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Raytrace") && !keyPressed) {
                Raytrace = true;
            }
            
            if (name.equals("CreateCube") && !keyPressed) {
                action_createCube();
            }
            
            if (name.equals("CreateWall") && !keyPressed) {       
                action_createWall();             
            }
            
            if (name.equals("Shoot") && !keyPressed) {
                action_shoot();
            }
            
            if (name.equals("Pause") && !keyPressed) {
                action_pause();
            }
            
            if (name.equals("Save") && !keyPressed) {
                action_save();
            }
            if (name.equals("Load") && !keyPressed) {
                action_load();
            }
            
        }

       
        

        
       
    };
    
     private void action_save() {
            String userHome = System.getProperty("user.home");
            BinaryExporter exporter = BinaryExporter.getInstance();
            File file = new File(userHome+"\\MyModel.j3o");
            try {
              exporter.save(rootNode, file);
              System.err.println("save to : "+file);
                      
            } catch (IOException ex) {
              //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error: Failed to save game!", ex);
              ex.printStackTrace();
            }
        }
        
        private void action_load() {
            String userHome = System.getProperty("user.home");
            assetManager.registerLocator(userHome, FileLocator.class);
            Node loadedNode = (Node)assetManager.loadModel("MyModel.j3o");
            loadedNode.setName("loaded node");
            rootNode.detachAllChildren();
            rootNode.attachChild(loadedNode);
        }
        
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        
        // Explosion particle remover
        Iterator it = list_pe.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Entry<ParticleEmitter, Long> entry = (Entry<ParticleEmitter, Long>) it.next();
            ParticleEmitter next = entry.getKey();
            long creationtime = entry.getValue();
            long currenttime = System.currentTimeMillis();
            float elapseinms = (float)(currenttime-creationtime)/1000.f;
            if (elapseinms > 1.5)
            {
                rootNode.detachChild(next);
                next.killAllParticles();
                it.remove();
            }
            i++;
        }
       
        helloText.setText("Hello World : "+System.currentTimeMillis());
        
        if (Raytrace == true) {
            tracer = new RayTracePathTracer(rootNode, cam, 180, 180);
            tracer.show();
            boolean toto = true;
            while (toto) {
                tracer.update(sun.getDirection(), assetManager);//pl.getPosition());
            }
            //pl.getPosition());
            Raytrace = false;
        }
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);
    }
    
    private void initKeys() {
        inputManager.addMapping("CreateCube",
                new KeyTrigger(KeyInput.KEY_1),
                new MouseButtonTrigger(0));        
        inputManager.addListener(actionListener, "CreateCube");
        
        inputManager.addMapping("CreateWall",
                new KeyTrigger(KeyInput.KEY_2),
                new MouseButtonTrigger(2));        
        inputManager.addListener(actionListener, "CreateWall");
        
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_3),
                new MouseButtonTrigger(1));        
        inputManager.addListener(actionListener, "Shoot");
        
        inputManager.addMapping("Raytrace", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(actionListener, "Raytrace");
        
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(actionListener, "Pause");
        inputManager.addMapping("Save", new KeyTrigger(KeyInput.KEY_NUMPAD2));
        inputManager.addListener(actionListener, "Save");
        inputManager.addMapping("Load", new KeyTrigger(KeyInput.KEY_NUMPAD1));
        inputManager.addListener(actionListener, "Load");
        
        
        
    }

    /**
     * A plus sign used as crosshairs to help the player with aiming.
     */
    protected void initCrossHairs() {
        //guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
    
    private void initLights() {
        /**
         * A white, directional light source
         */
        sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
    
    private void initFilters() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 2048, 2);
        dlsf.setLight(sun);
        fpp.addFilter(dlsf);

        /**
         * Show scattered light beams when camera looks into "sun".
         */
        LightScatteringFilter sunlight = new LightScatteringFilter(sun.getDirection().mult(-3000));
        fpp.addFilter(sunlight);
        
        SSAOFilter ssaoFilter = new SSAOFilter();
        //SSAOFilter ssaoFilter = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
        fpp.addFilter(ssaoFilter);
        //viewPort.addProcessor(fpp);

        water = new WaterFilter(rootNode, sun.getDirection());
        
        water.setWaterHeight(-4.0f);
        water.setUseFoam(true);
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Textures/foam.jpg"));
        
        fpp.addFilter(water);

        //Depth of field Filter
        DepthOfFieldFilter dof = new DepthOfFieldFilter();
        dof.setFocusDistance(0);
        dof.setFocusRange(100);
        fpp.addFilter(dof);
    }
    
    private float getRnd(float d) {
        return (float) (Math.random() - 0.5) * d;
    }
    
    
    private void action_createWall() {
        
        //Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.

        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        rootNode.collideWith(ray, results);
        //         loadedNode.collideWith(ray, results);
        if (results.size() != 0) {
            float dist = results.getClosestCollision().getDistance();
            Vector3f pt = results.getClosestCollision().getContactPoint();
            int tri = results.getClosestCollision().getTriangleIndex();
            //    Vector3f  norm = results.getCollision(i).getTriangle(new Triangle()).getNormal();
            String hit = results.getClosestCollision().getGeometry().getName();
            
            System.err.println("Geometry name : " + hit);
            System.err.println("Dst : " + dist);
            System.err.println("XYZ : " + pt);
            
            // Orientation ??! : Faut ptet creer un node en fait pour pouvoir rotate facilement ?!
            pt.y += 0.01f;

            initWall(pt);
        }
        
           
    }
    
    private void action_createCube() {
        //Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.

        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        rootNode.collideWith(ray, results);
        //         loadedNode.collideWith(ray, results);
        if (results.size() != 0) {
            float dist = results.getClosestCollision().getDistance();
            Vector3f pt = results.getClosestCollision().getContactPoint();
            int tri = results.getClosestCollision().getTriangleIndex();
            //    Vector3f  norm = results.getCollision(i).getTriangle(new Triangle()).getNormal();
            String hit = results.getClosestCollision().getGeometry().getName();
            
            System.err.println("Geometry name : " + hit);
            System.err.println("Dst : " + dist);
            System.err.println("XYZ : " + pt);
            
            float sx, sy, sz;
            float maxDev = 0.5f;
            sx = 0.25f + getRnd(maxDev);
            sy = 0.25f + getRnd(maxDev);
            sz = 0.25f + getRnd(maxDev);
            
            pt.y += sy;
            
            makeBrick(pt, sx, sy, sz);
        }
    }

    private void action_shoot() {
        /**
         * This method creates one individual physical cannon ball. By defaul,
         * the ball is accelerated and flies from the camera position in the
         * camera direction.
         */
        
        if (ball == null) {
            ball = new Sphere(32, 32, 0.25f, true, false);
            ball.setTextureMode(TextureMode.Projected);
            ball_mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        }

        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", ball);
        ball_geo.setShadowMode(ShadowMode.CastAndReceive);
        ball_geo.setMaterial(ball_mat);
        rootNode.attachChild(ball_geo);
        /**
         * Position the cannon ball
         */
        ball_geo.setLocalTranslation(cam.getLocation());
        /**
         * Make the ball physcial with a mass > 0.0f
         */
        ball_phy = new RigidBodyControl(1f);
        /**
         * Add physical ball to physics space.
         */
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /**
         * Accelerate the physcial ball to shoot it.
         */
        ball_phy.setLinearVelocity(cam.getDirection().mult(25));

        /*
        
        ParticleEmitter fire =  new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture("Textures/Effects/Explosion/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2);
        fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow

        fire.setLocalTranslation(cam.getLocation());
        fire.scale(0.5f);
        
        rootNode.attachChild(fire);
         */
    }
    
    
    private void action_pause() {
        switch(paused)
        {
            case 0:
                paused=1;
                bulletAppState.setEnabled(true);
                bulletAppState.setSpeed(0.25f);
                bulletAppState.getPhysicsSpace().setMaxSubSteps(16);
                break;
            case 1:
                paused=2;
                bulletAppState.setEnabled(false);
                break;
            case 2:
                paused=3;
                bulletAppState.setEnabled(true);
                bulletAppState.setSpeed(0.25f);
                bulletAppState.getPhysicsSpace().setMaxSubSteps(16);
                break;
            case 3:
                paused=0;
                bulletAppState.setEnabled(true);
                bulletAppState.setSpeed(1.0f);
                bulletAppState.getPhysicsSpace().setMaxSubSteps(4);
                break;
        }
        
    }
    
    float brickLength = 0.48f;
    float brickWidth = 0.24f;
    float brickHeight = 0.12f;

    /**
     * This loop builds a wall out of individual bricks.
     */
    public void initWall(Vector3f location) {

        /**
         * dimensions used for bricks and wall
         */
        Node nodewall= new Node("NodeWall");
        float startpt = brickLength / 4;
        float height = 0;
        for (int j = 0; j < 15; j++) {
            for (int i = 0; i < 6; i++) {
                Vector3f vt  = new Vector3f(i * brickLength * 2 + startpt, brickHeight + height, 0);
                Geometry geom = makeBrickWall(vt.add(location));
                nodewall.attachChild(geom);
            }
            startpt = -startpt;
            height += 2 * brickHeight;
        }
        rootNode.attachChild(nodewall);
    }

    /**
     * This method creates one individual physical brick.
     */
    public Geometry makeBrickWall(Vector3f loc) {
        
        Material wall_mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        /**
         * Initialize the brick geometry
         */
        Box box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, .5f));

        /**
         * Create a brick geometry and attach to scene graph.
         */
        Geometry brick_geo = new Geometry("brick", box);
        brick_geo.setShadowMode(ShadowMode.CastAndReceive);
        brick_geo.setMaterial(wall_mat);
        
        /**
         * Position the brick geometry
         */
        brick_geo.setLocalTranslation(loc);
        /**
         * Make brick physical with a mass > 0.0f.
         */
        RigidBodyControl brick_phy = new RigidBodyControl(2f);
        /**
         * Add physical brick to physics space.
         */
        brick_geo.addControl(brick_phy);
        bulletAppState.getPhysicsSpace().add(brick_phy);
        return brick_geo;
        
    }
    
        /**
     * This method creates one individual physical brick.
     */
    public void makeBrick(Vector3f loc, float sx, float sy, float sz) {
        
        Box b = new Box(sx, sy, sz);

        /**
         * Create a brick geometry and attach to scene graph.
         */
        Geometry brick_geo = new Geometry("brick", b);
        brick_geo.setShadowMode(ShadowMode.CastAndReceive);
        Material matN = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        brick_geo.setLocalTranslation(loc);
        brick_geo.setMaterial(matN);
        rootNode.attachChild(brick_geo);
        /**
         * Position the brick geometry
         */

        /**
         * Make brick physical with a mass > 0.0f.
         */
        RigidBodyControl brick_phy = new RigidBodyControl(0.5f);
        /**
         * Add physical brick to physics space.
         */
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
       
    
    public void addDebris(Vector3f position)
    {
        ParticleEmitter pe = createParticleDebris();
        list_pe.put(pe, System.currentTimeMillis());
        pe.setInWorldSpace(false);
        pe.setLocalTranslation(position);
        rootNode.attachChild(pe);
    }
    
    
    ParticleEmitter createParticleDebris()
    {
        ParticleEmitter debris = new ParticleEmitter("Debris "+list_pe.size(), ParticleMesh.Type.Triangle, 100);
        Material debris_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debris_mat.setTexture("Texture", assetManager.loadTexture("Textures/Effects/Explosion/Debris.png"));
        
        debris.setParticlesPerSec(0);
        debris.setMaterial(debris_mat);
        debris.setImagesX(3);
        debris.setImagesY(3); // 3x3 texture animation
        debris.setRotateSpeed(4);
        debris.setSelectRandomImage(true);
        debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
        debris.setStartColor(ColorRGBA.White);
        debris.setGravity(0, 6, 0);
        debris.getParticleInfluencer().setVelocityVariation(.60f);
        debris.setStartSize(0.05f);
        debris.setEndSize(0.001f);
        debris.emitAllParticles();
        debris.setShadowMode(ShadowMode.Cast);
        return debris;
    
    }
    
    @Override
        public void collision(PhysicsCollisionEvent event) {
            if ((event.getNodeA().getName().equalsIgnoreCase("cannon ball"))
                    || (event.getNodeB().getName().equalsIgnoreCase("cannon ball"))) {
                Geometry target = (Geometry) event.getNodeB();
                Geometry cball = (Geometry) event.getNodeA();
                if (event.getNodeB().getName().equalsIgnoreCase("cannon ball")) {
                    target = (Geometry) event.getNodeA();
                    cball = (Geometry) event.getNodeB();
                }
                if (target.getName().equalsIgnoreCase("Box")) return;
                RigidBodyControl rb = cball.getControl(RigidBodyControl.class);
                Vector3f pos = target.getLocalTranslation().clone();
                Vector3f lvel = rb.getLinearVelocity();
                float velomax = Math.max(lvel.z, Math.max(lvel.x, lvel.y));
                System.err.println("Velo "+velomax);
                if (velomax > 0.75)
                  addDebris(pos);
            }
        }
}
