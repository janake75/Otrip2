package com.example.janakeblomqvist.otrip;

/**
 * Created by jan-akeblomqvist on 2016-10-17.
 */

public class HeadingFilter {
    private static int size = 10;
    private float[] headings;
    private int pos = 0;

    public HeadingFilter() {
        headings = new float[size];
        for (int i = 0; i < size; i++)
            headings[i] = 0.0f;
    }

    public void add(float head) {
        headings[pos++] = head;
        if (pos >= size)
            pos = 0;
    }

    public float getHeading() {
        float tmp = 0.0f;
        for (int i = 0; i < size; i++)
            tmp += headings[i];

        return tmp / size;
    }

    ;


}
