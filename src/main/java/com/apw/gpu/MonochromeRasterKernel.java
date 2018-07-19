package com.apw.gpu;

import com.aparapi.Kernel;

/**
 * The <code>MonochromeRasterKernel</code> subclass describes a {@link com.aparapi.Kernel Kernel}
 * that converts a bayer rgb byte array into a monochromatic bayer byte array.
 */
public class MonochromeRasterKernel extends Kernel {

    private int nrows, ncols;

    private byte[] bayer, mono;

    /**
     * Constructs an <code>MonochromeRasterKernel</code> Aparapi {@link com.aparapi.opencl.OpenCL OpenCL} kernel.
     *
     * @param bayer Array of bayer arranged rgb colors
     * @param mono  Monochrome copy of the bayer array
     * @param nrows Number of rows to filter
     * @param ncols Number of columns to filter
     */
    public MonochromeRasterKernel(byte[] bayer, byte[] mono, int nrows, int ncols) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public void setValues(byte[] bayer, byte[] mono, int nrows, int ncols) {
        this.bayer = bayer;
        this.mono = mono;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Returns a monochrome bayer byte array,
     * Should be called to retrieve result after kernel is executed.
     *
     * @return Monochrome bayer byte array
     */
    public byte[] getMono() {
        return mono;
    }

    @Override
    public void run() {

        // these might not be accurate
        int rows = getGlobalId(1);
        int cols = getGlobalId(0);

        mono[rows * ncols + cols] = bayer[rows * ncols * 2 + cols * 2 + 1];
    }
}