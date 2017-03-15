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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class RayTrace {

    private BufferedImage image;
    private Camera cam;
    private Spatial scene;
    private CollisionResults results = new CollisionResults();
    private JFrame frame;
    private JLabel label;
    BufferedImage images[];
  //  BufferedImage IBL;
    

    public RayTrace(Spatial scene, Camera cam, int width, int height){
    	image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
    	images = new BufferedImage[10];
    /*	try {
    	//	images[0] = ImageIO.read(new File("data\\KAMEN.jpg"));
    	//	images[1] = ImageIO.read(new File("data\\KAMEN-stup.jpg"));
    	//	images[2] = ImageIO.read(new File("data\\KAMEN_normal.png"));
    	//	images[3] = ImageIO.read(new File("data\\KAMEN_STUP_normal.png"));
    	//	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\spherical1_probe.jpg"));
    	//	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\Barce_Rooftop_C_8k.jpg"));
    	//	IBL = ImageIO.read(new File("D:\\Textures\\EnvMap\\HDR Maps\\Small\\Alexs_Apartment\\Alexs_Apt_8k.jpg"));
    		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

        this.scene = scene;
        this.cam = cam;
    }

    public void show(){
        frame = new JFrame("HDR View");
        label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label);
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

 //       tex_ml = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        
        final Random rand = new Random();
        
        //Vector3f LightDir = new Vector3f(-0.2f, 0.6f,0.5f);
        int from=0; 
        int to = h; 
        
        // cannot be multithread it seems :( 
        Thread t1 = new Thread()
        {
        	public void run()
        	{
        		ooo(lightDir, w, h, wr, hr, rand, h*0/4, h*4/4);
        		/*ooo(lightDir, w, h, wr, hr, rand, h*0/4, h*1/4);
                ooo(lightDir, w, h, wr, hr, rand, h*1/4, h*1/2);
                ooo(lightDir, w, h, wr, hr, rand, h*1/2, h*3/4);
                ooo(lightDir, w, h, wr, hr, rand, h*3/4, h*4/4);*/
        	}
        };

        t1.start();
        try {
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        }

	private void ooo(Vector3f lightDir, int w, int h, float wr, float hr, Random rand, int from, int to) {
		for (int y = from; y < to; y++){
        	label.repaint();
            for (int x = 0; x < w; x++){
            	
            	ColorRGBA AccColor = new ColorRGBA(0,0,0,0);
            	float fragmentx = x;
            	float fragmenty = y;

      //   	for( fragmentx = x; fragmentx < x + 1.0f; fragmentx += 0.5f)
      //    	   for( fragmenty = y; fragmenty < y + 1.0f; fragmenty += 0.5f)

            		   {
            		   
            		   
                Vector2f v = new Vector2f(fragmentx * wr,fragmenty * hr);
                Vector3f pos = cam.getWorldCoordinates(v, 0.0f);
                Vector3f dir = cam.getWorldCoordinates(v, 0.3f);
                dir.subtractLocal(pos).normalizeLocal();

                
				ColorRGBA finalCol=computeCol(lightDir, rand, pos, dir, false, false, true, false, 0);
				
			//	finalCol=finalCol.mult(2);
				
				if (finalCol.r>1.0f)
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
					finalCol.b=0;
				
                    
				AccColor=AccColor.add(finalCol.mult(1f/1f));
				
            		   }
            	int Color =
            		((int)(AccColor.r*255f)<<16)+
            		((int)(AccColor.g*255f)<<8)+
            		((int)(AccColor.b*255f));
                    image.setRGB(x, h - y - 1, Color);
                  //  label.repaint();
                  
            }
            label.repaint();
                
                     
            }
	}
    
    
    
    // TEXTURECUBE
    
 //   #define PI 3.1415926

    Vector2f latlong(Vector3f v) {
    	v=v.clone();
      v = v.normalize();
      float theta = (float) ((float) Math.acos(v.z)); // +z is up
       float phi = (float) (Math.atan2(v.y, v.x) + Math.PI);
      return new Vector2f(phi,theta ).multLocal(new Vector2f(.1591549f, .6366198f));
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

    private ColorRGBA computeCol(Vector3f lightDir, Random rand, Vector3f pos, Vector3f dir, boolean computeShadow, boolean computeReflection, boolean computeAO, boolean computeShading, int depth) {
		float r1=0,g=0,b=0;
		
		
		CollisionResults colision = trace(pos.clone(), dir);
		if (colision!=null)
		{
			
			// Obtain the targeted triangle
			Triangle triangle = new Triangle();
			triangle=results.getCollision(0).getTriangle(triangle);

			int idx_triangle=results.getCollision(0).getTriangleIndex();
			// Get the Geometry targeted
			Geometry geom = results.getCollision(0).getGeometry();
			// Get the mesh from geometry
			Mesh mesh = geom.getMesh();

			
			Vector3f normal = results.getCollision(0).getContactNormal();
			
			if (geom.getName()!=null)
			if (geom.getName().contains("sphere"))
				normal = results.getCollision(0).getContactPoint().clone().subtract(geom.getLocalTranslation().clone()).normalize();
			
			
			
			com.jme3.material.Material material = results.getCollision(0).getGeometry().getMaterial();
			
			Vector3f contactPt = results.getCollision(0).getContactPoint().clone();
			pos = contactPt.clone();
			Vector3f shadowdir = lightDir.clone().negate();//.subtractLocal(pos).normalizeLocal(); 
			pos.addLocal(shadowdir.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
		    
			
		    if (true)
		    {
		    	String diffuse="0.8 0.8 0.8 1.0";
		    	
		    	rand.setSeed(geom.hashCode());
		    	//diffuse=""+rand.nextFloat()+ " "+rand.nextFloat()+ " "+rand.nextFloat()+ " "+rand.nextFloat()+ " ";
		    	
		  //  	if ((geom.getName() != null) && (geom.getName().contains("sphere")==true))
		  //  	{
		    //	if (material.getParam("Diffuse")!=null)
		    //		diffuse = material.getParam("Diffuse").getValueAsString();
		    	//else
		 //   	if (geom.getName().contains("Floor"))
		    //	{
		   // 		diffuse=""+rand.nextFloat()+ " "+rand.nextFloat()+ " "+rand.nextFloat()+ " "+rand.nextFloat()+ " ";
		    		
		    	//	diffuse = getDiffuseFromTexture(triangle, idx_triangle, geom, mesh, contactPt);
		    		
	//  	normal = ApplyNormal(normal, normpix);
		    		
		   // 	}
		   // 	}
		    	String n [] = diffuse.split(" ");
		    	
		    	r1=new Float(""+n[0]);
		    	g=new Float(""+n[1]);
		    	b=new Float(""+n[2]);
		    }
		    
		    float shade = 1.0f;
		    if (computeShadow==true)
		    {
		    int amount = 2;
		    for (int i = 0 ; i < amount;i++)
		    {
		    Vector3f shadowdir1 = deviateRandom(shadowdir, normal,25f);
	    	if (trace(pos, shadowdir1)!=null)
	    		shade-=0.40f;
		    }
		    }
	      
		    
		    if (computeAO==true)
		    {
		    	float AOv = computeAO(pos, triangle, normal);
		    	shade=mix(shade, AOv, 0.15f);
		    }
		    shade*=1.0f;
		  
		   
		    
	
		    
		    if (computeShading)
		    {
		    	 float NdotL = (float) Math.max(normal.clone().dot(shadowdir.clone()), 0.0);
		   //dotL=mix(NdotL,1,0.2f);
		    if (NdotL > 0)
		    {
		    	// add specular component to ray color
		    	//a_Acc += spec * light->GetMaterial()->GetColor();
		    	float specular = 1.5f;
		    	float spec = (float) (Math.pow( NdotL, 3) * specular );
		    	// add specular component to ray color
		    	//a_Acc += spec * light->GetMaterial()->GetColor();
		    //	spec=NdotL*shade;
		    	r1=r1*spec;
		    	g=g*spec;
		    	b=b*spec;
		    	//r1+=0.2f;
		    	//g+=0.2f;
		    	//b+=0.2f;
		    } 
		    else
		    {
		    	r1=0;
		    	g=0;
		    	b=0;
		    }
		    }
		    
		 /*   if (geom.getName().contains("Plane.007"))
		    {
		    	Vector3f reflectionDir = dir.clone();//refraction(dir, normal) ;
				pos = contactPt.clone();
				pos.addLocal(reflectionDir.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
				CollisionResults rc = trace(pos, reflectionDir);
				if (rc!=null)
				{
				    ColorRGBA reflection = computeCol(lightDir, rand, pos, reflectionDir, true, true,  false, computeShading, depth+1);
				    
				    r1=reflection.r;
				    g=reflection.g;
				    b=reflection.b;
				}
		    }*/
		    
		    
		    if ((computeReflection==true) && (depth<=1))
		    {
		    	
		    //	pos = contactPt.clone();
		    	normal=deviateRandom(normal, normal, 20f);
				Vector3f reflectionDir = reflection(dir, normal) ;
				pos = contactPt.clone();
				float dst = contactPt.distance(cam.getLocation());
				pos.addLocal(reflectionDir.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
				CollisionResults rc = trace(pos, reflectionDir);
				if (rc!=null)
				{
					
					ColorRGBA reflection = computeCol(lightDir, rand, pos, reflectionDir, false, false, false,computeShading, depth+1);
		    		float ratio = 0.5f; 
		    		r1=mix(r1, reflection.r, ratio);
		    		g=mix(g, reflection.g, ratio);
		    		b=mix(b, reflection.b, ratio);

				}
				else
				{
			/*		Vector2f uvIBL = latlong(reflectionDir.clone().negate());
					double envpix [] = new double[4];
					IBL.getRaster().getPixel((int)Math.abs(uvIBL.y*(IBL.getWidth()-1))%IBL.getWidth(),(int)Math.abs(1.0*uvIBL.x*(IBL.getHeight()-1))%IBL.getHeight(), envpix);
					//r1=(float) envpix[0]/255f;
					//g=(float) envpix[1]/255f;
					//b=(float) envpix[2]/255f;
					r1=mix(r1, (float) envpix[0]/255f, 0.75f);
					g=mix(g, (float) envpix[1]/255f, 0.75f);
					b=mix(b, (float) envpix[2]/255f, 0.75f);*/
				}
		  //  r1/=2;
		  //  g/=2;
		  //  b/=2;
		    }
		    
		    r1*=shade*1.0f;
		    g*=shade*1.0f;
		    b*=shade*1.0f;
		    
		   
		}
		else
		{
		/*	Vector2f uvIBL = latlong(dir.clone().negate());
			double envpix [] = new double[4];
	//		IBL.getRaster().getPixel((int)Math.abs(uvIBL.y*(IBL.getWidth()-1))%IBL.getWidth(),(int)Math.abs(1.0*uvIBL.x*(IBL.getHeight()-1))%IBL.getHeight(), envpix);
			r1=(float) envpix[0]/255f;
			g=(float) envpix[1]/255f;
			b=(float) envpix[2]/255f;*/
		}
		ColorRGBA finalCol = new ColorRGBA(r1,g,b,1.0f);

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
    private Vector3f refraction(Vector3f I, Vector3f N)
    {
    	// TODO : elle pupe c't refraction
    	float eta = 1;//index_external/index_internal;
    	float cos_theta1 = I.dot(N);
    	Vector3f R = I.clone().add( N.clone().mult( (float) (2.0 * cos_theta1)));
    	return R;
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
	private float computeAO(Vector3f pos, Triangle triangle, Vector3f normal)
	{
		float shade = 1;
		pos = pos.clone();
		int amount = 32;
		for (int i = 0; i < amount; i++)
		{
			//Vector3f direction = deviateRandom(normal, normal, 2.5f);
			Vector3f direction = RandomPointOnPlane(normal, normal, 0.65f);
			
			pos.addLocal(direction.clone().divide(100)); // Pour eviter certains kist (faudrait donner une objet a eviter a trace peut etre).
			CollisionResults colFF = trace(pos, direction);

			if (colFF != null)
			{
				float distance = colFF.getClosestCollision().getDistance();
				Triangle tri2 = new Triangle();
				colFF.getClosestCollision().getTriangle(tri2);
				if (tri2 != triangle)
				{
					float max_dist = 0.5f;
					if (distance>max_dist)
						distance=0.5f;
					else
						distance = max_dist/distance;
					shade -= (1f / ((float) amount*2 )) * distance;
				}
			}
			if (shade<0)
			{
			//ystem.err.println("Extraa!!!!");
				break;
				
			}
		}
		//shade=(float) Math.sqrt(shade);
		return shade;
	}
}
