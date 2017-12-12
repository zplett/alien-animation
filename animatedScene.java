/** Zac Plett
*
* This is the main class that handles all functionality related to 
* creating and animating the scene.
*
*/

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class animatedScene implements ChangeListener, ActionListener, GLEventListener {

    float diam;
    ArrayList<GLObject> objects;
    static String alienFile = "Alien.obj";
    static String saucer1file = "FlyingSaucer.obj";
    static String saucer2file = "saucer.obj";
    GLObject object, object2;
    float theta = 0f;
    float saucyTilt = 0f;
    private static final String[] texFileName = { "Alien_BaseColor.png", "FlyingSaucer_BaseColor.png" };
    private int[] texName = new int[texFileName.length + 1];
    private int texIndex = 0;
    private int objIndex = 0;
    boolean rotate = false;
    boolean fill = false;
    float red;
    float green;
    float yellow;
    float violet;
    float blue;
    float xCam;
    float yCam;
    float zCam;
    float cx, cy, cz;
    GL2 gl;
    GLU glu;
    GLUT glut;
    int ww = 1400, wh = 700; // screen dimensions
    static JFrame frame;
    JSlider zoomSlider, panVerticalSlider, panHorizontalSlider;
    private JSlider ambientSlider, diffuseSlider, specularSlider, shininessSlider;
    private JButton pauseButton, resetButton, quitButton;

    private float lightAmbient[] = { .3f, .3f, .3f, 1f };
    private float lightDiffuse[] = { .7f, .7f, .7f, .7f };
    private float lightSpecular[] = { 1f, 1f, 1f, 1f };
    private float light0Position[] = { 0f, 0f, 1f, 0f };
    private float light1Position[] = { 0f, 1f, 0f, 0f };
    private double time = 0.0;
    private int timespan = 10; // 8 seconds
    private float startPos0[] = { 0, 0, 0 };
    private float endPos0[] = { 0, 0, 0 };
    private float currPos0[] = { 0, 0, 0 };
    private float startPos1[] = { 0, 0, 0 };
    private float endPos1[] = { 0, 0, 0 };
    private float currPos1[] = { 0, 0, 0 };

    private float materialAmbient[] = { 1, 1, 1, 1 };
    private float materialDiffuse[] = { 1, 1, 1, 1 };
    private float materialSpecular[] = { 1, 1, 1, 1 };
    private int materialShininess = 20;

    int subdivisions = 10;
    boolean wire = false;
    boolean light0On = true;
    boolean light1On = true;
    boolean pause = false;
    boolean reset = false;

    public void init(GLAutoDrawable drawable) {

	gl = drawable.getGL().getGL2();
	glu = new GLU();
	glut = new GLUT();
	gl.glEnable(GL2.GL_DEPTH_TEST);

	gl.glClearColor(135 / 256f, 206 / 256f, 250 / 256f, 1);

	gl.glShadeModel(GL2.GL_SMOOTH);
	float dir[] = { -1, 0, 0 };
	float pos[] = { 2, 0, 0 };
	gl.glEnable(GL2.GL_LIGHTING);
	gl.glEnable(GL2.GL_LIGHT0);
	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0Position, 0);
	gl.glEnable(GL2.GL_LIGHT1);
	gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1Position, 0);

	gl.glShadeModel(GL2.GL_SMOOTH);
	gl.glEnable(GL2.GL_TEXTURE_2D);
	gl.glEnable(GL2.GL_DEPTH_TEST);
	gl.glGenTextures(texFileName.length + 1, texName, 0);

	gl.glBindTexture(GL2.GL_TEXTURE_2D, texName[0]);
	gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
	gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
	gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
	gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);

	makeTextures();

    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	ww = width;
	wh = height;

	float xshift = (xMax() - xMin()) / 2f;
	float yshift = (yMax() - yMin()) / 2f;
	float zshift = (zMax() - zMin()) / 2f;

	cx = (xMax() + xMin()) / 2f;
	cy = (yMax() + yMin()) / 2f;
	cz = (zMax() + zMin()) / 2f;

	diam = 200;

	float aspect = ww / (float) wh;
	gl.glEnable(GL2.GL_DEPTH_TEST);
	gl.glViewport(0, 0, ww, wh);
	gl.glMatrixMode(GL2.GL_PROJECTION);
	gl.glLoadIdentity();
	float fdist = (float) (diam / 0.5773f);
	float fnear = fdist - diam;
	float ffar = fdist + 2 * diam;
	glu.gluPerspective(60f, aspect, fnear / 10, ffar);
	gl.glMatrixMode(GL2.GL_MODELVIEW);

    }

    public void display(GLAutoDrawable drawable) {

	gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	gl.glBindTexture(GL2.GL_TEXTURE_2D, texName[texIndex]);
	if (!pause)
	    update();
	render(drawable);
	if (reset) {
	    reset();
	    reset = false;
	}

    }

    public void update() {

	if (time < 2.0) { // advance the timer
	    time += (1.0 / (30 * 18));
	    // System.out.println(time);
	    // if( time >= timespan )
	    // System.exit(0);
	}
	if (time <= 1.0) {// object 0 position
	    zCam += ( time / timespan ) / 1.25;
	    currPos0[2] += 0.1;
	    currPos0[1] += 0.035;
	    currPos0[0] += 0.035;
	    currPos1[1] += 0.02;
	    theta += 0.13;
	} else {
	    reset();
	    zCam = -270;
	    // currPos0[0] = endPos0[0];
	    // currPos1[0] = endPos1[0];
	    // time = 0;
	}

    }

    public void reset() {
	// System.out.println("here");
	zCam = -270;
	time = 0;
	currPos0[0] = endPos0[0];
	currPos0[1] = endPos0[1];
	currPos0[2] = endPos0[2];
	gl.glTranslatef(-cx, -cy, -cz);
	currPos1[0] = startPos1[0];
	currPos1[1] = endPos1[1];
	currPos1[2] = endPos1[2];

    }

    public void render(GLAutoDrawable drawable) {

	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

	gl.glMatrixMode(GL2.GL_MODELVIEW);
	gl.glLoadIdentity();
	float fdist = (float) (diam / Math.tan(30));
	float fnear = fdist - diam;
	glu.gluLookAt(0, 0, fdist - zCam, 0, 0, 0, 0, 1, 0);
	gl.glTranslatef(cx, cy, cz);
	gl.glRotatef(yCam, 0, 1, 0);
	gl.glRotatef((float) xCam, 1, 0, 0);
	gl.glTranslatef(-cx, -cy, -cz);

	gl.glEnable(GL2.GL_NORMALIZE);
	gl.glEnable(GL2.GL_COLOR_MATERIAL);

	gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_SPECULAR);
	gl.glColor3f(0, 1, 0);
	gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

	gl.glEnable(GL2.GL_TEXTURE_2D);

	// turn the lights on or off
	if (light0On)
	    gl.glEnable(GL2.GL_LIGHT0);
	else
	    gl.glDisable(GL2.GL_LIGHT0);
	if (light1On)
	    gl.glEnable(GL2.GL_LIGHT1);
	else
	    gl.glDisable(GL2.GL_LIGHT1);

	for (int light = GL2.GL_LIGHT0; light <= GL2.GL_LIGHT1; light++) {
	    gl.glLightfv(light, GL2.GL_AMBIENT, lightAmbient, 0);
	    gl.glLightfv(light, GL2.GL_DIFFUSE, lightDiffuse, 0);
	    gl.glLightfv(light, GL2.GL_SPECULAR, lightSpecular, 0);
	}

	float sizeX = 50, sizeZ = 50;

	gl.glPushMatrix();
	gl.glBegin(GL2.GL_QUADS);
	float quadmin = (float) 0.5 + (yMin() / 2);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);
	for (int x = (int) -20; x < 20; ++x) {
	    for (int z = (int) -20; z < 20; ++z) {

		if ((x + z) % 2 == 0)
		    gl.glColor3f(0, 100 / 256f, 0);
		else
		    gl.glColor3f(124 / 256f, 252 / 256f, 0);

		gl.glVertex3f(x * sizeX, quadmin, z * sizeZ);
		gl.glVertex3f((x + 1) * sizeX, quadmin, z * sizeZ);
		gl.glVertex3f((x + 1) * sizeX, quadmin, (z + 1) * sizeZ);
		gl.glVertex3f(x * sizeX, quadmin, (z + 1) * sizeZ);
	    }
	}
	gl.glEnd();
	gl.glPopMatrix();

	drawSaucer1(-cx, -cy, -cz, drawable, objects.get(2));
	drawSaucer1c(-cx, -cy, -cz, drawable, objects.get(2));
	//gl.glRotatef(theta, 0, 1, 0);
	drawSaucer1a(currPos1[2], currPos1[1], currPos1[0], drawable, objects.get(2));

	//gl.glRotatef(-theta, 0, 1, 0);
	gl.glBindTexture(GL2.GL_TEXTURE_2D, texName[2]);
	drawSaucer2a(-currPos0[0], -currPos0[1], -currPos0[2], drawable, objects.get(1));

	gl.glBindTexture(GL2.GL_TEXTURE_2D, texName[1]);
	drawAlien(-cx, -cy, -cz, drawable, objects.get(0));
	// gl.glRotatef(180, 0, 1, 0);
	drawAliena(-cx, -cy, -cz, drawable, objects.get(0));
	drawAlienb(-cx, -cy, -cz, drawable, objects.get(0));
	drawAlienc(-cx, -cy, -cz, drawable, objects.get(0));
	//gl.glRotatef(100, 0, 1, 0);
	drawAliend(-cx, -cy, -cz, drawable, objects.get(0));

	gl.glDisable(GL2.GL_TEXTURE_2D);
	gl.glFlush();

    }

    public void drawAlien(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(3.3f, 3.3f, 3.3f);
	gl.glTranslatef(x, y + 8, z + 40);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }

    public void drawAliena(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(2.3f, 2.3f, 2.3f);
	gl.glTranslatef(x + 5, y + 8, z + 50);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }
    
    public void drawAlienb(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(2.5f, 2.5f, 2.5f);
	gl.glTranslatef(x - 10, y + 8, z + 55);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }
    
    public void drawAlienc(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(2.75f, 2.75f, 2.75f);
	gl.glTranslatef(x + 20, y + 8, z + 55);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }
    
    public void drawAliend(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(3f, 3f, 3f);
	gl.glTranslatef(x - 20, y + 8, z + 55);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }


    public void drawSaucer1(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	// System.out.println(theta);
	//gl.glRotatef(180, 0, 1, 0);
	gl.glScalef(15f, 15f, 15f);
	gl.glTranslatef(x - 8, y + 9, z + 10);
	gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
	gl.glPolygonOffset(1, 0.9f);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
	gl.glColor3f(0, 0, 0);
	o.render(drawable, GL2.GL_POLYGON);
	gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(211/255f, 211/255f, 211/255f);
	o.renderNormal(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }
    
    public void drawSaucer1c(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	// System.out.println(theta);
	//gl.glRotatef(180, 0, 1, 0);
	gl.glScalef(15f, 15f, 15f);
	gl.glTranslatef(x - 7, y + 9, z + 5);
	gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
	gl.glPolygonOffset(1, 0.9f);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
	gl.glColor3f(0, 0, 0);
	o.render(drawable, GL2.GL_POLYGON);
	gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(211/255f, 211/255f, 211/255f);
	o.renderNormal(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }

    public void drawSaucer1a(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {

	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	//System.out.println(theta);
	//gl.glRotatef(180, 0, 1, 0);
	gl.glScalef(35, 35, 35);
	gl.glTranslatef(x - 6, y + 8, z - 7);
	gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
	gl.glPolygonOffset(1, 0.9f);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
	gl.glColor3f(0, 0, 0);
	o.render(drawable, GL2.GL_POLYGON);
	gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(211/255f, 211/255f, 211/255f);
	o.renderNormal(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }

    public void drawSaucer2a(float x, float y, float z, GLAutoDrawable drawable, GLObject o) {
	gl.glPushMatrix();

	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);

	gl.glScalef(5f, 5f, 5f);
	gl.glTranslatef(x, y + 15, z);
	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	gl.glColor3f(1, 1, 1);
	o.render(drawable, GL2.GL_POLYGON);

	gl.glPopMatrix();

    }

    static public void main(String[] args) throws FileNotFoundException, IOException {

	new animatedScene();

    }

    public void makeObject(String filePath) throws FileNotFoundException, IOException {

	this.object = new GLObject(filePath);
	objects.add(object);

    }

    animatedScene() throws FileNotFoundException, IOException {

	objects = new ArrayList<GLObject>();
	makeObject(alienFile);
	makeObject(saucer1file);
	makeObject(saucer2file);

	frame = new JFrame("animated object");
	GLProfile glp = GLProfile.getDefault();
	GLCapabilities caps = new GLCapabilities(glp);
	GLCanvas canvas = new GLCanvas(caps);

	frame.add(canvas);
	frame.setLayout(new BorderLayout());

	JPanel north = new JPanel(new BorderLayout());
	JPanel northWest = new JPanel(new BorderLayout());
	JPanel northEast = new JPanel(new BorderLayout());
	JPanel northCenter = new JPanel(new BorderLayout());

	JPanel fourth = new JPanel(new BorderLayout());
	JPanel ninth = new JPanel(new BorderLayout());
	JPanel tenth = new JPanel(new BorderLayout());

	zCam = (float) -270;
	xCam = (float) 10;
	yCam = (float) 0;

	cx = (xMax() + xMin()) / 2f;
	cy = (yMax() + yMin()) / 2f;
	cz = (zMax() + zMin()) / 2f;

	startPos0[0] = -cx;
	startPos0[1] = -cy;
	startPos0[2] = -cz;
	startPos1[0] = -cx;
	startPos1[1] = -cy;
	startPos1[2] = -cz;
	lightAmbient[0] = lightAmbient[1] = lightAmbient[2] = 5 / 100f;
	lightDiffuse[0] = lightDiffuse[1] = lightDiffuse[2] = 60 / 100f;
	lightSpecular[0] = lightSpecular[1] = lightSpecular[2] = 0 / 100f;
	materialShininess = 50;
	endPos0 = startPos0.clone();
	currPos0 = startPos0.clone();
	endPos1 = startPos1.clone();
	currPos1 = startPos1.clone();

	pauseButton = new JButton("Pause");
	pauseButton.addActionListener(this);
	fourth.add(pauseButton);

	resetButton = new JButton("Reset");
	resetButton.addActionListener(this);
	ninth.add(resetButton);

	quitButton = new JButton("Quit");
	quitButton.addActionListener(this);
	tenth.add(quitButton);
	northWest.add(fourth, BorderLayout.NORTH);
	northWest.add(ninth, BorderLayout.CENTER);
	northWest.add(tenth, BorderLayout.SOUTH);

	north.add(northWest, BorderLayout.WEST);
	north.add(northEast, BorderLayout.EAST);
	north.add(northCenter, BorderLayout.CENTER);

	JPanel center = new JPanel(new GridLayout(1, 1));
	center.add(canvas);

	frame.add(north, BorderLayout.NORTH);
	// frame.setVisible(true);
	frame.add(center, BorderLayout.CENTER);
	frame.pack();

	frame.setSize(ww, wh);
	frame.setLocation(-50, 50);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.setVisible(true);

	canvas.addGLEventListener(this);

	FPSAnimator animator = new FPSAnimator(canvas, 60);
	animator.start();
    }

    public void stateChanged(ChangeEvent e) {

	if (e.getSource() == zoomSlider) {
	    zCam = (float) (zoomSlider.getValue());
	} else if (e.getSource() == panHorizontalSlider) {
	    xCam = (float) panHorizontalSlider.getValue();
	} else if (e.getSource() == panVerticalSlider) {
	    yCam = (float) panVerticalSlider.getValue();
	}
	if (e.getSource() == ambientSlider) {
	    lightAmbient[0] = lightAmbient[1] = lightAmbient[2] = ambientSlider.getValue() / 100.0f;
	}
	if (e.getSource() == diffuseSlider) {
	    lightDiffuse[0] = lightDiffuse[1] = lightDiffuse[2] = diffuseSlider.getValue() / 100.0f;
	}
	if (e.getSource() == specularSlider) {
	    lightSpecular[0] = lightSpecular[1] = lightSpecular[2] = specularSlider.getValue() / 100.0f;
	}
	if (e.getSource() == shininessSlider) {
	    materialShininess = shininessSlider.getValue();
	}

    }

    public void actionPerformed(ActionEvent event) {

	if (event.getSource() == resetButton)
	    reset = !reset;
	else if (event.getSource() == pauseButton) {
	    pause = !pause;
	} else if (event.getSource() == quitButton) {
	    System.exit(0);
	}

    }

    public void dispose(GLAutoDrawable arg0) {
    }

    public float xMax() {

	ArrayList<Float> max = new ArrayList<Float>();
	for (GLObject o : objects)
	    max.add(o.getMaxX());
	return Collections.max(max);

    }

    public float xMin() {

	ArrayList<Float> min = new ArrayList<Float>();
	for (GLObject o : objects)
	    min.add(o.getMinX());
	return Collections.min(min);

    }

    public float yMax() {

	ArrayList<Float> max = new ArrayList<Float>();
	for (GLObject o : objects)
	    max.add(o.getMaxY());
	return Collections.max(max);

    }

    public float yMin() {

	ArrayList<Float> min = new ArrayList<Float>();
	for (GLObject o : objects)
	    min.add(o.getMinY());
	return Collections.min(min);

    }

    public float zMax() {

	ArrayList<Float> max = new ArrayList<Float>();
	for (GLObject o : objects)
	    max.add(o.getMaxZ());
	return Collections.max(max);

    }

    public float zMin() {

	ArrayList<Float> min = new ArrayList<Float>();
	for (GLObject o : objects)
	    min.add(o.getMinZ());
	return Collections.min(min);

    }

    private JSlider newSlider(JPanel parent, int min, int max, int step, String label) {

	JSlider slider = new JSlider(min, max);
	slider.setMajorTickSpacing(step);
	slider.setPaintTicks(true);
	slider.setPaintLabels(true);
	slider.addChangeListener(this);
	JLabel name = new JLabel(label);
	parent.add(name, BorderLayout.WEST);
	parent.add(slider, BorderLayout.CENTER);
	return slider;

    }

    void makeTextures() {

	TexImage img = null;
	for (int k = 0; k < texFileName.length; k++) {
	    String fname = texFileName[k];

	    try {
		// call the TexImage constructor
		img = new TexImage(new File(fname));
	    } catch (IOException e) {
		System.out.println("error " + e.getMessage() + " opening file " + fname);
		continue;
	    }

	    byte[] pixels = img.getPixels();
	    gl.glBindTexture(GL2.GL_TEXTURE_2D, texName[k + 1]);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
	    gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL2.GL_RGBA,
		    GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(pixels));
	}

    }

}