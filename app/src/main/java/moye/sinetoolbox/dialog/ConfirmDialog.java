package moye.sinetoolbox.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;

public class ConfirmDialog extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        Intent intent = this.getIntent();

        TextView title = findViewById(R.id.title);
        if (intent.getStringExtra("title") != null) title.setText(intent.getStringExtra("title"));
        else title.setVisibility(View.GONE);

        TextView content = findViewById(R.id.content);
        if (intent.getStringExtra("content") != null)
            content.setText(intent.getStringExtra("content"));
        else content.setVisibility(View.GONE);

        findViewById(R.id.confirm_button).setOnClickListener(view -> {
            Intent intent2 = new Intent();
            intent2.putExtra("key", intent.getStringExtra("key"));
            intent2.putExtra("value", intent.getStringExtra("value"));
            intent2.putExtra("confirm", true);
            setResult(RESULT_OK, intent2);
            finish();
        });
        findViewById(R.id.cancel_button).setOnClickListener(view -> {
            Intent intent2 = new Intent();
            intent2.putExtra("key", intent.getStringExtra("key"));
            intent2.putExtra("value", intent.getStringExtra("value"));
            intent2.putExtra("confirm", false);
            setResult(RESULT_OK, intent2);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
    }
}