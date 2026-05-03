package com.ubx.rfid;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel compartido entre todas las pantallas de la app RFID.
 */
public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> connected;
    private final MutableLiveData<Integer> currentFragment;
    private final MutableLiveData<String>  locationEpc;
    private final MutableLiveData<Byte>    readId;
    private final MutableLiveData<Boolean> profileMode;
    private final MutableLiveData<Boolean> updateProfile;
    private final MutableLiveData<Boolean> isFirstLoad;

    public MainViewModel() {
        connected       = new MutableLiveData<>(false);
        currentFragment = new MutableLiveData<>(0);
        locationEpc     = new MutableLiveData<>("");
        readId          = new MutableLiveData<>((byte) -1);
        profileMode     = new MutableLiveData<>(false);
        updateProfile   = new MutableLiveData<>(false);
        isFirstLoad     = new MutableLiveData<>(true);
    }

    public LiveData<Boolean> isConnected() { return connected; }
    public void setConnected(Boolean v) { connected.setValue(v); }

    public LiveData<Integer> getCurrentFragment() { return currentFragment; }
    public void setCurrentFragment(int v) { currentFragment.setValue(v); }

    public LiveData<String> getLocationEpc() { return locationEpc; }
    public void setLocationEpc(String v) { locationEpc.setValue(v); }

    public LiveData<Byte> getReadId() { return readId; }
    public void setReadId(byte v) { readId.setValue(v); }

    public LiveData<Boolean> getProfileMode() { return profileMode; }
    public void setProfileMode(boolean v) { profileMode.setValue(v); }

    public LiveData<Boolean> getUpdateProfile() { return updateProfile; }
    public void setUpdateProfile(boolean v) { updateProfile.setValue(v); }

    public LiveData<Boolean> getIsFirstLoad() { return isFirstLoad; }
    public void setIsFirstLoad(boolean v) { isFirstLoad.setValue(v); }
}
