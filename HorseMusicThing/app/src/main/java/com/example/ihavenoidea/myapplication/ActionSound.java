public class ActionSound {
    private String resourceName;
    private float freq;

    public String getResourceName() {
        return resourceName;
    }

    public float getFreq() {
        return freq;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ActionSound) {
            ActionSound sound = (ActionSound) o;
            return (sound.resourceName.equals(this.resourceName) && sound.freq == this.freq);
        }
        return false;
    }
}