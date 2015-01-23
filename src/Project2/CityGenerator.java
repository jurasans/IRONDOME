package Project2;

import java.awt.Rectangle;
import java.util.Random;

/**
 * generates a fixed area filled with random ractangles (buildings)
 * complete
 * 
 * @author ilia
 *
 */
public class CityGenerator {
    // MAXIMUM BUILDING SIZES
    final int maxWidth = 50;
    final int maxHeight = 50;

    // STATIC REFERENCES TO GAME OBJECTS
    static Loader load;
    static Random rand = new Random();

    // COLLECTION OF TARGETS FOR DRONE STRIKER
    private Rectangle[] country;

    private int size;
    private int yMax;
    private int xMax;

    /**
     * generates a random city
     * 
     * @param size
     *            the quantity of building to build for our country.
     */
    CityGenerator(int size) {

        yMax = load.sm.getHeight();
        xMax = load.sm.getWidth();
        this.size = size;
        country = new Rectangle[size];
        populateCountry(country);
    }

    /**
     * populates the stage with random targets which do not intersect each other
     * 
     * @param cities
     *            the collection of the cities for the dronestriker
     */
    private void populateCountry(Rectangle[] cities) {
        cities[0] = new Rectangle(new Rectangle(randInt(55, xMax - 55), randInt(yMax - 250, yMax - 55), randInt(5, maxWidth), randInt(5,
                maxHeight)));
        // System.out.println(cities[0]);
        for (int i = 1; i < cities.length; i++) {
            cities[i] = new Rectangle(randInt(55, xMax - 55), randInt(yMax - 250, yMax - 55), randInt(5, maxWidth), randInt(5, maxHeight));
            // System.out.println(cities[i]);
            for (int j = 0; j < i; j++) {
                if (cities[i].intersects(cities[j])) {
                    // System.out.println("intersected");
                    cities[i] = new Rectangle(randInt(55, xMax - 55), randInt(55, yMax - 55), randInt(5, maxWidth), randInt(5, maxHeight));
                    i--;
                }
            }

        }
    }

    /**
     * taken from stackoverflow
     * http://stackoverflow.com/questions/363681/generating-random-integers-in-a-range-with-java
     * 
     * @param min
     *            minimum number
     * @param max
     *            maximum number
     * @return random int between min and max
     */
    public static int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * get static reference to the loading class of the game engine
     * 
     * @return
     */
    static Loader getLoad() {
        return load;
    }

    /**
     * set a loading class for this class from which to take parameters like screen sizes and graphics etc..
     * 
     * @param load
     *            the class loading the game engine
     */
    static void setLoader(Loader load) {
        CityGenerator.load = load;
    }

    /**
     * get target list
     * 
     * @return a reference to current city collection
     */
    Rectangle[] getCountry() {
        return country;
    }

    /**
     * get size of stage
     * 
     * @return the number of maximum cities in a country
     */
    int getSize() {
        return size;
    }

    /**
     * change the number of cities allowed in a stage
     * 
     * @param size
     */
    void setSize(int size) {
        this.size = size;
    }
}
