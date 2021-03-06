/**
 * Copyright 2011-2015 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.integrationTest.saveState;

import android.os.AsyncTask;
import android.os.SystemClock;
import org.androidtransfuse.annotations.UIThread;
import org.androidtransfuse.event.EventManager;

import javax.inject.Inject;

public class BackgroundAsyncTask extends AsyncTask<Object, Integer, ResetEvent> {

    private static final int SLEEP_STEPS = 100;
    private static final int SLEEP_TIME = 200;

    @Inject
    private EventManager eventManager;
    private boolean running = false;
    private volatile boolean paused = false;
    private int progress = 0;

    @Override
    protected ResetEvent doInBackground(Object... params) {
        progress = 0;
        running = true;
        broadcastUpdate();
        while (progress < SLEEP_STEPS) {
            if(!paused){
                progress++;
                broadcastProgress();
            }
            SystemClock.sleep(SLEEP_TIME);
            if(isCancelled()){
                return null;
            }
        }
        running = false;
        paused = false;
        broadcastUpdate();
        return new ResetEvent();
    }

    @Override
    protected void onPostExecute(ResetEvent resetEvent) {
        eventManager.trigger(resetEvent);
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void togglePause() {
        paused = !paused;
        broadcastUpdate();
    }

    @UIThread
    protected void broadcastProgress(){
        eventManager.trigger(new ProgressEvent(progress));
    }

    @UIThread
    protected void broadcastUpdate(){
        eventManager.trigger(new UpdateEvent(paused));
    }
}