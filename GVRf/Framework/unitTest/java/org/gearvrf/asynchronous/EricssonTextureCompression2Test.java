package org.gearvrf.asynchronous;

import android.opengl.GLES30;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.gearvrf.utility.RuntimeAssertion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(EricssonTextureCompression2.class)
public class EricssonTextureCompression2Test {

    private EricssonTextureCompression2 ericssonTexture;

    @Before
    public void initEricssonTextureTest() {
        ericssonTexture = new EricssonTextureCompression2();
    }

    @Test
    public void sniffSuccessfulTest() {
        // 80 - 75 - 77 - 32 - 50 - 30 - 0 -> right values
        final byte[] array = { (byte)0x00000050, (byte)0x0000004B, (byte)0x0000004D, (byte) 0x00000020,
                (byte)0x00000032,(byte)0x00000030,(byte)0x00000000,14,2,5,5,5,5,5,5,5,5};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(array);

        final boolean result = ericssonTexture.sniff( array , reader );
        Assert.assertTrue(result);
    }

    @Test
    public void sniffFailWrongPKMTest() {
        // 81 - 75 - 77 - 32 - 50 - 30 - 0 -> wrong pkm values
        final byte[] array = { (byte)0x00000051, (byte)0x0000004B, (byte)0x0000004D, (byte) 0x00000020,
                (byte)0x00000032,(byte)0x00000030,(byte)0x00000000,14,2,5,5,5,5,5,5,5,5};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(array);

        final boolean result = ericssonTexture.sniff(array, reader );
        Assert.assertFalse(result);
    }

    @Test
    public void sniffFailWrongVersionTest() {
        // 80 - 75 - 77 - 32 - 49 - 30 - 0 -> wrong version values
        byte[] array = { (byte)0x00000050, (byte)0x0000004B, (byte)0x0000004D, (byte) 0x00000020,
                (byte)0x00000031,(byte)0x00000030,(byte)0x00000000,14,2,5,5,5,5,5,5,5,5};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(array);

        final boolean result = ericssonTexture.sniff( array, reader );
        Assert.assertFalse(result);
    }

    @Test
    public void parseTest() {
        // 80 - 75 - 77 - 32 - 49 - 30 - 0 - 4 -> right values for parse
        final byte[] array = { (byte)0x00000050, (byte)0x0000004B, (byte)0x0000004D, (byte) 0x00000020,
                (byte)0x00000031,(byte)0x00000030,(byte)0x00000000,0x04,2,5,5,5,5,5,5,5,5};

        final GVRCompressedTextureLoader.Reader reader = new GVRCompressedTextureLoader.Reader(array);

        final CompressedTexture result = ericssonTexture.parse(array, reader);

        Assert.assertNotNull(result);
    }

}
