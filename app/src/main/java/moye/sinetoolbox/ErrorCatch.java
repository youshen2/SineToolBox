package moye.sinetoolbox;

import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import moye.sinetoolbox.dialog.Dialog;

public class ErrorCatch implements Thread.UncaughtExceptionHandler {

    private static ErrorCatch instance;
    private Context context;

    public static ErrorCatch getInstance() {
        if (instance == null) instance = new ErrorCatch();
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        Intent intent = new Intent(context, Dialog.class);
        intent.putExtra("title", "正弦工具箱崩溃了");
        intent.putExtra("quit", true);

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        arg1.printStackTrace(printWriter);

        String ct = result + "\n请上报开发者";
        intent.putExtra("content", ct);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        arg1.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}