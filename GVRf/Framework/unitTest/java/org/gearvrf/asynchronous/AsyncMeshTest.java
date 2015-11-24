package org.gearvrf.asynchronous;

import junit.framework.Assert;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCompressedCubemapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Throttler.class,
        GVRContext.class,
        GVRAndroidResource.class,
        AsyncMesh.class})
public class AsyncMeshTest {

    //Fields for tests
    private GVRContext context;
    private GVRAndroidResource.CancelableCallback<GVRMesh> meshCallback;
    private final int priority = 0;
    private final Class<? extends GVRHybridObject> meshClass = GVRMesh.class;
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

    @Before
    public void initAsyncMeshTest() throws Exception {
        //      mock static
        MockRepository.clear();
        PowerMockito.mockStatic(GVRContext.class);
        PowerMockito.mockStatic(Throttler.class);


        context = PowerMockito.mock(GVRContext.class);
        meshCallback = PowerMockito.mock(GVRAndroidResource.CancelableCallback.class);

        //static block interceptor
        PowerMockito.when(Throttler.class, "registerDatatype", Mockito.any(meshCallback.getClass()),
                Mockito.any(Throttler.AsyncLoaderFactory.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                factory = (Throttler.AsyncLoaderFactory) args[1];
                return null;
            }
        });

        //necessary instantiation in order to call static block
        AsyncMesh object = new AsyncMesh() {
            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }
        };
    }

    @Test
    public void loadMeshTest() {
        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        final GVRAndroidResource.CancelableCallback<GVRMesh> callbacks = PowerMockito
                .mock(GVRAndroidResource.CancelableCallback.class);

        AsyncMesh.loadMesh(context, meshCallback, resource, priority);

        PowerMockito.verifyStatic();
        Throttler.registerCallback(Mockito.any(GVRContext.class), Mockito.any(meshClass.getClass()), Mockito.any(callbacks.getClass()),
                Mockito.any(resource.getClass()), Mockito.anyInt());
    }

    @Test
    public void createGVRMeshTest() throws Exception {
        final GVRMesh mesh = Mockito.mock(GVRMesh.class);
        PowerMockito.when(context, "runOnGlThread", Mockito.any(Runnable.class)).thenAnswer(runnableAnswer);

        final GVRAndroidResource resource = PowerMockito.mock(GVRAndroidResource.class);
        PowerMockito.when(context, "loadMesh", Mockito.any(resource.getClass())).thenReturn(mesh);

        final Runnable runnable = factory.threadProc(context, resource, meshCallback, 2);
        runnable.run();
        Mockito.verify(meshCallback, Mockito.times(1)).loaded(Mockito.any(GVRMesh.class),
                Mockito.any(GVRAndroidResource.class));
    }


}
