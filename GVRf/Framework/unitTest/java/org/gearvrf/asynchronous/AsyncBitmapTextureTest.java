package org.gearvrf.asynchronous;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.view.Display;
import android.view.WindowManager;

import junit.framework.Assert;

import org.apache.commons.math3.analysis.function.Power;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.testUtils.NativeClassUtils;
import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.RecycleBin;
import org.gearvrf.utility.RuntimeAssertion;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;

/**
 * Created by darlison.osorio on 17/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Throttler.class,BitmapFactory.Options.class, AsyncBitmapTexture.class,
        BitmapRegionDecoder.class, BitmapFactory.class, Bitmap.class, RecycleBin.class})
@SuppressStaticInitializationFor({NativeClassUtils.GLES20})
public class AsyncBitmapTextureTest {

    private GVRContext gvrcontext;
    private Context androidContext;
    private WindowManager windowManager;
    private Display display;
    private ActivityManager activityManager;

    // value for test - valid if > 0
    private static final int maxTextureSize = 3;

    //runs instead of GLES20.glGetIntegerv method
    private final Answer glGetIntegervStub = new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {

            final Object[] args = invocation.getArguments();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof int[]) {
                    int[] size = (int[]) args[i];
                    size[0] = maxTextureSize;
                    break;
                }
            }
            return null;
        }
    };

    //executes the runnable
    private final Answer runnableAnswer = new Answer<Object>() {
        public Object answer(InvocationOnMock invocation) throws Exception {
            final Object[] args = invocation.getArguments();
            final Runnable runnable = (Runnable) args[0];
            runnable.run();
            return null;
        }
    };

    @Before
    public void initAsyncBitmapTextureTest() throws Exception {

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.GLES20));

        //mock GVRContext and Context
        gvrcontext = PowerMockito.mock(GVRContext.class);
        androidContext = PowerMockito.mock(Context.class);
        PowerMockito.when(gvrcontext.getContext()).thenReturn(androidContext);

        //mock WindowManager
        windowManager = PowerMockito.mock(WindowManager.class);
        PowerMockito.when(androidContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);

        //mock Display
        display = PowerMockito.mock(Display.class);
        PowerMockito.when(windowManager.getDefaultDisplay()).thenReturn(display);

        //mock ActivityManager
        activityManager = PowerMockito.mock(ActivityManager.class);
        PowerMockito.when(androidContext, "getSystemService", Activity.ACTIVITY_SERVICE).thenReturn(activityManager);
        PowerMockito.when(activityManager.getMemoryClass()).thenReturn(32);

        //field glUninitialized is static private boolean
        Field field = AsyncBitmapTexture.class.getDeclaredField("glUninitialized");
        field.setAccessible(true);
        field.setBoolean(null, true);
    }

    @Test
    public void setupTest() throws Exception {

        //mock native Method
        PowerMockito.when(Class.forName(NativeClassUtils.GLES20), "glGetIntegerv",
                Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyInt()).thenAnswer(glGetIntegervStub);
        PowerMockito.when(gvrcontext, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        //run the method
        final Context result = AsyncBitmapTexture.setup(gvrcontext);

        //assertions
        Mockito.verify(gvrcontext, Mockito.times(1)).runOnGlThread(Mockito.any(Runnable.class));

        // 4194304 = 32 * 1024 * 1024 * 0.125f
        Assert.assertEquals(4194304, AsyncBitmapTexture.maxImageSize);
        Assert.assertEquals(AsyncBitmapTexture.glMaxTextureSize, maxTextureSize);

        //field glUninitialized is static private boolean
        Field field = AsyncBitmapTexture.class.getDeclaredField("glUninitialized");
        field.setAccessible(true);

        //checks if initialization is completed
        Assert.assertFalse(field.getBoolean(null));

        //test the result
        Assert.assertEquals(androidContext, result);
    }

    @Test
    public void setupReturningApplicationContextTest() throws Exception {

        //mock native Method
        PowerMockito.when(Class.forName(NativeClassUtils.GLES20), "glGetIntegerv",
                Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyInt()).thenAnswer(glGetIntegervStub);
        PowerMockito.when(gvrcontext, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        //testing with an application context
        final Context applicationContext = Mockito.mock(Context.class);
        Mockito.when(androidContext.getApplicationContext()).thenReturn(applicationContext);
        Mockito.when(applicationContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);

        //run the method
        final Context result = AsyncBitmapTexture.setup(gvrcontext);

        //assertions
        Mockito.verify(gvrcontext, Mockito.times(1)).runOnGlThread(Mockito.any(Runnable.class));

        //field glUninitialized is static private boolean
        Field field = AsyncBitmapTexture.class.getDeclaredField("glUninitialized");
        field.setAccessible(true);

        //checks if initialization is completed
        Assert.assertFalse(field.getBoolean(null));

        //test the result
        Assert.assertNotSame(androidContext, result);
    }

    @Test
    public void setupWithInvalidSizeTest() throws Exception {

        //not gonna mock native in order to throw error
        PowerMockito.when(gvrcontext, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        //run the method
        try {
            AsyncBitmapTexture.setup(gvrcontext);

        } catch (RuntimeAssertion assertion) {

            //it throws an RuntimeAssertion
            Assert.assertTrue(true);
        }

        //assertions
        Mockito.verify(gvrcontext, Mockito.times(1)).runOnGlThread(Mockito.any(Runnable.class));

        //field glUninitialized is static private boolean
        Field field = AsyncBitmapTexture.class.getDeclaredField("glUninitialized");
        field.setAccessible(true);

        //checks if initialization is completed
        Assert.assertTrue(field.getBoolean(null));
    }

    @Test
    public void setupWithErrorCodeTest() throws Exception {

        //mock native Method
        PowerMockito.when(Class.forName(NativeClassUtils.GLES20), "glGetIntegerv",
                Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyInt()).thenAnswer(glGetIntegervStub);
        //mock native method to return error code
        PowerMockito.when(Class.forName(NativeClassUtils.GLES20), "glGetError").thenReturn(GLES20.GL_ACTIVE_TEXTURE);
        PowerMockito.when(gvrcontext, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        //run the method
        try {
            AsyncBitmapTexture.setup(gvrcontext);

        } catch (RuntimeAssertion assertion) {

            //it throws an RuntimeAssertion
            Assert.assertTrue(true);
        }

        //assertions
        Mockito.verify(gvrcontext, Mockito.times(1)).runOnGlThread(Mockito.any(Runnable.class));

        //field glUninitialized is static private boolean
        Field field = AsyncBitmapTexture.class.getDeclaredField("glUninitialized");
        field.setAccessible(true);

        //checks if initialization is completed
        Assert.assertTrue(field.getBoolean(null));
    }

    @Test
    public void loadTextureTest() {

        PowerMockito.mockStatic(Throttler.class);

        final GVRAndroidResource.CancelableCallback<GVRTexture> callback = PowerMockito.mock(GVRAndroidResource.CancelableCallback.class);
        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        final int priority = 0;

        AsyncBitmapTexture.loadTexture(gvrcontext, callback, resource, priority);

        // checks if callback has been registered
        PowerMockito.verifyStatic();
        Throttler.registerCallback(
                Mockito.any(GVRContext.class),
                Mockito.any(Class.class),
                Mockito.any(callback.getClass()),
                Mockito.any(GVRAndroidResource.class),
                Mockito.anyInt());
    }

    @Test
    public void loadTextureWithBitmapTextureCallbackTest() {

        PowerMockito.mockStatic(Throttler.class);

        final org.gearvrf.GVRAndroidResource.BitmapTextureCallback callback = PowerMockito.mock(org.gearvrf.GVRAndroidResource.BitmapTextureCallback.class);
        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        final int priority = 0;

        AsyncBitmapTexture.loadTexture(gvrcontext, callback, resource, priority);

        // checks if callback has been registered
        PowerMockito.verifyStatic();
        Throttler.registerCallback(
                Mockito.any(GVRContext.class),
                Mockito.any(Class.class),
                Mockito.any(callback.getClass()),
                Mockito.any(GVRAndroidResource.class),
                Mockito.anyInt());
    }

    @Test
    public void decodeStreamUsingAlternativeBitmapTest() throws Exception {

        //mocks the recycleBin

        PowerMockito.mockStatic(RecycleBin.class);
        RecycleBin recycleBin = PowerMockito.mock(RecycleBin.class);
        PowerMockito.when(RecycleBin.class, "soft").thenReturn(recycleBin);
        PowerMockito.when(recycleBin.synchronize()).thenReturn(recycleBin);

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);
        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenReturn(Long.valueOf(0));
        PowerMockito.when(stream.getChannel()).thenReturn(channel);


        final int requestedWidth = 600;
        final int requestedHeight = 300;
        final boolean canShrink = true;
        final Bitmap bitmanp = PowerMockito.mock(Bitmap.class);
        final boolean closeStream = true;

        final Bitmap bitmap = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertTrue(bitmap != null);

    }

    @Test
    public void decodeStreamUsingAlternativeBitmapWithoutFileInputStreamTest() throws IOException {

        final InputStream stream = PowerMockito.mock(InputStream.class);
        PowerMockito.when(stream.markSupported()).thenReturn(true);

        final int requestedWidth = 600;
        final int requestedHeight = 300;
        final boolean canShrink = true;
        final Bitmap bitmanp = PowerMockito.mock(Bitmap.class);
        final boolean closeStream = true;

        final Bitmap bitmap = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertTrue(bitmap != null);
    }


    @Test
    public void decodeStreamWithIOExceptionOnCloseTest() throws Exception {

        final InputStream stream = PowerMockito.mock(InputStream.class);
        PowerMockito.when(stream,"close").thenThrow(new IOException());


        final int requestedWidth = 600;
        final int requestedHeight = 300;
        final boolean canShrink = true;
        final Bitmap bitmap = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmap, closeStream);

        Assert.assertNull(result);
    }


    @Test
    public void decodeInputStreamWithIOExceptionOnDecoderTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(Bitmap.class);
        PowerMockito.mockStatic(BitmapFactory.class);

        final InputStream stream = PowerMockito.mock(InputStream.class);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenThrow(new IOException());
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapFactory.decodeStream(Mockito.any(InputStream.class), Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class)))
                .thenReturn(bitmap);

        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertEquals(bitmap, result);

        //even throwing IOException from Android Method the test cannot show any exception error.
    }


    @Test
    public void decodeFileInputStreamWithIOExceptionOnDecoderTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.class);
        PowerMockito.mockStatic(Bitmap.class);

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);

        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenReturn(Long.valueOf(0));
        PowerMockito.when(stream.getChannel()).thenReturn(channel);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(FileDescriptor.class), Mockito.anyBoolean())).thenThrow(new IOException());
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapFactory.decodeFileDescriptor(Mockito.any(FileDescriptor.class), Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertEquals(bitmap, result);
        Mockito.verify(bitmap, Mockito.times(1)).recycle();

        //even throwing IOException from Android Method the test cannot show any exception error.
    }

    @Test
    public void decodeFileInputStreamWithIOExceptionOnHelperTest() throws Exception {

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);

        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenThrow(new IOException());
        PowerMockito.when(stream.getChannel()).thenReturn(channel);


        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertNull(result);

        //even throwing IOException from Android Method the test cannot show any exception error.
    }

    @Test
    public void decodeSucessfulInputStreamTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(Bitmap.class);

        final InputStream stream = PowerMockito.mock(InputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(),Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);

        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertEquals(bitmap, result);
        Mockito.verify(decoder, Mockito.times(1)).recycle();
    }


    @Test
    public void decodeSucessfulFileInputStreamTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(BitmapFactory.class);
        PowerMockito.mockStatic(Bitmap.class);

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenReturn(Long.valueOf(0));
        PowerMockito.when(stream.getChannel()).thenReturn(channel);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapFactory.decodeFileDescriptor(Mockito.any(FileDescriptor.class), Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertEquals(bitmap, result);
        Mockito.verify(bitmap, Mockito.times(1)).recycle();
    }

    @Test()
    public void decodeInputStreamWithOutOfMemoryTest() throws Exception {

        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(Bitmap.class);

        final InputStream stream = PowerMockito.mock(InputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        //sets markSupported
        Mockito.when(stream.markSupported()).thenReturn(true);
        PowerMockito.when(stream,"reset").thenThrow(new IOException());

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);

        //throws OutOfMemory
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenThrow(new OutOfMemoryError());

        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = false;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;

        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertNull(result);
    }

    @Test
    public void decodeFileInputStreamWithOutOfMemoryTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(Bitmap.class);

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenReturn(Long.valueOf(0));
        PowerMockito.when(stream.getChannel()).thenReturn(channel);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenThrow(new OutOfMemoryError());
        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 400;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -399;
        final int requestedHeight = -299;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertNull(result);
        Mockito.verify(bitmap, Mockito.never()).recycle();
    }

    /**
    * should return the same bitmap if the size is 0
            * @throws Exception
    */
    @Test
    public void decodeFileInputStreamWithZeroSizeTest() throws Exception {

        final FileInputStream stream = PowerMockito.mock(FileInputStream.class);

        final FileChannel channel = PowerMockito.mock(FileChannel.class);
        PowerMockito.when(channel.position()).thenReturn(Long.valueOf(0));
        PowerMockito.when(stream.getChannel()).thenReturn(channel);

        final int requestedWidth = 0;
        final int requestedHeight = 0;
        final boolean canShrink = true;
        final Bitmap bitmap = Mockito.mock(Bitmap.class);
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmap, closeStream);

        Assert.assertEquals(bitmap, result);
    }

    /**
     * should return the same bitmap if the size is 0
     * @throws Exception
     */
    @Test
    public void decodeInputStreamWithZeroSizeTest() throws Exception {

        final InputStream stream = PowerMockito.mock(InputStream.class);

        final int requestedWidth = 0;
        final int requestedHeight = 0;
        final boolean canShrink = true;
        final Bitmap bitmap = Mockito.mock(Bitmap.class);
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmap, closeStream);

        Assert.assertEquals(bitmap, result);
    }

    @Test
    public void decodeInputStreamWithWrongHeightTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(Bitmap.class);

        final InputStream stream = PowerMockito.mock(InputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(),Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);

        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 1025;
        options.outWidth = 1025;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedWidth = -299;
        final int requestedHeight = 0;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertNull(result);
        //returns the first bitmap due to invalid values on height or width
    }


    @Test
    public void decodeInputStreamWithBitmapWidthLowerThanOptionWidthTest() throws Exception {
        PowerMockito.mockStatic(BitmapRegionDecoder.class);
        PowerMockito.mockStatic(BitmapFactory.Options.class);
        PowerMockito.mockStatic(Bitmap.class);

        final InputStream stream = PowerMockito.mock(InputStream.class);
        final BitmapRegionDecoder decoder = Mockito.mock(BitmapRegionDecoder.class);

        //bitmap for encode
        final Bitmap bitmap = Mockito.mock(Bitmap.class);

        Mockito.when(decoder.decodeRegion(Mockito.any(Rect.class), Mockito.any(BitmapFactory.Options.class))).thenReturn(bitmap);
        PowerMockito.when(BitmapRegionDecoder.newInstance(Mockito.any(InputStream.class), Mockito.anyBoolean())).thenReturn(decoder);
        PowerMockito.when(Bitmap.createBitmap(Mockito.anyInt(),Mockito.anyInt(), Mockito.any(Bitmap.Config.class))).thenReturn(bitmap);

        PowerMockito.spy(AsyncBitmapTexture.class);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 300;
        options.outWidth = 3;
        options.inPurgeable = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inTempStorage = new byte[ 1024 * 16];

        PowerMockito.doReturn(options).when(AsyncBitmapTexture.class,"standardBitmapFactoryOptions");

        final int requestedHeight = -299;
        final int requestedWidth = 2;
        final boolean canShrink = true;
        final Bitmap bitmanp = null;
        final boolean closeStream = true;


        final Bitmap result = AsyncBitmapTexture.decodeStream(stream, requestedWidth, requestedHeight,
                canShrink, bitmanp, closeStream);

        Assert.assertEquals(bitmap,result);

        //with none of width or height == 0, the code accepts the parameters
    }



}
