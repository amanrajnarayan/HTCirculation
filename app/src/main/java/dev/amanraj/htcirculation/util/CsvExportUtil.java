package dev.amanraj.htcirculation.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class CsvExportUtil {

    public static void export(Context context, String fileName, List<String[]> rows) {
        try {
            // Documents/HTCirculation/exports
            File baseDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "HTCirculation/exports"
            );

            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }

            File file = new File(baseDir, fileName);

            FileWriter writer = new FileWriter(file);
            for (String[] row : rows) {
                writer.append(String.join(",", row));
                writer.append("\n");
            }
            writer.flush();
            writer.close();

            //Share intent
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(
                    Intent.createChooser(shareIntent, "Share CSV")
            );

        } catch (Exception e) {
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
