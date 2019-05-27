package datasync.entity;


import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.UnixStat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @program: DataSync
 * @description: compress files
 * @author: caohq
 * @create: 2018-12-23 13:12
 **/
public class ZipUtils {

    public static void zipDirectory(File file, String path, ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
        if (path == null)
            path = new String();
        else if (!path.isEmpty())
            path += File.separatorChar;
        ZipArchiveEntry entry = new ZipArchiveEntry(file, path + file.getName());
        zipArchiveOutputStream.putArchiveEntry(entry);
        if (!file.isDirectory()) {
            entry.setUnixMode(UnixStat.DEFAULT_FILE_PERM);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                IOUtils.copy(fileInputStream, zipArchiveOutputStream);
                fileInputStream.close();
            } finally {
                zipArchiveOutputStream.closeArchiveEntry();

            }
        } else {
//            entry.setUnixMode(UnixStat.DEFAULT_DIR_PERM);
//            zipArchiveOutputStream.closeArchiveEntry();
//            File[] files = file.listFiles();
//            for (File child : files) {
//                zipDirectory(child, path + file.getName(), zipArchiveOutputStream);
//            }
        }
    }
}
