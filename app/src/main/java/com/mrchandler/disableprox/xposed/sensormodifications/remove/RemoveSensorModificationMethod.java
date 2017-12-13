package com.mrchandler.disableprox.xposed.sensormodifications.remove;

import android.content.Context;
import android.hardware.Sensor;

import com.mrchandler.disableprox.BuildConfig;
import com.mrchandler.disableprox.util.Constants;
import com.mrchandler.disableprox.xposed.sensormodifications.SensorModificationMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * A modification method for all API levels. Removes the sensor by modifying what is returned by
 * SystemSensorManager.getFullSensorList.
 *
 * @author Wardell Bagby
 */

public class RemoveSensorModificationMethod extends SensorModificationMethod {
    @Override
    public void modifySensor(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.hardware.SystemSensorManager", lpparam.classLoader, "getFullSensorList", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //Without this, you'd never be able to edit the values for a removed sensor! Aaah!
                if (!lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                    //Create a new list so we don't modify the original list.
                    @SuppressWarnings("unchecked") List<Sensor> fullSensorList = new ArrayList<>((Collection<? extends Sensor>) param.getResult());
                    Iterator<Sensor> iterator = fullSensorList.iterator();
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    while (iterator.hasNext()) {
                        Sensor sensor = iterator.next();
                        if (!isPackageAllowedToSeeTrueSensor(lpparam.processName, sensor, context) && getSensorStatus(sensor, context) == Constants.SENSOR_STATUS_REMOVE_SENSOR) {
                            iterator.remove();
                        }
                    }
                    param.setResult(fullSensorList);
                }
            }
        });
    }
}
