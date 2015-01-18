package Project2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;



import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

public class DroneStriker {
    public static final int DUMBFIRE_FLIGHTPLAN = 0;//streight forward
    public static final int EVASOR_FLIGHTPLAN = 1;//trys to evade incoming interception missles
    public static final int SINUSOIDAL_FLIGHTPLAN = 2;//makes a random sinusoidal flight plan so it would be harder to hit
    public static final int BALLISTIC_FLIGHTPLAN = 3;//follow quadratic path to target
    
    public static final int MIRAGE_WARHEAD = 4;//splits into 2-4 HEAT missles
    public static final int HEAT_WARHEAD = 5;//slightly faster kills building
    public static final int KORNET_WARHEAD = 6;// kills everybuilding in flight path
    public static final int SURVIVOR_WARHEAD = 7;//need 2 hits to down. first hit slows down the missle
	
    static Loader load;
	static CityGenerator country;
	static ArrayList<DroneStriker> flyers;
	
	private byte evasorLife;
	private int speed, randomTarget,type,warhead,blastLife,radius;
	private Point locator;
	boolean isDestroyed , interceptor , evasiveManeuver;
	private Rectangle target,sineTransformer;
	Ellipse2D.Double evasorRadar;
	ArrayList<Point> misslesPath;
	private Line2D.Double flightPath;
	private Ellipse2D.Double blastRadius;

	//TODO replace projectiles with affine transformed missle :D
	/**
	 * make a projectile for the interceptors
	 * @param id the defence system currently deployed
	 */
	DroneStriker(IronDome id){
		speed=24;
		locator=id.getControlledGroundPoint();
		isDestroyed=false;
		interceptor = true;
		type=DUMBFIRE_FLIGHTPLAN;
		warhead = HEAT_WARHEAD;
		Point location =getLocator();
		flightPath = new Line2D.Double(location.x,location.y,load.aiming.x,load.aiming.y);
		misslesPath=new ArrayList<Point>();
		line((int)flightPath.x1,(int)flightPath.y1,(int)flightPath.x2,(int)flightPath.y2,misslesPath,0);
		blastRadius =new Ellipse2D.Double(getFlightPath().getP2().getX()-25,getFlightPath().getP2().getY()-25,50,50);
		flyers.add(this);
		
	}
	

	
	
/**
 * make a projectile : target random city in a country. initilize random starting location. initilize firing solution.
 * @param speed - speed of projectile	
 * @param typeOfMissle - flight path 
 * @param typeOfWarhead -behaviour
 */
	DroneStriker(int speed,int typeOfMissle,int typeOfWarhead){
		// baisc attribute assignment speed,target,starting points and weapon types
		this.speed=speed;
		this.target=country.getCountry()[CityGenerator.randInt(0, country.getSize()-1)];//embed random targetting
		isDestroyed=false;
		interceptor=false;
		locator = new Point(CityGenerator.randInt(0, load.sm.getWidth()), CityGenerator.randInt(0, load.sm.getHeight()/20));
		type = typeOfMissle;
		warhead = typeOfWarhead;
		
		//init firing solutions
		if (typeOfMissle==DroneStriker.DUMBFIRE_FLIGHTPLAN) {
			flightPath = new Line2D.Double(locator.x,locator.y,target.getCenterX(),target.getCenterY());
			misslesPath=new ArrayList<Point>();
			line((int)flightPath.x1,(int)flightPath.y1,(int)flightPath.x2,(int)flightPath.y2,misslesPath,0);
		}
		
		if(typeOfMissle==DroneStriker.SINUSOIDAL_FLIGHTPLAN) {
			setLocator(new Point (target.x,locator.y));//set right above the mark
			misslesPath=new ArrayList<Point>();
			flightPath = new Line2D.Double(locator.x,locator.y,target.getCenterX(),target.getCenterY());
			sineWaver((int)flightPath.x1,(int)flightPath.y1,(flightPath.y2-flightPath.y1)/180,misslesPath);
		
		}
		
		if (typeOfMissle==DroneStriker.EVASOR_FLIGHTPLAN){
			evasorLife = (byte) 20;//set 1-3 retries
			misslesPath=new ArrayList<Point>();
			evasorRadar=new Ellipse2D.Double(locator.x-250,locator.y-250,500,500); //create a radar to follow rocket around
			flightPath = new Line2D.Double(locator.x,locator.y,target.getCenterX(),target.getCenterY());
			line((int)flightPath.x1,(int)flightPath.y1,(int)flightPath.x2,(int)flightPath.y2,misslesPath,0);//calculate firing solution 
			evasiveManeuver=false;
			
		}
		//TODO all the remainig types of missles
		

		
		//if (typeOfWarhead==DroneStriker.HEAT_WARHEAD)
		//if (typeOfwarhead==DroneStriker.KORNET_WARHEAD)
		//if (typeOfwarhead==DroneStriker.SURVIVOR_WARHEAD)
		flyers.add(this);
	}
	
	
	/**
	 * firing solution. fills the points array with projectile locations viable for a correct hit.
	 * @param x start x
	 * @param y start y
	 * @param x2 end x
	 * @param y2 end y
	 * @param points the array to fill up
	 * @param interval keep 0 if u dont want any trouble. used for another developing algorithm
	 * @return
	 */
	public void  line(int x,int y,int x2, int y2,ArrayList<Point> points,int interval) {
	    int w = x2 - x ;
	    int h = y2 - y ;
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {
	    	if (interval==0||i%interval==0){
	        points.add(new Point(x,y));
	//       System.out.println("test: "+x+","+y);
	    	}
	        
	        numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
	        }
	    }
	    
	}
	
	/**
	 * firing solution : used for a sine approach to overhead target. didnt quite actually mastered a diagonal approach. could use help.
	 * @param x1 start x
	 * @param y1 start y
	 * @param repeat desired length of operaton
	 * @param points for the firing solution projectile path .the array to be written to
	 * @return
	 */
	void sineWaver (int x1,int y1,double repeat,ArrayList<Point> points){
		int xBase   = y1;//x location where sin wave is being generated on x 
        int top     = x1;//phase
        int yScale  = 100;//magnitude of sin wave
        double xAxis   = 360*repeat;

        int yBase   = top + yScale;
        int x, y;

        for( int i=0; i < xAxis; i++ )
        {   x = xBase + i;
            y = (int)( yBase - Math.sin( Math.toRadians(i) ) * yScale );
            
           points.add(new Point( y, x ));
           
        }
	}

//TODO add angle and by choosing to add certain points and not 
		//old circle like algorithm is in code dumpe 
		
		//final note : this gives out all the points in the sine wave
	
		/** 
		 * implementing method for moving the projectile through its flight plan. could range from different behaviours to anything else
		 * this method is called  after draw and called with the same frequency
		 */
	void homing() {
		
		if(!isDestroyed()) {
			
			if (getType()==DroneStriker.DUMBFIRE_FLIGHTPLAN) {	
				load.sm.getGraphics().fillOval(getMissles().get(0).x-5, getMissles().get(0).y-5, 10, 10);// projectile render
			
				
				
															//maintain locator functioning...for the evasor quite probably 
				if (!isInterceptor()){									//interceptors only care about the explosion.
				locator.x=getMissles().get(0).x;
				locator.y=getMissles().get(0).y;
				}
				
				//System.out.println("locator changed:"+locator);
				
				for (int i=0;i<getSpeed();i++){
					if (getMissles().size()>=2)
						getMissles().remove(0);
					else
						break;
				}
				//System.out.println(missles.size());
				if ( (!isInterceptor()) && (getMissles().get(0).distance(new Point((int)target.getCenterX(),(int)target.getCenterY()))<10)) {//case of missle being a strike
					IsDestroyed(true);
					//missles=new ArrayList<Point>();
					System.out.println("Building destroyed");
					for (Rectangle city:getCountry().getCountry()){
						if(getTarget().contains(city)){
							city=null;
						}
					}
				}
				if ( (isInterceptor()) && (getMissles().get(0).distance(getFlightPath().getP2())<10) ){ // case of missle being interceptor missle
					System.out.println("50 kiloton detonation");
					for (DroneStriker ds:flyers){
						if ( (!this.equals(ds)) && (getBlastRadius().contains(ds.getMissles().get(0))) ){
							System.out.println((!this.equals(ds)));
							System.out.println((getBlastRadius().contains(ds.getMissles().get(0))));
							System.out.println("intercept successful");
							ds.IsDestroyed(true);
						}
					}
						
						load.sm.getGraphics().fillOval((int)getFlightPath().getP2().getX()-25,(int)getFlightPath().getP2().getY()-25 , 50, 50); //blast render
						IsDestroyed(true);
						
						//TODO add behaviour for the case of the interceptor meeting another object from flyer collection
					
					
					
				}
			}
			
			 if(getType()==DroneStriker.SINUSOIDAL_FLIGHTPLAN) {					//case for sinusoidal flightpath stage 1

				load.sm.getGraphics().fillOval(getMissles().get(0).x-5, getMissles().get(0).y-5, 10, 10);
				locator.x=misslesPath.get(0).x;
				locator.y=misslesPath.get(0).y;
				
				

				misslesPath.remove(0);
					//TODO fix this code for the sinus destruction
				if (misslesPath.get(0).distance(new Point((int)target.getCenterX(),(int)target.getCenterY()))<150) { //second stage of sinusoidal flight path
					int centerOfTargetX=(int) getFlightPath().x2;
					int centerOfTargetY=(int) getFlightPath().y2;
					setFlightPath(new Line2D.Double(locator.x,locator.y,getTarget().x,getTarget().y));
					misslesPath=new ArrayList<Point>();
					line((int)flightPath.x1,(int)flightPath.y1,centerOfTargetX,centerOfTargetY,misslesPath,0);
					setSpeed(1);
					setType(DUMBFIRE_FLIGHTPLAN);
					if (getTarget().contains(getLocator())){
						IsDestroyed(true);
						
					}
				}
			 }
			 
			 if(getType()==DroneStriker.EVASOR_FLIGHTPLAN) {

				 //TODO calculate angle of incoming projectile make evasor fly away from that angle.and continue its flight
				 locator.x=misslesPath.get(0).x;
				 locator.y=misslesPath.get(0).y;
				 evasorRadar.x=locator.x-250;
				 evasorRadar.y=locator.y-250;
				 load.sm.getGraphics().fillOval(getMissles().get(0).x-5, getMissles().get(0).y-5, 10, 10);
				 //load.sm.getGraphics().draw(getEvasorRadar());
				 for (int i=0;i<getSpeed();i++){
						if (getMissles().size()>=2)
							getMissles().remove(0);
						else
							break;
					}
				 
				 for (DroneStriker ds:flyers){
					 if ( (getEvasorRadar().contains(ds.getMissles().get(0))) &&(ds.isInterceptor()) && getEvasorLife()>0){
						 this.setSpeed(10);
						 setEvasorLife((byte) (getEvasorLife()-1));
					 }
					 if ( (!isInterceptor()) && (getMissles().get(0).distance(new Point((int)target.getCenterX(),(int)target.getCenterY()))<10)){
						 IsDestroyed(true);
					 }
					 
					 else{
						 this.setSpeed(1);
					 }
					 
					 
					 
				 }
		    }
		}
	}

	static Loader getLoad() {
		return load;
	}

	static void setLoad(Loader load) {
		DroneStriker.load = load;
	}

	static CityGenerator getCountry() {
		return country;
	}

	static void setCountry(CityGenerator country) {
		DroneStriker.country = country;
	}

	int getSpeed() {
		return speed;
	}

	void setSpeed(int speed) {
		this.speed = speed;
	}

	int getRandomTarget() {
		return randomTarget;
	}

	void setRandomTarget(int randomTarget) {
		this.randomTarget = randomTarget;
	}
	
	
	

	int getType() {
		return type;
	}

	void setType(int type) {
		this.type = type;
	}

	int getWarhead() {
		return warhead;
	}

	void setWarhead(int warhead) {
		this.warhead = warhead;
	}

	Point getLocator() {
		return locator;
	}

	void setLocator(Point locator) {
		this.locator = locator;
	}

	Rectangle getTarget() {
		return target;
	}

	void setTarget(Rectangle target) {
		this.target = target;
	}

	boolean isDestroyed() {
		return isDestroyed;
	}


	void IsDestroyed(boolean isdestroyed) {
		this.isDestroyed = isdestroyed;
	}


	public Rectangle getSineTransformer() {
		return sineTransformer;
	}


	public void setSineTransformer(Rectangle sineTransformer) {
		this.sineTransformer = sineTransformer;
	}


	public Line2D.Double getFlightPath() {
		return flightPath;
	}


	public void setFlightPath(Line2D.Double flightPath) {
		this.flightPath = flightPath;
	}



	boolean isInterceptor() {
		return interceptor;
	}



	void setInterceptor(boolean interceptor) {
		this.interceptor = interceptor;
	}



	ArrayList<Point> getMissles() {
		return misslesPath;
	}



	void setMissles(ArrayList<Point> misslesPath) {
		this.misslesPath = misslesPath;
	}



	Ellipse2D.Double getBlastRadius() {
		return blastRadius;
	}



	void setBlastRadius(Ellipse2D.Double blastRadius) {
		this.blastRadius = blastRadius;
	}



	int getBlastLife() {
		return blastLife;
	}



	void setBlastLife(int blastLife) {
		this.blastLife = blastLife;
	}



	int getRadius() {
		return radius;
	}



	void setRadius(int radius) {
		this.radius = radius;
	}




	byte getEvasorLife() {
		return evasorLife;
	}




	void setEvasorLife(byte evasorLife) {
		this.evasorLife = evasorLife;
	}




	Ellipse2D.Double getEvasorRadar() {
		return evasorRadar;
	}




	void setEvasorRadar(Ellipse2D.Double evasorRadar) {
		this.evasorRadar = evasorRadar;
	}
	
	
		
		
	}









				
		
	
	