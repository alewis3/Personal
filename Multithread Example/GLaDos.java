/*
 * Copyright (c) 2019. Challstrom. All Rights Reserved.
 */

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by TJ Challstrom on 17-Apr-19 at 09:11 PM.
 * Dispenser goes here!
 */
public class GLaDos {
    //https://www.youtube.com/watch?v=dxTNqYAWISs
    public static void main(String[] args) {
        //This is a triumph


        //I'm making a note here,
        //huge success.
        HashMap<CoreType, CoreRunnable> cores = new HashMap<>(8);
        //let's use an executor service because it's much less messy than having a bunch of runnables and threads running around in here!
        ExecutorService coreExecutor = Executors.newCachedThreadPool();


        //it's hard to overstate my satisfaction
        //build a map (lists work too!!) so we can **maintain the reference to the object we create** - this reference is required so we can set the shutdown condition later!
        cores.put(CoreType.Adventure, new CoreRunnable(CoreType.Adventure, 6*1000));
        cores.put(CoreType.Anger, new CoreRunnable(CoreType.Anger, 5*1000));
        cores.put(CoreType.Curiosity, new CoreRunnable(CoreType.Curiosity, 6*1000));
        cores.put(CoreType.Dampening, new CoreRunnable(CoreType.Dampening, 9*1000));
        cores.put(CoreType.Fact, new CoreRunnable(CoreType.Fact, 4*1000));
        cores.put(CoreType.Intelligence, new CoreRunnable(CoreType.Intelligence, 4*1000));
        cores.put(CoreType.Morality, new CoreRunnable(CoreType.Morality, 10*1000));
        cores.put(CoreType.Space, new CoreRunnable(CoreType.Space, 2*1000));


        /*
              .,-:;//;:=,
          . :H@@@MM@M#H/.,+%;,
       ,/X+ +M@@M@MM%=,-%HMMM@X/,
     -+@MM; $M@@MH+-,;XMMMM@MMMM@+-
    ;@M@@M- XM@X;. -+XXXXXHHH@M@M#@/.
  ,%MM@@MH ,@%=             .---=-=:=,.
  =@#@@@MX.,                -%HX$$%%%:;
 =-./@M@M$                   .;@MMMM@MM:
 X@/ -$MM/                    . +MM@@@M$
,@M@H: :@:                    . =X#@@@@-
,@@@MMX, .                    /H- ;@M@M=
.H@@@@M@+,                    %MM+..%#$.
 /MMMM@MMH/.                  XM@MH; =;
  /%+%$XHH@$=              , .H@@@@MX,
   .=--------.           -%H.,@@@@@MX,
   .%MM@@@HHHXX$$$%+- .:$MMX =M@@MM%.
     =XMMM@MM@MM#H;,-+HMM@M+ /MMMX=
       =%@M@M#@$-.=$@MM@@@M; %M%=
         ,:+$+-,/H#MMMMMMM@= =,
               =++%%%%+/:-.
        Aperture Science
         */


        //we do what we must
        //submit each runnable coreRunnable to the the ExecutorService coreExecutor which will build a new thread and run the runnable
        cores.values().forEach(coreExecutor::execute);

        //because

        //we can.

        //stop everything after 20 seconds
        try {
            Thread.sleep(20*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //kindly request all cores to exit (won't happen until they've finished their sleep()!
        cores.values().forEach(coreRunnable -> coreRunnable.shouldRun.set(false));
        System.out.println("GLaDos shutting down.");
        coreExecutor.shutdown();


    }
}
//still alive.
