//        Tobacco Shop
//
// Ingredients id:
//
// TOBACCO = 0
// PAPER = 1
// MATCHES = 2
// SMOKING = -1
// WAITING = -2

//import java.io.FileOutputStream;
//import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.Semaphore;

// ################### MAIN ###################
class TobaccoShop {
    public static void main(String[] args) {

        try { // sets the default I/O to the text file
//            System.setOut(new PrintStream(new FileOutputStream(System.getProperty("user.dir")+"/output.txt", false)));
        } catch (Exception e) {
            System.out.println(e);
        }

        // initializing Semaphore
        Semaphore semaphore = new Semaphore(1,true);

        // initializing the supplier
        Supplier supplier = new Supplier(semaphore);

        // initializing the smokers
        Smoker[] smokers = new Smoker[3];
        smokers[0] = new Smoker(0, 0, supplier, semaphore);
        smokers[1] = new Smoker(1, 1, supplier, semaphore);
        smokers[2] = new Smoker(2, 2, supplier, semaphore);

        // starting
        System.out.println("----------- WELCOME TO TOBACCO SHOP -----------");
        supplier.start();
        smokers[0].start();
        smokers[1].start();
        smokers[2].start();
    }
}

// ################### SUPPLIER ###################
class Supplier extends Thread {
    private int[] table;
    Semaphore semaphore;

    // get and set methods are used by smoker to access the shop table
    public int[] getTable() {
        return table;
    }
    public void setTable(int i, int value) {
        this.table[i] = value;
    }

    public Supplier(Semaphore semaphore) {
        this.semaphore = semaphore;
        this.table = new int[2];
        for (int i = 0; i < 2; i++) {
            table[i] = -2; // initialize the table with none ingredients
        }
    }

    // returns true only when table state are WAITING (-2)
    private boolean waiting() {
        return ( (table[0] == -2) && (table[1] == -2) );
    }

    // put new random ingredients on shop table
    private void putIngredient() {
        try {
            Random random = new Random();
            // put on table random ingredient
            table[0] = random.nextInt(3);
            table[1] = random.nextInt(3);
            // change case equal
            while(table[0] == table[1]) { table[1] = random.nextInt(3); }
            // inform which ingredient has putted
            System.out.println("The Supplier put on table " + showIngredient(0));
            System.out.println("The Supplier put on table " + showIngredient(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns the correspondent name to ingredient number
    private String showIngredient(int i) {
        switch (table[i]) {
            case 0: return "TOBACCO";
            case 1: return "PAPER";
            case 2: return "MATCHES";
            default: return "";
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                while ( !waiting() ) { Thread.sleep(2000); }
                semaphore.acquire();
                putIngredient();
                semaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// ################### SMOKER ###################
class Smoker extends Thread{
    private int id;
    private int ingredient;
    private Supplier supplier;
    private Semaphore semaphore;

    public Smoker(int id, int ingredient, Supplier supplier, Semaphore semaphore) {
        this.id = id;
        this.ingredient = ingredient;
        this.supplier = supplier;
        this.semaphore = semaphore;
    }

    // returns the correspondent name to ingredient number
    private String showIngredient(int i) {
        switch (i) {
            case 0: return "TOBACCO";
            case 1: return "PAPER";
            case 2: return "MATCHES";
            default: return "";
        }
    }

    // returns true only if ingredient on table is wanted
    private boolean checkTable() {
        return (supplier.getTable()[0] != ingredient) &&
               (supplier.getTable()[1] != ingredient) &&
               (supplier.getTable()[0] > -1) &&
               (supplier.getTable()[1] > -1);
    }

    // informs what ingredient take from table and sets the new table state: SMOKING (-1)
    private void takesIngredient() {
        try {
            System.out.println("Smoker #" + id + " takes " + showIngredient(supplier.getTable()[0]));
            supplier.setTable(0,-1);
            System.out.println("Smoker #" + id + " takes " + showIngredient(supplier.getTable()[1]));
            supplier.setTable(1,-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // informs the number of smoker that is smoking, wait between 2 to 5 seconds, sets the table state: WAITING (-2)
    private void smokes() {
        try {
            System.out.println("Smoker #" + id + " are SMOKING");
            Thread.sleep(2000+(int)(Math.random()*((5000-2000)+1))); // between 2 and 5 seconds
            supplier.setTable(0,-2);
            supplier.setTable(1,-2);
            System.out.println("Smoker #" + id + " are FINISHED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Smoker #" + id + " have " + showIngredient(ingredient));
            while (true) {
                while ( !checkTable() ) {
                    System.out.println("Smoker #" + id + " tried to check table and hadn't lucky");
                    Thread.sleep(5000);
                }
                semaphore.acquire();
                takesIngredient();
                smokes();
                semaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}