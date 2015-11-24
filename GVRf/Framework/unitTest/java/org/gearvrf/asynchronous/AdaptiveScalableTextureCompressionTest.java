package org.gearvrf.asynchronous;

import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;

import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.animation.GVRTransformAnimation;
import org.gearvrf.utility.RuntimeAssertion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, TextUtils.class})
public class AdaptiveScalableTextureCompressionTest {

    @Before
    public void initAdaptiveScalableTextureCompressionTest() {
        PowerMockito.mockStatic(SparseArray.class);

    }

    /**
     * Tests the parseMethod
     */
    @Test
    public void parseTest() {

        final AdaptiveScalableTextureCompression adaptive = new AdaptiveScalableTextureCompression();
        byte[] data = { (byte)0x00000013, (byte)0x000000AB, (byte)0x000000A1, (byte) 0x0000005C,
                4,4,14,14,2,5,5,5,5,5,5,5,5}; //valid values

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(data);
        final CompressedTexture texture = adaptive.parse(data, reader);

        Assert.assertNotNull(texture);
        Assert.assertEquals(data, texture.getArray());
    }


    /**
     * Tests the parseMethod
     */
    @Test (expected = RuntimeAssertion.class)
    public void parseInvalidTest() {

        final AdaptiveScalableTextureCompression adaptive = new AdaptiveScalableTextureCompression();

        // trying to get format 9 x 4, that doesn't contain in formats list

        byte[] data = { (byte)0x00000013, (byte)0x000000AB, (byte)0x000000A1, (byte) 0x0000005C,
                9,4, //invalid values for format
                14,14,2,5,5,5,5,5,5,5,5};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(data);
        adaptive.parse(data, reader);
    }

    /**
     * Tests the sniff method
     */
    @Test
    public void sniffTest() {
        final AdaptiveScalableTextureCompression adaptive = new AdaptiveScalableTextureCompression();

        //header - values: 19, 171, 161, 92 - result: 0x5CA1AB13

        byte[] data = {(byte)0x00000013, (byte)0x000000AB, (byte)0x000000A1, (byte) 0x0000005C, //values for header
                0,0,0,0,0,0,0,0,0,0,0,0,0};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(data);
        final boolean magicNumber = adaptive.sniff(data, reader);

        Assert.assertTrue(magicNumber);

    }

    /**
     * Tests the sniff method with false return
     */
    @Test
    public void sniffFalseTest() {
        final AdaptiveScalableTextureCompression adaptive = new AdaptiveScalableTextureCompression();

        //header - values: 20, 171, 161, 92 - result: 0x5CA1AB14

        byte[] data = {(byte)0x00000014, (byte)0x000000AB, (byte)0x000000A1, (byte) 0x0000005C,
                0,0,0,0,0,0,0,0,0,0,0,0,0};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(data);
        final boolean magicNumber = adaptive.sniff(data, reader);

        Assert.assertFalse(magicNumber);

    }

}
