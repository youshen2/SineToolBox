package moye.sinetoolbox.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import moye.sinetoolbox.Activity.about.AboutLogActivity;
import moye.sinetoolbox.Activity.root.RootDPIActivity;
import moye.sinetoolbox.Activity.root.RootRebootActivity;
import moye.sinetoolbox.Activity.root.RootRemountActivity;
import moye.sinetoolbox.Activity.root.RootShellActivity;
import moye.sinetoolbox.Activity.webview.WebviewBeforeActivity;
import moye.sinetoolbox.R;
import moye.sinetoolbox.Utils.ShellUtils;
import moye.sinetoolbox.Utils.ToolUtils;
import moye.sinetoolbox.dialog.ConfirmDialog;
import moye.sinetoolbox.dialog.RemoveDialog;
import moye.sinetoolbox.view.DragableLuncher;

public class MainActivity extends BaseActivity {
    public boolean has_root = true;
    private DragableLuncher launcher;
    private ImageView launcher_status;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    /*
    ========
    关于工具箱
    ========
     */
    private int version_text_click_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launcher = findViewById(R.id.main_launcher);
        launcher.mCurrentScreen = 1;
        launcher_status = findViewById(R.id.main_launcher_status);

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        update_status();
        open_activity_init();
        init_root_page();
        init_setting_page();
        reload_background_image();
    }

    private void reload_background_image() {
        ImageView main_activity_page = findViewById(R.id.activity_main_bg);
        switch (sharedPreferences.getInt("background_image", -1)) {
            case -1:
                main_activity_page.setImageResource(0);
                break;
            case 0:
                main_activity_page.setImageResource(R.drawable.bg_1);
                break;
            case 1:
                main_activity_page.setImageResource(R.drawable.bg_2);
                break;
            case 2:
                main_activity_page.setImageResource(R.drawable.bg_3);
                break;
            case 3:
                main_activity_page.setImageResource(R.drawable.bg_4);
                break;
            case 4:
                main_activity_page.setImageResource(R.drawable.bg_5);
                break;
            case 5:
                main_activity_page.setImageResource(R.drawable.bg_6);
                break;
            case 6:
                main_activity_page.setImageResource(R.drawable.blur_bg);
                break;
            case 7:
                main_activity_page.setImageResource(R.drawable.splash);
                break;
        }
    }

    private void update_status() {
        if (sharedPreferences.getBoolean("root_enable", true)) {
            switch (launcher.getCurrentScreen()) {
                case 0:
                    finish();
                case 1:
                    launcher_status.setImageResource(R.drawable.frame_1in4);
                    break;
                case 2:
                    launcher_status.setImageResource(R.drawable.frame_2in4);
                    break;
                case 3:
                    launcher_status.setImageResource(R.drawable.frame_3in4);
                    break;
                case 4:
                    launcher_status.setImageResource(R.drawable.frame_4in4);
                    break;
            }
        } else {
            switch (launcher.getCurrentScreen()) {
                case 0:
                    finish();
                case 1:
                    launcher_status.setImageResource(R.drawable.frame_1in3);
                    break;
                case 2:
                    launcher_status.setImageResource(R.drawable.frame_2in3);
                    break;
                case 3:
                    launcher_status.setImageResource(R.drawable.frame_3in3);
                    break;
            }
        }
        new Handler().postDelayed(() -> update_status(), 300);
    }

    /*
    =======
    打开活动
    =======
     */
    public void open_activity_init() {
        findViewById(R.id.activity_main_webview_btn).setOnClickListener(view -> ToolUtils.open_activity(this, WebviewBeforeActivity.class));

        if (Build.VERSION.SDK_INT < 21)
            findViewById(R.id.activity_main_ftp_btn).setOnClickListener(view -> Toast.makeText(this, getResources().getString(R.string.cant_use), Toast.LENGTH_SHORT).show());
        else
            findViewById(R.id.activity_main_ftp_btn).setOnClickListener(view -> ToolUtils.open_activity(this, FTPActivity.class));

        reload_activitys();

        findViewById(R.id.activity_main_activity_add_xtc).setOnClickListener(view -> {
            Intent intent = new Intent(this, ConfirmDialog.class);
            intent.putExtra("title", "确定要添加吗");
            intent.putExtra("content", "这会在活动列表的末尾插入内置的XTC活动");
            intent.putExtra("key", "add");
            intent.putExtra("value", "xtc");
            startActivityForResult(intent, 0);
        });
    }

    private void add_activity_list(String type) {
        try {
            if (type.equals("xtc")) {
                JSONArray jsonArray = new JSONArray(sharedPreferences.getString("activity_list", "[]"));
                for (int i = 0; i < getResources().getStringArray(R.array.activity_packages_xtc).length; i++) {
                    JSONObject object = new JSONObject();
                    object.put("package", getResources().getStringArray(R.array.activity_packages_xtc)[i]);
                    object.put("uri", getResources().getStringArray(R.array.activity_uris_xtc)[i]);
                    object.put("name", getResources().getStringArray(R.array.activity_names_xtc)[i]);
                    jsonArray.put(object);
                }
                editor.putString("activity_list", jsonArray.toString());
                editor.commit();
            }
            reload_activitys();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reload_activitys() {
        try {
            JSONArray activity_list = new JSONArray(sharedPreferences.getString("activity_list", "[]"));
            LinearLayout btn_layout = findViewById(R.id.activity_main_activity_btn_list);
            btn_layout.removeAllViews();
            for (int i = 0; i < activity_list.length(); i++) {
                int finalI = i;
                //活动列表
                View view = LayoutInflater.from(this).inflate(R.layout.item_activity_btn, btn_layout, false);
                ((TextView) view.findViewById(R.id.name)).setText(activity_list.getJSONObject(i).getString("name"));
                ((TextView) view.findViewById(R.id.package_name)).setText(activity_list.getJSONObject(finalI).getString("package"));
                view.setOnClickListener(view1 -> {
                    try {
                        int result_code = ToolUtils.open_activity(MainActivity.this, activity_list.getJSONObject(finalI).getString("package"), activity_list.getJSONObject(finalI).getString("uri"));
                        if (result_code == 1) {
                            if (has_root) {
                                new Thread(() -> {
                                    Looper.prepare();
                                    try {
                                        String exec_return = ShellUtils.exec("am start " + activity_list.getJSONObject(finalI).getString("package") + "/" + activity_list.getJSONObject(finalI).getString("uri"), true);
                                        if (exec_return.equals("Er00"))
                                            Toast.makeText(MainActivity.this, getResources().getString(R.string.activity_need_root), Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(MainActivity.this, getResources().getString(R.string.activity_opening_root), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Looper.loop();
                                }).start();
                            } else
                                Toast.makeText(this, getResources().getString(R.string.activity_need_root), Toast.LENGTH_SHORT).show();
                        } else if (result_code == 2)
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                view.setOnLongClickListener(view1 -> {
                    Intent intent = new Intent(MainActivity.this, RemoveDialog.class);
                    intent.putExtra("index", finalI);
                    startActivityForResult(intent, 3);
                    return true;
                });
                btn_layout.addView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    =========
    ROOT工具箱
    =========
     */
    private void init_root_page() {
        refresh_selinux_status();

        if (!has_root) {
            findViewById(R.id.root_main).setVisibility(View.GONE);
            findViewById(R.id.root_main_noroot).setVisibility(View.VISIBLE);
            if (sharedPreferences.getBoolean("root_enable", true))
                Toast.makeText(MainActivity.this, getResources().getString(R.string.cant_find_root), Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.activity_main_root_shell).setOnClickListener(view -> ToolUtils.open_activity(this, RootShellActivity.class));
        findViewById(R.id.activity_main_root_dpi).setOnClickListener(view -> ToolUtils.open_activity(this, RootDPIActivity.class));
        findViewById(R.id.activity_main_root_reboot).setOnClickListener(view -> ToolUtils.open_activity(this, RootRebootActivity.class));
        findViewById(R.id.activity_main_root_mount).setOnClickListener(view -> ToolUtils.open_activity(this, RootRemountActivity.class));
        findViewById(R.id.activity_main_root_wifiadb).setOnClickListener(view -> {
            try {
                if (ShellUtils.exec_result("setprop service.adb.tcp.port 5555\nstop adbd\nstart adbd", true)) {
                    TextView textView = findViewById(R.id.wifi_adb_url);
                    view.setEnabled(false);
                    textView.setText("WifiADB已于" + ToolUtils.getLocalIPAddress(this) + ":5555上开启");
                    Toast.makeText(this, "WifiADB已于 " + ToolUtils.getLocalIPAddress(this) + ":5555 上开启", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, getResources().getString(R.string.root_running_failed), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.catch_exception), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        findViewById(R.id.activity_main_root_appupdate_enable).setOnClickListener(view -> ToolUtils.able_package(this, "com.xtc.appupdate", true));
        findViewById(R.id.activity_main_root_appupdate_disable).setOnClickListener(view -> ToolUtils.able_package(this, "com.xtc.appupdate", false));
        findViewById(R.id.activity_main_root_systemupdatei11_enable).setOnClickListener(view -> ToolUtils.able_package(this, "com.xtc.systemupdate_i11", true));
        findViewById(R.id.activity_main_root_systemupdatei11_disable).setOnClickListener(view -> ToolUtils.able_package(this, "com.xtc.systemupdate_i11", false));
        findViewById(R.id.activity_main_root_trun_on).setOnClickListener(view -> {
            if (ShellUtils.exec_result("content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:1", true))
                Toast.makeText(this, getResources().getString(R.string.screen_turn_on), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.need_root), Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.activity_main_root_trun_off).setOnClickListener(view -> {
            if (ShellUtils.exec_result("content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:0", true))
                Toast.makeText(this, getResources().getString(R.string.screen_turn_off), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.need_root), Toast.LENGTH_SHORT).show();
        });

        if (!sharedPreferences.getBoolean("root_enable", true))
            launcher.removeView(findViewById(R.id.main_root_page));
    }


    private void refresh_selinux_status() {
        ToggleButton selinux_button = findViewById(R.id.selinux_togglebutton);
        String selinux_status = ShellUtils.exec("getenforce", true);
        selinux_button.setOnCheckedChangeListener((compoundButton, b) -> {
        });
        selinux_button.setChecked(false);
        if (Objects.equals(selinux_status, "Permissive\n")) selinux_button.setChecked(true);
        else if (Objects.equals(selinux_status, "Disabled\n"))
            selinux_button.setTextOff("SELinux：禁用模式");
        else if (Objects.equals(selinux_status, "Enforcing\n"))
            selinux_button.setTextOff("SELinux：强制模式");
        else if (!sharedPreferences.getBoolean("debug_fakeroot", false)) has_root = false;
        selinux_button.setOnCheckedChangeListener((compoundButton, b) -> change_selinux(b));
    }

    public void change_selinux(boolean state) {
        try {
            if (state) {
                if (ShellUtils.exec_result("setenforce 0", true))
                    Toast.makeText(this, getResources().getString(R.string.selinux_permissive), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getResources().getString(R.string.root_running_failed), Toast.LENGTH_SHORT).show();
            } else {
                if (ShellUtils.exec_result("setenforce 1", true))
                    Toast.makeText(this, getResources().getString(R.string.selinux_enforcing), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getResources().getString(R.string.root_running_failed), Toast.LENGTH_SHORT).show();
            }
            refresh_selinux_status();
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.catch_exception), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /*
    =======
    工具箱设置
    =======
     */
    private void init_setting_page() {
        LinearLayout btn_layout = findViewById(R.id.activity_main_setting_bg_group);
        for (int i = -1; i < getResources().getStringArray(R.array.background_images).length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_radio_btn, btn_layout, false);
            TextView title_view = view.findViewById(R.id.title);
            if (i > -1)
                title_view.setText(getResources().getStringArray(R.array.background_images)[i]);
            else title_view.setText("默认背景");
            ((TextView) view.findViewById(R.id.description)).setText("#" + (i + 1));

            RadioButton radioButton = view.findViewById(R.id.radio);

            int finalI = i;
            view.setOnClickListener(v -> {
                for (int j = 0; j < btn_layout.getChildCount(); j++) {
                    View btn = btn_layout.getChildAt(j);
                    ((RadioButton) btn.findViewById(R.id.radio)).setChecked(false);
                }
                radioButton.setChecked(true);
                editor.putInt("background_image", finalI);
                editor.commit();
                reload_background_image();
            });
            btn_layout.addView(view);

            if (sharedPreferences.getInt("background_image", -1) == i)
                radioButton.setChecked(true);
        }

        reload_setting_root_btn();

        findViewById(R.id.activity_main_activity_add_btn).setOnClickListener(view -> {
            Intent intent = new Intent(this, ActivityAddActivity.class);
            startActivityForResult(intent, 2);
        });
    }

    private void reload_setting_root_btn() {
        ToggleButton root_btn = findViewById(R.id.activity_main_setting_root);
        root_btn.setOnCheckedChangeListener(((compoundButton, b) -> {
        }));
        root_btn.setChecked(sharedPreferences.getBoolean("root_enable", true));
        root_btn.setOnCheckedChangeListener((compoundButton, b) -> {
            Intent intent = new Intent(this, ConfirmDialog.class);
            intent.putExtra("title", "确定要切换吗");
            intent.putExtra("key", "root_enable");
            startActivityForResult(intent, 0);
        });
    }

    private void reload_this_activity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void _on_version_text_click(View view) {
        if (version_text_click_count++ >= 4) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    public void _on_updatelog_click(View view) {
        ToolUtils.open_activity(this, AboutLogActivity.class);
    }

    @Override
    public void onBackPressed() {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case 3:
                    int index = data.getIntExtra("index", -1);
                    if (index == -1)
                        Toast.makeText(this, getResources().getString(R.string.catch_exception), Toast.LENGTH_SHORT).show();
                    else if (index != -2) {
                        JSONArray array = new JSONArray(sharedPreferences.getString("activity_list", "[]"));
                        array.remove(index);
                        editor.putString("activity_list", array.toString()).commit();
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                        reload_activitys();
                    }
                    break;
                case 2:
                    reload_activitys();
                    break;
                case 1:
                    reload_this_activity();
                    break;
                case 0:
                    boolean confirm = data.getBooleanExtra("confirm", false);
                    switch (data.getStringExtra("key")) {
                        case "root_enable":
                            if (confirm) {
                                editor.putBoolean("root_enable", !sharedPreferences.getBoolean("root_enable", true));
                                editor.commit();
                                reload_this_activity();
                            } else
                                reload_setting_root_btn();
                        case "add":
                            if (confirm) add_activity_list(data.getStringExtra("value"));
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.catch_exception), Toast.LENGTH_SHORT).show();
        }
    }
}