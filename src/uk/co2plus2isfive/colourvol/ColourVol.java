package uk.co2plus2isfive.colourvol;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

public class ColourVol extends Activity {


    MediaRecorder recorder;
    LinearLayout display;
    private volatile Thread runner;
    private static final int MAX_RGB = 255;
	private static final int MAX_AMPLITUDE = 16384;

    final Runnable updater = new Runnable(){			//create new runnable object

        public void run(){          
            updateBackground();
        };
    };
    
    final Handler mHandler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {		//initialise activity
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_colour_vol);		//set ContentView to res/layout/activity_colour_vol.xml
        display = (LinearLayout) findViewById(R.id.myDisplay);	//initialise xml linear layout

        
        if (runner == null)					//check if thread has been created
        { 
            runner = new Thread(){				//create thread for new runnable object
                public void run()
                {
                    while (runner != null)			//while thread is running
                    {
                        try
                        {
                            Thread.sleep(50);			//pause thread for 50ms 
                            Log.i("Noise", "Tock");		//Log sleep
                        } catch (InterruptedException e) {};	//Ignore
                        mHandler.post(updater);			//access UI thread - updater.post
                    }
                }
            };
            runner.start();	
            Log.d("Noise", "start runner()");
        }
    }
    
    public void onResume()					//manage activity stack
    {
        super.onResume();
        startRecorder();
    }

    public void onPause()
    {
        super.onPause();
        stopRecorder();
    }

    public void startRecorder(){				//start the media recorder
        if (recorder == null)
        {
            recorder = new MediaRecorder();			//new MediaRecorder object
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);		//media source is microphone
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);	//file type compression Three_GPP
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);	//file encoding AMR_NB
            recorder.setOutputFile("/dev/null");		//assign the recording to a null file as we don't intend on keeping it
            try
            {           
                recorder.prepare();
            }catch (IllegalStateException e) {
                Log.e("prepare", "called after start() or before setOutputFormat " + Log.getStackTraceString(e));
                
            }catch (IOException ioex) {
                Log.e("prepare", "if prepare fails otherwise " + Log.getStackTraceString(ioex));
            }
           try
            {           
                recorder.start();
            }catch (IllegalStateException e) {
                Log.e("start", "if called before prepare  " + Log.getStackTraceString(e));  //
            }
        }
    }
    public void stopRecorder() {				//stop mediarecorder
        if (recorder != null) {
            recorder.stop();       
            recorder.release();
            recorder = null;
        }
    }

    public int getAmplitude() {					//getAmplitude method
        if (recorder != null)					//check the mediarecorder object has been initialised			
            return  (recorder.getMaxAmplitude());		//return the maximum amplitude measured since the last call
        else
            return 0;
    }
    
    private void updateBackground() {							
    	float amplF = (float) getAmplitude();			//cast integer value for MaxAmplitude into floating point
    	int alpha = (int) (amplF / MAX_AMPLITUDE * MAX_RGB);	//calculate colour alpha value, cast value back to integer
    	
	display.setBackgroundColor(Color.argb(alpha, 255, 000, 000));	//set linear layout background colour 
    	
    }
}
