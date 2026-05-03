package com.ubx.rfid.p006ui.scan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para la pantalla de escaneo de TAGs RFID.
 */
public class ScanViewModel extends ViewModel {

    private final MutableLiveData<Integer> labelCount;
    private final MutableLiveData<Integer> totalCount;
    private final MutableLiveData<Integer> scanSpeed;
    private final MutableLiveData<Integer> totalTime;
    private final MutableLiveData<List<ScanModel>> scanData;
    private final MutableLiveData<Boolean> keyNotify;
    private final MutableLiveData<Integer> currentPosition;

    public ScanViewModel() {
        labelCount = new MutableLiveData<>(0);
        totalCount = new MutableLiveData<>(0);
        scanSpeed  = new MutableLiveData<>(0);
        totalTime  = new MutableLiveData<>(0);
        scanData   = new MutableLiveData<>(new ArrayList<>());
        keyNotify  = new MutableLiveData<>(false);
        currentPosition = new MutableLiveData<>(-1);
    }

    // --- Label count (tags únicos) ---
    public LiveData<Integer> getLabelCount() { return labelCount; }
    public void setLabelCount(int v) { labelCount.setValue(v); }

    // --- Total count (lecturas totales) ---
    public LiveData<Integer> getTotalCount() { return totalCount; }
    public void setTotalCount(int v) { totalCount.setValue(v); }

    // --- Velocidad de escaneo (tags/s) ---
    public LiveData<Integer> geScanSpeed() { return scanSpeed; }
    public void setScanSpeed(int v) { scanSpeed.setValue(v); }

    // --- Tiempo total de escaneo (segundos) ---
    public LiveData<Integer> geTotalTime() { return totalTime; }
    public void setTotalTime(int v) { totalTime.setValue(v); }

    // --- Lista de tags escaneados ---
    public LiveData<List<ScanModel>> getScanData() { return scanData; }
    public void setScanData(List<ScanModel> list) { scanData.setValue(list); }

    // --- Notificación de tecla física del trigger ---
    public LiveData<Boolean> getKeyNotify() { return keyNotify; }
    public void setKeyNotify(boolean v) { keyNotify.setValue(v); }

    // --- Posición seleccionada en la lista ---
    public int getCurrentPosition() {
        Integer v = currentPosition.getValue();
        return v != null ? v : -1;
    }
    public void setCurrentPosition(int v) { currentPosition.setValue(v); }
}
