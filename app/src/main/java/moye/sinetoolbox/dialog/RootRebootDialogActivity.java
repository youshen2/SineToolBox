package moye.sinetoolbox.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;
import moye.sinetoolbox.Utils.ShellUtils;

public class RootRebootDialogActivity extends BaseActivity {
    private int reboot_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_reboot_tip);
        Intent intent = this.getIntent();
        reboot_type = intent.getIntExtra("reboot", 0);
    }

    public void _on_tip_yes_click(View view) {
        if (Objects.equals(reboot_type, 0)) {
            Toast.makeText(this, "你是怎么打开这个页面的？", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(reboot_type, 1)) {
            if (ShellUtils.exec_result("reboot recovery", true)) {
                Toast.makeText(this, "即将重启至Recovery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "执行ROOT语句失败", Toast.LENGTH_SHORT).show();
            }
        } else if (Objects.equals(reboot_type, 2)) {
            if (ShellUtils.exec_result("reboot edl", true)) {
                Toast.makeText(this, "即将重启至9008（EDL）", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "执行ROOT语句失败", Toast.LENGTH_SHORT).show();
            }
        } else if (Objects.equals(reboot_type, 3)) {
            if (ShellUtils.exec_result("reboot bootloader", true)) {
                Toast.makeText(this, "即将重启至bootloader（用于伪砖）", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "执行ROOT语句失败", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    public void _on_tip_no_click(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}