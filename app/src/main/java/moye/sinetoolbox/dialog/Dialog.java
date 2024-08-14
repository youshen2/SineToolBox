package moye.sinetoolbox.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;

public class Dialog extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        Intent intent = this.getIntent();

        TextView title = findViewById(R.id.title);
        title.setText(intent.getStringExtra("title"));

        TextView content = findViewById(R.id.content);
        content.setText(intent.getStringExtra("content"));

        findViewById(R.id.ok_btn).setOnClickListener(view -> {
            if (intent.getBooleanExtra("quit", false))
                android.os.Process.killProcess(android.os.Process.myPid());
            else finish();
        });
    }

    @Override
    public void onBackPressed() {
    }
}