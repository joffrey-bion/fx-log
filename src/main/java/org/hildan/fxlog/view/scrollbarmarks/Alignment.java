package org.hildan.fxlog.view.scrollbarmarks;

public enum Alignment {
    START(0), CENTER(-0.5), END(-1);

    private final double thicknessFactor;

    Alignment(double thicknessFactor) {
        this.thicknessFactor = thicknessFactor;
    }

    public double computeOffset(double thickness) {
        return thickness * thicknessFactor;
    }
}
