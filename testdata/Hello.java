
/* Test class */ 
public class Hello implements Runnable {

    private String name;

    public Hello(String name){
        this.name = name;
    }

    public void run(){
        for (int i = 0; i < name.length(); i++){
            System.out.println("Hello there " + name);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        
        Thread[] tp = new Thread[args.length];
        int i = 0;
        for (String arg : args){
            tp[i] = new Thread(new Hello(arg));
            tp[i].start();
            i++;
        }

        for (Thread t : tp){
            t.join();
        }
    }
}
