package moye.sinetoolbox.Utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class ExecRunner {
    private final ExecCallback callback;
    private final ArrayList<String> commands = new ArrayList<>();
    private final Handler handler;

    public ExecRunner(String command, ExecCallback callback) {
        Collections.addAll(commands, command.split("\n"));
        this.callback = callback;
        handler = new Handler(Looper.getMainLooper());
        new ExecRunnerTask().start();
    }

    private class ExecRunnerTask extends Thread {
        @Override
        public void run() {
            super.run();
            ShellUtils.exec(commands, new ShellUtils.ShellResult() {
                @Override
                public void onStdout(final String text) {
                    Log.e("运行Shell", text);
                    handler.post(() -> callback.onLog(text));
                }

                @Override
                public void onStderr(final String text) {
                    Log.e("运行Shell", "发生错误：" + text);
                    handler.post(() -> callback.onErrorLog(text));
                }

                @Override
                public void onCommand(String command) {
                    Log.e("运行Shell", "命令：" + command);
                }

                @Override
                public void onFinish(int resultCode) {
                    Log.e("运行Shell", "运行完成，返回值：" + resultCode);
                    handler.post(() -> callback.onFinish(resultCode));
                }
            }, true);

        }
    }
}
