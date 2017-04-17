package org.gearvrf.asynchronous;

import junit.framework.Assert;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darlison.osorio on 23/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ByteBuffer.class, GVRContext.class, GVRCompressedTextureLoader.class})
@SuppressStaticInitializationFor({NativeClassUtils.NATIVE_COMPRESSED_TEXTURE, NativeClassUtils.NATIVE_TEXTURE, NativeClassUtils.GLES20})
public class CompressedTextureTest {

    private CompressedTexture texture;
    private GVRContext context;
    private ByteBuffer data;

    private void setValues(byte[] array) {
        array[0] = (byte) 0x00000013;
        array[1] = (byte) 0x000000AB;
        array[2] = (byte) 0x000000A1;
        array[3] = (byte) 0x0000005C;
        array[4] = 4;
        array[5] = 4;
        array[6] = 14;
        array[7] = 14;
        array[8] = 2;
        array[9] = 5;
        array[10] = 5;
        array[11] = 5;
        array[12] = 5;
        array[13] = 5;
        array[14] = 5;
        array[15] = 5;
        array[16] = 5;
    }

    @Before
    public void beforeCompressedTexture() throws ClassNotFoundException {
        PowerMockito.mockStatic(GVRContext.class);
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_COMPRESSED_TEXTURE));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_TEXTURE));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.GLES20));
        context = Mockito.mock(GVRContext.class);

        data = ByteBuffer.allocate(1);

        texture = new CompressedTexture(1, 600, 300, 3, 4, data);
    }

    @Test
    public void toTextureWithDefaultParametersTest() throws Exception {

        final GVRTextureParameters textureParameters = Mockito.mock(GVRTextureParameters.class);
        Whitebox.setInternalState(context, "DEFAULT_TEXTURE_PARAMETERS", textureParameters);

        final GVRCompressedTexture compressedTexture = texture.toTexture(context, 1);

        Assert.assertNotNull(compressedTexture);

        byte[] data = new byte[5];
        int[] textures = new int[5];

        //checks if the native methods have been called
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_COMPRESSED_TEXTURE),
                VerificationModeFactory.times(1)).invoke("normalConstructor", Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(),Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any(data.getClass()), Mockito.anyInt(), Mockito.any(textures.getClass()));

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_TEXTURE),
                VerificationModeFactory.times(1)).invoke("getId", Mockito.anyLong());

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.GLES20),
                VerificationModeFactory.times(1)).invoke("glTexParameteri", Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt());

    }

    @Test
    public void toTextureWithCustomParametersTest() throws Exception {
        final GVRTextureParameters textureParameters = Mockito.mock(GVRTextureParameters.class);
        final GVRCompressedTexture compressedTexture = texture.toTexture(context, 1, textureParameters);
        Assert.assertNotNull(compressedTexture);

        byte[] data = new byte[5];
        int[] textures = new int[5];

        //checks if the native methods have been called
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_COMPRESSED_TEXTURE),
                VerificationModeFactory.times(1)).invoke("normalConstructor", Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(),Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any(data.getClass()), Mockito.anyInt(), Mockito.any(textures.getClass()));

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_TEXTURE),
                VerificationModeFactory.times(1)).invoke("getId", Mockito.anyLong());

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.GLES20),
                VerificationModeFactory.times(1)).invoke("glTexParameteri", Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void loadTest() throws Exception {

        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        final int maxLength = -1;
        final boolean closeStream = true;
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {

            private int result = 17;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                byte[] array = (byte[]) args[0];
                setValues(array);

                int last = result;
                result -= 17;
                return last;
            }
        });

        CompressedTexture result = texture.load(stream, maxLength, closeStream);

        Assert.assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadWithNullLoaderTest() throws Exception {
        PowerMockito.mockStatic(GVRCompressedTextureLoader.class);

        List<GVRCompressedTextureLoader> list = new ArrayList<GVRCompressedTextureLoader>();
        PowerMockito.when(GVRCompressedTextureLoader.getLoaders()).thenReturn(list);

        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        final int maxLength = -1;
        final boolean closeStream = true;
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {

            private int result = 17;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                byte[] array = (byte[]) args[0];
                setValues(array);

                int last = result;
                result -= 17;
                return last;
            }
        });

        texture.load(stream, maxLength, closeStream);
    }


    @Test(expected = IllegalArgumentException.class)
    public void loadWithTwoLoadersSniffsTrueTest() throws Exception {
        PowerMockito.mockStatic(GVRCompressedTextureLoader.class);
        final byte[] array = new byte[5];

        KTX loader = Mockito.mock(KTX.class);
        Mockito.when(loader.sniff(Mockito.any(array.getClass()),Mockito.any(GVRCompressedTextureLoader.Reader.class))).thenReturn(true);

        List<GVRCompressedTextureLoader> list = new ArrayList<GVRCompressedTextureLoader>();
        list.add(new AdaptiveScalableTextureCompression());
        list.add(loader);

        PowerMockito.when(GVRCompressedTextureLoader.getLoaders()).thenReturn(list);

        final InputStream stream = Mockito.mock(InputStream.class);
        final int maxLength = -1;
        final boolean closeStream = true;
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {

            private int result = 17;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                byte[] array = (byte[]) args[0];
                setValues(array);

                int last = result;
                result -= 17;
                return last;
            }
        });

        texture.load(stream, maxLength, closeStream);
    }

    @Test
    public void loadWithLenghtTest() throws Exception {

        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        final int maxLength = 17;
        final boolean closeStream = true;
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {

            private int result = 17;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                byte[] array = (byte[]) args[0];
                setValues(array);

                return result--;
            }
        });

        CompressedTexture result = texture.load(stream, maxLength, closeStream);

        Assert.assertNotNull(result);
    }

    @Test
    public void loadWithMoreThanMaxLenghtTest() throws Exception {

        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        final int maxLength = -1;
        final boolean closeStream = true;
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {

            private int result = 4096;
            private int last = 2;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                byte[] array = (byte[]) args[0];
                setValues(array);
                int value = result;
                result = last--;

                return value;
            }
        });

        CompressedTexture result = texture.load(stream, maxLength, closeStream);

        Assert.assertNotNull(result);
    }

    @Test
    public void sniffTest() throws Exception {
        final InputStream stream = Mockito.mock(InputStream.class);

        final byte[] array = new byte[5];

        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {
            private int result = 17;
            private int last = 2;

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                final byte[] array = (byte[]) args[0];
                setValues(array);
                final int value = result;
                result = last--;
                return value;
            }
        });

        final GVRCompressedTextureLoader loader = texture.sniff(stream);
        Assert.assertNotNull(loader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sniffWithTwoLoadersTest() throws Exception {

        PowerMockito.mockStatic(GVRCompressedTextureLoader.class);
        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];

        final KTX loader = Mockito.mock(KTX.class);
        Mockito.when(loader.sniff(Mockito.any(array.getClass()),Mockito.any(GVRCompressedTextureLoader.Reader.class))).thenReturn(true);

        final List<GVRCompressedTextureLoader> list = new ArrayList<GVRCompressedTextureLoader>();
        list.add(new AdaptiveScalableTextureCompression());
        list.add(loader);

        PowerMockito.when(GVRCompressedTextureLoader.getLoaders()).thenReturn(list);

        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {
            private int result = 17;
            private int last = 2;

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                final byte[] array = (byte[]) args[0];
                setValues(array);
                final int value = result;
                result = last--;
                return value;
            }
        });

        texture.sniff(stream);
    }

    @Test
    public void parseTest() throws Exception {
        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {
            private int result = 17;
            private int last = 2;

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                final byte[] array = (byte[]) args[0];
                setValues(array);
                final int value = result;
                result = last--;
                return value;
            }
        });


        CompressedTexture result = texture.parse(stream, true, new AdaptiveScalableTextureCompression());

        Assert.assertNotNull(result);
    }

    @Test(expected = IOException.class)
    public void parseThrowingIOExceptionTest() throws Exception {
        final InputStream stream = Mockito.mock(InputStream.class);
        final byte[] array = new byte[5];
        Mockito.when(stream.read(Mockito.any(array.getClass()))).thenAnswer(new Answer<Object>() {
            private int result = 17;
            private int last = 2;

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                final byte[] array = (byte[]) args[0];
                setValues(array);
                final int value = result;
                result = last--;
                return value;
            }
        });
        PowerMockito.when(stream,"close").thenThrow(new IOException());

        texture.parse(stream, true, new AdaptiveScalableTextureCompression());
    }

    @Test
    public void getDataTest() {

        final ByteBuffer result = texture.getData();
        Assert.assertEquals(result, data);

    }

}