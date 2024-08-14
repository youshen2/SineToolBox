package moye.sinetoolbox.Utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellUtils {

    public static Boolean exec_result(String command, boolean is_root) {
        Process process = null;
        DataOutputStream os = null;
        try {
            if (is_root) process = Runtime.getRuntime().exec("su");
            else process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public static String exec(String command, boolean is_root) {
        Process process = null;
        DataOutputStream os = null;
        String data = "";
        try {
            if (is_root) process = Runtime.getRuntime().exec("su");
            else process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

            BufferedReader successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String tmp;
            while ((tmp = successResult.readLine()) != null) data += tmp + "\n";
            while ((tmp = errorResult.readLine()) != null) data += tmp + "\n";
        } catch (Exception e) {
            return "Er00";
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return data;
    }

    public static int exec(ArrayList<String> commands, final ShellResult result, boolean is_root) {
        Process process;
        DataOutputStream stdin = null;
        OutputReader stdout = null;
        OutputReader stderr = null;
        int resultCode = -1;
        try {
            if (is_root) process = Runtime.getRuntime().exec("su");
            else process = Runtime.getRuntime().exec("sh");
            stdin = new DataOutputStream(process.getOutputStream());
            if (result != null) {
                stdout = new OutputReader(new BufferedReader(new InputStreamReader(process.getInputStream())), new Output() {
                    @Override
                    public void output(String text) {
                        result.onStdout(text);
                    }
                });
                stderr = new OutputReader(new BufferedReader(new InputStreamReader(process.getErrorStream())), new Output() {
                    @Override
                    public void output(String text) {
                        result.onStderr(text);
                    }
                });
                stdout.start();
                stderr.start();
            }
            for (String cmd : commands) {
                if (result != null) {
                    result.onCommand(cmd);
                }
                stdin.write(cmd.getBytes());
                stdin.writeBytes("\n");
                stdin.flush();
            }
            stdin.writeBytes("exit $?\n");
            stdin.flush();
            resultCode = process.waitFor();
            if (result != null) result.onFinish(resultCode);
        } catch (Exception e) {
            e.printStackTrace();
            result.onStderr(e.getMessage());
            result.onFinish(-1);
        } finally {
            try {
                if (stderr != null) stderr.cancel();
                if (stdout != null) stdout.cancel();
                if (stderr != null) stderr.close();
                if (stdout != null) stdout.close();
                if (stdin != null) stdin.close();
            } catch (Exception e) {
            }
        }
        return resultCode;
    }

    public interface ShellResult {
        void onStdout(String text);

        void onStderr(String text);

        void onCommand(String command);

        void onFinish(int resultCode);
    }

    private interface Output {
        void output(String text);
    }

    public static class OutputReader extends Thread implements Closeable {
        private Output output = null;
        private BufferedReader reader = null;
        private boolean isRunning = false;

        private OutputReader(BufferedReader reader, Output output) {
            this.output = output;
            this.reader = reader;
            this.isRunning = true;
        }

        @Override
        public void close() {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        @Override
        public void run() {
            super.run();
            String line;
            while (isRunning) {
                try {
                    line = reader.readLine();
                    if (line != null)
                        output.output(line);
                } catch (IOException e) {
                }
            }
        }

        private void cancel() {
            synchronized (this) {
                isRunning = false;
                this.notifyAll();
            }
        }
    }
}
