package mygame;


import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Triangle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class RayTracePathTracer {

    private BufferedImage image;
    private BufferedImage image2;
    private Camera cam;
 //   private Spatial scene;
    
    private JFrame frame;
    private JLabel label;
    private JLabel label2;
    BufferedImage images[];
    BufferedImage IBL;
	private Spatial scene;
	int accum = 0;
	Vector3f imagePIXELS [][];
	int width, height;
    

    public RayTracePathTracer(Spatial scene, Camera cam, int width, int height){
    	image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
    	imagePIXELS = new Vector3f [width][height];
    	for (int x = 0; x < width; x++)
		{
    		for (int y = 0; y < height; y++)
    		{
    			imagePIXELS[x][y]=new Vector3f();
    		}
		}
    	this.width=width;
    	this.height=height;
    	
    	images = new BufferedImage[10];
   
       // try {
            //	images[0] = ImageIO.read(new File("data\\KAMEN.jpg"));
            //	images[1] = ImageIO.read(new File("data\\KAMEN-stup.jpg"));
            //	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\spherical1_probe.jpg"));
            //IBL = ImageIO.read(new File("F:\\Wax\\EnvMap\\Theatre_Center\\Theatre-Center_8k_TMap.jpg"));
            //IBL = ImageIO.read(new File("F:\\Wax\\EnvMap\\Alexs_Apartment\\ENV.jpg"));
            
            
            //	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\Barce_Rooftop_C_8k.jpg"));
            //	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\Alexs_Apartment\\Alexs_Apt_8k.jpg"));
       // } catch (IOException ex) {
       //     Logger.getLogger(RayTracePathTracer.class.getName()).log(Level.SEVERE, null, ex);
       // }
    		
		

        this.scene = scene;
        this.cam = cam;
    }
    
    public void resetBuffer()
    {
    	accum=0;
    	// TODO : faire une boucle ...
    	//imagePIXELS = new Vector3f [width][height];
    	for (int x = 0; x < width; x++)
		{
    		for (int y = 0; y < height; y++)
    		{
    			imagePIXELS[x][y].set(0,0,0);
    		}
		}
    }
    

    /**
	 * 
	 */
	protected void BufferToImage()
	{
		float min=Float.MAX_VALUE, max= Float.MIN_NORMAL;
		for (int y = 0; y < height; y++){
    		
            for (int x = 0; x < width; x++){
            	float R = imagePIXELS[x][y].x;///(float)accum;
            	float G = imagePIXELS[x][y].y;///(float)accum;
            	float B = imagePIXELS[x][y].z;///(float)accum;
            	if (R<min) min=R;
            	if (G<min) min=G;
            	if (B<min) min=B;
            	if (R>max) max=R;
            	if (G>max) max=G;
            	if (B>max) max=B;
            }
		}
		float delta = (max-min)/2.0f;
		
		System.err.println("Accum = "+accum);
		System.err.println("Min = "+min);
		System.err.println("Max = "+max);
		for (int y = 0; y < height; y++){
    		
            for (int x = 0; x < width; x++){
               	float R = (imagePIXELS[x][y].x-min)/(float)delta;
            	float G = (imagePIXELS[x][y].y-min)/(float)delta;
            	float B = (imagePIXELS[x][y].z-min)/(float)delta;
               	
     
            	//R/=accum;
            	//G/=accum;
            	//B/=accum;
            	
            	
            	
            	if (R>1.0f) R=1;
            	if (G>1.0f) G=1;
            	if (B>1.0f) B=1;

            	if (R<0.0f) R=0;
            	if (G<0.0f) G=0;
            	if (B<0.0f) B=0;
				
            	 int Color =
                    		((int)(R*255f)<<16)+
                    		((int)(G*255f)<<8)+
                    		((int)(B*255f));
            	        	image2.setRGB(x, height - y - 1, Color);
            }
		}
		label2.repaint();
	}

    
   

    public void show(){
        frame = new JFrame("HDR View");
        label = new JLabel(new ImageIcon(image));
        label2 = new JLabel(new ImageIcon(image2));
        frame.getContentPane().add(label);
        frame.getContentPane().add(label2);
        frame.setLayout(new FlowLayout());
        frame.pack();
        frame.setVisible(true);
    }

    Texture tex_ml;
	
    
    public void update(final Vector3f lightDir, AssetManager assetManager){
    	//System.err.println("Light dir == "+lightDir);
        final int w = image.getWidth();
        final int h = image.getHeight();

        final float wr = (float) cam.getWidth()  / image.getWidth();
        final float hr = (float) cam.getHeight() / image.getHeight();

        scene.updateGeometricState();

        //tex_ml = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        
        final Random rand = new Random();
        
       
        //Vector3f LightDir = new Vector3f(-0.2f, 0.6f,0.5f);
        int from=0; 
        int to = h; 
        
        // cannot be multithread it seems :( 

        		ooo(lightDir, w, h, wr, hr, rand, h*0/4, h*4/4);
        		/*ooo(lightDir, w, h, wr, hr, rand, h*0/4, h*1/4);
                ooo(lightDir, w, h, wr, hr, rand, h*1/4, h*1/2);
                ooo(lightDir, w, h, wr, hr, rand, h*1/2, h*3/4);
                ooo(lightDir, w, h, wr, hr, rand, h*3/4, h*4/4);*/
        		BufferToImage();
        		label.repaint();
        		


        
    }

	
	private void ooo(Vector3f lightDir, int w, int h, float wr, float hr, Random rand, int from, int to) {
		
		for (int y = from; y < to; y++)
		{
    		label.repaint();
    		int x = 0;
            for (x = 0; x < w; x++)
            {
            	image.setRGB(x, h - y - 1, 0);
            	/*int val = rand.nextInt(5);
            	if (val!=2)
            	{
            		continue;
            	}*/
            	Vector3f AccColor = new Vector3f(0,0,0);
            	float fragmentx = x;
            	float fragmenty = y;
            	//System.err.println("PrintX == "+x);
        	//for( fragmentx = x; fragmentx < x + 1.0f; fragmentx += 0.25f)
          	//   for( fragmenty = y; fragmenty < y + 1.0f; fragmenty += 0.25f)
            		   {
            		   
            		   
                Vector2f v = new Vector2f(fragmentx * wr,fragmenty * hr);
                Vector3f pos = cam.getWorldCoordinates(v, 0.0f);
                Vector3f dir = cam.getWorldCoordinates(v, 0.3f);
                dir.subtractLocal(pos).normalizeLocal();

                
                Vector3f finalCol=computeCol( lightDir, rand, pos, dir, false, false, true, true, 0);
				
			//	finalCol=finalCol.mult(2);
				
			/*	if (finalCol.r>1.0f)
					finalCol.r=1;
				if (finalCol.g>1.0f)
					finalCol.g=1;
				if (finalCol.b>1.0f)
					finalCol.b=1;
				
				if (finalCol.r<0.0f)
					finalCol.r=0;
				if (finalCol.g<0.0f)
					finalCol.g=0;
				if (finalCol.b<0.0f)
					finalCol.b=0;*/
				
                    
				AccColor=AccColor.add(finalCol.mult(1f/1f));
		 		}
            		   
            		   imagePIXELS[x][y].x+=AccColor.x;
            		   imagePIXELS[x][y].y+=AccColor.y;
            		   imagePIXELS[x][y].z+=AccColor.z;
            		   
            		   int Color =
                          		((int)(AccColor.x*1.0f)<<16)+
                          		((int)(AccColor.y*1.0f)<<8)+
                          		((int)(AccColor.z*1.0f));
            		   image.setRGB(x, h - y - 1, Color);
                             //  label.repaint();
                  
            }
            label.repaint();
                
                     
            }
	}
    
    
    
    // TEXTURECUBE
    
 //   #define PI 3.1415926

    Vector2f latlong(Vector3f v) {
    /*	v=v.clone();
      v = v.normalize();
      float theta = (float) ((float) Math.acos(v.z)); // +z is up
       float phi = (float) (Math.atan2(v.y, v.x) + Math.PI);
      return new Vector2f(phi,theta ).multLocal(new Vector2f(.1591549f, .6366198f));
      */
      
      // Blinn/Newel latitude mapping	
    	
    //  float u = (float) ((float)(Math.atan(v.x/v.z)+Math.PI)/(Math.PI*2));
    //  float vM = (float) ((float)(Math.asin(v.y)+Math.PI/2f)/(Math.PI*2));
      // Spherical mapping
      float m = (float) (2f*Math.sqrt(v.x*v.x + v.y*v.y + (v.z+1)*(v.z+1)   ));
      float u = v.y / m + 0.5f;
     float vM = v.x / m + 0.5f;
      return new Vector2f(u,vM);
      
    }
   
    // AMBIENT OCCLUSION
    
    /*while (true) {
    
    x = RandomFloat(-1, 1); // random float between -1 and 1
    y = RandomFloat(-1, 1);
    z = RandomFloat(-1, 1);
    if (x * x + y * y + z * z > 1) continue; // ignore ones outside unit
                                             
     // sphere
    
     if (dot(Vector(x, y, z), N) < 0) continue; // ignore "down" dirs
    
     return normalize(Vector(x, y, z)); // success!
  }*/

    private Vector3f computeCol( Vector3f lightDir, Random rand, Vector3f pos, Vector3f dir, boolean computeShadow, boolean computeReflection, boolean computeAO, boolean computeShading, int depth) {
		float r1=0,g=0,b=0;

	//	 CollisionResults results = new CollisionResults();
		CollisionResults colision = trace(pos.clone(), dir);
		if (colision!=null)
		{
			
			// Obtain the targeted triangle
			Triangle triangle = new Triangle();



			int idx_triangle=colision.getCollision(0).getTriangleIndex();
			// Get the Geometry targeted
			Geometry geom = colision.getCollision(0).getGeometry();
			// Get the mesh from geometry
			Mesh mesh = geom.getMesh();

			
			Vector3f normal = colision.getCollision(0).getContactNormal();
			
			if (geom.getName()!=null)
			if (geom.getName().contains("sphere"))
				normal = colision.getCollision(0).getContactPoint().clone().subtract(geom.getLocalTranslation().clone()).normalize();
			
			com.jme3.material.Material material = colision.getCollision(0).getGeometry().getMaterial();
			
			Vector3f contactPt = colision.getCollision(0).getContactPoint().clone();
			
			float distance = pos.distance(contactPt);
			
			pos = contactPt.clone();
			
			
			
			//Vector3f shadowdir = lightDir.clone().negate();//.subtractLocal(pos).normalizeLocal(); 
			//pos.addLocal(shadowdir.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
		    
			int sample = 6;
			
			
			if ((geom.getName()!=null) && (geom.getName().contains("Light")))
		    {
				String diffuse="60.0 60.0 60.0 1.0";
				String n [] = diffuse.split(" ");
		    	
		    	r1=new Float(""+n[0]);
		    	g=new Float(""+n[1]);
		    	b=new Float(""+n[2]);
		    	
		    	//float factor = distance;
		    	
		    	if (depth==0)
		    	{
		    		r1=300;
		    		g=300;
		    		b=300;
		    	}
		    	
		    }
		    else
		    {
		    	float rr = 0;
		    	float gg = 0;
		    	float bb = 0;
		    	
		    	int real_sample=0;
		    	
		    	if (depth<=1)
		    		
		    	// create new rays et rapelle cette fct. en faisant une moyenne ?
		    	for (int i = 0; i < sample; i++)
				{
		    	//	if (rand.nextDouble()>=0.50)
		    		{
		    			Vector3f normalM = normal.clone();
		    			Vector3f newdir = deviateRandom(normalM, normalM, 0.8f);
		    			
		    			
		    			
		    		
		    			Vector3f pos2 = pos.clone().addLocal(newdir.clone().divide(10000f)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
		    		
		    		Vector3f col = computeCol(null, rand, pos2, newdir, false, false, false, false, depth+1);
		    		
		    		rr += col.x*64;
		    		gg += col.y*64;
		    		bb += col.z*64;
		    		real_sample++;
		    		}
				}
		    	/*rr/=real_sample;
		    	gg/=real_sample;
		    	bb/=real_sample;*/
		    	
		    	Random rand2 = new Random();
		    	rand2.setSeed(geom.hashCode());
		    	String diffuse=""+rand2.nextFloat()+ " "+rand2.nextFloat()+ " "+rand2.nextFloat()+ " "+rand2.nextFloat()+ " ";
		    	if (material.getParam("Diffuse")!=null)
		    		diffuse = material.getParam("Diffuse").getValueAsString();
		    	if (material.getParam("Color")!=null)
		    		diffuse = material.getParam("Color").getValueAsString();
		    	String n [] = diffuse.split(" ");
		    	
		    	r1=new Float(""+n[0]);
		    	g=new Float(""+n[1]);
		    	b=new Float(""+n[2]);
		    	
		    	r1*=rr;
		    	g*=gg;
		    	b*=bb;
		    	
		    }
		    
		}
		else
		{
		/*	Vector2f uvIBL = latlong(dir.clone().negate());
			double envpix [] = new double[4];
			IBL.getRaster().getPixel((int)Math.abs(uvIBL.y*(IBL.getWidth()-1))%IBL.getWidth(),(int)Math.abs(1.0*uvIBL.x*(IBL.getHeight()-1))%IBL.getHeight(), envpix);
			r1=(float) envpix[0]/255f;
			g=(float) envpix[1]/255f;
			b=(float) envpix[2]/255f;*/
			r1=1f;
			g=1f;
			b=1f;
		}
		Vector3f finalCol = new Vector3f(r1,g,b);

       return finalCol;
    }

	private String getDiffuseFromTexture(Triangle triangle, int idx_triangle, Geometry geom, Mesh mesh, Vector3f contactPt) {
		String diffuse;
		double []pix = new double[4];
		double []normpix = new double[4];
		float x= contactPt.x,y= contactPt.z;
		x=Math.abs(x*16*4);
		y=Math.abs(y*16*4);
		
		//System.err.println(""+pix);
		
		float u = 0, v = 0;
		// Get the texCoord buffer for that mesh.
		VertexBuffer texcoords = mesh.getBuffer(Type.TexCoord);

		// Get the U/V coordinate
		if (texcoords != null) {

			VertexBuffer index = mesh.getBuffer(Type.Index);
			
			int index1 =-1;
				int index2 =  -1;
					int index3 = -1;
			if (index.getData() instanceof IntBuffer) {
				java.nio.IntBuffer buff = (java.nio.IntBuffer)index.getData();
				index1 = ((IntBuffer) index.getData()).get(idx_triangle * 3 + 0);
				 index2 = ((IntBuffer) index.getData()).get(idx_triangle * 3 + 1);
				 index3 = ((IntBuffer) index.getData()).get(idx_triangle * 3 + 2);
				
			}
			if (index.getData() instanceof ShortBuffer) {
				index1 = ((ShortBuffer) index.getData()).get(idx_triangle * 3 + 0);
				 index2 = ((ShortBuffer) index.getData()).get(idx_triangle * 3 + 1);
				 index3 = ((ShortBuffer) index.getData()).get(idx_triangle * 3 + 2);
			}
				
			   //Get the barycentric coordinates
		  // Vector3f collisionResult = new Vector3f();
		  //  Ray ray = new Ray(pos.clone(), dir.clone());
		  //  ray.intersectWherePlanar(triangle, collisionResult);
			
			
			if (geom.getName().contains("Cube"))
				System.err.println("TOTO");
			
				FloatBuffer fb = (FloatBuffer) texcoords.getData();
				
				Vector3f ctp = contactPt.subtract(geom.getLocalTranslation()).clone();
				
				
				float s0 = fb.get(index1 * 2);
				float t0 = fb.get(index1 * 2 + 1);
				float s1 = fb.get(index2 * 2);
				float t1 = fb.get(index2 * 2 + 1);
				float s2 = fb.get(index3 * 2);
				float t2 = fb.get(index3 * 2 + 1);
				
				Vector3f v0 = triangle.get3().clone().subtract(triangle.get1());
			    Vector3f v1 = triangle.get2().clone().subtract(triangle.get1());
			    Vector3f v2 = ctp.clone().subtract(triangle.get1());
			    
			    // Compute dot products
			    float dot00 = v0.dot( v0);
			    float dot01 = v0.dot( v1);
			    float dot02 =v0.dot( v2);
			    float dot11 =v1.dot( v1);
			    float dot12 =v1.dot( v2);

			    // Compute barycentric coordinates
			    float invDenom = 1f / (dot00 * dot11 - dot01 * dot01);
			    float uM = (dot11 * dot02 - dot01 * dot12) * invDenom;
			    float vM = (dot00 * dot12 - dot01 * dot02) * invDenom;
									
			    Vector2f tt2 = new Vector2f(s1,t1).subtract(new Vector2f(s0,t0)); //uv2-uv1;
			    Vector2f tt1 = new Vector2f(s2,t2).subtract(new Vector2f(s0,t0)); //uv3-uv1;

			    Vector2f newuv = new Vector2f(s0,t0).add( tt1.mult(uM) .add( tt2.mult(vM)));
				
				
				
				
		//		newuv.x=Math.abs(newuv.x);
		//		newuv.y=Math.abs(newuv.y);
				u = newuv.x-(int)newuv.x;
				v = newuv.y-(int)newuv.y;
				
				fb.rewind();
			
		}
		
		
		
		images[0].getRaster().getPixel((int)Math.abs(1*u*(images[0].getWidth()-1))%images[0].getWidth(), (int)Math.abs(1.0*v*(images[0].getHeight()-1))%images[0].getHeight(), pix);
		
		images[2].getRaster().getPixel((int)Math.abs(1*u*(images[2].getWidth()-1))%images[2].getWidth(), (int)Math.abs(1.0*v*(images[2].getHeight()-1))%images[2].getHeight(), normpix);
  	
   //		pix[0]=u*254f;
   // 		pix[1]=v*254f;
   // 		pix[2]=0;
		diffuse=""+(pix[0]/255f)+ " "+(pix[1]/255f)+ " "+(pix[2]/255f)+ " ";
		return diffuse;
	}
    
    private Vector3f ApplyNormal(Vector3f normal, double[] normpix) {
    	normal = normal.clone();
    	Vector3f normTex = new Vector3f();
    	float mult = 255f;
    	normTex.x=(float) normpix[0]/mult;
    	normTex.y=(float) normpix[1]/mult;
    	normTex.z=(float) normpix[2]/mult;
    //	normTex=normTex.add(new Vector3f(1,1,1)).divide(2);
    //	normal=normal.mult(normTex);
    //	return normal;
    	
    	
    	float currentMatbump = 0.15f;
    	float noiseCoefx = (normTex.x-0.0f)*1f;
    	float noiseCoefy = (normTex.y-0.0f)*1f;
    	float noiseCoefz = (normTex.z-0.0f)*1f;
    	
    	normTex.x = (1.0f - currentMatbump ) * normal.x + currentMatbump * noiseCoefx;  
    	normTex.y = (1.0f - currentMatbump ) * normal.y + currentMatbump * noiseCoefy;  
    	normTex.z = (1.0f - currentMatbump ) * normal.z + currentMatbump * noiseCoefz;
    	return normTex.normalize();
    }

	private float mix(float r1, float r, float d)
	{
    	return (r1 * d + r * (1-d));
		
	}

    private Vector3f reflection(Vector3f I, Vector3f N)
    {
    	float eta = 1;//index_external/index_internal;
    	float cos_theta1 = I.dot(N);
    	Vector3f R = I.clone().subtract( N.clone().mult( (float) (2.0 * cos_theta1)));
    	return R;
    }
    private Vector3f refraction(Vector3f I, Vector3f N, float n)
    {
    	Vector3f thisM = new Vector3f(I);
    //	public final Vector refract(Vector Normal, float n1, float n2)
     //   {
        	

        	//float n = n1 / n2;
        	float cosI = thisM.dot(N);
        	float sinT2 = (float) ((float)n * n * (1.0 - cosI * cosI));
        	if (sinT2 > 1.0)
        	{
        		return null;
        	}
        	float V = (float) ((float)n + Math.sqrt(1.0f - sinT2));
        	
        	return thisM.mult(n).add(N.mult(V).negate());
      //  }
    /*	// TODO : elle pupe c't refraction
    	float eta = 1;//index_external/index_internal;
    	float cos_theta1 = I.dot(N);
    	Vector3f R = I.clone().add( N.clone().mult( (float) (2.0 * cos_theta1)));
    	return R;*/
    }

	private Vector3f deviateRandom(Vector3f shadowdirM, Vector3f NORMAL, float mult) {
		int cpt=100;
		Vector3f shadowdir;
		do
		{
			shadowdir=shadowdirM.clone();
		
		shadowdir.x+=(Math.random()-0.5f)/mult;
		shadowdir.y+=(Math.random()-0.5f)/mult;
		shadowdir.z+=(Math.random()-0.5f)/mult;
		shadowdir=shadowdir.normalize();
		if (cpt--<0) 
		{
			System.err.println("TRIAL OUT !!!!!");
			break;
		}
		}
		while(shadowdir.dot(NORMAL) < 0);		 
		return shadowdir;
	}

	private CollisionResults trace(Vector3f pos, Vector3f dir) {
		Ray r = new Ray(pos, dir.clone());
		CollisionResults results=new CollisionResults();
		results.clear();
		
		scene.collideWith(r, results);
		
		if (results.size()!=0)
			return results;
		return null;
		
	}

	
	
	
	private Vector3f RandomPointOnPlane(Vector3f position, Vector3f normal, float radius)
	{
		Vector3f random = new Vector3f();
		Vector3f randomPoint;
	 
	 do
	 {
	  random.x = 2.0f * (float)Math.random() - 1.0f;
	  random.y = 2.0f * (float)Math.random() - 1.0f;
	  random.z = 2.0f * (float)Math.random() - 1.0f;
	  randomPoint = random.cross(normal);
	 } while (randomPoint == Vector3f.ZERO);
	 
	 randomPoint=randomPoint.normalize();
	 randomPoint = randomPoint.mult(radius * (float)Math.sqrt(Math.random()));
	 randomPoint = randomPoint.add(position);
	 
	 return randomPoint;
	}
	
	   /**
     * 
     * @param pos Point de depart du rayon d'ao
     * @param triangle le triangle actuel (a enlever une fois la bonne procedure deviate)
     * @param normal la normale 
     * @return
     */
	private float computeAO(CollisionResults results, Vector3f pos, Triangle triangle, Vector3f normal)
	{
		float shade = 1;
		pos = pos.clone();
		int amount = 16*4;
		for (int i = 0; i < amount; i++)
		{
			// TODO : Si on utilise la normale, on risque d'avoir pas d'occlusion si la normal est invers�e par rapport a la vue
			
			//Vector3f direction = deviateRandom(normal, normal, 2.5f);
			Vector3f direction = RandomPointOnPlane(normal, normal, 8f);
			
			pos.addLocal(direction.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
			CollisionResults colFF = trace(pos, direction);

			if (colFF != null)
			{
				float distance = colFF.getClosestCollision().getDistance();
				Triangle tri2 = new Triangle();
				colFF.getClosestCollision().getTriangle(tri2);
				if (tri2 != triangle)
				{
					//distance = distance * distance;
					distance = 3.5f / distance;

					shade -= (1f / ((float) amount * 2f)) * distance;
				}
			}
		}
		//shade=(float) Math.sqrt(shade);
		return shade;
	}

}
