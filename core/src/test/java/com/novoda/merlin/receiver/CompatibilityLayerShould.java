package com.novoda.merlin.receiver;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;

import com.novoda.merlin.service.AndroidVersion;
import com.novoda.merlin.service.MerlinService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CompatibilityLayerShould {

    @Mock
    private Context context;
    @Mock
    private ConnectivityManager connectivityManager;
    @Mock
    private AndroidVersion androidVersion;
    @Mock
    private MerlinService merlinService;

    private CompatibilityLayer compatibilityLayer;

    @Before
    public void setUp() {
        initMocks(this);

        compatibilityLayer = new CompatibilityLayer(context, connectivityManager, androidVersion, merlinService);
    }

    @Test
    public void registerBroadcastReceiverWhenAndroidVersionIsBelowLollipop() {
        when(androidVersion.isLollipopOrHigher()).thenReturn(false);

        compatibilityLayer.bind();

        verify(context).registerReceiver(eq(compatibilityLayer.getConnectivityReceiver()), Matchers.refEq(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)));
    }

    @Test
    public void unregisterBroadcastReceiverWhenAndroidVersionIsBelowLollipop() {
        when(androidVersion.isLollipopOrHigher()).thenReturn(false);

        compatibilityLayer.unbind();

        verify(context).unregisterReceiver(compatibilityLayer.getConnectivityReceiver());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void givenRegisteredMerlinNetworkCallbacksWhenBindingForASecondTimeThenOriginalNetworkCallbacksIsRegisteredAgain() {
        ArgumentCaptor<MerlinNetworkCallbacks> merlinNetworkCallback = givenRegisteredMerlinNetworkCallbacks();

        compatibilityLayer.bind();

        verify(connectivityManager, times(2)).registerNetworkCallback(Matchers.refEq((new NetworkRequest.Builder()).build()), eq(merlinNetworkCallback.getValue()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void givenRegisteredMerlinNetworkCallbackWhenUnbindingThenUnregistersOriginallyRegisteredNetworkCallbacks() {
        ArgumentCaptor<MerlinNetworkCallbacks> merlinNetworkCallback = givenRegisteredMerlinNetworkCallbacks();

        compatibilityLayer.unbind();

        verify(connectivityManager).unregisterNetworkCallback(merlinNetworkCallback.getValue());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArgumentCaptor<MerlinNetworkCallbacks> givenRegisteredMerlinNetworkCallbacks() {
        when(androidVersion.isLollipopOrHigher()).thenReturn(true);
        compatibilityLayer.bind();
        ArgumentCaptor<MerlinNetworkCallbacks> argumentCaptor = ArgumentCaptor.forClass(MerlinNetworkCallbacks.class);
        verify(connectivityManager).registerNetworkCallback(Matchers.refEq((new NetworkRequest.Builder()).build()), argumentCaptor.capture());
        return argumentCaptor;
    }

}
