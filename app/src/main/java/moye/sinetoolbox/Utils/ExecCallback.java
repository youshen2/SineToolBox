package moye.sinetoolbox.Utils;

public interface ExecCallback {
    void onLog(String logText);

    void onErrorLog(String logText);

    void onFinish(int resultCode);
}
