package com.turningcircles;

public class Line {
    public int x1, y1, x2, y2;

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /// returns side of the line that point x,y is on
    /// if > 0 on left, if < 0 right, if = 0 on line
    public int calculateSide(int x, int y) {
        return (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1);
    }
}
