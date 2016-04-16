package cn.bestwu.framework.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;

public class FileUtil {

	//-----------------------------------------------------------------------
	public static Charset toCharset(Charset charset) {
		return charset == null ? Charset.defaultCharset() : charset;
	}

	public static Charset toCharset(String charset) {
		return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
	}

	//-----------------------------------------------------------------------

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	//-----------------------------------------------------------------------

	public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file, append);
	}

	//-----------------------------------------------------------------------

	public static Collection<File> listFiles(File directory, FileFilter filter, boolean recursive) {
		Collection<File> files = new java.util.LinkedList<>();
		innerListFiles(files, directory, filter, false, recursive);
		return files;
	}

	private static void innerListFiles(Collection<File> files, File directory, FileFilter filter, boolean includeSubDirectories, boolean recursive) {
		File[] found = directory.listFiles(filter);

		if (found != null) {
			for (File file : found) {
				if (file.isDirectory()) {
					if (includeSubDirectories) {
						files.add(file);
					}
					if (recursive)
						innerListFiles(files, file, filter, includeSubDirectories, recursive);
				} else {
					files.add(file);
				}
			}
		}
	}

	//-----------------------------------------------------------------------

	public static List<String> readLines(InputStream input, Charset encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, toCharset(encoding)));
		List<String> list = new ArrayList<>();
		String line = reader.readLine();
		while (line != null) {
			list.add(line);
			line = reader.readLine();
		}
		return list;
	}

	public static List<String> readLines(File file, Charset encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return readLines(in, toCharset(encoding));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static List<String> readLines(File file, String encoding) throws IOException {
		return readLines(file, toCharset(encoding));
	}

	public static List<String> readLines(File file) throws IOException {
		return readLines(file, Charset.defaultCharset());
	}

	//-----------------------------------------------------------------------

	public static void writeLines(File file, String encoding, Collection<?> lines) throws IOException {
		writeLines(file, encoding, lines, null, false);
	}

	public static void writeLines(File file, String encoding, Collection<?> lines, boolean append) throws IOException {
		writeLines(file, encoding, lines, null, append);
	}

	public static void writeLines(File file, Collection<?> lines) throws IOException {
		writeLines(file, null, lines, null, false);
	}

	public static void writeLines(File file, Collection<?> lines, boolean append) throws IOException {
		writeLines(file, null, lines, null, append);
	}

	public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding)
			throws IOException {
		writeLines(file, encoding, lines, lineEnding, false);
	}

	public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, Charset encoding)
			throws IOException {
		if (lines == null) {
			return;
		}
		if (lineEnding == null) {
			lineEnding = LINE_SEPARATOR;
		}
		Charset cs = toCharset(encoding);
		for (Object line : lines) {
			if (line != null) {
				output.write(line.toString().getBytes(cs));
			}
			output.write(lineEnding.getBytes(cs));
		}
	}

	public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding, boolean append)
			throws IOException {
		FileOutputStream out = null;
		try {
			out = openOutputStream(file, append);
			final BufferedOutputStream buffer = new BufferedOutputStream(out);
			writeLines(lines, lineEnding, buffer, toCharset(encoding));
			buffer.flush();
			out.close(); // don't swallow close Exception if copy completes normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void writeLines(File file, Collection<?> lines, String lineEnding) throws IOException {
		writeLines(file, null, lines, lineEnding, false);
	}

	public static void writeLines(File file, Collection<?> lines, String lineEnding, boolean append)
			throws IOException {
		writeLines(file, null, lines, lineEnding, append);
	}

	//-----------------------------------------------------------------------

}