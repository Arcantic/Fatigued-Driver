package com.fatigue.driver.app;

public enum SoundType {

    RECORDING_FINISHED(1),
    TRAINING_COMPLETE(2),
    BLANK(3);

    public int value;

    private SoundType(int value) {
        this.value = value;
    }

    public String toString() {
        switch(value) {
            case 1:
                return "Recording Finished";
            case 2:
                return "Training Complete";
            case 3:
                return "Blank";
            default:
                return "UNKNOWN";
        }
    }
}