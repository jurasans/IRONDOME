package Project2;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Loader extends Engine implements KeyListener, MouseListener,
MouseMotionListener,Serializable{

	//TODO limit the sinus missle to x-200 of width 
	//DONE find out why after missle is destroyed it takes time to launch another one.
	
	
	String location="";
	Point aiming;
	CityGenerator city;
	IronDome IDF;
	long refresh1=System.currentTimeMillis(),refresh2;
	Image rocket,x1,x2,x3,x4,x5;
	Animation explode;

	boolean fire=false;
	
	
	public void init() {
		super.init();
		Window w = sm.getFullScreenWindow();
		w.addKeyListener(this);
		w.addMouseListener(this);
		w.addMouseMotionListener(this);
		CityGenerator.setLoader(this);
		aiming=new Point(0,0);
		IronDome.setLoad(this);
		IDF= new IronDome();
		try {
			rocket=ImageIO.read(new File("res/rocket animated.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		city= new CityGenerator(40);
		DroneStriker.setLoad(this);
		DroneStriker.setCountry(city);
		DroneStriker.flyers= new ArrayList<DroneStriker>();
		

		stage1();
		

		

		
		
		

		
	}
	
	public static void main(String[] args) {
		new Loader().run();
	}
	
	@Override
	public void draw(Graphics2D g) {
		

		g.setColor(Color.black);
		g.fillRect(0, 0, sm.getWidth(), sm.getHeight());		//create background

		for(int i=0;i<city.getSize();i++) {						//draw all the cities
			g.setColor(Color.BLUE);
			                     //highlight the targeted city
			g.draw( city.getCountry()[i]);
		}										
		g.setColor(Color.WHITE);
		g.drawString(location+"ammoL: "+IDF.getAmmoL()+" ammoR: "+IDF.getAmmoR()+" can fire: "+IDF.fireEnabled(),20,20);	
																						//draw mouse location for debugging and other info
										
		g.setColor(Color.green);

		for (DroneStriker ds:DroneStriker.flyers){
			if (ds!=null)
				ds.homing();	
			if (stageIsComplete())
				nextStage();
		}
		//TODO start stage Building when all the other stuff end

		IDF.homing(aiming);										//irondome turret control
		//System.out.println("ground control point is: "+IDF.getControlledGroundPoint());
	
			
		
	}
	
	
	
	private void nextStage() {
		// TODO stage managment tool
		
	}

	private boolean stageIsComplete() {
		// TODO boolean to check if stage is complete
		return false;
	}

	void stage1(){
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.DUMBFIRE_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.SINUSOIDAL_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.EVASOR_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.DUMBFIRE_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.SINUSOIDAL_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.EVASOR_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.DUMBFIRE_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.SINUSOIDAL_FLIGHTPLAN,0);
		new DroneStriker(CityGenerator.randInt(1, 3),DroneStriker.EVASOR_FLIGHTPLAN,0);
	
		
	}
	
	//mouse listeners 
	@Override
	public void mouseDragged(MouseEvent arg0) {
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		aiming=e.getPoint();
		location = e.getLocationOnScreen().toString();

	}
	
	@Override
	public void mouseClicked(MouseEvent e) {	
		//TODO make an indication for the ammo system status 
		if (e.getButton()==MouseEvent.BUTTON1){

			if (IDF.getControlledGroundPoint().equals(IDF.getGroundPointL())){ //check if controlled point is left one if its not then its right one
				 //create missle from control point.

				if ((!IDF.isFiredL()) && (System.currentTimeMillis()-IDF.getT1l()>IDF.getReload()) && (!IDF.isReloadingL()) && (System.currentTimeMillis()-IDF.getT2l()>IDF.getLongReload()) ){			//check if missle has been launched. and timer is not up
					DroneStriker LF = new DroneStriker(IDF);					//if not fired ,and time is up then launch one
					IDF.setFiredL(true);
					//System.out.println("fired? : "+IDF.isFiredL());
					IDF.setAmmoL(IDF.getAmmoL()-1);	
					if (IDF.getAmmoL()==0){
						IDF.setReloadingL(true);//check if its been 2 seconds since last fire 
					}
				}
			}
			
		else{
			if ( (!IDF.isFiredR()) && ((System.currentTimeMillis()-IDF.getT1r()>IDF.getReload())) && (!IDF.isReloadingR()) && (System.currentTimeMillis()-IDF.getT2r()>IDF.getLongReload()) ){
					DroneStriker RF = new DroneStriker(IDF);
					IDF.setFiredR(true);
					System.out.println("fired? : "+IDF.isFiredR());
					IDF.setAmmoR(IDF.getAmmoR()-1);	
					System.out.println(IDF.getAmmoR()-1);
					if (IDF.getAmmoR()==0){
						IDF.setReloadingR(true);
					}
				}
			}
		}
		
		if (e.getButton()==MouseEvent.BUTTON3){
			if (IDF.getControlledGroundPoint().equals(IDF.getGroundPointL())){ //check if controlled point is left one if its not then its right one
				IDF.setReloadingL(true);
			}
			else{
				IDF.setReloadingR(true);
			}
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_Q) {
			System.exit(0);
		}
		if (e.getKeyCode()==KeyEvent.VK_0){
			
		}
		if (e.getKeyCode()==KeyEvent.VK_1){
			IDF.setTurretControl(IronDome.leftInterceptor);
			System.out.println("interceptor Control = 1");
		}
		if (e.getKeyCode()==KeyEvent.VK_2){
			IDF.setTurretControl(IronDome.rightInterceptor);//TODO fix this IDF.setTurretControl(turretControl);
		    System.out.println("interceptor Control = 2");
		}
	}
	
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	Point getAiming() {
		return aiming;
	}

	void setAiming(Point aiming) {
		this.aiming = aiming;
	}

	boolean isFire() {
		return fire;
	}

	void setFire(boolean fire) {
		this.fire = fire;
	}

	IronDome getIDF() {
		return IDF;
	}

	void setIDF(IronDome iDF) {
		IDF = iDF;
	}




	
	
	
	}
	
	
