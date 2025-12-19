package com.turningcircles;

public class Rect {
    public float centreX, centreY, width, height;
    float[] coordsX, coordsY;

    public Rect(float centreX, float centreY, float width, float height) {
        this.centreX = centreX;
        this.centreY = centreY;
        this.width = width;
        this.height = height;

        var hw = width / 2;
        var hh = height / 2;

        coordsX = new float[]{
                centreX + hw,
                centreX + hw,
                centreX - hw,
                centreY - hw
        };

        coordsY = new float[]{
                centreY - hh,
                centreY + hh,
                centreY + hh,
                centreY - hh
        };
    }
}
