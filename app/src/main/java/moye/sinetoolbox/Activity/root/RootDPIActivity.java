package moye.sinetoolbox.Activity.root;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;
import moye.sinetoolbox.Utils.ShellUtils;

public class RootDPIActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_dpi);

        LinearLayout btn_list = findViewById(R.id.btn_list);
        for (String dpi : getResources().getStringArray(R.array.dpi_list)) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_btn, btn_list, false);
            ((TextView) view.findViewById(R.id.title)).setText(dpi);
            view.setOnClickListener(view1 -> change_dpi(Integer.parseInt(dpi)));
            btn_list.addView(view);
        }

        findViewById(R.id.activity_root_dpi_diy).setOnClickListener(view -> {
            EditText editText = findViewById(R.id.dpiInput);
            if (Integer.parseInt(editText.getText().toString()) > 500)
                Toast.makeText(this, "不建议使用大于500的DPI", Toast.LENGTH_SHORT).show();
            else if (Integer.parseInt(editText.getText().toString()) < 40)
                Toast.makeText(this, "不建议使用低于40的DPI", Toast.LENGTH_SHORT).show();
            else change_dpi(Integer.parseInt(editText.getText().toString()));
        });
    }

    private void change_dpi(int density) {
        String dpi;
        if (density == 0) dpi = "reset";
        else dpi = String.valueOf(density);
        if (ShellUtils.exec_result("wm density " + dpi, true))
            Toast.makeText(this, "DPI已复原", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "执行ROOT语句失败", Toast.LENGTH_SHORT).show();
    }
}