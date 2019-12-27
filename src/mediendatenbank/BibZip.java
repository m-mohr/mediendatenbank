package mediendatenbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BibZip {

	private List<String> fileList;
	private File source;

	public BibZip(String source) {
		setSource(source);
	}

	public void saveZipFile(String target) {
		byte[] buffer = new byte[1024];

		try {
			FileOutputStream fos = new FileOutputStream(target);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String file : this.fileList) {
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(source + File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			zos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public final void setSource(String source) {
		this.fileList = new ArrayList<String>();
		File f = new File(source);
		this.source = f.getAbsoluteFile();
		setSource(this.source);
	}

	private void setSource(File node) {
		node = node.getAbsoluteFile();
		
		if (node.isFile()) {
			fileList.add(generateZipEntry(node));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				setSource(new File(node, filename));
			}
		}

	}

	private String generateZipEntry(File file) {
		String path = file.getAbsolutePath();
		return path.substring(source.getAbsolutePath().length() + 1, path.length());
	}
}
