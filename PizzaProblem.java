

package pizzaproblem;

import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 120323L
 */

public class PizzaProblem {
    static int num_slices = 0,slices_per_pizza=0;  
    static Boolean isFirst=true;                 //A boolean value to store whether someone has already noticed that pizza is over
    static Semaphore slice = new Semaphore(0);   //slice signals for each slice in a new pizza. Students wait on pizza to get a slice
    static Semaphore orderPizza = new Semaphore(0); //orderPizza semaphore signals when a student orders a new pizza  
    static Semaphore mutex = new Semaphore(1);  //The mutex protects the num_slices and isFirst

    
    /*
    This method enables the user to define the number of slices per a pizza
     */
    public void setPizzaSize(int slices){
        slices_per_pizza=slices;
    }
    
    /*
     * An inner class to represent an instance of a Student
     */
    class Student implements Runnable {
        private int std_id; 
        
        /*
        Each Student is assigned a unique identifier so that it is easy to demonstrate the procedure when there are more than one bus. 
        */
        Student(int index) {
            this.std_id = index;
        }


        
        @Override
        public void run() {
            while(Boolean.TRUE){
                try {
                    mutex.acquire();                     //Acquire the mutex to get access to num_slices and isfirst
                    if((isFirst)&&(num_slices==0)){      //If he is the first to notice that there are no remaining slices 
                        System.out.println("Student " + std_id + "called the deliver guy");
                        orderPizza.release();            //Signals the deliver guy to order a new pizza
                        isFirst=false;                   //Set is first to false to tell the next one that discovers pizza is gone, that he already noticed it
                        
                    }
                    mutex.release();
                    
                    slice.acquire();                     //Wait on a pizza slice; if there are no slices go to sleep
                    
                    mutex.acquire();
                    num_slices--;                        //Reduce the number of remaining slices by one
                    System.out.println("Student " + std_id + "got slice "+(num_slices+1));  
                    mutex.release();
                  
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(PizzaProblem.class.getName()).log(Level.SEVERE, "Bus " + std_id + "'s thread got interrupted !!", ex);
                }
                
                System.out.println("Student " + std_id + " is eating and studying! "); //Student eats izza and study
                System.out.println("Student " + std_id + "Finished eating!");          //Student finishes eating pizza
            }
        }
    }

    /*
     * DeliveryGuy Java inner class to represent an instance of a DeliveryGuy
     */
    class DeliveryGuy implements Runnable {

        @Override
        public void run() {
            while(Boolean.TRUE){
                try {
                    orderPizza.acquire();       //Wait on the orderPizza for an order
                    System.out.println("Making Pizza!!!");    
                    
                    mutex.acquire();  
                    num_slices=slices_per_pizza; //Set the number of slices to the slices on a new pizza
                    
                    for(int i=0;i<slices_per_pizza;i++){
                        slice.release();        //Signal for each slice
                    }
                    
                    System.out.println("Delivered a pizza with  "+slices_per_pizza+" slices!");
                    isFirst=true;
                    mutex.release();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(PizzaProblem.class.getName()).log(Level.SEVERE, "Rider's thread got interrupted !!", ex);
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Scanner reader = new Scanner(System.in); 
        System.out.println("Enter the number of students : ");
        int nu_of_stdnts = reader.nextInt(); 
        System.out.println("Enter the number of slices in a pizza : ");
        int nu_of_slices = reader.nextInt(); 
        
        
        PizzaProblem pizzaProb=new PizzaProblem();
        pizzaProb.setPizzaSize(nu_of_slices);
        
        for(int i=1;i<=nu_of_stdnts;i++){
            Student std = pizzaProb.new Student(i);
            new Thread(std).start();
        }
        
        DeliveryGuy delGuy = pizzaProb.new DeliveryGuy();
            new Thread(delGuy).start();
        
    }  
}
