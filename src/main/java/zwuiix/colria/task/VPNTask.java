package zwuiix.colria.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.Closure;

public class VPNTask extends AsyncTask {
    private final String ip;
    private final Closure<Boolean> closure;

    public VPNTask(String ip, Closure<Boolean> closure) {
        this.ip = ip;
        this.closure = closure;
    }

    @Override
    public void onRun() {
        boolean isVpn = false;

        try {
            var url = new java.net.URL("https://proxycheck.io/v3/" + ip + "?vpn=1");
            var conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream())
            );

            var response = reader.lines().reduce("", (a, b) -> a + b);
            reader.close();

            var json = new JsonParser()
                    .parse(response)
                    .getAsJsonObject();

            var ipData = json.getAsJsonObject(ip);

            if (ipData != null && ipData.has("detections")) {
                var detections = ipData.getAsJsonObject("detections");

                boolean vpn = detections.get("vpn").getAsBoolean();
                boolean proxy = detections.get("proxy").getAsBoolean();
                boolean hosting = detections.get("hosting").getAsBoolean();

                isVpn = vpn || proxy || hosting;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setResult(isVpn);
    }

    @Override
    public void onCompletion(Server server) {
        closure.execute((boolean) this.getResult());
    }
}