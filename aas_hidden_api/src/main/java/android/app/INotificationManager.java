package android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;

import java.util.List;

public interface INotificationManager extends IInterface {
    StatusBarNotification[] getActiveNotifications(String callingPkg);

    abstract class Stub extends Binder {
        public static INotificationManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
