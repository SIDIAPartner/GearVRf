package org.gearvrf.asynchronous;

import junit.framework.Assert;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCompressedCubemapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.ClassLoaderUtil;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Throttler.class,
        GVRContext.class,
        GVRAndroidResource.class,
        CompressedTexture.class,
        ZipInputStream.class,
        AsyncCompressedCubemapTexture.class})
@SuppressStaticInitializationFor({NativeClassUtils.NATIVE_COMPRESSED_CUBEMAP_TEXTURE})
public class AsyncCompressedCubemapTextureTest {

    //Fields for tests
    private GVRContext context;
    private GVRAndroidResource.CancelableCallback<GVRHybridObject> hybridCallback;
    private final int priority = 0;
    private InputStream stream;
    private final Class<? extends GVRHybridObject> textureClass = GVRCompressedCubemapTexture.class;
    private ZipInputStream zipStream;

    private static Throttler.AsyncLoaderFactory factory;

    //executes the runnable
    private final Answer runnableAnswer = new Answer<Object>() {
        public Object answer(InvocationOnMock invocation) throws Exception {
            final Object[] args = invocation.getArguments();
            final Runnable runnable = (Runnable) args[0];
            runnable.run();
            return null;
        }
    };

    private static ZipEntry newZip(String name) {
        return new ZipEntry(name);
    }

    @Before
    public void initAsyncCompressed() throws Exception {

//      mock static
        MockRepository.clear();
        PowerMockito.mockStatic(GVRContext.class);
        PowerMockito.mockStatic(ZipInputStream.class);
        zipStream = PowerMockito.mock(ZipInputStream.class);

        PowerMockito.whenNew(ZipInputStream.class).withArguments(Mockito.any(InputStream.class)).thenReturn(zipStream);
        PowerMockito.when(zipStream.getNextEntry()).thenReturn(newZip("file0"),newZip("file1"), newZip("file2"),
                newZip("file3"), newZip("file4"), newZip("file5"), null);

        PowerMockito.mockStatic(Throttler.class);
        PowerMockito.mockStatic(GVRContext.class);

        context = PowerMockito.mock(GVRContext.class);
        hybridCallback = PowerMockito.mock(GVRAndroidResource.CancelableCallback.class);
        stream = PowerMockito.mock(InputStream.class);

        //static block interceptor
        PowerMockito.when(Throttler.class, "registerDatatype", Mockito.any(hybridCallback.getClass()),
                Mockito.any(Throttler.AsyncLoaderFactory.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                final Object[] args = invocation.getArguments();
                factory = (Throttler.AsyncLoaderFactory) args[1];
                return null;
            }
        });

        //necessary instantiation in order to call static block
        AsyncCompressedCubemapTexture object = new AsyncCompressedCubemapTexture() {
            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }
        };
    }

    @Test
    public void loadTextureTest() throws Exception {

        final Map<String, Integer> map = PowerMockito.mock(Map.class);;
        Whitebox.setInternalState(AsyncCompressedCubemapTexture.class, "faceIndexMap", map);

        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        PowerMockito.when(resource.getStream()).thenReturn(stream);

        final GVRAndroidResource.CancelableCallback<GVRTexture> callbacks = PowerMockito
                .mock(GVRAndroidResource.CancelableCallback.class);
        AsyncCompressedCubemapTexture.loadTexture(context, callbacks, resource, priority, map);

        //verify Throttler call
        PowerMockito.verifyStatic();
        Throttler.registerCallback(Mockito.any(GVRContext.class), Mockito.any(textureClass.getClass()), Mockito.any(callbacks.getClass()),
                Mockito.any(resource.getClass()), Mockito.anyInt());
        Assert.assertEquals(Whitebox.getInternalState(AsyncCompressedCubemapTexture.class, "faceIndexMap"), map);
    }

    @Test
    public void createGVRCompressedCubemapTextureWithEmptyImageTest() throws Exception {

        final Map<String, Integer> map = PowerMockito.mock(Map.class);;
        Whitebox.setInternalState(AsyncCompressedCubemapTexture.class, "faceIndexMap", map);

        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        PowerMockito.when(resource.getStream()).thenReturn(stream);

        //checks if catches the IOException
        final Runnable runnable = factory.threadProc(context, resource, hybridCallback, 2);
        runnable.run();

        Mockito.verify(zipStream, Mockito.times(1)).close();
        Mockito.verify(hybridCallback, Mockito.times(2)).failed(Mockito.any(Throwable.class),
                Mockito.any(GVRAndroidResource.class));
    }

    @Test
    public void createGVRCompressedCubemapTextureWithIOExceptionOnCloseTest() throws Exception {
        PowerMockito.mockStatic(CompressedTexture.class);
        PowerMockito.when(context, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        final Map<String, Integer> map = PowerMockito.mock(Map.class);
        Whitebox.setInternalState(AsyncCompressedCubemapTexture.class, "faceIndexMap", map);

        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        PowerMockito.when(resource.getStream()).thenReturn(stream);

        PowerMockito.when(map.get(Mockito.anyString())).thenReturn(0);

        PowerMockito.when(CompressedTexture.load(Mockito.any(InputStream.class), Mockito.anyInt(),
                Mockito.anyBoolean())).thenThrow(new IOException());

        //checks if catches the IOException
        PowerMockito.when(zipStream, "close").thenThrow(new IOException());
        final Runnable runnable = factory.threadProc(context, resource, hybridCallback, 1);
        runnable.run();

        Mockito.verify(resource, Mockito.times(1)).closeStream();

        //should not stop even though IOException has been called before Array instantiation
        Mockito.verify(hybridCallback, Mockito.times(1)).loaded(Mockito.any(GVRHybridObject.class),
                Mockito.any(GVRAndroidResource.class));
    }

    @Test
    public void sucessfullcreateGVRCompressedCubemapTextureTest() throws Exception {
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_COMPRESSED_CUBEMAP_TEXTURE));
        PowerMockito.mockStatic(CompressedTexture.class);
        PowerMockito.when(context, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        final Map<String, Integer> map = PowerMockito.mock(Map.class);
        Whitebox.setInternalState(AsyncCompressedCubemapTexture.class, "faceIndexMap", map);

        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        PowerMockito.when(resource.getStream()).thenReturn(stream);

        PowerMockito.when(map.get(Mockito.anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                //pattern of filenames must be "file"+number: EX: file1, file2, file3...
                final Object[] args = invocation.getArguments();
                final String fileName = (String) args[0];
                return Integer.parseInt(fileName.substring(4, 5));
            }
        });

        final CompressedTexture compTexture = Mockito.mock(CompressedTexture.class);
        PowerMockito.when(compTexture.getArray()).thenReturn(new byte[5]);
        PowerMockito.when(compTexture.getArrayOffset()).thenReturn(0);

        final GVRTextureParameters textureParameters = Mockito.mock(GVRTextureParameters.class);
        Whitebox.setInternalState(context, "DEFAULT_TEXTURE_PARAMETERS", textureParameters);

        PowerMockito.when(CompressedTexture.load(Mockito.any(InputStream.class), Mockito.anyInt(),
                        Mockito.anyBoolean())).thenReturn(Mockito.mock(CompressedTexture.class));

        //checks if catches the IOException
        PowerMockito.when(zipStream, "close").thenThrow(new IOException());
        final Runnable runnable = factory.threadProc(context, resource, hybridCallback, 1);
        runnable.run();

        //Mockito.verify(resource, Mockito.times(1)).closeStream();

        //should stop and invok fail because IOException has been called before Array instantiation
        Mockito.verify(hybridCallback, Mockito.times(1)).loaded(Mockito.any(GVRHybridObject.class),
                Mockito.any(GVRAndroidResource.class));
        Mockito.verify(textureParameters, Mockito.times(1)).getCurrentValuesArray();
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_COMPRESSED_CUBEMAP_TEXTURE));

    }


}
