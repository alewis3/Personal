import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TJ Challstrom on 17-Apr-19 at 09:10 PM.
 * Dispenser goes here!
 */
public class CoreRunnable implements Runnable {
    //We use an Atomic type (courtesy of Java) because we're going to be modifying this value from another thread. *Atomic is required here*
    final AtomicBoolean shouldRun;
    private final Random random = new Random();
    private final CoreType coreType;
    private final long coreDelay;

    public CoreRunnable(CoreType coreType, long coreDelay) {
        this.coreType = coreType;
        this.coreDelay = coreDelay;
        this.shouldRun = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        //check shouldRun to see if we should keep running, or should finish up run()
        while (shouldRun.get()) {
            //print this core's message
            printCoreMessage();
            //wait coreDelay milliseconds before allowing it to speak again
            try {
                Thread.sleep(coreDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(coreType + " exiting.");
    }

    private void printCoreMessage() {
        //generate random number then throw it back into the range of valid options
        byte dialogueOption = (byte) (random.nextInt() % 3);

        //print a message to the tester depending on the core type and a random number
        switch (coreType) {
            case Fact:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("The billionth digit of Pi is 9");
                        break;
                    case 1:
                        System.out.println("Humans can survive underwater. But not for very long.");
                        break;
                    case 2:
                        System.out.println("Polymerase I polypeptide A is a human gene.");
                        break;
                }
                break;
            case Anger:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("*snarl*");
                        break;
                    case 1:
                        System.out.println("*roar*");
                        break;
                    case 2:
                        System.out.println("*growl*");
                        break;
                }
                break;
            case Space:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("Space space wanna go to space yes please space. Space space. Go to space.");
                        break;
                    case 1:
                        System.out.println("Love space. Need to go to space.");
                        break;
                    case 2:
                        System.out.println("Ohmygodohmygodohmygod! I'm in space!");
                        break;
                }
                break;
            case Morality:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("*");
                        break;
                    case 1:
                        System.out.println("**");
                        break;
                    case 2:
                        System.out.println("***");
                        break;
                }
                break;
            case Adventure:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("What, are you fighting that guy? You got that under control? You know, because, looks like there's a lot of stuff on fire...");
                        break;
                    case 1:
                        System.out.println("I'll tell ya, it's times like this I wish I had a waist so I could wear all my black belts. Yeah, I'm a black belt. In pretty much everything. Karate. Larate. Jiu Jitsu. Kick punching. Belt making. Taekwondo... Bedroom.");
                        break;
                    case 2:
                        System.out.println("Dun-dun-dun-dun-dun-dun-DUN! DUN DUN! Dunna-dunna-na-dunna-na-DUN! DUN DUN! nananaDUNDUNDUN dun-dun-dun-dun-dun-dun...");
                        break;
                }
                break;
            case Curiosity:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("Hey, look at THAT thing! No, that other thing.");
                        break;
                    case 1:
                        System.out.println("What is THAT?");
                        break;
                    case 2:
                        System.out.println("Do you smell something burning?");
                        break;
                }
                break;
            case Dampening:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("Okay, I'll take that as a no, then.");
                        break;
                    case 1:
                        System.out.println("Did something break back there?");
                        break;
                    case 2:
                        System.out.println("Alright, fine. I'm not saying another word until you do it properly. I'm sick of this.");
                        break;
                }
                break;
            case Intelligence:
                switch (dialogueOption) {
                    case 0:
                        System.out.println("One 18.25 ounce package chocolate cake mix.");
                        break;
                    case 1:
                        System.out.println("Nine large egg yolks.");
                        break;
                    case 2:
                        System.out.println("One cross borehole electro-magnetic imaging rhubarb.");
                        break;
                }
                break;
            default:
                throw new RuntimeException("Invalid Core type. You monster.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoreRunnable that = (CoreRunnable) o;
        return coreType == that.coreType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coreType);
    }
}
