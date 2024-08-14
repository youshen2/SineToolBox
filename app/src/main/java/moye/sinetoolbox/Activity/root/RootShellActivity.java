package moye.sinetoolbox.Activity.root;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;
import moye.sinetoolbox.Utils.ExecCallback;
import moye.sinetoolbox.Utils.ExecRunner;
import moye.sinetoolbox.dialog.RemoveDialog;
import moye.sinetoolbox.view.DragableLuncher;

public class RootShellActivity extends BaseActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private JSONArray history_json;
    private LinearLayout history_btns;
    private DragableLuncher launcher;
    private ImageView launcher_status;

    private boolean command_runing = false;

    private SpannableStringBuilder command_result = new SpannableStringBuilder("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_shell);

        launcher = findViewById(R.id.root_shell_launcher);
        launcher_status = findViewById(R.id.shell_status);
        history_btns = findViewById(R.id.root_shell_right_linear);

        sharedPreferences = getSharedPreferences("root_shell", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try {
            history_json = new JSONArray(sharedPreferences.getString("history", "[]"));
            for (int i = 0; i < history_json.length(); i++)
                create_history_button(history_json.getString(i), i);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        findViewById(R.id.activity_root_shell_clear).setOnClickListener(view -> ((EditText) findViewById(R.id.shell_edittext)).setText(""));
        findViewById(R.id.activity_root_shell_enter).setOnClickListener(view -> ((EditText) findViewById(R.id.shell_edittext)).setText(((TextView) findViewById(R.id.shell_edittext)).getText() + "\n"));
        findViewById(R.id.activity_root_shell_run).setOnClickListener(view -> {
            if (!((EditText) findViewById(R.id.shell_edittext)).getText().toString().isEmpty())
                run_command(((EditText) findViewById(R.id.shell_edittext)).getText().toString(), true);
        });
        findViewById(R.id.activity_root_shell_exit).setOnClickListener(view -> finish());
        findViewById(R.id.activity_root_shell_history_clear).setOnClickListener(view -> {
            history_btns.removeAllViews();
            history_json = new JSONArray();
            editor.putString("history", history_json.toString());
            editor.commit();
        });
        update_status();
    }

    void update_status() {
        findViewById(R.id.activity_root_shell_run).setEnabled(!command_runing);
        switch (launcher.getCurrentScreen()) {
            case 0:
                launcher_status.setImageResource(R.drawable.frame_1in2);
                break;
            case 1:
                launcher_status.setImageResource(R.drawable.frame_2in2);
                break;
        }
        new Handler().postDelayed(() -> update_status(), 300);
    }

    private void create_history_button(String history, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_btn, history_btns, false);
        ((TextView) view.findViewById(R.id.title)).setText(history);
        view.setOnClickListener(view1 -> {
            ((EditText) findViewById(R.id.shell_edittext)).setText(history);
            run_command(history, false);
        });
        view.setOnLongClickListener(view1 -> {
            Intent intent = new Intent(RootShellActivity.this, RemoveDialog.class);
            intent.putExtra("index", index);
            startActivityForResult(intent, 1);
            return false;
        });
        history_btns.addView(view);
    }

    public void run_command(String command, boolean new_history) {
        launcher.snapToScreen(0);
        if (new_history) {
            try {
                if (history_json.length() == 0 || (history_json.length() > 0 && !history_json.getString(0).equals(command))) {
                    history_json.put(command);
                    create_history_button(command, history_json.length() + 1);
                    editor.putString("history", history_json.toString());
                    editor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.history_exception), Toast.LENGTH_SHORT).show();
            }
        }
        command_result = new SpannableStringBuilder("");
        ((TextView) findViewById(R.id.shell_statustext)).setText("");
        new ExecRunner(command, new ExecCallback() {
            @Override
            public void onLog(String logText) {
                command_result.append(logText + "\n");
                ((TextView) findViewById(R.id.shell_statustext)).setText(command_result);
            }

            @Override
            public void onErrorLog(String logText) {
                int log_length = command_result.length();
                command_result.append(logText + "\n");
                if (logText.contains("Cannot run program \"su\""))
                    command_result.append("可能原因：没有或未授权ROOT" + "\n");
                else if (logText.contains("Permission denied"))
                    command_result.append("可能原因：权限不足" + "\n");
                command_result.setSpan(new ForegroundColorSpan(Color.RED), log_length, command_result.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((TextView) findViewById(R.id.shell_statustext)).setText(command_result);
            }

            @Override
            public void onFinish(int resultCode) {
                command_result.append("返回值：" + resultCode);
                ((TextView) findViewById(R.id.shell_statustext)).setText(command_result);
                command_runing = false;
            }
        });
    }

    @Override  //接受初入页面返回的参数
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            int index = data.getIntExtra("index", -1);
            if (index == -1) {
                Toast.makeText(this, "返回值错误", Toast.LENGTH_SHORT).show();
            } else if (index != -2) {
                history_json.remove(index);
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                history_btns.removeAllViews();
                try {
                    for (int i = 0; i < history_json.length(); i++) {
                        create_history_button(history_json.getString(i), i);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                editor.putString("history", history_json.toString());
                editor.commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}