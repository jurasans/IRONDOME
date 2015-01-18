package Project2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

class IronDome {
	static Loader load;
	Rectangle left,right;
	Point groundPointL,groundPointR;
	
	private int turretControl,reload,longReload,ammoR,ammoL,magazine;
	private long t1l,t1r,t2l,t2r;													//time managment for ammo managment. t1 - time launched ,t2 long reload wait
	double angleL,angleR;	
	boolean firedR,firedL,reloadingR,reloadingL;

	public static final int leftInterceptor = 1;
	public static final int rightInterceptor = 2;

//DONE make it possible to shoot rockets from any launcher after reload. make a long reload period after magazine is over
	//DONE make it realistic reload and remag time
	public IronDome(){
		turretControl = 0;
		left = new Rectangle(30,load.sm.getHeight()-350,50,25);
		right = new Rectangle(load.sm.getWidth()-130,load.sm.getHeight()-350,50,25);
		groundPointL=new Point((int)left.getMinX(),(int)left.getCenterY());
		groundPointR=new Point((int)right.getMaxX(),(int)right.getCenterY());
		angleL=Math.toRadians(180);
		angleR=Math.toRadians(180);
		firedR=false;
		firedL=false;
		reload=1500;
		longReload=13000;
		magazine=6;
		ammoR=getMagazine();
		ammoL=getMagazine();
		
		
	}
	
	void homing(Point aim){			
		Graphics2D g2d=load.sm.getGraphics();
		if(fireEnabled())
			g2d.setColor(Color.green);
		else
			g2d.setColor(Color.red);
		g2d.drawOval(aim.x-5, aim.y-5, 10, 10);				//drawSights
		g2d.drawLine(aim.x-10, aim.y, aim.x+10, aim.y);
		g2d.drawLine(aim.x, aim.y+10, aim.x, aim.y-10);

		if (getTurretControl()==0){								//start position
			g2d.setColor(Color.GREEN);
			g2d.fill(left);
			g2d.fill(right);
		}
		
		if (getTurretControl()==IronDome.leftInterceptor){  
			//case for left interceptor
			g2d.setColor(Color.GREEN);
			g2d.fill(right);
			
		    setAngleL(-(Math.atan2(getGroundPointL().y-aim.y ,aim.x-getGroundPointL().x)* 180 / Math.PI)/75);
			
			AffineTransform transform = new AffineTransform();
			transform.rotate(getAngleL(),left.getMinX(), left.getMaxY());
			
			AffineTransform old = g2d.getTransform();
			g2d.transform(transform);
			
			g2d.fill(left);
			

			g2d.setTransform(old);							//move the launcher if has control
			//put this somewhere
			
		}
		if (getTurretControl()==IronDome.rightInterceptor){
			
			g2d.setColor(Color.GREEN);
			g2d.fill(left);
			//System.out.println("homing Right interceptor");
			
			setAngleR((Math.atan2(getGroundPointR().y-aim.y, getGroundPointR().x-aim.x) * 180 / Math.PI)/75);
			
			AffineTransform transform = new AffineTransform();
			transform.rotate(getAngleR(),right.getMaxX(),right.getMaxY());

			AffineTransform old = g2d.getTransform();						//same thing with left
			g2d.transform(transform);
			
			g2d.fill(right);
		
			g2d.setTransform(old);
		}
		
		// weapon handling system
		if (isFiredL()){							//if weapon has been fired start timer
			setT1l(System.currentTimeMillis());
			setFiredL(false);
		}
		if (isFiredR()){
			setT1r(System.currentTimeMillis());
			setFiredR(false);
		}
		if (isReloadingL()){
			setT2l(System.currentTimeMillis());
			setReloadingL(false);
			setAmmoL(getMagazine());
		}
		if (isReloadingR()){
			setT2r(System.currentTimeMillis());
			setReloadingR(false);
			setAmmoR(getMagazine());
		}
		
	}
	
	boolean fireEnabled(){
		if (getControlledGroundPoint()!=null){
		if (getControlledGroundPoint().equals(getGroundPointL())){
			if ((!isFiredL()) && (System.currentTimeMillis()-getT1l()>getReload()) && (!isReloadingL()) && (System.currentTimeMillis()-getT2l()>getLongReload()) )
				return true;
		}
		
		else if ((getControlledGroundPoint().equals(getGroundPointR()))){
				if ( (!isFiredR()) && ((System.currentTimeMillis()-getT1r()>getReload())) && (!isReloadingR()) && (System.currentTimeMillis()-getT2r()>getLongReload()) ){
					return true;
			}
		}
		else 
			return false;
		}
		return false;
	}
	

	
	static Loader getLoad() {
		return load;
	}
	static void setLoad(Loader load) {
		IronDome.load = load;
	}
	Rectangle getLeft() {
		return left;
	}
	void setLeft(Rectangle left) {
		this.left = left;
	}
	Rectangle getRight() {
		return right;
	}
	void setRight(Rectangle right) {
		this.right = right;
	}
	double getAngleL() {
		return angleL;
	}
	void setAngleL(double angleL) {
		this.angleL = angleL;
	}
	double getAngleR() {
		return angleR;
	}
	void setAngleR(double angleR) {
		this.angleR = angleR;
	}

	

	Point getGroundPointL() {
		return groundPointL;
	}

	Point getGroundPointR() {
		return groundPointR;
	}
	
	int getTurretControl() {
		return turretControl;
	}

	void setTurretControl(int turretControl) {
		this.turretControl = turretControl;
	}

	Rectangle getControlledTurret(){
		if (getTurretControl()==IronDome.leftInterceptor)
			return left;
		if (getTurretControl()==IronDome.rightInterceptor)
			return right;
		return null;
	}
	
	Point getControlledGroundPoint(){
		if (getTurretControl()==IronDome.leftInterceptor)
			return groundPointL;
		if (getTurretControl()==IronDome.rightInterceptor)
			return groundPointR;
		return null;
	}

	int getReload() {
		return reload;
	}

	void setReload(int reload) {
		this.reload = reload;
	}

	int getLongReload() {
		return longReload;
	}

	void setLongReload(int longReload) {
		this.longReload = longReload;
	}

	int getAmmoR() {
		return ammoR;
	}

	void setAmmoR(int ammoR) {
		this.ammoR = ammoR;
	}

	int getAmmoL() {
		return ammoL;
	}

	void setAmmoL(int ammoL) {
		this.ammoL = ammoL;
	}

	boolean isFiredR() {
		return firedR;
	}

	void setFiredR(boolean firedR) {
		this.firedR = firedR;
	}

	boolean isFiredL() {
		return firedL;
	}

	void setFiredL(boolean firedL) {
		this.firedL = firedL;
	}

	long getT1l() {
		return t1l;
	}

	void setT1l(long t1l) {
		this.t1l = t1l;
	}

	long getT1r() {
		return t1r;
	}

	void setT1r(long t1r) {
		this.t1r = t1r;
	}

	boolean isReloadingR() {
		return reloadingR;
	}

	void setReloadingR(boolean reloadingR) {
		this.reloadingR = reloadingR;
	}

	boolean isReloadingL() {
		return reloadingL;
	}

	void setReloadingL(boolean reloadingL) {
		this.reloadingL = reloadingL;
	}

	long getT2l() {
		return t2l;
	}

	void setT2l(long t2l) {
		this.t2l = t2l;
	}

	long getT2r() {
		return t2r;
	}

	void setT2r(long t2r) {
		this.t2r = t2r;
	}

	int getMagazine() {
		return magazine;
	}

	void setMagazine(int magazine) {
		this.magazine = magazine;
	}


	
}


