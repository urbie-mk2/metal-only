package com.codingspezis.android.metalonly.player.stream;

import android.media.*;
import android.os.Process;
import android.util.*;

import com.codingspezis.android.metalonly.player.stream.exceptions.*;
import com.spoledge.aacdecoder.*;

/**
 * A Runnable that executes a play loop using OpencorePlayer.
 */
class OpenCorePlayRunnable implements Runnable {
    private static final String TAG = OpenCorePlayRunnable.class.getSimpleName();

    private OpencorePlayer opencorePlayer;
    private final String url;
    private final int expectedKBitSecRate;
    private int samplerateRestarts = 0;

    public OpenCorePlayRunnable(OpencorePlayer opencorePlayer, String url, int expectedKBitSecRate) {
        this.opencorePlayer = opencorePlayer;
        this.url = url;
        this.expectedKBitSecRate = expectedKBitSecRate;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        acquireLocks();
        try {
            startPlayLoop();
        } catch (Exception e) {
            Log.e(TAG, "playAsync():", e);
            getPlayerCallback().playerException(e);
        }
        releaseLocks();
    }

    private void acquireLocks() {
        opencorePlayer.streamPlayerOpencore.wakeLock.acquire();
        opencorePlayer.streamPlayerOpencore.wifiLock.acquire();
    }

    private void releaseLocks() {
        opencorePlayer.streamPlayerOpencore.wakeLock.release();
        opencorePlayer.streamPlayerOpencore.wifiLock.release();
    }

    /**
     * Starts the play loop.
     *
     * @throws Exception Either a WrongSampleRateException which could not be ignored
     *                   or another unspecified Exception from OpenCorePlayer
     */
    private void startPlayLoop() throws Exception {
        try {
            executePlayLoop();
        } catch (WrongSampleRateException e) {
            handleWrongSampleRateException(e);
        }
    }

    /**
     * The exception is ignored ten times; after that it is rethrown.
     *
     * @param wrongSampleRateException the WrongSampleRateException
     * @throws Exception the WrongSampleRateException which was repeated too often
     */
    private void handleWrongSampleRateException(WrongSampleRateException wrongSampleRateException) throws Exception {
        samplerateRestarts++;
        Log.e(TAG, "playAsync(): " + wrongSampleRateException.getMessage());
        Log.e(TAG, "playAsync(): restarting playback #" + samplerateRestarts);
        sleep500ms();

        if (samplerateRestarts <= 10) {
            startPlayLoop();
        } else {
            throw wrongSampleRateException;
        }
    }

    private void executePlayLoop() throws Exception {
        while (opencorePlayer.streamPlayerOpencore.shouldPlay) {
            opencorePlayer.play(url, expectedKBitSecRate);
        }
    }

    private void sleep500ms() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private PlayerCallback getPlayerCallback() {
        final PlayerCallback opencorePlayerPlayerCallback = opencorePlayer.getPlayerCallback();
        if (opencorePlayerPlayerCallback != null) {
            return opencorePlayerPlayerCallback;
        }

        return sDummyCallback;
    }

    PlayerCallback sDummyCallback = new PlayerCallback() {
        @Override
        public void playerStarted() {
        }

        @Override
        public void playerPCMFeedBuffer(boolean b, int i, int i2) {
        }

        @Override
        public void playerStopped(int i) {
        }

        @Override
        public void playerException(Throwable throwable) {
        }

        @Override
        public void playerMetadata(String s, String s2) {
        }

        @Override
        public void playerAudioTrackCreated(AudioTrack audioTrack) {
        }
    };
}
