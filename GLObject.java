/**
 * 
 * This class is used for loading .obj files and accessing pertinent
 * information from them
 * 
 */

import java.util.*;
import java.io.*;
import com.jogamp.opengl.*;

class GLObject {
    List<Coord> vlist;  // list of vertex coordinates
    List<Coord> tlist;  // list of texture coordinates
    List<Coord> nlist;  // list of normal components
    List<Face> flist;   // list of faces
    
    /* nested Coord class */
    class Coord {
	float x,y,z;  // x, y and z coordinates of a point or vector
	Coord(float x, float y, float z){
	    this.x=x; this.y=y; this.z=z;
	}
	public String toString(){
	    return "("+x+","+y+","+z+")";
	}
    }
    /* end of Coord class */
    
    /* nested Face class */   
    class Face {
	List<Vertex> vertexList;  // list of vertices
	Coord normal;  // face normal
	
	Face(){
	    vertexList = new ArrayList<Vertex>();
	}
	
	public void computeNormal(){
	    // get the first 3 vertices
	    Vertex v0 = vertexList.get(0);
	    Vertex v1 = vertexList.get(1);
	    Vertex v2 = vertexList.get(2);
	    
	    // compute the vector components
	    float x0 = v1.point.x - v0.point.x;
	    float y0 = v1.point.y - v0.point.y;
	    float z0 = v1.point.z - v0.point.z;
	    float x1 = v2.point.x - v1.point.x;
	    float y1 = v2.point.y - v1.point.y;
	    float z1 = v2.point.z - v1.point.z;
	    
	    // compute and save the normal
	    normal = new Coord(y0*z1-y1*z0,z0*x1-z1*x0,x0*y1-x1*y0);
	}
	
	public void addVertex(Vertex v){
	    vertexList.add(v);
	}
	
	public Coord getNormal(){
	    return normal;
	}
	
	public List<Vertex> getVlist(){
	    return vertexList;
	}
	
	public String toString(){
	    return vertexList.toString()+normal;
	}
    }
    /* end of Face class */
    
    /* nested Vertex class */
    class Vertex {
	Coord point;  // vertex coordinates
	Coord texel;  // texture coordinates
	Coord normal; // vertex normal components
	
	/* constructors */
	Vertex(Coord point){
	    this.point = point;
	}
	
	Vertex(Coord point, Coord texel){
	    this(point);
	    this.texel = texel;
	}
	
	Vertex(Coord point, Coord texel, Coord normal){
	    this(point,texel);
	    this.normal = normal;
	}
	
	public void setNormal(Coord n){
	    normal = n;
	}
	
	public String toString(){
	    return "["+point+"/"+texel+"/"+normal+"]";
	}
    }
    /* end of nested Vertex class */
    
    /* GLObject no-arg constructor */
    GLObject(){
	vlist = new ArrayList<Coord>();
	nlist = new ArrayList<Coord>();
	tlist = new ArrayList<Coord>();
	flist = new ArrayList<Face>();
    }
    
    public List<Coord> getVlist(){
	return vlist;
    }
    
    public List<Face> getFlist(){
	return flist;
    }
    
    public boolean hasNormals(){
	return !nlist.isEmpty();
    }
    
    /* render method
       the mode option expects a value of either GL2.GL_FLAT or GL2.GL_SMOOTH

       GL_FLAT indicates flat shading
       A single shade is used for each face.
       The face's normal vector is used for every vertex of the face.
       It assumes that face normals have been computed.

       GL_SMOOTH indicates smooth shading
       The shade of each pixel in face is interpolated from the
       shades computed (using the Phong lighting model) at each vertex.
       Each vertex has its own normal vector.
    */    
    public void render(GLAutoDrawable drawable, int mode){
	GL2 gl = drawable.getGL().getGL2();
	gl.glShadeModel(mode);
	if(mode==GL2.GL_FLAT){  // flat shading
	    for(Face face : flist){  
		// use the face normal for each vertex
		if(face.normal!=null){
		    gl.glNormal3d(face.normal.x,face.normal.y,face.normal.z);
		}
		gl.glBegin(GL2.GL_POLYGON);
		for(Vertex v : face.getVlist()){
		    if(v.texel!=null){ // set texture coordinate if present
			gl.glTexCoord2f(v.texel.x,v.texel.y);
		    }
		    gl.glVertex3f(v.point.x,v.point.y,v.point.z);
		}
		gl.glEnd();
	    }
	}
	else { // smooth shading
	    for(Face face : flist){
		// each vertex has its own normal
		gl.glBegin(GL2.GL_POLYGON);
		for(Vertex v : face.getVlist()){
		    if(v.texel!=null){
			gl.glTexCoord2f(v.texel.x,v.texel.y);
		    }
		    if(v.normal!=null){
			gl.glNormal3f(v.normal.x,v.normal.y,v.normal.z);
		    }
		    gl.glVertex3f(v.point.x,v.point.y,v.point.z);
		}
		gl.glEnd();
	    }
	}
    }
    
    public void renderNormal(GLAutoDrawable drawable, int mode){
	GL2 gl = drawable.getGL().getGL2();
	gl.glShadeModel(mode);
	if(mode==GL2.GL_FLAT){  // flat shading
	    for(Face face : flist){  
		face.computeNormal();
		gl.glBegin(GL2.GL_POLYGON);
		for(Vertex v : face.getVlist()){
		    gl.glVertex3f(v.point.x,v.point.y,v.point.z);
		}
		gl.glEnd();
	    }
	}
	else { // smooth shading
	    for(Face face : flist){
		// each vertex has its own normal
		gl.glBegin(GL2.GL_POLYGON);
		face.computeNormal();
		for(Vertex v : face.getVlist()){
		    if(v.normal!=null){
			gl.glNormal3f(v.normal.x,v.normal.y,v.normal.z);
		    }
		    gl.glVertex3f(v.point.x,v.point.y,v.point.z);
		}
		gl.glEnd();
	    }
	}
    }

    /*
      The default rendering method is SMOOTH
    */    
    public void render(GLAutoDrawable drawable){
	render(drawable,GL2.GL_SMOOTH);
    }

    public float getMinX(){
	float min = Float.POSITIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.x<min)
		min = v.x;
	}
	return min;
    }
    
    public float getMinY(){
	float min = Float.POSITIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.y<min)
		min = v.y;
	}
	return min;
    }
    
    public float getMinZ(){
	float min = Float.POSITIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.z<min)
		min = v.z;
	}
	return min;
    }
    
    public float getMaxX(){
	float max = Float.NEGATIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.x>max)
		max = v.x;
	}
	return max;
    }
    
    public float getMaxY(){
	float max = Float.NEGATIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.y>max)
		max = v.y;
	}
	return max;
    }
    
    public float getMaxZ(){
	float max = Float.NEGATIVE_INFINITY;
	for(Coord v : vlist){
	    if(v.z>max)
		max = v.z;
	}
	return max;
    }
    
    /*
      This method can be applied to object files that do
      not contain vertex normals.
    */
    public void computeFaceNormals(){
	for(Face f : flist){
	    f.computeNormal();
	}
    }

    /*
      This method can be applied to object files that do
      not contain vertex normals.
      It assumes that face normals have already been computed.
    */
    public void computeVertexNormals(){
	HashMap<Coord,List<Face>> fmap = new HashMap<Coord,List<Face>>();
	// construct vertex-to-face map
	for(Face face : flist){
	    for(Vertex v : face.vertexList){
		List<Face> faces = fmap.get(v.point);
		if(faces==null){
		    faces = new ArrayList<Face>();
		    fmap.put(v.point,faces);
		}
		faces.add(face);
	    }
	}
	
	/* compute average normals */
	HashMap<Coord,Coord> normmap = new HashMap<Coord,Coord>();
	for(Coord v : vlist){
	    double sx=0,sy=0,sz=0;
	    List<Face> faces = fmap.get(v);
	    if(faces!=null){
		for(Face f : faces){
		    Coord normal = f.getNormal();
		    sx += normal.x;
		    sy += normal.y;
		    sz += normal.z;
		}
		normmap.put(v,new Coord(
					(float)sx/faces.size(),
					(float)sy/faces.size(),
					(float)sz/faces.size()));
	    }
	}
	
	/* insert normals into vertices */
	for(Face face : flist){
	    for(Vertex v : face.vertexList){
		v.normal = normmap.get(v.point);
	    }
	}
    }
    
    /* 
       GLObject constructor 
       Reads the object description from a .obj file   
    */
    GLObject(String fname) throws FileNotFoundException,
				  InputMismatchException,
				  IndexOutOfBoundsException {
	
	this();
	Scanner scanner = new Scanner(new File(fname));
	
	while(scanner.hasNextLine()){
	    String line = scanner.nextLine();
	    Scanner scan = new Scanner(line);
	    if(scan.hasNext()){
		String command = scan.next();
		if(command.equals("v")){ // vertex
		    float x = scan.nextFloat();
		    float y = scan.nextFloat();
		    float z = scan.nextFloat();
		    vlist.add(new Coord(x,y,z));
		}
		else if(command.equals("vn")){ // vertex normal
		    float x = scan.nextFloat();
		    float y = scan.nextFloat();
		    float z = scan.nextFloat();
		    nlist.add(new Coord(x,y,z));
		}
		else if(command.equals("vt")){ // tex coordinates
		    float s = scan.nextFloat();
		    float t = scan.nextFloat();
		    tlist.add(new Coord(s,t,0));
		}
		else if(command.equals("f")){ // face command
		    Face face = new Face();
		    flist.add(face);
		    while(scan.hasNext()){
			// get next vertex
			String s = scan.next();
			String[] ss = s.split("/");
			Coord vc = null;
			Coord tc = null;
			Coord nc = null;
			
			if(ss.length>0 && ss[0].length()>0){ // get vertex index
			    vc = vlist.get(Integer.parseInt(ss[0])-1);
			}
			if(ss.length>1 && ss[1].length()>0){ // get texel index
			    tc = tlist.get(Integer.parseInt(ss[1])-1);
			}
			if(ss.length>2 && ss[2].length()>0){ // get normal index
			    nc = nlist.get(Integer.parseInt(ss[2])-1);
			}
			// create vertex object
			Vertex v = new Vertex(vc,tc,nc);
			// add it to the face
			face.addVertex(v);
		    }
		}
	    }
	}
    }
}