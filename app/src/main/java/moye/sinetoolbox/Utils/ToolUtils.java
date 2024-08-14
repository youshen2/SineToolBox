package moye.sinetoolbox.Utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class ToolUtils {
    public static String getLocalIPAddress(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI
                            .hasMoreElements(); ) {
                        NetworkInterface netI = enNetI.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } else {
                    return "0.0.0.0";
                }
            } else {
                return "0.0.0.0";
            }
        } catch (Exception e) {
            Toast.makeText(context, "获取IP时出错", Toast.LENGTH_SHORT).show();
        }
        return "0.0.0.0";
    }

    public static void open_activity(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }

    public static int open_activity(Context context, String package_name, String activity_name) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(package_name, activity_name));
            context.startActivity(intent);
            return 0;
        } catch (SecurityException e) {
            e.printStackTrace();
            return 1;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return 2;
        }
    }

    public static void able_package(Context context, String package_name, Boolean is_enable) {
        new Thread(() -> {
            Looper.prepare();
            String cmd_return;
            if (is_enable) cmd_return = ShellUtils.exec("pm enable --user 0 " + package_name, true);
            else cmd_return = ShellUtils.exec("pm disable --user 0 " + package_name, true);
            if (cmd_return.contains("Unknown package"))
                Toast.makeText(context, "没有找到“" + package_name + "”", Toast.LENGTH_LONG).show();
            else if (cmd_return.contains("enabled") || cmd_return.contains("disable"))
                Toast.makeText(context, "成功", Toast.LENGTH_LONG).show();
            else if (cmd_return.contains("Er00"))
                Toast.makeText(context, "没有ROOT权限", Toast.LENGTH_LONG).show();
            else Toast.makeText(context, "未知错误", Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
    }
}
